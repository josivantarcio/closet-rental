package com.jtarcio.closetrental.modules.relatorios.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Resumo de uma locação no histórico de um cliente.
 * Com locação multi-produto, os detalhes dos itens são buscados via GET /v1/locacoes/{id}.
 */
public record HistoricoClienteResponse(
        Long locacaoId,
        String status,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal valorTotal,
        BigDecimal multa,
        LocalDateTime devolucaoAt
) {}
