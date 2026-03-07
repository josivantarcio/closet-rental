-- Fase 3.3 — Registro de danos na devolução

CREATE TABLE danos_produto (
    id               BIGSERIAL       PRIMARY KEY,
    item_locacao_id  BIGINT          NOT NULL REFERENCES itens_locacao(id),
    locacao_id       BIGINT          NOT NULL REFERENCES locacoes(id),
    produto_id       BIGINT          NOT NULL,
    tenant_id        UUID            NOT NULL,
    descricao        VARCHAR(500)    NOT NULL,
    valor_desconto   NUMERIC(10, 2)  NOT NULL DEFAULT 0.00,
    registrado_at    TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_danos_locacao_id  ON danos_produto (locacao_id);
CREATE INDEX idx_danos_produto_id  ON danos_produto (produto_id);
CREATE INDEX idx_danos_item_id     ON danos_produto (item_locacao_id);
