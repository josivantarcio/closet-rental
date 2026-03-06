package com.jtarcio.closetrental.modules.estoque.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model de MovimentacaoEstoque — POJO puro sem Spring/JPA.
 * Rastreia cada evento que altera a disponibilidade de uma peca no estoque.
 */
public class MovimentacaoEstoque {

    private Long id;
    private UUID tenantId;
    private Long produtoId;
    private Long locacaoId;     // null para movimentacoes manuais (manutencao)
    private TipoMovimentacao tipo;
    private String motivo;
    private String operadorNome; // quem executou a acao
    private LocalDateTime criadoEm;

    public MovimentacaoEstoque() {}

    public MovimentacaoEstoque(Long produtoId, Long locacaoId,
                                TipoMovimentacao tipo, String motivo, String operadorNome) {
        this.produtoId = produtoId;
        this.locacaoId = locacaoId;
        this.tipo = tipo;
        this.motivo = motivo;
        this.operadorNome = operadorNome;
    }

    /** Retorna true se esta movimentacao representa uma saida de peca do estoque. */
    public boolean eSaida() {
        return TipoMovimentacao.SAIDA.equals(this.tipo) || TipoMovimentacao.MANUTENCAO.equals(this.tipo);
    }

    /** Retorna true se esta movimentacao representa um retorno de peca ao estoque. */
    public boolean eEntrada() {
        return TipoMovimentacao.ENTRADA.equals(this.tipo) || TipoMovimentacao.RETORNO_MANUTENCAO.equals(this.tipo);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public Long getLocacaoId() { return locacaoId; }
    public void setLocacaoId(Long locacaoId) { this.locacaoId = locacaoId; }

    public TipoMovimentacao getTipo() { return tipo; }
    public void setTipo(TipoMovimentacao tipo) { this.tipo = tipo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getOperadorNome() { return operadorNome; }
    public void setOperadorNome(String operadorNome) { this.operadorNome = operadorNome; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
