package com.jtarcio.closetrental.modules.catalogo.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface ProdutoRepositoryJpa extends JpaRepository<ProdutoEntity, Long> {

    Page<ProdutoEntity> findByTenantId(UUID tenantId, Pageable pageable);

    /**
     * Busca produtos disponiveis no periodo informado (sem filtro de tenant — SUPER_ADMIN).
     * Complexidade: O(log n) com indices em produto_id, data_inicio, data_fim.
     */
    @Query("""
        SELECT p FROM ProdutoEntity p
        WHERE p.disponivel = true
          AND NOT EXISTS (
            SELECT 1 FROM LocacaoEntity l
            WHERE l.produtoId = p.id
              AND l.status IN ('RESERVADO', 'RETIRADO')
              AND l.dataInicio < :dataFim
              AND l.dataFim > :dataInicio
          )
        """)
    Page<ProdutoEntity> buscarDisponiveisNoPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            Pageable pageable);

    /**
     * Busca produtos disponiveis no periodo informado filtrados por tenant.
     * Complexidade: O(log n) com indice composto tenant_id, produto_id, data_inicio, data_fim.
     */
    @Query("""
        SELECT p FROM ProdutoEntity p
        WHERE p.tenantId = :tenantId
          AND p.disponivel = true
          AND NOT EXISTS (
            SELECT 1 FROM LocacaoEntity l
            WHERE l.produtoId = p.id
              AND l.tenantId = :tenantId
              AND l.status IN ('RESERVADO', 'RETIRADO')
              AND l.dataInicio < :dataFim
              AND l.dataFim > :dataInicio
          )
        """)
    Page<ProdutoEntity> buscarDisponiveisNoPeriodoByTenant(
            @Param("tenantId") UUID tenantId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            Pageable pageable);
}
