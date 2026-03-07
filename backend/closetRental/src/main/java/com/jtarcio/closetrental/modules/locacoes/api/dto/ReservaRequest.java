package com.jtarcio.closetrental.modules.locacoes.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de entrada para criacao de reserva multi-produto.
 * Uma reserva pode conter 1..N produtos (itens).
 */
public record ReservaRequest(

        @NotNull(message = "clienteId e obrigatorio")
        Long clienteId,

        @NotEmpty(message = "A reserva deve ter ao menos um produto")
        @Valid
        List<ItemReservaRequest> itens,

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
