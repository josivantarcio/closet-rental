package com.jtarcio.closetrental.modules.locacoes.api.dto;

/**
 * DTO de entrada para cancelamento de reserva.
 */
public record CancelamentoRequest(
        String motivo
) {}
