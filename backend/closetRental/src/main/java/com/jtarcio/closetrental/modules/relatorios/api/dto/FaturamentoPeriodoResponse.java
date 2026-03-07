package com.jtarcio.closetrental.modules.relatorios.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Faturamento mensal dentro de um intervalo de datas (para gráfico de linha).
 * Cada item representa um mês com sua receita e quantidade de pagamentos.
 */
public record FaturamentoPeriodoResponse(
        List<FaturamentoResponse> meses,
        BigDecimal totalPeriodo,
        long totalPagamentosPeriodo
) {}
