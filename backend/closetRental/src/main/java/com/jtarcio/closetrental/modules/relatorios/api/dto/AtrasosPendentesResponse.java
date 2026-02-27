package com.jtarcio.closetrental.modules.relatorios.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Locação em atraso: status RETIRADO com dataFim anterior a hoje.
 * diasAtraso calculado na construção para evitar lógica no controller.
 */
public record AtrasosPendentesResponse(
        Long locacaoId,
        Long clienteId,
        Long produtoId,
        LocalDate dataFim,
        long diasAtraso,
        BigDecimal valorDiaria,
        BigDecimal multaEstimada
) {
    public static AtrasosPendentesResponse of(
            Long locacaoId, Long clienteId, Long produtoId,
            LocalDate dataFim, LocalDate hoje,
            BigDecimal valorDiaria, BigDecimal percentualMulta) {
        long dias = ChronoUnit.DAYS.between(dataFim, hoje);
        BigDecimal multa = valorDiaria.multiply(BigDecimal.valueOf(dias)).multiply(percentualMulta);
        return new AtrasosPendentesResponse(locacaoId, clienteId, produtoId, dataFim, dias, valorDiaria, multa);
    }
}
