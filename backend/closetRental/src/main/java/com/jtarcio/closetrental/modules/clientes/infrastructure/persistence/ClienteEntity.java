package com.jtarcio.closetrental.modules.clientes.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidade JPA mapeada para a tabela {@code clientes}.
 * Índices únicos em documento e email garantem O(1) na verificação de unicidade.
 */
@Entity
@Table(
    name = "clientes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_clientes_documento", columnNames = "documento"),
        @UniqueConstraint(name = "uk_clientes_email",    columnNames = "email")
    },
    indexes = {
        @Index(name = "idx_clientes_documento", columnList = "documento"),
        @Index(name = "idx_clientes_email",     columnList = "email"),
        @Index(name = "idx_clientes_ativo",     columnList = "ativo")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(nullable = false, length = 20)
    private String documento;

    @Column(length = 300)
    private String endereco;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
