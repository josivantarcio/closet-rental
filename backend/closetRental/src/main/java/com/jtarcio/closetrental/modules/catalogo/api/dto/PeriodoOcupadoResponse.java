package com.jtarcio.closetrental.modules.catalogo.api.dto;

import java.time.LocalDate;

/**
 * Representa um período em que um produto está ocupado (RESERVADO ou RETIRADO).
 * Usado pelo endpoint GET /v1/catalogo/{id}/agenda.
 */
public record PeriodoOcupadoResponse(
        Long locacaoId,
        LocalDate dataInicio,
        LocalDate dataFim,
        String status
) {}
