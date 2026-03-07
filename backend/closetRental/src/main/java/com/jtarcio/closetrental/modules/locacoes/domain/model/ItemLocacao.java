package com.jtarcio.closetrental.modules.locacoes.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Representa um item de uma locação — um produto específico com seu valor de diária.
 * A locação é a agregação de um ou mais itens.
 * Nenhuma dependência de Spring ou JPA.
 */
public class ItemLocacao {

    private Long id;
    private Long locacaoId;
    private Long produtoId;
    private String nomeProduto;
    private UUID tenantId;
    private BigDecimal valorDiaria;
    private BigDecimal subtotal;

    public ItemLocacao() {}

    /**
     * Calcula o subtotal do item: valorDiaria * dias.
     * Dias calculados a partir das datas da locação.
     * Complexidade: O(1).
     */
    public BigDecimal calcularSubtotal(long dias) {
        return valorDiaria
                .multiply(BigDecimal.valueOf(dias))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLocacaoId() { return locacaoId; }
    public void setLocacaoId(Long locacaoId) { this.locacaoId = locacaoId; }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public BigDecimal getValorDiaria() { return valorDiaria; }
    public void setValorDiaria(BigDecimal valorDiaria) { this.valorDiaria = valorDiaria; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
