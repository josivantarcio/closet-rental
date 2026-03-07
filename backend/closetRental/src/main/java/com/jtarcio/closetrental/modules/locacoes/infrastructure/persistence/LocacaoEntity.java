package com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence;

import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusCaucao;
import com.jtarcio.closetrental.modules.locacoes.domain.model.StatusLocacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade JPA da tabela {@code locacoes}.
 *
 * A partir da Fase 3.5 (multi-produto), produto_id e valor_diaria foram movidos
 * para a tabela {@code itens_locacao}. Cada locação possui 1..N itens.
 *
 * Índices compostos são a chave de performance neste módulo:
 * - idx_locacoes_produto_periodo: usado pela query de overlap via itens — O(log n)
 * - idx_locacoes_cliente:         listagem por cliente — O(log n)
 * - idx_locacoes_status:          filtragem por status — O(log n)
 */
@Entity
@Table(
    name = "locacoes",
    indexes = {
        @Index(name = "idx_locacoes_cliente",
               columnList = "cliente_id"),
        @Index(name = "idx_locacoes_status",
               columnList = "status"),
        @Index(name = "idx_locacoes_cliente_status",
               columnList = "cliente_id, status"),
        @Index(name = "idx_locacoes_tenant",
               columnList = "tenant_id"),
        @Index(name = "idx_locacoes_periodo",
               columnList = "data_inicio, data_fim")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LocacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLocacao status;

    @Column(precision = 10, scale = 2)
    private BigDecimal caucao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_caucao", nullable = false, length = 20)
    private StatusCaucao statusCaucao = StatusCaucao.PENDENTE;

    @Column(name = "caucao_retida", precision = 10, scale = 2)
    private BigDecimal caucaoRetida;

    @Column(precision = 10, scale = 2)
    private BigDecimal multa;

    @Column(name = "multa_cancelamento", precision = 10, scale = 2)
    private BigDecimal multaCancelamento;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "retirada_at")
    private LocalDateTime retiradaAt;

    @Column(name = "devolucao_at")
    private LocalDateTime devolucaoAt;

    @Column(length = 500)
    private String observacoes;

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "locacao_id", referencedColumnName = "id")
    @Builder.Default
    private List<ItemLocacaoEntity> itens = new ArrayList<>();
}
