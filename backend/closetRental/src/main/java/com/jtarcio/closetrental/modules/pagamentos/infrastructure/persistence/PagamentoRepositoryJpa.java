package com.jtarcio.closetrental.modules.pagamentos.infrastructure.persistence;

import com.jtarcio.closetrental.modules.pagamentos.domain.model.StatusPagamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositório Spring Data JPA para {@link PagamentoEntity}.
 * Consultas por índice — O(log n).
 */
public interface PagamentoRepositoryJpa extends JpaRepository<PagamentoEntity, Long> {

    Page<PagamentoEntity> findByLocacaoIdOrderByCriadoEmDesc(Long locacaoId, Pageable pageable);

    Page<PagamentoEntity> findByStatusOrderByCriadoEmDesc(StatusPagamento status, Pageable pageable);

    Page<PagamentoEntity> findByTenantIdAndStatusOrderByCriadoEmDesc(UUID tenantId, StatusPagamento status, Pageable pageable);

    Page<PagamentoEntity> findByTenantIdOrderByCriadoEmDesc(UUID tenantId, Pageable pageable);
}
