package com.jtarcio.closetrental.modules.relatorios.infrastructure.persistence;

import com.jtarcio.closetrental.modules.relatorios.api.dto.AtrasosPendentesResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.DisponibilidadeDataResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.HistoricoClienteResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.PecaMaisAlugadaResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.ReceitaCategoriaResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.TicketMedioResponse;
import com.jtarcio.closetrental.modules.relatorios.api.dto.TopClienteResponse;
import com.jtarcio.closetrental.modules.relatorios.infrastructure.persistence.projection.FaturamentoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence.LocacaoEntity;

/**
 * Repositório de leitura do módulo de Relatórios.
 *
 * Todas as queries usam projeções (record DTO ou interface) — sem carregar entidades completas.
 * Após migração para locação multi-produto, as queries de produto usam join com ItemLocacaoEntity.
 */
public interface RelatorioRepositoryJpa extends JpaRepository<LocacaoEntity, Long> {

    /**
     * Peças disponíveis em uma data: produtos sem locação ativa (RESERVADO ou RETIRADO)
     * que cubra a data consultada.
     * Usa join com ItemLocacaoEntity para localizar produto_id após migração multi-produto.
     */
    @Query("""
        SELECT new com.jtarcio.closetrental.modules.relatorios.api.dto.DisponibilidadeDataResponse(
            p.id, p.nome, p.categoria, p.valorDiaria
        )
        FROM ProdutoEntity p
        WHERE p.disponivel = true
          AND NOT EXISTS (
              SELECT 1 FROM ItemLocacaoEntity i
              JOIN LocacaoEntity l ON l.id = i.locacaoId
              WHERE i.produtoId = p.id
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
     * Retorna uma linha por locação com a lista de produtoIds como string concatenada
     * (via subquery não é possível em JPQL DTO constructor — retorna locacaoId para o frontend buscar itens).
     * Usa: idx_locacoes_cliente → O(log n)
     */
    @Query("""
        SELECT new com.jtarcio.closetrental.modules.relatorios.api.dto.HistoricoClienteResponse(
            l.id, CAST(l.status AS string),
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
     * usando join com ItemLocacaoEntity para agrupar por produto.
     * Complexidade: O(n log n) no GROUP BY, limitado pelo PageRequest.
     */
    @Query("""
        SELECT new com.jtarcio.closetrental.modules.relatorios.api.dto.PecaMaisAlugadaResponse(
            p.id, p.nome, p.categoria, COUNT(DISTINCT l.id)
        )
        FROM ItemLocacaoEntity i
        JOIN LocacaoEntity l ON l.id = i.locacaoId
        JOIN ProdutoEntity p ON p.id = i.produtoId
        WHERE l.status <> 'CANCELADO'
        GROUP BY p.id, p.nome, p.categoria
        ORDER BY COUNT(DISTINCT l.id) DESC
        """)
    Page<PecaMaisAlugadaResponse> findPecasMaisAlugadas(Pageable pageable);

    /**
     * Locações RETIRADAS com dataFim anterior a hoje (atrasos não devolvidos).
     * Retorna uma linha por item — no caso de locação multi-produto, aparece uma linha por produto.
     * O service agrupa/filtra conforme necessário.
     * Usa: idx_locacoes_status + idx_itens_locacao_locacao_id → O(log n)
     */
    @Query("""
        SELECT new com.jtarcio.closetrental.modules.relatorios.api.dto.AtrasosPendentesResponse(
            l.id, l.clienteId, i.produtoId, l.dataFim,
            0L,
            i.valorDiaria,
            i.valorDiaria
        )
        FROM LocacaoEntity l
        JOIN l.itens i
        WHERE l.status = 'RETIRADO'
          AND l.dataFim < :hoje
        ORDER BY l.dataFim ASC, i.id ASC
        """)
    Page<AtrasosPendentesResponse> findAtrasosPendentes(
            @Param("hoje") LocalDate hoje, Pageable pageable);

    /**
     * Top clientes por receita: soma dos pagamentos APROVADOS por cliente.
     * JOIN pagamentos via locacaoId — usa índice composto status+locacao.
     */
    @Query(value = """
        SELECT l.cliente_id  AS clienteId,
               c.nome        AS nomeCliente,
               COUNT(DISTINCT l.id) AS totalLocacoes,
               COALESCE(SUM(p.valor), 0) AS receitaTotal
        FROM locacoes l
        JOIN clientes c ON c.id = l.cliente_id
        LEFT JOIN pagamentos p ON p.locacao_id = l.id AND p.status = 'APROVADO'
        WHERE l.status <> 'CANCELADO'
        GROUP BY l.cliente_id, c.nome
        ORDER BY receitaTotal DESC
        LIMIT :limite
        """, nativeQuery = true)
    List<Object[]> findTopClientes(@Param("limite") int limite);

    /**
     * Ticket médio mensal: receita total / número de locações não-canceladas no mês.
     */
    @Query(value = """
        SELECT EXTRACT(MONTH FROM p.criado_em) AS mes,
               EXTRACT(YEAR  FROM p.criado_em) AS ano,
               COUNT(DISTINCT l.id)            AS totalLocacoes,
               SUM(p.valor)                    AS receitaTotal
        FROM pagamentos p
        JOIN locacoes l ON l.id = p.locacao_id
        WHERE p.status = 'APROVADO'
          AND p.criado_em >= :de
          AND p.criado_em < :ate
        GROUP BY mes, ano
        ORDER BY ano, mes
        """, nativeQuery = true)
    List<Object[]> findTicketMedioPorPeriodo(
            @Param("de")  java.time.LocalDateTime de,
            @Param("ate") java.time.LocalDateTime ate);

    /**
     * Receita por categoria de produto.
     */
    @Query(value = """
        SELECT p.categoria   AS categoria,
               COUNT(DISTINCT l.id) AS totalLocacoes,
               COALESCE(SUM(pg.valor), 0) AS receitaTotal
        FROM itens_locacao il
        JOIN locacoes l ON l.id = il.locacao_id
        JOIN produtos p ON p.id = il.produto_id
        LEFT JOIN pagamentos pg ON pg.locacao_id = l.id AND pg.status = 'APROVADO'
        WHERE l.status <> 'CANCELADO'
        GROUP BY p.categoria
        ORDER BY receitaTotal DESC
        """, nativeQuery = true)
    List<Object[]> findReceitaPorCategoria();

    /**
     * Faturamento mensal num intervalo: usado para gráfico de linha multi-mês.
     */
    @Query(value = """
        SELECT EXTRACT(MONTH FROM p.criado_em) AS mes,
               EXTRACT(YEAR  FROM p.criado_em) AS ano,
               SUM(p.valor)   AS totalReceita,
               COUNT(p.id)    AS totalPagamentos
        FROM pagamentos p
        WHERE p.status = 'APROVADO'
          AND p.criado_em >= :de
          AND p.criado_em < :ate
        GROUP BY mes, ano
        ORDER BY ano, mes
        """, nativeQuery = true)
    List<Object[]> findFaturamentoPorPeriodo(
            @Param("de")  java.time.LocalDateTime de,
            @Param("ate") java.time.LocalDateTime ate);
}
