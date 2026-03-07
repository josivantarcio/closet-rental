package com.jtarcio.closetrental.modules.locacoes.domain.ports;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de repositório de Locacao.
 * Define o contrato que a infraestrutura implementa;
 * o domínio nunca depende de JPA diretamente.
 */
public interface LocacaoRepositoryPort {

    Locacao salvar(Locacao locacao);

    Optional<Locacao> buscarPorId(Long id);

    Page<Locacao> listarPorCliente(UUID tenantId, Long clienteId, Pageable pageable);

    Page<Locacao> listarPorStatus(UUID tenantId, StatusLocacao status, Pageable pageable);

    Page<Locacao> listarTodos(UUID tenantId, Pageable pageable);

    /**
     * Verifica se existe sobreposição de datas para o produto no período informado,
     * excluindo locações CANCELADAS e DEVOLVIDAS.
     * Usar índice composto (produto_id, data_inicio, data_fim) — O(log n).
     */
    boolean existeOverlapNoPeriodo(Long produtoId, LocalDate dataInicio, LocalDate dataFim, Long excluirLocacaoId);

    /**
     * Retorna true se o produto possui alguma locação RESERVADA ou RETIRADA.
     * Usado para impedir a exclusão do produto.
     */
    boolean existeLocacaoAtivaParaProduto(Long produtoId);

    /**
     * Retorna true se o cliente possui alguma locação RESERVADA ou RETIRADA.
     * Usado para impedir a exclusão do cliente.
     */
    boolean existeLocacaoAtivaParaCliente(Long clienteId);

    /**
     * Retorna as locações ativas (RESERVADO ou RETIRADO) de um produto
     * que se intersectam com o intervalo [inicioMes, fimMes].
     * Usado para montar o calendário de ocupação.
     */
    List<Locacao> buscarPeriodosOcupadosNoMes(Long produtoId, LocalDate inicioMes, LocalDate fimMes);

    /** Locações RESERVADAS com retirada na data informada. */
    List<Locacao> buscarReservadosComRetiradaEm(LocalDate data);

    /** Locações RETIRADAS com devolução na data informada. */
    List<Locacao> buscarRetiradosComDevolucaoEm(LocalDate data);

    /** Locações RETIRADAS com dataFim anterior a hoje (em atraso). */
    List<Locacao> buscarRetiradosEmAtraso(LocalDate hoje);
}
