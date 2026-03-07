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
     * Busca produtos disponíveis no período informado (sem filtro de tenant — SUPER_ADMIN).
     * Usa join com itens_locacao para verificar overlap, pois produto_id migrou para itens.
     * Complexidade: O(log n) com índices em itens_locacao(produto_id) e locacoes(data_inicio, data_fim).
     */
    @Query("""
        SELECT p FROM ProdutoEntity p
        WHERE p.disponivel = true
          AND NOT EXISTS (
            SELECT 1 FROM ItemLocacaoEntity i
            JOIN LocacaoEntity l ON l.id = i.locacaoId
            WHERE i.produtoId = p.id
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
     * Busca produtos disponíveis no período informado filtrados por tenant.
     * Complexidade: O(log n) com índice composto tenant_id, produto_id, data_inicio, data_fim.
     */
    @Query("""
        SELECT p FROM ProdutoEntity p
        WHERE p.tenantId = :tenantId
          AND p.disponivel = true
          AND NOT EXISTS (
            SELECT 1 FROM ItemLocacaoEntity i
            JOIN LocacaoEntity l ON l.id = i.locacaoId
            WHERE i.produtoId = p.id
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
