package com.jtarcio.closetrental.modules.catalogo.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ProdutoRepositoryJpa extends JpaRepository<ProdutoEntity, Long> {

    // TODO: incluir subquery de LocacaoEntity quando módulo de locações for implementado
    @Query("SELECT p FROM ProdutoEntity p WHERE p.disponivel = true")
    Page<ProdutoEntity> buscarDisponiveisNoPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            Pageable pageable);
}
