package com.jtarcio.closetrental.modules.pagamentos.api.dto;

import com.jtarcio.closetrental.modules.pagamentos.domain.model.MetodoPagamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO de entrada para processar um pagamento.
 */
public record PagamentoRequest(

        @NotNull(message = "locacaoId e obrigatorio")
        Long locacaoId,

        @NotNull(message = "valor e obrigatorio")
        @Positive(message = "valor deve ser positivo")
        BigDecimal valor,

        @NotNull(message = "metodo e obrigatorio")
        MetodoPagamento metodo
) {}
