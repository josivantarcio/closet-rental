package com.jtarcio.closetrental.modules.relatorios.api.dto;

import java.math.BigDecimal;

/**
 * Ranking de clientes por receita gerada (pagamentos APROVADOS).
 */
public record TopClienteResponse(
        Long clienteId,
        String nomeCliente,
        long totalLocacoes,
        BigDecimal receitaTotal
) {}
