package com.jtarcio.closetrental.modules.relatorios.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Resumo de uma locação no histórico de um cliente.
 */
public record HistoricoClienteResponse(
        Long locacaoId,
        Long produtoId,
        String status,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal valorTotal,
        BigDecimal multa,
        LocalDateTime devolucaoAt
) {}
