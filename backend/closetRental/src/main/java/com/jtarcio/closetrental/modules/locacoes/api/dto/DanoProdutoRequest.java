package com.jtarcio.closetrental.modules.locacoes.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Dano em um produto específico, informado durante a devolução.
 */
public record DanoProdutoRequest(

        @NotNull(message = "itemLocacaoId e obrigatorio")
        Long itemLocacaoId,

        @NotBlank(message = "Descricao do dano e obrigatoria")
        @Size(max = 500)
        String descricao,

        @NotNull(message = "Valor do desconto e obrigatorio")
        @DecimalMin(value = "0.01", message = "Valor do desconto deve ser maior que zero")
        BigDecimal valorDesconto
) {}
