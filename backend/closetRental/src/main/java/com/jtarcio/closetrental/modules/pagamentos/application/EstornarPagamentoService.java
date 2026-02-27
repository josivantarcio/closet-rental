package com.jtarcio.closetrental.modules.pagamentos.application;

import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.StatusPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.PagamentoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Use Case: Estornar um pagamento aprovado.
 *
 * Regras:
 * 1. Pagamento deve existir
 * 2. Apenas pagamentos com status APROVADO podem ser estornados
 * 3. Status é alterado para ESTORNADO e o motivo é registrado
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EstornarPagamentoService {

    private final PagamentoRepositoryPort pagamentoPort;

    @Transactional
    public Pagamento executar(Long pagamentoId, String motivo) {
        Pagamento pagamento = pagamentoPort.buscarPorId(pagamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento nao encontrado com id: " + pagamentoId));

        if (!pagamento.podeEstornar()) {
            throw new BusinessException(
                "Nao e possivel estornar um pagamento com status: " + pagamento.getStatus() +
                ". Apenas pagamentos com status APROVADO podem ser estornados.");
        }

        pagamento.setStatus(StatusPagamento.ESTORNADO);
        pagamento.setMotivo(motivo);
        pagamento.setProcessadoEm(LocalDateTime.now());

        Pagamento salvo = pagamentoPort.salvar(pagamento);
        log.info("Pagamento estornado: id={}, locacao={}", salvo.getId(), salvo.getLocacaoId());
        return salvo;
    }
}
