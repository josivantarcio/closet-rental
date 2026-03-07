package com.jtarcio.closetrental.modules.tenants.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants",
    indexes = {
        @Index(name = "idx_tenants_slug",  columnList = "slug"),
        @Index(name = "idx_tenants_ativo", columnList = "ativo")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(nullable = false, length = 100, unique = true)
    private String slug;

    @Column(nullable = false, length = 20)
    private String plano;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "pct_cancelamento_3d", nullable = false, precision = 5, scale = 2)
    private BigDecimal pctCancelamento3d = new BigDecimal("30.00");

    @Column(name = "pct_cancelamento_1d", nullable = false, precision = 5, scale = 2)
    private BigDecimal pctCancelamento1d = new BigDecimal("70.00");

    @Column(name = "pct_cancelamento_0d", nullable = false, precision = 5, scale = 2)
    private BigDecimal pctCancelamento0d = new BigDecimal("100.00");

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
