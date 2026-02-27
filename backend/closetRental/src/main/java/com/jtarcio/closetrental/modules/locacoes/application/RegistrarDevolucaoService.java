package com.jtarcio.closetrental.modules.locacoes.application;

import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Use Case: Registrar a devolucao de uma locacao.
 *
 * Regras:
 * 1. Locacao deve existir e estar no status RETIRADO
 * 2. Registra devolucaoAt = agora
 * 3. Calcula multa por atraso se devolucaoAt > dataFim
 * 4. Muda status para DEVOLVIDO
 * 5. Marca o produto como disponivel novamente
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrarDevolucaoService {

    private final LocacaoRepositoryPort locacaoPort;
    private final ProdutoRepositoryPort produtoPort;

    @Value("${app.locacao.percentual-multa-diaria:0.10}")
    private BigDecimal percentualMultaDiaria;

    @Transactional
    public Locacao executar(Long locacaoId, String observacoes) {
        Locacao locacao = locacaoPort.buscarPorId(locacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Locacao nao encontrada com id: " + locacaoId));

        if (!locacao.podeDevolver()) {
            throw new BusinessException(
                "Nao e possivel devolver uma locacao com status: " + locacao.getStatus() +
                ". Status esperado: " + StatusLocacao.RETIRADO);
        }

        LocalDateTime agora = LocalDateTime.now();

        // Calcula multa — O(1)
        BigDecimal multa = locacao.calcularMulta(agora, percentualMultaDiaria);
        locacao.setMulta(multa);

        if (multa.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("Devolucao com atraso: locacaoId={}, dataFim={}, devolucaoAt={}, multa={}",
                    locacaoId, locacao.getDataFim(), agora, multa);
        }

        // Libera o produto
        var produto = produtoPort.buscarPorId(locacao.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado com id: " + locacao.getProdutoId()));
        produto.marcarDisponivel();
        produtoPort.salvar(produto);

        locacao.setDevolucaoAt(agora);
        locacao.setStatus(StatusLocacao.DEVOLVIDO);
        if (observacoes != null && !observacoes.isBlank()) {
            locacao.setObservacoes(observacoes);
        }

        Locacao salva = locacaoPort.salvar(locacao);
        log.info("Devolucao registrada: locacaoId={}, produtoId={}, multa={}, devolucaoAt={}",
                salva.getId(), salva.getProdutoId(), salva.getMulta(), salva.getDevolucaoAt());
        return salva;
    }
}
