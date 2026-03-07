package com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositório JPA de ItemLocacaoEntity.
 */
public interface ItemLocacaoRepositoryJpa extends JpaRepository<ItemLocacaoEntity, Long> {

    /**
     * Detecta overlap de período para o mesmo produto via join com locacoes,
     * ignorando CANCELADO e DEVOLVIDO. Exclui opcionalmente a própria locação.
     *
     * Complexidade: O(log n) com índice em itens_locacao(produto_id) + locacoes(data_inicio, data_fim).
     */
    @Query("""
        SELECT COUNT(i) > 0 FROM ItemLocacaoEntity i
        JOIN LocacaoEntity l ON l.id = i.locacaoId
        WHERE i.produtoId = :produtoId
          AND l.status IN ('RESERVADO', 'RETIRADO')
          AND l.dataInicio < :dataFim
          AND l.dataFim > :dataInicio
          AND (:excluirLocacaoId IS NULL OR l.id <> :excluirLocacaoId)
        """)
    boolean existeOverlapNoPeriodo(
            @Param("produtoId")        Long produtoId,
            @Param("dataInicio")       java.time.LocalDate dataInicio,
            @Param("dataFim")          java.time.LocalDate dataFim,
            @Param("excluirLocacaoId") Long excluirLocacaoId);

    /**
     * Retorna verdadeiro se existe ao menos um item com o produto em locação
     * com status RESERVADO ou RETIRADO.
     * Usado para bloquear exclusão de produto.
     */
    @Query("""
        SELECT COUNT(i) > 0 FROM ItemLocacaoEntity i
        JOIN LocacaoEntity l ON l.id = i.locacaoId
        WHERE i.produtoId = :produtoId
          AND l.status IN ('RESERVADO', 'RETIRADO')
        """)
    boolean existeLocacaoAtivaParaProduto(@Param("produtoId") Long produtoId);

    /**
     * Retorna os itens de locações ativas (RESERVADO ou RETIRADO) para um produto
     * que se intersectam com o mês informado.
     * Usado pelo calendário de ocupação.
     */
    @Query("""
        SELECT i FROM ItemLocacaoEntity i
        JOIN LocacaoEntity l ON l.id = i.locacaoId
        WHERE i.produtoId = :produtoId
          AND l.status IN ('RESERVADO', 'RETIRADO')
          AND l.dataInicio <= :fimMes
          AND l.dataFim >= :inicioMes
        ORDER BY l.dataInicio ASC
        """)
    java.util.List<ItemLocacaoEntity> buscarItensPorProdutoNoMes(
            @Param("produtoId")  Long produtoId,
            @Param("inicioMes") java.time.LocalDate inicioMes,
            @Param("fimMes")    java.time.LocalDate fimMes);
}
