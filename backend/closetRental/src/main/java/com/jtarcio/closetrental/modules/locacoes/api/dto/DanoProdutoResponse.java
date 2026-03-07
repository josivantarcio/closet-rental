package com.jtarcio.closetrental.modules.locacoes.api.dto;

import com.jtarcio.closetrental.modules.locacoes.domain.model.DanoProduto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DanoProdutoResponse(
        Long id,
        Long itemLocacaoId,
        Long produtoId,
        String descricao,
        BigDecimal valorDesconto,
        LocalDateTime registradoAt
) {
    public static DanoProdutoResponse fromDomain(DanoProduto d) {
        return new DanoProdutoResponse(
                d.getId(),
                d.getItemLocacaoId(),
                d.getProdutoId(),
                d.getDescricao(),
                d.getValorDesconto(),
                d.getRegistradoAt()
        );
    }
}
