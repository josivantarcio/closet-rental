package com.jtarcio.closetrental.modules.locacoes.domain.model;

/**
 * Status do ciclo de vida da caução de uma locação.
 *
 * PENDENTE      — caução cobrada, aguardando devolução
 * LIBERADA      — devolução sem pendências; caução devolvida integralmente ao cliente
 * RETIDA_PARCIAL — parte da caução retida para cobrir multa ou danos; restante devolvido
 * RETIDA_TOTAL  — caução retida integralmente (multa/danos >= valor da caução)
 */
public enum StatusCaucao {
    PENDENTE,
    LIBERADA,
    RETIDA_PARCIAL,
    RETIDA_TOTAL
}
