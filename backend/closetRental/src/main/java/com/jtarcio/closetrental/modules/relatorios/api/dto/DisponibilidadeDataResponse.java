package com.jtarcio.closetrental.modules.relatorios.api.dto;

import java.math.BigDecimal;

/**
 * Produto disponível (não alugado) em uma data específica.
 * Projeção direta da query — sem carregar entidades completas.
 */
public record DisponibilidadeDataResponse(
        Long produtoId,
        String nome,
        String categoria,
        BigDecimal valorDiaria
) {}
