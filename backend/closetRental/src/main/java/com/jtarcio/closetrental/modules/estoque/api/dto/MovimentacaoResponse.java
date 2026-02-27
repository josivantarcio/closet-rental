package com.jtarcio.closetrental.modules.estoque.api.dto;

import com.jtarcio.closetrental.modules.estoque.domain.model.MovimentacaoEstoque;
import com.jtarcio.closetrental.modules.estoque.domain.model.TipoMovimentacao;

import java.time.LocalDateTime;

/**
 * DTO de saida para movimentacoes de estoque.
 */
public record MovimentacaoResponse(
        Long id,
        Long produtoId,
        Long locacaoId,
        TipoMovimentacao tipo,
        String motivo,
        String operadorNome,
        LocalDateTime criadoEm
) {
    public static MovimentacaoResponse fromDomain(MovimentacaoEstoque m) {
        return new MovimentacaoResponse(
                m.getId(),
                m.getProdutoId(),
                m.getLocacaoId(),
                m.getTipo(),
                m.getMotivo(),
                m.getOperadorNome(),
                m.getCriadoEm()
        );
    }
}
