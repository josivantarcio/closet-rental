package com.jtarcio.closetrental.modules.pagamentos.domain.model;

/**
 * Estados possíveis de um pagamento.
 * Fluxo: PENDENTE -> APROVADO ou FALHA
 * Estorno: APROVADO -> ESTORNADO
 */
public enum StatusPagamento {
    PENDENTE,
    APROVADO,
    FALHA,
    ESTORNADO
}
