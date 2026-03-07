package com.jtarcio.closetrental.modules.tenants.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Tenant {

    private UUID id;
    private String nome;
    private String slug;
    private String plano;
    private boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Política de cancelamento — percentual sobre o valorTotal (sem caução)
    // Faixas: >= 7 dias → 0% | 3–6 dias → pct3d | 1–2 dias → pct1d | 0 dias → pct0d
    private BigDecimal pctCancelamento3d = new BigDecimal("30.00");
    private BigDecimal pctCancelamento1d = new BigDecimal("70.00");
    private BigDecimal pctCancelamento0d = new BigDecimal("100.00");

    public Tenant() {}

    public Tenant(UUID id, String nome, String slug, String plano, boolean ativo,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nome = nome;
        this.slug = slug;
        this.plano = plano;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void ativar()    { this.ativo = true; }
    public void desativar() { this.ativo = false; }

    /**
     * Calcula a multa de cancelamento com base na política do tenant.
     *
     * @param dataInicio    data de início da locação
     * @param valorBase     base de cálculo (valorTotal sem caução = soma dos subtotais dos itens)
     * @param hoje          data atual (injetada para facilitar testes)
     * @return multa em valor monetário (pode ser ZERO)
     *
     * Complexidade: O(1)
     */
    public BigDecimal calcularMultaCancelamento(LocalDate dataInicio, BigDecimal valorBase, LocalDate hoje) {
        long diasAteInicio = ChronoUnit.DAYS.between(hoje, dataInicio);

        BigDecimal percentual;
        if (diasAteInicio >= 7) {
            percentual = BigDecimal.ZERO;
        } else if (diasAteInicio >= 3) {
            percentual = pctCancelamento3d;
        } else if (diasAteInicio >= 1) {
            percentual = pctCancelamento1d;
        } else {
            // diasAteInicio == 0 (mesmo dia) ou negativo (já iniciou — não deveria cancelar, mas protege)
            percentual = pctCancelamento0d;
        }

        if (percentual.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return valorBase
                .multiply(percentual)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public UUID getId()                  { return id; }
    public void setId(UUID id)           { this.id = id; }

    public String getNome()              { return nome; }
    public void setNome(String nome)     { this.nome = nome; }

    public String getSlug()              { return slug; }
    public void setSlug(String slug)     { this.slug = slug; }

    public String getPlano()             { return plano; }
    public void setPlano(String plano)   { this.plano = plano; }

    public boolean isAtivo()             { return ativo; }
    public void setAtivo(boolean ativo)  { this.ativo = ativo; }

    public LocalDateTime getCreatedAt()                   { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)     { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt()                   { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)     { this.updatedAt = updatedAt; }

    public BigDecimal getPctCancelamento3d()                              { return pctCancelamento3d; }
    public void setPctCancelamento3d(BigDecimal pctCancelamento3d)       { this.pctCancelamento3d = pctCancelamento3d; }

    public BigDecimal getPctCancelamento1d()                              { return pctCancelamento1d; }
    public void setPctCancelamento1d(BigDecimal pctCancelamento1d)       { this.pctCancelamento1d = pctCancelamento1d; }

    public BigDecimal getPctCancelamento0d()                              { return pctCancelamento0d; }
    public void setPctCancelamento0d(BigDecimal pctCancelamento0d)       { this.pctCancelamento0d = pctCancelamento0d; }
}
