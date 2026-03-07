package com.jtarcio.closetrental.modules.tenants.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TenantRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255)
        String nome,

        @NotBlank(message = "Slug é obrigatório")
        @Size(max = 100)
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug deve conter apenas letras minúsculas, números e hífens")
        String slug,

        @NotBlank(message = "Plano é obrigatório")
        @Pattern(regexp = "^(FREE|BASIC|PRO)$", message = "Plano deve ser FREE, BASIC ou PRO")
        String plano,

        /** Percentual de multa para cancelamento com 3–6 dias de antecedência (ex: 30.00). Opcional — default 30%. */
        @DecimalMin(value = "0.00", message = "Percentual não pode ser negativo")
        @DecimalMax(value = "100.00", message = "Percentual não pode exceder 100%")
        BigDecimal pctCancelamento3d,

        /** Percentual de multa para cancelamento com 1–2 dias de antecedência (ex: 70.00). Opcional — default 70%. */
        @DecimalMin(value = "0.00", message = "Percentual não pode ser negativo")
        @DecimalMax(value = "100.00", message = "Percentual não pode exceder 100%")
        BigDecimal pctCancelamento1d,

        /** Percentual de multa para cancelamento no dia (ex: 100.00). Opcional — default 100%. */
        @DecimalMin(value = "0.00", message = "Percentual não pode ser negativo")
        @DecimalMax(value = "100.00", message = "Percentual não pode exceder 100%")
        BigDecimal pctCancelamento0d
) {}
