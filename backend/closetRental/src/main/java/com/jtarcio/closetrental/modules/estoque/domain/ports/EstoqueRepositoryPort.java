package com.jtarcio.closetrental.modules.estoque.domain.ports;

import com.jtarcio.closetrental.modules.estoque.domain.model.MovimentacaoEstoque;
import com.jtarcio.closetrental.modules.estoque.domain.model.TipoMovimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port de repositorio do modulo de estoque.
 */
public interface EstoqueRepositoryPort {

    MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao);

    /**
     * Historico paginado de movimentacoes de um produto.
     * Indice em produto_id garante O(log n).
     */
    Page<MovimentacaoEstoque> listarPorProduto(Long produtoId, Pageable pageable);

    /**
     * Historico paginado por tipo de movimentacao.
     */
    Page<MovimentacaoEstoque> listarPorTipo(TipoMovimentacao tipo, Pageable pageable);

    /**
     * Historico completo paginado — para o painel administrativo.
     */
    Page<MovimentacaoEstoque> listarTodos(Pageable pageable);

    /**
     * Conta quantas vezes uma peca foi alugada (tipo SAIDA).
     * Usado no relatorio de pecas mais alugadas — O(log n) com indice.
     */
    long contarSaidasPorProduto(Long produtoId);
}
