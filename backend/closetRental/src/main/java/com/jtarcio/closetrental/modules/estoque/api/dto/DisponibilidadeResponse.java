package com.jtarcio.closetrental.modules.estoque.api.dto;

import com.jtarcio.closetrental.modules.estoque.application.EstoqueService;

/**
 * DTO de saida para verificacao de disponibilidade de uma peca.
 */
public record DisponibilidadeResponse(
        Long produtoId,
        String nomeProduto,
        String categoria,
        boolean disponivel,
        long totalLocacoes
) {
    public static DisponibilidadeResponse fromService(EstoqueService.DisponibilidadeResponse r) {
        return new DisponibilidadeResponse(
                r.produtoId(), r.nomeProduto(), r.categoria(), r.disponivel(), r.totalLocacoes());
    }
}
