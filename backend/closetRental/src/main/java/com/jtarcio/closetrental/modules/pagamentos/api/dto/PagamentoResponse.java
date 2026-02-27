package com.jtarcio.closetrental.modules.pagamentos.api.dto;

import com.jtarcio.closetrental.modules.pagamentos.domain.model.MetodoPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de saída com os dados de um pagamento.
 */
public record PagamentoResponse(
        Long id,
        Long locacaoId,
        BigDecimal valor,
        StatusPagamento status,
        MetodoPagamento metodo,
        String motivo,
        LocalDateTime criadoEm,
        LocalDateTime processadoEm
) {
    public static PagamentoResponse fromDomain(Pagamento p) {
        return new PagamentoResponse(
                p.getId(),
                p.getLocacaoId(),
                p.getValor(),
                p.getStatus(),
                p.getMetodo(),
                p.getMotivo(),
                p.getCriadoEm(),
                p.getProcessadoEm()
        );
    }
}
