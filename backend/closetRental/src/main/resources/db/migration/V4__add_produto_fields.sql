-- V4: Adiciona campos descritivos ao produto (tamanho, cor, descricao, imagem_url)
-- Todos opcionais para manter compatibilidade com registros existentes

ALTER TABLE produtos
    ADD COLUMN tamanho    VARCHAR(20),
    ADD COLUMN cor        VARCHAR(50),
    ADD COLUMN descricao  TEXT,
    ADD COLUMN imagem_url VARCHAR(500);

-- Índice para buscas por cor e tamanho (catálogo com filtros)
CREATE INDEX idx_produtos_categoria ON produtos (categoria);
CREATE INDEX idx_produtos_disponivel ON produtos (disponivel);
