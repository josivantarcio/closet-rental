package com.jtarcio.closetrental.modules.locacoes.application;

import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.estoque.application.EstoqueService;
import com.jtarcio.closetrental.modules.locacoes.domain.model.ItemLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoContextBuilder;
import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoService;
import com.jtarcio.closetrental.modules.notificacoes.domain.model.TipoNotificacao;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Use Case: Registrar a retirada de uma locacao.
 *
 * Regras:
 * 1. Locacao deve existir e estar no status RESERVADO
 * 2. Registra retiradaAt = agora
 * 3. Muda status para RETIRADO
 * 4. Para cada item: marca o produto como indisponivel e registra SAIDA no estoque
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrarRetiradaService {

    private final LocacaoRepositoryPort locacaoPort;
    private final ProdutoRepositoryPort produtoPort;
    private final EstoqueService estoqueService;
    private final NotificacaoService notificacaoService;
    private final NotificacaoContextBuilder contextBuilder;

    @Transactional
    public Locacao executar(Long locacaoId) {
        Locacao locacao = locacaoPort.buscarPorId(locacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Locacao nao encontrada com id: " + locacaoId));

        if (!locacao.podeRetirar()) {
            throw new BusinessException(
                "Nao e possivel retirar uma locacao com status: " + locacao.getStatus() +
                ". Status esperado: " + StatusLocacao.RESERVADO);
        }

        // Para cada produto na locação: marcar indisponível + registrar saída no estoque
        for (ItemLocacao item : locacao.getItens()) {
            var produto = produtoPort.buscarPorId(item.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado com id: " + item.getProdutoId()));
            produto.marcarIndisponivel();
            produtoPort.salvar(produto);
            estoqueService.registrarSaida(item.getProdutoId(), locacaoId, "sistema");
        }

        locacao.setRetiradaAt(LocalDateTime.now());
        locacao.setStatus(StatusLocacao.RETIRADO);
        Locacao salva = locacaoPort.salvar(locacao);

        log.info("Retirada registrada: locacaoId={}, produtos={}, retiradaAt={}",
                salva.getId(), salva.getProdutoIds(), salva.getRetiradaAt());

        contextBuilder.construir(salva).ifPresent(r ->
            notificacaoService.enviar(TipoNotificacao.RETIRADA_REGISTRADA, r.email(), r.tenantId(), r.contexto())
        );

        return salva;
    }
}
