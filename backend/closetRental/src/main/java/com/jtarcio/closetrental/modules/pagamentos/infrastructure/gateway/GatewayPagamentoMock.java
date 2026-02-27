package com.jtarcio.closetrental.modules.pagamentos.infrastructure.gateway;

import com.jtarcio.closetrental.modules.pagamentos.domain.model.MetodoPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.GatewayPagamentoPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Implementação mock do gateway de pagamento.
 *
 * Comportamento:
 * - PIX e DINHEIRO: sempre APROVADO
 * - CARTAO_CREDITO e CARTAO_DEBITO: {@code taxaFalha}% de chance de FALHA (padrão 10%)
 *
 * Sem Thread.sleep — latência zero para manter os testes rápidos.
 * A taxa de falha é injetável via propriedade para facilitar testes de configuração.
 */
@Component
public class GatewayPagamentoMock implements GatewayPagamentoPort {

    @Value("${app.pagamento.gateway.taxa-falha:0.10}")
    private double taxaFalha;

    @Override
    public PagamentoResultado processar(Pagamento pagamento) {
        LocalDateTime agora = LocalDateTime.now();

        if (MetodoPagamento.PIX.equals(pagamento.getMetodo()) ||
            MetodoPagamento.DINHEIRO.equals(pagamento.getMetodo())) {
            return new PagamentoResultado(true, "Pagamento aprovado via " + pagamento.getMetodo(), agora);
        }

        boolean falhou = Math.random() < taxaFalha;
        if (falhou) {
            return new PagamentoResultado(false, "Pagamento recusado pela operadora do cartao.", agora);
        }
        return new PagamentoResultado(true, "Pagamento aprovado via " + pagamento.getMetodo(), agora);
    }
}
