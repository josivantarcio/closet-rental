package com.jtarcio.closetrental.modules.locacoes.application;

import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
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
 * 4. Marca o produto como indisponivel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrarRetiradaService {

    private final LocacaoRepositoryPort locacaoPort;
    private final ProdutoRepositoryPort produtoPort;

    @Transactional
    public Locacao executar(Long locacaoId) {
        Locacao locacao = locacaoPort.buscarPorId(locacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Locacao nao encontrada com id: " + locacaoId));

        if (!locacao.podeRetirar()) {
            throw new BusinessException(
                "Nao e possivel retirar uma locacao com status: " + locacao.getStatus() +
                ". Status esperado: " + StatusLocacao.RESERVADO);
        }

        // Marca produto como indisponivel
        var produto = produtoPort.buscarPorId(locacao.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado com id: " + locacao.getProdutoId()));
        produto.marcarIndisponivel();
        produtoPort.salvar(produto);

        locacao.setRetiradaAt(LocalDateTime.now());
        locacao.setStatus(StatusLocacao.RETIRADO);

        Locacao salva = locacaoPort.salvar(locacao);
        log.info("Retirada registrada: locacaoId={}, produtoId={}, retiradaAt={}",
                salva.getId(), salva.getProdutoId(), salva.getRetiradaAt());
        return salva;
    }
}
