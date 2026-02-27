package com.jtarcio.closetrental.modules.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para autenticação.
 */
public record LoginRequest(

        @NotBlank(message = "username e obrigatorio")
        String username,

        @NotBlank(message = "password e obrigatorio")
        String password
) {}
