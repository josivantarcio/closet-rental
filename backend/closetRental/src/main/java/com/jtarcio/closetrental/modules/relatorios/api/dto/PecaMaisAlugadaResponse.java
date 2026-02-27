package com.jtarcio.closetrental.modules.relatorios.api.dto;

/**
 * Ranking de peças por número de locações (descrescente).
 */
public record PecaMaisAlugadaResponse(
        Long produtoId,
        String nome,
        String categoria,
        long totalLocacoes
) {}
