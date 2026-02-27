package com.jtarcio.closetrental.modules.pagamentos.domain.ports;

import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;

import java.time.LocalDateTime;

/**
 * Port do gateway de pagamento.
 * Abstrai a chamada ao processador externo (ou mock).
 */
public interface GatewayPagamentoPort {

    /**
     * Processa o pagamento e retorna o resultado do gateway.
     *
     * @param pagamento pagamento a ser processado
     * @return resultado com sucesso/falha, mensagem e data/hora do processamento
     */
    PagamentoResultado processar(Pagamento pagamento);

    /**
     * Resultado imutável retornado pelo gateway de pagamento.
     *
     * @param sucesso          true se o pagamento foi aprovado
     * @param mensagem         mensagem descritiva do resultado
     * @param dataProcessamento data/hora em que o gateway processou
     */
    record PagamentoResultado(
            boolean sucesso,
            String mensagem,
            LocalDateTime dataProcessamento
    ) {}
}
