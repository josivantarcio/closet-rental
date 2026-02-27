package com.jtarcio.closetrental.modules.locacoes.application;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Cancelar uma reserva.
 *
 * Regras:
 * 1. Locacao deve existir
 * 2. Apenas locacoes com status RESERVADO podem ser canceladas
 * 3. Locacoes RETIRADAS ou DEVOLVIDAS nao podem ser canceladas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancelarReservaService {

    private final LocacaoRepositoryPort locacaoPort;

    @Transactional
    public Locacao executar(Long locacaoId, String motivoCancelamento) {
        Locacao locacao = locacaoPort.buscarPorId(locacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Locacao nao encontrada com id: " + locacaoId));

        if (!locacao.podeCancelar()) {
            throw new BusinessException(
                "Nao e possivel cancelar uma locacao com status: " + locacao.getStatus() +
                ". Apenas locacoes com status RESERVADO podem ser canceladas.");
        }

        locacao.setStatus(StatusLocacao.CANCELADO);
        if (motivoCancelamento != null && !motivoCancelamento.isBlank()) {
            locacao.setObservacoes(motivoCancelamento);
        }

        Locacao salva = locacaoPort.salvar(locacao);
        log.info("Reserva cancelada: locacaoId={}", salva.getId());
        return salva;
    }
}
