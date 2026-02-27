package com.jtarcio.closetrental.modules.locacoes.api.dto;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de saida para respostas de locacao.
 * Inclui campos calculados (diasLocacao, estaAtrasada) para facilitar o frontend.
 */
public record LocacaoResponse(
        Long id,
        Long clienteId,
        Long produtoId,
        LocalDate dataInicio,
        LocalDate dataFim,
        long diasLocacao,
        StatusLocacao status,
        BigDecimal valorDiaria,
        BigDecimal caucao,
        BigDecimal multa,
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

        return new LocacaoResponse(
                l.getId(),
                l.getClienteId(),
                l.getProdutoId(),
                l.getDataInicio(),
                l.getDataFim(),
                l.calcularDias(),
                l.getStatus(),
                l.getValorDiaria(),
                l.getCaucao(),
                l.getMulta(),
                l.getValorTotal(),
                l.getRetiradaAt(),
                l.getDevolucaoAt(),
                atrasada,
                l.getObservacoes(),
                l.getCreatedAt()
        );
    }
}
