package com.jtarcio.closetrental.modules.estoque.domain.model;

/**
 * Tipos de movimentacao de estoque.
 *
 * SAIDA:       peca retirada pelo cliente (locacao iniciada)
 * ENTRADA:     peca devolvida pelo cliente (locacao encerrada)
 * MANUTENCAO:  peca enviada para manutencao/limpeza (manual pelo ADMIN/ATENDENTE)
 * RETORNO_MANUTENCAO: peca retorna da manutencao e volta ao estoque disponivel
 */
public enum TipoMovimentacao {
    ENTRADA,
    SAIDA,
    MANUTENCAO,
    RETORNO_MANUTENCAO
}
