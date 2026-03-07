package com.jtarcio.closetrental.modules.locacoes.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidade JPA da tabela {@code itens_locacao}.
 *
 * Cada item representa um produto dentro de uma locação multi-produto.
 * Índice em (locacao_id) para joins eficientes — O(log n).
 */
@Entity
@Table(
    name = "itens_locacao",
    indexes = {
        @Index(name = "idx_itens_locacao_locacao_id", columnList = "locacao_id"),
        @Index(name = "idx_itens_locacao_produto_id", columnList = "produto_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemLocacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "locacao_id", nullable = false)
    private Long locacaoId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "nome_produto", length = 255)
    private String nomeProduto;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "valor_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDiaria;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}
