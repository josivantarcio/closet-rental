package com.jtarcio.closetrental.modules.locacoes.application;

import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.estoque.application.EstoqueService;
import com.jtarcio.closetrental.modules.locacoes.domain.model.DanoProduto;
import com.jtarcio.closetrental.modules.locacoes.domain.model.ItemLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.DanoProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoContextBuilder;
import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoService;
import com.jtarcio.closetrental.modules.notificacoes.domain.model.TipoNotificacao;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import com.jtarcio.closetrental.shared.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case: Registrar a devolucao de uma locacao.
 *
 * Regras:
 * 1. Locacao deve existir e estar no status RETIRADO
 * 2. Registra devolucaoAt = agora
 * 3. Calcula multa por atraso se devolucaoAt > dataFim
 * 4. Registra danos por item (opcional) — persiste em danos_produto
 * 5. Calcula retencao da caucao: usa valorRetencaoCaucao explícito, ou soma dos danos, ou 0
 * 6. Muda status para DEVOLVIDO e aplica statusCaucao
 * 7. Para cada item: marca o produto como disponivel e registra ENTRADA no estoque
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrarDevolucaoService {

    private final LocacaoRepositoryPort locacaoPort;
    private final ProdutoRepositoryPort produtoPort;
    private final EstoqueService estoqueService;
    private final DanoProdutoRepositoryPort danoPort;
    private final NotificacaoService notificacaoService;
    private final NotificacaoContextBuilder contextBuilder;

    @Value("${app.locacao.percentual-multa-diaria:0.10}")
    private BigDecimal percentualMultaDiaria;

    /**
     * @param locacaoId            ID da locação
     * @param observacoes          observações livres (opcional)
     * @param danos                lista de danos por item (opcional); cada dano referencia um itemLocacaoId
     * @param valorRetencaoCaucao  override manual do valor retido da caução;
     *                             null = usa soma dos danos; 0 = libera tudo
     */
    @Transactional
    public Locacao executar(Long locacaoId, String observacoes,
                            List<DanoProduto> danos, BigDecimal valorRetencaoCaucao) {
        Locacao locacao = locacaoPort.buscarPorId(locacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Locacao nao encontrada com id: " + locacaoId));

        if (!locacao.podeDevolver()) {
            throw new BusinessException(
                "Nao e possivel devolver uma locacao com status: " + locacao.getStatus() +
                ". Status esperado: " + StatusLocacao.RETIRADO);
        }

        LocalDateTime agora = LocalDateTime.now();

        // Calcula multa — O(n) onde n = itens
        BigDecimal multa = locacao.calcularMulta(agora, percentualMultaDiaria);
        locacao.setMulta(multa);

        if (multa.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("Devolucao com atraso: locacaoId={}, dataFim={}, devolucaoAt={}, multa={}",
                    locacaoId, locacao.getDataFim(), agora, multa);
        }

        // Valida e persiste danos — O(d) onde d = qtde de danos (tipicamente muito pequeno)
        BigDecimal somaDanos = BigDecimal.ZERO;
        if (danos != null && !danos.isEmpty()) {
            // Monta índice itemId → item para validação — O(n)
            Map<Long, ItemLocacao> itensPorId = locacao.getItens().stream()
                    .collect(Collectors.toMap(ItemLocacao::getId, i -> i));

            UUID tenantId = TenantContext.getTenantId();

            for (DanoProduto dano : danos) {
                ItemLocacao item = itensPorId.get(dano.getItemLocacaoId());
                if (item == null) {
                    throw new BusinessException(
                        "Item " + dano.getItemLocacaoId() + " nao pertence a locacao " + locacaoId);
                }
                dano.setLocacaoId(locacaoId);
                dano.setProdutoId(item.getProdutoId());
                dano.setTenantId(tenantId);
                dano.setRegistradoAt(agora);
                danoPort.salvar(dano);
                somaDanos = somaDanos.add(dano.getValorDesconto());
            }
            log.info("Danos registrados: locacaoId={}, qtde={}, somaDanos={}", locacaoId, danos.size(), somaDanos);
        }

        // Retencao efetiva: override explícito prevalece sobre soma de danos
        BigDecimal retencaoEfetiva = (valorRetencaoCaucao != null) ? valorRetencaoCaucao : somaDanos;

        // Para cada produto na locação: liberar + registrar entrada no estoque
        for (ItemLocacao item : locacao.getItens()) {
            var produto = produtoPort.buscarPorId(item.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado com id: " + item.getProdutoId()));
            produto.marcarDisponivel();
            produtoPort.salvar(produto);
            estoqueService.registrarEntrada(item.getProdutoId(), locacaoId, "sistema");
        }

        // Aplica destino da caução
        locacao.aplicarDestinoCaucao(retencaoEfetiva);

        locacao.setDevolucaoAt(agora);
        locacao.setStatus(StatusLocacao.DEVOLVIDO);
        if (observacoes != null && !observacoes.isBlank()) {
            locacao.setObservacoes(observacoes);
        }

        Locacao salva = locacaoPort.salvar(locacao);

        log.info("Devolucao registrada: locacaoId={}, produtos={}, multa={}, statusCaucao={}, caucaoRetida={}, devolucaoAt={}",
                salva.getId(), salva.getProdutoIds(), salva.getMulta(),
                salva.getStatusCaucao(), salva.getCaucaoRetida(), salva.getDevolucaoAt());

        contextBuilder.construir(salva).ifPresent(r ->
            notificacaoService.enviar(TipoNotificacao.DEVOLUCAO_REGISTRADA, r.email(), r.tenantId(), r.contexto())
        );

        return salva;
    }
}
