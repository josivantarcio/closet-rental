package com.jtarcio.closetrental.modules.pagamentos.application;

import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import com.jtarcio.closetrental.modules.locacoes.domain.ports.LocacaoRepositoryPort;
import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoContextBuilder;
import com.jtarcio.closetrental.modules.notificacoes.application.NotificacaoService;
import com.jtarcio.closetrental.modules.notificacoes.domain.model.TipoNotificacao;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.MetodoPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.Pagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.StatusPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.GatewayPagamentoPort;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.GatewayPagamentoPort.PagamentoResultado;
import com.jtarcio.closetrental.modules.pagamentos.domain.ports.PagamentoRepositoryPort;
import com.jtarcio.closetrental.shared.exception.BusinessException;
import com.jtarcio.closetrental.shared.exception.EntityNotFoundException;
import com.jtarcio.closetrental.shared.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Use Case: Processar um pagamento para uma locação.
 *
 * Regras:
 * 1. Locação deve existir
 * 2. Locação deve estar em RETIRADO ou DEVOLVIDO
 * 3. Valor deve ser positivo
 * 4. Pagamento é criado como PENDENTE, enviado ao gateway e atualizado para APROVADO ou FALHA
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessarPagamentoService {

    private final LocacaoRepositoryPort locacaoPort;
    private final PagamentoRepositoryPort pagamentoPort;
    private final GatewayPagamentoPort gatewayPort;
    private final NotificacaoService notificacaoService;
    private final NotificacaoContextBuilder contextBuilder;

    @Transactional
    public Pagamento executar(Long locacaoId, BigDecimal valor, MetodoPagamento metodo, String operador) {
        Locacao locacao = locacaoPort.buscarPorId(locacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Locacao nao encontrada com id: " + locacaoId));

        if (!StatusLocacao.RETIRADO.equals(locacao.getStatus()) &&
            !StatusLocacao.DEVOLVIDO.equals(locacao.getStatus())) {
            throw new BusinessException(
                "Pagamento nao permitido para locacao com status: " + locacao.getStatus() +
                ". A locacao deve estar em RETIRADO ou DEVOLVIDO.");
        }

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do pagamento deve ser positivo.");
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setTenantId(TenantContext.getTenantId());
        pagamento.setLocacaoId(locacaoId);
        pagamento.setValor(valor);
        pagamento.setMetodo(metodo);
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setCriadoEm(LocalDateTime.now());
        pagamento.setCreatedBy(operador);

        // Persiste antes de chamar o gateway para obter o ID
        pagamento = pagamentoPort.salvar(pagamento);

        PagamentoResultado resultado = gatewayPort.processar(pagamento);

        pagamento.setStatus(resultado.sucesso() ? StatusPagamento.APROVADO : StatusPagamento.FALHA);
        pagamento.setProcessadoEm(resultado.dataProcessamento());
        pagamento.setMotivo(resultado.mensagem());

        pagamento = pagamentoPort.salvar(pagamento);

        log.info("Pagamento processado: id={}, locacao={}, status={}, metodo={}",
                pagamento.getId(), locacaoId, pagamento.getStatus(), metodo);

        // Recibo por e-mail somente quando aprovado
        if (StatusPagamento.APROVADO.equals(pagamento.getStatus())) {
            final Pagamento pago = pagamento;
            contextBuilder.construir(locacao).ifPresent(r -> {
                Map<String, Object> ctx = new HashMap<>(r.contexto());
                ctx.put("valorPago",        pago.getValor().toPlainString());
                ctx.put("metodoPagamento",  pago.getMetodo().name());
                notificacaoService.enviar(TipoNotificacao.RECIBO_PAGAMENTO, r.email(), r.tenantId(), ctx);
            });
        }

        return pagamento;
    }
}
