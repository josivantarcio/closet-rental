package com.jtarcio.closetrental.modules.pagamentos.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Aggregate Root do domínio de pagamentos.
 * Representa um pagamento associado a uma locação.
 * Sem dependência de Spring ou JPA.
 */
public class Pagamento {

    private Long id;
    private Long locacaoId;
    private BigDecimal valor;
    private StatusPagamento status;
    private MetodoPagamento metodo;
    private String motivo;
    private LocalDateTime criadoEm;
    private LocalDateTime processadoEm;
    private String createdBy;

    public Pagamento() {}

    // ─────────────────────────────────────────────────────────────────
    // Regras de negócio de domínio
    // ─────────────────────────────────────────────────────────────────

    /**
     * Verifica se o pagamento pode ser estornado.
     * Apenas pagamentos APROVADOS podem ser estornados.
     * Complexidade: O(1).
     */
    public boolean podeEstornar() {
        return StatusPagamento.APROVADO.equals(this.status);
    }

    /**
     * Verifica se o pagamento já foi processado pelo gateway.
     * Complexidade: O(1).
     */
    public boolean foiProcessado() {
        return processadoEm != null;
    }

    // ─────────────────────────────────────────────────────────────────
    // Getters e Setters
    // ─────────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLocacaoId() { return locacaoId; }
    public void setLocacaoId(Long locacaoId) { this.locacaoId = locacaoId; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public StatusPagamento getStatus() { return status; }
    public void setStatus(StatusPagamento status) { this.status = status; }

    public MetodoPagamento getMetodo() { return metodo; }
    public void setMetodo(MetodoPagamento metodo) { this.metodo = metodo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public LocalDateTime getProcessadoEm() { return processadoEm; }
    public void setProcessadoEm(LocalDateTime processadoEm) { this.processadoEm = processadoEm; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
