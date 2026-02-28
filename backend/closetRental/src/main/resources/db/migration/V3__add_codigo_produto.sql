-- =============================================================================
-- V3__add_codigo_produto.sql
-- Adiciona campo codigo à tabela produtos.
-- Um modelo pode ter múltiplos tamanhos/IDs — o código identifica o modelo.
-- Único quando não nulo (permite null para registros legados).
-- =============================================================================

ALTER TABLE produtos
    ADD COLUMN codigo VARCHAR(50);

CREATE UNIQUE INDEX uq_produtos_codigo
    ON produtos (codigo)
    WHERE codigo IS NOT NULL;
