package com.jtarcio.closetrental.modules.pagamentos.api.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para estornar um pagamento.
 */
public record EstornoRequest(

        @Size(max = 500, message = "motivo deve ter no maximo 500 caracteres")
        String motivo
) {}
