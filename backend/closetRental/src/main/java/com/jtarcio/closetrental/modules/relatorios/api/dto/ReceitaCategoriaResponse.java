package com.jtarcio.closetrental.modules.relatorios.api.dto;

import java.math.BigDecimal;

/**
 * Receita agrupada por categoria de produto.
 */
public record ReceitaCategoriaResponse(
        String categoria,
        long totalLocacoes,
        BigDecimal receitaTotal
) {}
