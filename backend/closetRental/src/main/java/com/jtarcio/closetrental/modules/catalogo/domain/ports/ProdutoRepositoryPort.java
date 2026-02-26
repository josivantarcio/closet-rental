package com.jtarcio.closetrental.modules.catalogo.domain.ports;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface ProdutoRepositoryPort {

    Produto salvar(Produto produto);

    Page<Produto> listarTodos(Pageable pageable);

    Optional<Produto> buscarPorId(Long id);

    Page<Produto> buscarDisponiveisNoPeriodo(LocalDate dataInicio, LocalDate dataFim, Pageable pageable);

    void deletar(Long id);

    boolean existeEmLocacaoAtiva(Long id);
}
