package com.jtarcio.closetrental.modules.tenants.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Tenant {

    private UUID id;
    private String nome;
    private String slug;
    private String plano;
    private boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public void ativar()   { this.ativo = true; }
    public void desativar() { this.ativo = false; }

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
}
