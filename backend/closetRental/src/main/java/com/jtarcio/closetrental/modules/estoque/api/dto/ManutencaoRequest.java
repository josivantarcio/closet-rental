package com.jtarcio.closetrental.modules.estoque.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para envio manual de peca a manutencao.
 */
public record ManutencaoRequest(

        @NotNull(message = "produtoId e obrigatorio")
        Long produtoId,

        @Size(max = 500, message = "Motivo deve ter no maximo 500 caracteres")
        String motivo
) {}
