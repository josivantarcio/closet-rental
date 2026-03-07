package com.jtarcio.closetrental.modules.relatorios.api.dto;

import java.math.BigDecimal;

/**
 * Ticket médio por período (mes/ano ou intervalo).
 */
public record TicketMedioResponse(
        int mes,
        int ano,
        long totalLocacoes,
        BigDecimal receitaTotal,
        BigDecimal ticketMedio
) {}
