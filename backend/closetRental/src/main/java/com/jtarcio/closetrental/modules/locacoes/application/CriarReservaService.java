package com.jtarcio.closetrental.modules.locacoes.application;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.clientes.domain.ports.ClienteRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.domain.model.ItemLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoContextBuilder;
import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoService;
import com.jtarcio.closetrental.modules.notificacoes.domain.model.TipoNotificacao;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.ConflictException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import com.jtarcio.closetrental.shared.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Use Case: Criar uma nova reserva multi-produto.
 *
 * Regras aplicadas:
 * 1. dataInicio deve ser anterior a dataFim
 * 2. dataInicio nao pode ser no passado
 * 3. Cliente deve existir e estar ativo
 * 4. Cada produto deve existir e estar disponivel
 * 5. Nenhuma locacao ativa pode se sobrepor ao periodo para cada produto (O(log n) com indice)
 * 6. Calcula subtotal de cada item = valorDiaria * dias
 * 7. Calcula valorTotal = soma(subtotais) + caucao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CriarReservaService {

    private final LocacaoRepositoryPort locacaoPort;
    private final ProdutoRepositoryPort produtoPort;
    private final ClienteRepositoryPort clientePort;
    private final NotificacaoService notificacaoService;
    private final NotificacaoContextBuilder contextBuilder;

    @Value("${app.locacao.caucao-padrao:100.00}")
    private BigDecimal caucaoPadrao;

    @Transactional
    public Locacao executar(Long clienteId, List<Long> produtoIds,
                            LocalDate dataInicio, LocalDate dataFim,
                            BigDecimal caucaoCustom, String observacoes) {

        // 1. Validar datas
        if (!dataInicio.isBefore(dataFim)) {
            throw new BusinessException("A data de inicio deve ser anterior a data de fim");
        }
        if (dataInicio.isBefore(LocalDate.now())) {
            throw new BusinessException("A data de inicio nao pode ser no passado");
        }

        // 2. Validar cliente
        var cliente = clientePort.buscarPorId(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado com id: " + clienteId));
        if (!cliente.isAtivo()) {
            throw new BusinessException("Nao e possivel criar reserva para cliente inativo");
        }

        // 3. Validar produtos e overlap — O(n * log m) onde n = itens, m = locações
        UUID tenantId = TenantContext.getTenantId();
        List<ItemLocacao> itens = new ArrayList<>();

        for (Long produtoId : produtoIds) {
            Produto produto = produtoPort.buscarPorId(produtoId)
                    .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado com id: " + produtoId));
            if (!produto.isDisponivel()) {
                throw new BusinessException("Produto '" + produto.getNome() + "' nao esta disponivel para locacao");
            }

            boolean temOverlap = locacaoPort.existeOverlapNoPeriodo(produtoId, dataInicio, dataFim, null);
            if (temOverlap) {
                throw new ConflictException(
                    "Produto '" + produto.getNome() + "' ja possui locacao ativa no periodo de " + dataInicio + " a " + dataFim);
            }

            ItemLocacao item = new ItemLocacao();
            item.setProdutoId(produtoId);
            item.setNomeProduto(produto.getNome());
            item.setValorDiaria(produto.getValorDiaria());
            item.setTenantId(tenantId);
            itens.add(item);
        }

        // 4. Montar locação
        BigDecimal caucaoEfetiva = (caucaoCustom != null) ? caucaoCustom : caucaoPadrao;

        Locacao locacao = new Locacao();
        locacao.setTenantId(tenantId);
        locacao.setClienteId(clienteId);
        locacao.setDataInicio(dataInicio);
        locacao.setDataFim(dataFim);
        locacao.setStatus(StatusLocacao.RESERVADO);
        locacao.setCaucao(caucaoEfetiva);
        locacao.setMulta(BigDecimal.ZERO);
        locacao.setObservacoes(observacoes);
        locacao.setItens(itens);
        locacao.setValorTotal(locacao.calcularValorTotal());

        Locacao salva = locacaoPort.salvar(locacao);
        log.info("Reserva criada: id={}, cliente={}, produtos={}, periodo={} a {}, valorTotal={}",
                salva.getId(), clienteId, produtoIds, dataInicio, dataFim, salva.getValorTotal());

        contextBuilder.construir(salva).ifPresent(r ->
            notificacaoService.enviar(TipoNotificacao.RESERVA_CRIADA, r.email(), r.tenantId(), r.contexto())
        );

        return salva;
    }
}
