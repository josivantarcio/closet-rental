package com.jtarcio.closetrental.modules.locacoes.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada para criacao de reserva.
 */
public record ReservaRequest(

        @NotNull(message = "clienteId e obrigatorio")
        Long clienteId,

        @NotNull(message = "produtoId e obrigatorio")
        Long produtoId,

        @NotNull(message = "dataInicio e obrigatoria")
        @Future(message = "dataInicio deve ser uma data futura")
        LocalDate dataInicio,

        @NotNull(message = "dataFim e obrigatoria")
        @Future(message = "dataFim deve ser uma data futura")
        LocalDate dataFim,

        @PositiveOrZero(message = "caucao nao pode ser negativa")
        BigDecimal caucao,

        String observacoes
) {}
