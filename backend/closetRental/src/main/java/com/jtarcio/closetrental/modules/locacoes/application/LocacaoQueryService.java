package com.jtarcio.closetrental.modules.locacoes.application;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import com.jtarcio.closetrental.shared.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de consulta (leitura) de locacoes.
 * Separado dos use cases de escrita seguindo o principio de segregacao de responsabilidades.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocacaoQueryService {

    private final LocacaoRepositoryPort locacaoPort;

    @Transactional(readOnly = true)
    public Locacao buscarPorId(Long id) {
        log.info("Buscando locacao id: {}", id);
        return locacaoPort.buscarPorId(id)
                .orElseThrow(() -> new EntityNotFoundException("Locacao nao encontrada com id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Locacao> listarTodos(Pageable pageable) {
        log.info("Listando todas as locacoes — pagina {}", pageable.getPageNumber());
        return locacaoPort.listarTodos(TenantContext.getTenantId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Locacao> listarPorCliente(Long clienteId, Pageable pageable) {
        log.info("Listando locacoes do cliente id: {}", clienteId);
        return locacaoPort.listarPorCliente(TenantContext.getTenantId(), clienteId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Locacao> listarPorStatus(StatusLocacao status, Pageable pageable) {
        log.info("Listando locacoes com status: {}", status);
        return locacaoPort.listarPorStatus(TenantContext.getTenantId(), status, pageable);
    }
}
