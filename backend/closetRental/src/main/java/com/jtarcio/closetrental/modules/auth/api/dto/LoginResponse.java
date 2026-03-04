package com.jtarcio.closetrental.modules.auth.api.dto;

import java.util.List;

/**
 * DTO de saída após autenticação bem-sucedida.
 * accessToken: JWT de curta duração (padrão 15min).
 * refreshToken: token opaco de longa duração (padrão 7 dias) para renovar o access token.
 */
public record LoginResponse(
        String token,
        String tipo,
        long expiresIn,
        String username,
        List<String> roles,
        String refreshToken
) {}
