package com.jtarcio.closetrental.modules.locacoes.api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO representando um produto a incluir em uma nova reserva.
 */
public record ItemReservaRequest(

        @NotNull(message = "produtoId e obrigatorio")
        Long produtoId
) {}
