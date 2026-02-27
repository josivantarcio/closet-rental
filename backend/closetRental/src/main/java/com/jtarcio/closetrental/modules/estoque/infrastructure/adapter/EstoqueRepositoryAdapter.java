package com.jtarcio.closetrental.modules.estoque.infrastructure.adapter;

import com.jtarcio.closetrental.modules.estoque.domain.model.MovimentacaoEstoque;
import com.jtarcio.closetrental.modules.estoque.domain.model.TipoMovimentacao;
import com.jtarcio.closetrental.modules.estoque.domain.ports.EstoqueRepositoryPort;
import com.jtarcio.closetrental.modules.estoque.infrastructure.mapper.MovimentacaoEstoqueMapper;
import com.jtarcio.closetrental.modules.estoque.infrastructure.persistence.MovimentacaoEstoqueRepositoryJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Adapter que implementa EstoqueRepositoryPort usando JPA + MapStruct.
 */
@Component
@RequiredArgsConstructor
public class EstoqueRepositoryAdapter implements EstoqueRepositoryPort {

    private final MovimentacaoEstoqueRepositoryJpa jpa;
    private final MovimentacaoEstoqueMapper mapper;

    @Override
    public MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao) {
        return mapper.toDomain(jpa.save(mapper.toEntity(movimentacao)));
    }

    @Override
    public Page<MovimentacaoEstoque> listarPorProduto(Long produtoId, Pageable pageable) {
        return jpa.findByProdutoIdOrderByCriadoEmDesc(produtoId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<MovimentacaoEstoque> listarPorTipo(TipoMovimentacao tipo, Pageable pageable) {
        return jpa.findByTipoOrderByCriadoEmDesc(tipo, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<MovimentacaoEstoque> listarTodos(Pageable pageable) {
        return jpa.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public long contarSaidasPorProduto(Long produtoId) {
        return jpa.countSaidasByProdutoId(produtoId);
    }
}
