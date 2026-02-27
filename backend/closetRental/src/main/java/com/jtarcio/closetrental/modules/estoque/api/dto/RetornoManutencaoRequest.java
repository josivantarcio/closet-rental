package com.jtarcio.closetrental.modules.estoque.api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para retorno de peca da manutencao.
 */
public record RetornoManutencaoRequest(

        @NotNull(message = "produtoId e obrigatorio")
        Long produtoId,

        String observacoes
) {}
