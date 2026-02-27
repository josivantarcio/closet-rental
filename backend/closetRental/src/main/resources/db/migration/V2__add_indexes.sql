-- =============================================================================
-- V2__add_indexes.sql
-- Indexes de performance — baseados nos @Index declarados nas entidades JPA
-- Todos os lookups: O(log n) via B-tree
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Indexes: clientes
-- As UNIQUE constraints (uk_clientes_*) criadas em V1 ja implicam indexes B-tree.
-- Os @Index abaixo criam indexes nomeados adicionais para filtros por ativo.
-- idx_clientes_documento e idx_clientes_email cobrem findByDocumento/findByEmail.
-- -----------------------------------------------------------------------------
CREATE INDEX idx_clientes_documento ON clientes (documento);
CREATE INDEX idx_clientes_email     ON clientes (email);
CREATE INDEX idx_clientes_ativo     ON clientes (ativo);

-- -----------------------------------------------------------------------------
-- Indexes: locacoes
-- idx_locacoes_produto_periodo: critico para a query de overlap (O(log n))
-- Ordem: produto_id (filtra primeiro) -> data_inicio, data_fim (range scan)
-- -----------------------------------------------------------------------------
CREATE INDEX idx_locacoes_produto_periodo
    ON locacoes (produto_id, data_inicio, data_fim);

CREATE INDEX idx_locacoes_cliente        ON locacoes (cliente_id);
CREATE INDEX idx_locacoes_status         ON locacoes (status);
CREATE INDEX idx_locacoes_cliente_status ON locacoes (cliente_id, status);

-- -----------------------------------------------------------------------------
-- Indexes: movimentacoes_estoque
-- -----------------------------------------------------------------------------
CREATE INDEX idx_estoque_produto      ON movimentacoes_estoque (produto_id);
CREATE INDEX idx_estoque_tipo         ON movimentacoes_estoque (tipo);
CREATE INDEX idx_estoque_locacao      ON movimentacoes_estoque (locacao_id);
CREATE INDEX idx_estoque_produto_tipo ON movimentacoes_estoque (produto_id, tipo);
CREATE INDEX idx_estoque_criado_em    ON movimentacoes_estoque (criado_em);

-- -----------------------------------------------------------------------------
-- Indexes: pagamentos
-- -----------------------------------------------------------------------------
CREATE INDEX idx_pagamentos_locacao   ON pagamentos (locacao_id);
CREATE INDEX idx_pagamentos_status    ON pagamentos (status);
CREATE INDEX idx_pagamentos_metodo    ON pagamentos (metodo);
CREATE INDEX idx_pagamentos_criado_em ON pagamentos (criado_em);
