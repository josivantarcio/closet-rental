package com.jtarcio.closetrental.modules.tenants.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
        String plano
) {}
