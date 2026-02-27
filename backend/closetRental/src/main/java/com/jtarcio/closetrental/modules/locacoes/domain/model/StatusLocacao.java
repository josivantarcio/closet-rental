package com.jtarcio.closetrental.modules.locacoes.domain.model;

/**
 * Estados possíveis de uma locação.
 * Fluxo obrigatório: RESERVADO -> RETIRADO -> DEVOLVIDO
 * Cancelamento só é permitido a partir de RESERVADO.
 */
public enum StatusLocacao {
    RESERVADO,
    RETIRADO,
    DEVOLVIDO,
    CANCELADO
}
