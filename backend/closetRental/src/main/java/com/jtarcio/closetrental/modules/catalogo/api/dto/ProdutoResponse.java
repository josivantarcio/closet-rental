package com.jtarcio.closetrental.modules.catalogo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoResponse(
        Long id,
        String nome,
        String categoria,
        BigDecimal valorDiaria,
        String codigo,
        boolean disponivel,
        String tamanho,
        String cor,
        String descricao,
        String imagemUrl,
        String createdBy,
        LocalDateTime createdAt
) {}
