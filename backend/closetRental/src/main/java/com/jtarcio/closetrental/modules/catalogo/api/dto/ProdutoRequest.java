package com.jtarcio.closetrental.modules.catalogo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Categoria é obrigatória")
        String categoria,

        @NotNull(message = "Valor da diária é obrigatório")
        @DecimalMin(value = "0.0", message = "Valor da diária não pode ser negativo")
        BigDecimal valorDiaria,

        @NotNull(message = "Disponibilidade é obrigatória")
        Boolean disponivel
) {}
