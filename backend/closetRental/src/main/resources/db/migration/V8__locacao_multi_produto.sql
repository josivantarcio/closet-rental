-- =============================================================================
-- V8: Locação multi-produto
-- Cria tabela itens_locacao e migra dados existentes de locacoes.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Criar tabela itens_locacao
-- -----------------------------------------------------------------------------
CREATE TABLE itens_locacao (
    id           BIGSERIAL     PRIMARY KEY,
    locacao_id   BIGINT        NOT NULL REFERENCES locacoes(id) ON DELETE CASCADE,
    produto_id   BIGINT        NOT NULL,
    nome_produto VARCHAR(255),
    tenant_id    UUID          NOT NULL REFERENCES tenants(id),
    valor_diaria NUMERIC(10,2) NOT NULL,
    subtotal     NUMERIC(10,2) NOT NULL
);

CREATE INDEX idx_itens_locacao_locacao_id ON itens_locacao(locacao_id);
CREATE INDEX idx_itens_locacao_produto_id ON itens_locacao(produto_id);

-- -----------------------------------------------------------------------------
-- 2. Migrar dados existentes: cada locação existente vira 1 item
--    subtotal = valor_diaria * (data_fim - data_inicio + 1)
-- -----------------------------------------------------------------------------
INSERT INTO itens_locacao (locacao_id, produto_id, nome_produto, tenant_id, valor_diaria, subtotal)
SELECT
    l.id                                                    AS locacao_id,
    l.produto_id                                            AS produto_id,
    p.nome                                                  AS nome_produto,
    l.tenant_id                                             AS tenant_id,
    l.valor_diaria                                          AS valor_diaria,
    l.valor_diaria * (l.data_fim - l.data_inicio + 1)      AS subtotal
FROM locacoes l
LEFT JOIN produtos p ON p.id = l.produto_id
WHERE l.produto_id IS NOT NULL;

-- -----------------------------------------------------------------------------
-- 3. Remover colunas produto_id e valor_diaria de locacoes
--    (agora vivem em itens_locacao)
-- -----------------------------------------------------------------------------
ALTER TABLE locacoes DROP COLUMN IF EXISTS produto_id;
ALTER TABLE locacoes DROP COLUMN IF EXISTS valor_diaria;

-- -----------------------------------------------------------------------------
-- 4. Remover índice antigo que usava produto_id na tabela locacoes
-- -----------------------------------------------------------------------------
DROP INDEX IF EXISTS idx_locacoes_produto_periodo;
DROP INDEX IF EXISTS idx_locacoes_tenant_produto_periodo;
