package com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence;

import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

/**
 * Repositório JPA de LocacaoEntity.
 *
 * A query de overlap é o coração da regra de disponibilidade:
 * duas locações se sobrepõem quando o intervalo [A.inicio, A.fim] e [B.inicio, B.fim]
 * não são disjuntos: A.inicio < B.fim AND A.fim > B.inicio.
 * Com índice composto (produto_id, data_inicio, data_fim) -> O(log n).
 */
public interface LocacaoRepositoryJpa extends JpaRepository<LocacaoEntity, Long> {

    Page<LocacaoEntity> findByClienteId(Long clienteId, Pageable pageable);

    Page<LocacaoEntity> findByStatus(StatusLocacao status, Pageable pageable);

    /**
     * Detecta overlap de período para o mesmo produto, ignorando CANCELADO e DEVOLVIDO.
     * Exclui opcionalmente a própria locação (para updates futuros).
     *
     * Lógica: sobreposição ocorre quando dataInicio < :fim AND dataFim > :inicio.
     * Complexidade com índice: O(log n).
     */
    @Query("""
        SELECT COUNT(l) > 0 FROM LocacaoEntity l
        WHERE l.produtoId = :produtoId
          AND l.status IN ('RESERVADO', 'RETIRADO')
          AND l.dataInicio < :dataFim
          AND l.dataFim > :dataInicio
          AND (:excluirId IS NULL OR l.id <> :excluirId)
        """)
    boolean existeOverlapNoPeriodo(
            @Param("produtoId")  Long produtoId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim")    LocalDate dataFim,
            @Param("excluirId")  Long excluirId);

    /**
     * Verifica se existe locação ativa (RESERVADO ou RETIRADO) para o produto.
     * Usado para bloquear exclusão de produto.
     */
    @Query("""
        SELECT COUNT(l) > 0 FROM LocacaoEntity l
        WHERE l.produtoId = :produtoId
          AND l.status IN ('RESERVADO', 'RETIRADO')
        """)
    boolean existeLocacaoAtivaParaProduto(@Param("produtoId") Long produtoId);

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
}
