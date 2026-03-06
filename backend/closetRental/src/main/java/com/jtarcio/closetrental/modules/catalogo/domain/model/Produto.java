package com.jtarcio.closetrental.modules.catalogo.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Produto {

    private Long id;
    private String nome;
    private String categoria;
    private BigDecimal valorDiaria;
    private String codigo;
    private boolean disponivel;
    private String tamanho;
    private String cor;
    private String descricao;
    private String imagemUrl;
    private UUID tenantId;
    private String createdBy;
    private LocalDateTime createdAt;

    public Produto() {}

    public Produto(Long id, String nome, String categoria, BigDecimal valorDiaria,
                   String codigo, boolean disponivel, String tamanho, String cor,
                   String descricao, String imagemUrl, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.valorDiaria = valorDiaria;
        this.codigo = codigo;
        this.disponivel = disponivel;
        this.tamanho = tamanho;
        this.cor = cor;
        this.descricao = descricao;
        this.imagemUrl = imagemUrl;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public void marcarIndisponivel() {
        this.disponivel = false;
    }

    public void marcarDisponivel() {
        this.disponivel = true;
    }

    public BigDecimal calcularValor(long dias, BigDecimal caucao) {
        return valorDiaria.multiply(BigDecimal.valueOf(dias)).add(caucao);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getValorDiaria() { return valorDiaria; }
    public void setValorDiaria(BigDecimal valorDiaria) { this.valorDiaria = valorDiaria; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    public String getTamanho() { return tamanho; }
    public void setTamanho(String tamanho) { this.tamanho = tamanho; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
