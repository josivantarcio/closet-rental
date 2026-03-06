package com.jtarcio.closetrental.modules.estoque.infrastructure.persistence;

import com.jtarcio.closetrental.modules.estoque.domain.model.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA da tabela {@code movimentacoes_estoque}.
 *
 * Indices para consultas eficientes:
 * - idx_estoque_produto: historico por peca — O(log n)
 * - idx_estoque_tipo:    filtragem por tipo — O(log n)
 * - idx_estoque_locacao: rastreabilidade por locacao — O(log n)
 */
@Entity
@Table(
    name = "movimentacoes_estoque",
    indexes = {
        @Index(name = "idx_estoque_produto",        columnList = "produto_id"),
        @Index(name = "idx_estoque_tipo",           columnList = "tipo"),
        @Index(name = "idx_estoque_locacao",        columnList = "locacao_id"),
        @Index(name = "idx_estoque_produto_tipo",   columnList = "produto_id, tipo"),
        @Index(name = "idx_estoque_criado_em",      columnList = "criado_em")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MovimentacaoEstoqueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "locacao_id")
    private Long locacaoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimentacao tipo;

    @Column(length = 500)
    private String motivo;

    @Column(name = "operador_nome", length = 150)
    private String operadorNome;

    @CreatedDate
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;
}
