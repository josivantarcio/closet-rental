package com.jtarcio.closetrental.modules.tenants.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String nome,
        String slug,
        String plano,
        boolean ativo,
        LocalDateTime createdAt
) {}
