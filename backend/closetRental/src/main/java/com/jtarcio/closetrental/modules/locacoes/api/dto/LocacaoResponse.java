package com.jtarcio.closetrental.modules.locacoes.api.dto;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusCaucao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de saida para respostas de locacao.
 * Inclui a lista de itens (produtos) e campos calculados para facilitar o frontend.
 */
public record LocacaoResponse(
        Long id,
        Long clienteId,
        List<ItemLocacaoResponse> itens,
        LocalDate dataInicio,
        LocalDate dataFim,
        long diasLocacao,
        StatusLocacao status,
        BigDecimal caucao,
        StatusCaucao statusCaucao,
        BigDecimal caucaoRetida,
        BigDecimal multa,
        BigDecimal multaCancelamento,
        BigDecimal valorTotal,
        LocalDateTime retiradaAt,
        LocalDateTime devolucaoAt,
        boolean atrasada,
        String observacoes,
        LocalDateTime createdAt
) {
    public static LocacaoResponse fromDomain(Locacao l) {
        boolean atrasada = l.getStatus() == StatusLocacao.RETIRADO
                && LocalDate.now().isAfter(l.getDataFim());

        List<ItemLocacaoResponse> itensResponse = l.getItens().stream()
                .map(i -> new ItemLocacaoResponse(
                        i.getId(),
                        i.getProdutoId(),
                        i.getNomeProduto(),
                        i.getValorDiaria(),
                        i.getSubtotal()))
                .toList();

        return new LocacaoResponse(
                l.getId(),
                l.getClienteId(),
                itensResponse,
                l.getDataInicio(),
                l.getDataFim(),
                l.calcularDias(),
                l.getStatus(),
                l.getCaucao(),
                l.getStatusCaucao(),
                l.getCaucaoRetida(),
                l.getMulta(),
                l.getMultaCancelamento(),
                l.getValorTotal(),
                l.getRetiradaAt(),
                l.getDevolucaoAt(),
                atrasada,
                l.getObservacoes(),
                l.getCreatedAt()
        );
    }
}
