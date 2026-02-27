package com.jtarcio.closetrental.modules.auth.api.dto;

import java.util.List;

/**
 * DTO de saída após autenticação bem-sucedida.
 */
public record LoginResponse(
        String token,
        String tipo,
        long expiresIn,
        String username,
        List<String> roles
) {}
