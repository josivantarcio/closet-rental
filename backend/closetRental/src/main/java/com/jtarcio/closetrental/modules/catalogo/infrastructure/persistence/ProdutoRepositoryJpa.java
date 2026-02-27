package com.jtarcio.closetrental.modules.catalogo.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ProdutoRepositoryJpa extends JpaRepository<ProdutoEntity, Long> {

    /**
     * Busca produtos disponiveis no periodo informado.
     * Exclui produtos que possuam locacoes RESERVADAS ou RETIRADAS com overlap no periodo.
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
}
