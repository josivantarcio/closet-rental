package com.jtarcio.closetrental.modules.locacoes.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de entrada para registro de devolucao.
 *
 * danos: lista opcional de danos por item. Quando informada, o sistema soma os
 *   valorDesconto de cada dano e usa essa soma como valorRetencaoCaucao automaticamente,
 *   a menos que valorRetencaoCaucao seja informado explicitamente (prevalece sobre os danos).
 *
 * valorRetencaoCaucao: override manual da retencao.
 *   - null → usa soma dos danos (ou 0 se sem danos)
 *   - 0    → libera caucao independentemente dos danos
 *   - > 0  → usa o valor informado
 */
public record DevolucaoRequest(
        String observacoes,

        @Valid
        List<DanoProdutoRequest> danos,

        @DecimalMin(value = "0.00", message = "Retencao de caucao nao pode ser negativa")
        BigDecimal valorRetencaoCaucao
) {}
