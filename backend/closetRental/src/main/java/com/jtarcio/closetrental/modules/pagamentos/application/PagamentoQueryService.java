package com.jtarcio.closetrental.modules.pagamentos.application;

import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.StatusPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.PagamentoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Consultas de pagamentos.
 * Operações somente-leitura; sem efeitos colaterais.
 */
@Service
@RequiredArgsConstructor
public class PagamentoQueryService {

    private final PagamentoRepositoryPort pagamentoPort;

    @Transactional(readOnly = true)
    public Pagamento buscarPorId(Long id) {
        return pagamentoPort.buscarPorId(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento nao encontrado com id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Pagamento> listarPorLocacao(Long locacaoId, Pageable pageable) {
        return pagamentoPort.listarPorLocacao(locacaoId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Pagamento> listarPorStatus(StatusPagamento status, Pageable pageable) {
        return pagamentoPort.listarPorStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Pagamento> listarTodos(Pageable pageable) {
        return pagamentoPort.listarTodos(pageable);
    }
}
