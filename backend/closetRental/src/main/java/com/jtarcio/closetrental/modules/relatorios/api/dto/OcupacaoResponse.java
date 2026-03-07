package com.jtarcio.closetrental.modules.relatorios.api.dto;

import java.math.BigDecimal;

/**
 * Taxa de ocupação de um produto no período consultado.
 * taxaOcupacao = diasOcupados / diasPeriodo * 100 (0–100).
 */
public record OcupacaoResponse(
        Long produtoId,
        String nomeProduto,
        String categoria,
        int diasPeriodo,
        long diasOcupados,
        BigDecimal taxaOcupacao
) {}
