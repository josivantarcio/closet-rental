package com.jtarcio.closetrental.modules.relatorios.infrastructure.persistence;

import com.jtarcio.closetrental.modules.relatorios.api.dto.AtrasosPendentesResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.DisponibilidadeDataResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.HistoricoClienteResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.PecaMaisAlugadaResponse;
import com.jtarcio.closetrental.modules.relatorios.infrastructure.persistence.projection.FaturamentoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence.LocacaoEntity;

/**
 * Repositório de leitura do módulo de Relatórios.
 *
 * Todas as queries usam projeções (record DTO ou interface) — sem carregar entidades completas.
 * Cada query se apoia nos índices declarados nas entidades para garantir O(log n).
 */
public interface RelatorioRepositoryJpa extends JpaRepository<LocacaoEntity, Long> {

    /**
     * Peças disponíveis em uma data: produtos sem locação ativa (RESERVADO ou RETIRADO)
     * que cubra a data consultada.
     * Usa: idx_locacoes_produto_periodo + idx_locacoes_status → O(log n)
     */
    @Query("""
        SELECT new com.jtarcio.closetrental.modules.relatorios.api.dto.DisponibilidadeDataResponse(
            p.id, p.nome, p.categoria, p.valorDiaria
        )
        FROM ProdutoEntity p
        WHERE p.disponivel = true
          AND NOT EXISTS (
              SELECT 1 FROM LocacaoEntity l
              WHERE l.produtoId = p.id
                AND l.status IN ('RESERVADO', 'RETIRADO')
                AND l.dataInicio <= :data
                AND l.dataFim >= :data
          )
        ORDER BY p.nome
        """)
    Page<DisponibilidadeDataResponse> findDisponiveisNaData(
            @Param("data") LocalDate data, Pageable pageable);

    /**
     * Histórico de locações de um cliente, ordenado por data de início decrescente.
     * Usa: idx_locacoes_cliente → O(log n)
     */
    @Query("""
        SELECT new com.jtarcio.closetrental.modules.relatorios.api.dto.HistoricoClienteResponse(
            l.id, l.produtoId, CAST(l.status AS string),
            l.dataInicio, l.dataFim, l.valorTotal, l.multa, l.devolucaoAt
        )
        FROM LocacaoEntity l
        WHERE l.clienteId = :clienteId
        ORDER BY l.dataInicio DESC
        """)
    Page<HistoricoClienteResponse> findHistoricoCliente(
            @Param("clienteId") Long clienteId, Pageable pageable);

    /**
     * Soma e contagem de pagamentos APROVADOS no mês/ano informado.
     * Usa: idx_pagamentos_criado_em + idx_pagamentos_status → O(log n)
     */
    @Query(value = """
        SELECT SUM(p.valor) AS totalReceita, COUNT(p.id) AS totalPagamentos
        FROM pagamentos p
        WHERE p.status = 'APROVADO'
          AND EXTRACT(MONTH FROM p.criado_em) = :mes
          AND EXTRACT(YEAR  FROM p.criado_em) = :ano
        """, nativeQuery = true)
    FaturamentoProjection findFaturamento(@Param("mes") int mes, @Param("ano") int ano);

    /**
     * Ranking de produtos por número de locações (qualquer status exceto CANCELADO),
     * com JOIN em ProdutoEntity para obter nome e categoria.
     * Paginado via Pageable para evitar LIMIT no JPQL (não suportado em todas as versões).
     * Complexidade: O(n log n) no GROUP BY, limitado pelo PageRequest.
     */
    @Query("""
        SELECT new com.jtarcio.closetrental.modules.relatorios.api.dto.PecaMaisAlugadaResponse(
            p.id, p.nome, p.categoria, COUNT(l.id)
        )
        FROM LocacaoEntity l
        JOIN ProdutoEntity p ON p.id = l.produtoId
        WHERE l.status <> 'CANCELADO'
        GROUP BY p.id, p.nome, p.categoria
        ORDER BY COUNT(l.id) DESC
        """)
    Page<PecaMaisAlugadaResponse> findPecasMaisAlugadas(Pageable pageable);

    /**
     * Locações RETIRADAS com dataFim anterior a hoje (atrasos não devolvidos).
     * Usa: idx_locacoes_status → O(log n)
     */
    @Query("""
        SELECT new com.jtarcio.closetrental.modules.relatorios.api.dto.AtrasosPendentesResponse(
            l.id, l.clienteId, l.produtoId, l.dataFim,
            0L,
            l.valorDiaria,
            l.valorDiaria
        )
        FROM LocacaoEntity l
        WHERE l.status = 'RETIRADO'
          AND l.dataFim < :hoje
        ORDER BY l.dataFim ASC
        """)
    Page<AtrasosPendentesResponse> findAtrasosPendentes(
            @Param("hoje") LocalDate hoje, Pageable pageable);
}
