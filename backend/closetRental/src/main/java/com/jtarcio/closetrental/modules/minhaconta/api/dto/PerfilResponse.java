package com.jtarcio.closetrental.modules.minhaconta.api.dto;

import com.jtarcio.closetrental.modules.usuarios.domain.model.Usuario;

import java.time.LocalDateTime;

public record PerfilResponse(
        Long id,
        String nome,
        String username,
        String email,
        String role,
        Long clienteId,
        LocalDateTime createdAt
) {
    public static PerfilResponse fromDomain(Usuario u) {
        return new PerfilResponse(u.getId(), u.getNome(), u.getUsername(),
                u.getEmail(), u.getRole(), u.getClienteId(), u.getCreatedAt());
    }
}
