package com.jtarcio.closetrental.modules.estoque.application;

import com.jtarcio.closetrental.modules.catalogo.domain.model.Produto;
import com.jtarcio.closetrental.modules.catalogo.domain.ports.ProdutoRepositoryPort;
import com.jtarcio.closetrental.modules.estoque.domain.model.MovimentacaoEstoque;
import com.jtarcio.closetrental.modules.estoque.domain.model.TipoMovimentacao;
import com.jtarcio.closetrental.modules.estoque.domain.ports.EstoqueRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servico de estoque: registra movimentacoes e controla disponibilidade das pecas.
 *
 * Integracao com locacoes:
 *   - RegistrarRetiradaService chama registrarSaida() ao retirar a peca
 *   - RegistrarDevolucaoService chama registrarEntrada() ao devolver a peca
 *
 * Movimentacoes manuais (manutencao/retorno) sao feitas pelo ADMIN/ATENDENTE via API.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueRepositoryPort estoquePort;
    private final ProdutoRepositoryPort produtoPort;

    /**
     * Registra saida de peca (chamado automaticamente na retirada da locacao).
     * A peca ja foi marcada como indisponivel pelo RegistrarRetiradaService.
     */
    @Transactional
    public MovimentacaoEstoque registrarSaida(Long produtoId, Long locacaoId, String operador) {
        validarProdutoExiste(produtoId);
        MovimentacaoEstoque mov = new MovimentacaoEstoque(
                produtoId, locacaoId, TipoMovimentacao.SAIDA,
                "Peca retirada para locacao #" + locacaoId, operador);
        log.info("Estoque SAIDA: produto={}, locacao={}", produtoId, locacaoId);
        return estoquePort.salvar(mov);
    }

    /**
     * Registra entrada de peca (chamado automaticamente na devolucao da locacao).
     * A peca ja foi marcada como disponivel pelo RegistrarDevolucaoService.
     */
    @Transactional
    public MovimentacaoEstoque registrarEntrada(Long produtoId, Long locacaoId, String operador) {
        validarProdutoExiste(produtoId);
        MovimentacaoEstoque mov = new MovimentacaoEstoque(
                produtoId, locacaoId, TipoMovimentacao.ENTRADA,
                "Peca devolvida da locacao #" + locacaoId, operador);
        log.info("Estoque ENTRADA: produto={}, locacao={}", produtoId, locacaoId);
        return estoquePort.salvar(mov);
    }

    /**
     * Envia peca para manutencao ou limpeza (acao manual).
     * Marca a peca como indisponivel no catalogo.
     */
    @Transactional
    public MovimentacaoEstoque enviarParaManutencao(Long produtoId, String motivo, String operador) {
        Produto produto = buscarProduto(produtoId);
        if (!produto.isDisponivel()) {
            throw new BusinessException("Peca ja esta indisponivel. Verifique se ha locacao ativa.");
        }
        produto.marcarIndisponivel();
        produtoPort.salvar(produto);

        MovimentacaoEstoque mov = new MovimentacaoEstoque(
                produtoId, null, TipoMovimentacao.MANUTENCAO,
                motivo != null ? motivo : "Manutencao/limpeza", operador);
        log.info("Estoque MANUTENCAO: produto={}, motivo={}", produtoId, motivo);
        return estoquePort.salvar(mov);
    }

    /**
     * Registra o retorno da peca da manutencao.
     * Marca a peca como disponivel novamente.
     */
    @Transactional
    public MovimentacaoEstoque retornarDaManutencao(Long produtoId, String observacoes, String operador) {
        Produto produto = buscarProduto(produtoId);
        if (produto.isDisponivel()) {
            throw new BusinessException("Peca ja esta disponivel. Nao ha manutencao pendente registrada.");
        }
        produto.marcarDisponivel();
        produtoPort.salvar(produto);

        MovimentacaoEstoque mov = new MovimentacaoEstoque(
                produtoId, null, TipoMovimentacao.RETORNO_MANUTENCAO,
                observacoes != null ? observacoes : "Retorno da manutencao", operador);
        log.info("Estoque RETORNO_MANUTENCAO: produto={}", produtoId);
        return estoquePort.salvar(mov);
    }

    // ─────────────────────────── Consultas ───────────────────────────

    @Transactional(readOnly = true)
    public Page<MovimentacaoEstoque> listarHistoricoPorProduto(Long produtoId, Pageable pageable) {
        validarProdutoExiste(produtoId);
        return estoquePort.listarPorProduto(produtoId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoEstoque> listarPorTipo(TipoMovimentacao tipo, Pageable pageable) {
        return estoquePort.listarPorTipo(tipo, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoEstoque> listarTodos(Pageable pageable) {
        return estoquePort.listarTodos(pageable);
    }

    @Transactional(readOnly = true)
    public DisponibilidadeResponse verificarDisponibilidade(Long produtoId) {
        Produto produto = buscarProduto(produtoId);
        long totalLocacoes = estoquePort.contarSaidasPorProduto(produtoId);
        return new DisponibilidadeResponse(
                produto.getId(),
                produto.getNome(),
                produto.getCategoria(),
                produto.isDisponivel(),
                totalLocacoes
        );
    }

    // ─────────────────────────── Helpers ───────────────────────────

    private void validarProdutoExiste(Long produtoId) {
        if (produtoPort.buscarPorId(produtoId).isEmpty()) {
            throw new EntityNotFoundException("Produto nao encontrado com id: " + produtoId);
        }
    }

    private Produto buscarProduto(Long produtoId) {
        return produtoPort.buscarPorId(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto nao encontrado com id: " + produtoId));
    }

    /**
     * DTO interno de disponibilidade — sem dependencia de camada api.
     */
    public record DisponibilidadeResponse(
            Long produtoId,
            String nomeProduto,
            String categoria,
            boolean disponivel,
            long totalLocacoes
    ) {}
}
