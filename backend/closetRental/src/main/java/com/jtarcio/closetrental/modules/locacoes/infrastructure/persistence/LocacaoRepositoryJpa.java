package com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence;

import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Repositório JPA de LocacaoEntity.
 *
 * As queries de overlap e existência de locação ativa por produto foram movidas
 * para {@link ItemLocacaoRepositoryJpa} após a migração para locação multi-produto.
 */
public interface LocacaoRepositoryJpa extends JpaRepository<LocacaoEntity, Long> {

    Page<LocacaoEntity> findByClienteId(Long clienteId, Pageable pageable);

    Page<LocacaoEntity> findByTenantIdAndClienteId(UUID tenantId, Long clienteId, Pageable pageable);

    Page<LocacaoEntity> findByStatus(StatusLocacao status, Pageable pageable);

    Page<LocacaoEntity> findByTenantIdAndStatus(UUID tenantId, StatusLocacao status, Pageable pageable);

    Page<LocacaoEntity> findByTenantId(UUID tenantId, Pageable pageable);

    /**
     * Verifica se existe locação ativa (RESERVADO ou RETIRADO) para o cliente.
     * Usado para bloquear exclusão de cliente.
     */
    @Query("""
        SELECT COUNT(l) > 0 FROM LocacaoEntity l
        WHERE l.clienteId = :clienteId
          AND l.status IN ('RESERVADO', 'RETIRADO')
        """)
    boolean existeLocacaoAtivaParaCliente(@Param("clienteId") Long clienteId);

    /**
     * Retorna as locações ativas (RESERVADO ou RETIRADO) que se intersectam
     * com o mês informado, para qualquer produto com o produtoId.
     * Delegado ao adapter via join com itens.
     * Query base: busca por datas.
     */
    @Query("""
        SELECT DISTINCT l FROM LocacaoEntity l
        JOIN l.itens i
        WHERE i.produtoId = :produtoId
          AND l.status IN ('RESERVADO', 'RETIRADO')
          AND l.dataInicio <= :fimMes
          AND l.dataFim >= :inicioMes
        ORDER BY l.dataInicio ASC
        """)
    java.util.List<LocacaoEntity> buscarPeriodosOcupadosNoMes(
            @Param("produtoId")  Long produtoId,
            @Param("inicioMes") LocalDate inicioMes,
            @Param("fimMes")    LocalDate fimMes);
}
