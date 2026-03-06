package com.jtarcio.closetrental.modules.auth.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO de saída após autenticação bem-sucedida.
 * accessToken: JWT de curta duração (padrão 15min).
 * refreshToken: token opaco de longa duração (padrão 7 dias) para renovar o access token.
 * tenantId: UUID do tenant ao qual o usuário pertence (null para SUPER_ADMIN).
 */
public record LoginResponse(
        String token,
        String tipo,
        long expiresIn,
        String username,
        List<String> roles,
        String refreshToken,
        UUID tenantId
) {}
