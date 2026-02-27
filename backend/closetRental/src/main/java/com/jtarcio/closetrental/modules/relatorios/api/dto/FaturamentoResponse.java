package com.jtarcio.closetrental.modules.relatorios.api.dto;

import java.math.BigDecimal;

/**
 * Receita total de pagamentos aprovados em um mês/ano.
 * totalPagamentos = quantidade de pagamentos aprovados no período.
 */
public record FaturamentoResponse(
        int mes,
        int ano,
        BigDecimal totalReceita,
        long totalPagamentos
) {}
