package com.jtarcio.closetrental.modules.clientes.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain model puro de Cliente — sem dependências Spring/JPA.
 * Representa o cliente da loja que realiza locações.
 */
public class Cliente {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String documento; // CPF ou CNPJ
    private String endereco;
    private boolean ativo;
    private UUID tenantId;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Cliente() {}

    public Cliente(Long id, String nome, String email, String telefone,
                   String documento, String endereco, boolean ativo,
                   String createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.documento = documento;
        this.endereco = endereco;
        this.ativo = ativo;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Inativa o cliente sem excluí-lo (soft delete). */
    public void inativar() {
        this.ativo = false;
    }

    /** Reativa um cliente previamente inativado. */
    public void reativar() {
        this.ativo = true;
    }

    /** Remove pontuação do documento para comparação uniforme. */
    public String documentoSemFormatacao() {
        return documento == null ? null : documento.replaceAll("[^0-9]", "");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
