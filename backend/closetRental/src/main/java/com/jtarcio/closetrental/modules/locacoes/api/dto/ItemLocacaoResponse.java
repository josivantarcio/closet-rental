package com.jtarcio.closetrental.modules.locacoes.api.dto;

import java.math.BigDecimal;

/**
 * DTO de saída representando um item de locação (produto + valores).
 */
public record ItemLocacaoResponse(
        Long id,
        Long produtoId,
        String nomeProduto,
        BigDecimal valorDiaria,
        BigDecimal subtotal
) {}
