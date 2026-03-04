package com.jtarcio.closetrental.modules.catalogo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Categoria é obrigatória")
        String categoria,

        @NotNull(message = "Valor da diária é obrigatório")
        @DecimalMin(value = "0.0", message = "Valor da diária não pode ser negativo")
        BigDecimal valorDiaria,

        String codigo,

        Boolean disponivel,

        @Size(max = 20, message = "Tamanho deve ter no máximo 20 caracteres")
        String tamanho,

        @Size(max = 50, message = "Cor deve ter no máximo 50 caracteres")
        String cor,

        String descricao,

        @Size(max = 500, message = "URL da imagem deve ter no máximo 500 caracteres")
        String imagemUrl
) {}
