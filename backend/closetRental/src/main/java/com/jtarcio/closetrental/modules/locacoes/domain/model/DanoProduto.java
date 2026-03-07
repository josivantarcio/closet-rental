package com.jtarcio.closetrental.modules.locacoes.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa um dano registrado em um produto durante a devolução de uma locação.
 * Um item de locação pode ter zero ou um registro de dano.
 * Nenhuma dependência de Spring ou JPA.
 */
public class DanoProduto {

    private Long id;
    private Long itemLocacaoId;
    private Long locacaoId;
    private Long produtoId;
    private UUID tenantId;
    private String descricao;
    private BigDecimal valorDesconto;
    private LocalDateTime registradoAt;

    public DanoProduto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getItemLocacaoId() { return itemLocacaoId; }
    public void setItemLocacaoId(Long itemLocacaoId) { this.itemLocacaoId = itemLocacaoId; }

    public Long getLocacaoId() { return locacaoId; }
    public void setLocacaoId(Long locacaoId) { this.locacaoId = locacaoId; }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }

    public LocalDateTime getRegistradoAt() { return registradoAt; }
    public void setRegistradoAt(LocalDateTime registradoAt) { this.registradoAt = registradoAt; }
}
