package com.jtarcio.closetrental.modules.estoque.infrastructure.persistence;

import com.jtarcio.closetrental.modules.estoque.domain.model.TipoMovimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Repositorio JPA de movimentacoes de estoque.
 * Todas as queries usam indices — complexidade O(log n).
 */
public interface MovimentacaoEstoqueRepositoryJpa
        extends JpaRepository<MovimentacaoEstoqueEntity, Long> {

    Page<MovimentacaoEstoqueEntity> findByProdutoIdOrderByCriadoEmDesc(Long produtoId, Pageable pageable);

    Page<MovimentacaoEstoqueEntity> findByTipoOrderByCriadoEmDesc(TipoMovimentacao tipo, Pageable pageable);

    Page<MovimentacaoEstoqueEntity> findByTenantIdAndTipoOrderByCriadoEmDesc(UUID tenantId, TipoMovimentacao tipo, Pageable pageable);

    Page<MovimentacaoEstoqueEntity> findByTenantIdOrderByCriadoEmDesc(UUID tenantId, Pageable pageable);

    /**
     * Conta total de saidas (locacoes) de uma peca — para ranking de mais alugadas.
     * Usa indice composto (produto_id, tipo) — O(log n).
     */
    @Query("SELECT COUNT(m) FROM MovimentacaoEstoqueEntity m WHERE m.produtoId = :produtoId AND m.tipo = 'SAIDA'")
    long countSaidasByProdutoId(@Param("produtoId") Long produtoId);
}
