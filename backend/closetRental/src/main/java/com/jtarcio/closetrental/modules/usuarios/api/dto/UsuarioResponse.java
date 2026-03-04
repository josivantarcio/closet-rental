package com.jtarcio.closetrental.modules.usuarios.api.dto;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String username,
        String email,
        String role,
        boolean ativo,
        LocalDateTime createdAt
) {}
