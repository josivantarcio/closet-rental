package com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA da tabela {@code danos_produto}.
 *
 * Registra danos identificados na devolução de um item de locação.
 * Índice em (locacao_id) para busca eficiente por locação — O(log n).
 */
@Entity
@Table(
    name = "danos_produto",
    indexes = {
        @Index(name = "idx_danos_locacao_id",  columnList = "locacao_id"),
        @Index(name = "idx_danos_produto_id",  columnList = "produto_id"),
        @Index(name = "idx_danos_item_id",     columnList = "item_locacao_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DanoProdutoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_locacao_id", nullable = false)
    private Long itemLocacaoId;

    @Column(name = "locacao_id", nullable = false)
    private Long locacaoId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(name = "valor_desconto", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDesconto;

    @CreatedDate
    @Column(name = "registrado_at", updatable = false)
    private LocalDateTime registradoAt;
}
