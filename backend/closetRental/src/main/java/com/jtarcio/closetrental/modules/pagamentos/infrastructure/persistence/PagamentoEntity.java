package com.jtarcio.closetrental.modules.pagamentos.infrastructure.persistence;

import com.jtarcio.closetrental.modules.pagamentos.domain.model.MetodoPagamento;
import com.jtarcio.closetrental.modules.pagamentos.domain.model.StatusPagamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA da tabela {@code pagamentos}.
 *
 * Índices para consultas eficientes:
 * - idx_pagamentos_locacao:    pagamentos por locação — O(log n)
 * - idx_pagamentos_status:     filtragem por status — O(log n)
 * - idx_pagamentos_metodo:     filtragem por método — O(log n)
 * - idx_pagamentos_criado_em:  ordenação cronológica — O(log n)
 */
@Entity
@Table(
    name = "pagamentos",
    indexes = {
        @Index(name = "idx_pagamentos_locacao",   columnList = "locacao_id"),
        @Index(name = "idx_pagamentos_status",    columnList = "status"),
        @Index(name = "idx_pagamentos_metodo",    columnList = "metodo"),
        @Index(name = "idx_pagamentos_criado_em", columnList = "criado_em")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "locacao_id", nullable = false)
    private Long locacaoId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetodoPagamento metodo;

    @Column(length = 500)
    private String motivo;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "processado_em")
    private LocalDateTime processadoEm;

    @Column(name = "created_by", length = 150)
    private String createdBy;
}
