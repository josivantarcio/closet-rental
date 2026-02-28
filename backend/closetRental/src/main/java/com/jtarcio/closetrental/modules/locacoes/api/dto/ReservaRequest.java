package com.jtarcio.closetrental.modules.locacoes.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
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
        @FutureOrPresent(message = "dataInicio nao pode ser uma data passada")
        LocalDate dataInicio,

        @NotNull(message = "dataFim e obrigatoria")
        @FutureOrPresent(message = "dataFim nao pode ser uma data passada")
        LocalDate dataFim,

        @PositiveOrZero(message = "caucao nao pode ser negativa")
        BigDecimal caucao,

        String observacoes
) {}
