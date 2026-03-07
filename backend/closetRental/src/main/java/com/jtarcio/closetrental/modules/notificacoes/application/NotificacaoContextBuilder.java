package com.jtarcio.closetrental.modules.notificacoes.application;

import com.jtarcio.closetrental.modules.clientes.domain.model.Cliente;
import com.jtarcio.closetrental.modules.clientes.domain.ports.ClienteRepositoryPort;
import com.jtarcio.closetrental.modules.locacoes.domain.model.Locacao;
import com.jtarcio.closetrental.modules.tenants.domain.model.Tenant;
import com.jtarcio.closetrental.modules.tenants.domain.ports.TenantRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Monta o contexto (Map) necessário para os templates de notificação
 * a partir dos dados de uma locação.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacaoContextBuilder {

    private final ClienteRepositoryPort clientePort;
    private final TenantRepositoryPort  tenantPort;

    /**
     * Retorna o contexto base e o email do cliente.
     * Retorna empty se o cliente não tiver email cadastrado.
     */
    public Optional<NotificacaoContextResult> construir(Locacao locacao) {
        Optional<Cliente> clienteOpt = clientePort.buscarPorId(locacao.getClienteId());
        if (clienteOpt.isEmpty()) {
            log.warn("Cliente {} nao encontrado para montar contexto de notificacao", locacao.getClienteId());
            return Optional.empty();
        }

        Cliente cliente = clienteOpt.get();
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            log.debug("Cliente {} sem email — notificacao ignorada", cliente.getId());
            return Optional.empty();
        }

        String tenantNome = locacao.getTenantId() != null
                ? tenantPort.buscarPorId(locacao.getTenantId())
                            .map(Tenant::getNome)
                            .orElse("Atelier")
                : "Atelier";

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("clienteNome",  cliente.getNome());
        ctx.put("tenantNome",   tenantNome);
        ctx.put("locacaoId",    String.valueOf(locacao.getId()));
        ctx.put("dataInicio",   locacao.getDataInicio() != null ? locacao.getDataInicio().toString() : "");
        ctx.put("dataFim",      locacao.getDataFim()    != null ? locacao.getDataFim().toString()    : "");
        ctx.put("valorTotal",   locacao.getValorTotal() != null ? locacao.getValorTotal().toPlainString() : "0");
        ctx.put("multa",        locacao.getMulta()      != null ? locacao.getMulta().toPlainString()      : "0");
        ctx.put("statusCaucao", locacao.getStatusCaucao() != null ? locacao.getStatusCaucao().name()      : "");
        ctx.put("multaCancelamento",
                locacao.getMultaCancelamento() != null ? locacao.getMultaCancelamento().toPlainString() : "0");

        return Optional.of(new NotificacaoContextResult(cliente.getEmail(), locacao.getTenantId(), ctx));
    }

    public record NotificacaoContextResult(
            String email,
            java.util.UUID tenantId,
            Map<String, Object> contexto
    ) {}
}
