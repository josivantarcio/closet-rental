-- =============================================================================
-- V1__create_tables.sql
-- Schema inicial do Closet Rental
-- Dialeto: PostgreSQL 14+
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Tabela: produtos
-- -----------------------------------------------------------------------------
CREATE TABLE produtos (
    id           BIGSERIAL     PRIMARY KEY,
    nome         VARCHAR(255)  NOT NULL,
    categoria    VARCHAR(255)  NOT NULL,
    valor_diaria NUMERIC(10,2) NOT NULL,
    disponivel   BOOLEAN       NOT NULL DEFAULT TRUE,
    created_by   VARCHAR(255),
    created_at   TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- Tabela: clientes
-- -----------------------------------------------------------------------------
CREATE TABLE clientes (
    id          BIGSERIAL    PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    email       VARCHAR(200) NOT NULL,
    telefone    VARCHAR(20)  NOT NULL,
    documento   VARCHAR(20)  NOT NULL,
    endereco    VARCHAR(300),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by  VARCHAR(100),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT uk_clientes_documento UNIQUE (documento),
    CONSTRAINT uk_clientes_email     UNIQUE (email)
);

-- -----------------------------------------------------------------------------
-- Tabela: locacoes
-- -----------------------------------------------------------------------------
CREATE TABLE locacoes (
    id           BIGSERIAL     PRIMARY KEY,
    cliente_id   BIGINT        NOT NULL,
    produto_id   BIGINT        NOT NULL,
    data_inicio  DATE          NOT NULL,
    data_fim     DATE          NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    valor_diaria NUMERIC(10,2) NOT NULL,
    caucao       NUMERIC(10,2),
    multa        NUMERIC(10,2),
    valor_total  NUMERIC(10,2) NOT NULL,
    retirada_at  TIMESTAMP,
    devolucao_at TIMESTAMP,
    observacoes  VARCHAR(500),
    created_by   VARCHAR(100),
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    CONSTRAINT chk_locacoes_status CHECK (
        status IN ('RESERVADO', 'RETIRADO', 'DEVOLVIDO', 'CANCELADO')
    ),
    CONSTRAINT chk_locacoes_datas CHECK (data_fim >= data_inicio)
);

-- -----------------------------------------------------------------------------
-- Tabela: movimentacoes_estoque
-- locacao_id nullable: movimentacoes de manutencao manual nao tem locacao
-- -----------------------------------------------------------------------------
CREATE TABLE movimentacoes_estoque (
    id            BIGSERIAL    PRIMARY KEY,
    produto_id    BIGINT       NOT NULL,
    locacao_id    BIGINT,
    tipo          VARCHAR(30)  NOT NULL,
    motivo        VARCHAR(500),
    operador_nome VARCHAR(150),
    criado_em     TIMESTAMP,
    CONSTRAINT chk_estoque_tipo CHECK (
        tipo IN ('ENTRADA', 'SAIDA', 'MANUTENCAO', 'RETORNO_MANUTENCAO')
    )
);

-- -----------------------------------------------------------------------------
-- Tabela: pagamentos
-- -----------------------------------------------------------------------------
CREATE TABLE pagamentos (
    id            BIGSERIAL     PRIMARY KEY,
    locacao_id    BIGINT        NOT NULL,
    valor         NUMERIC(10,2) NOT NULL,
    status        VARCHAR(20)   NOT NULL,
    metodo        VARCHAR(20)   NOT NULL,
    motivo        VARCHAR(500),
    criado_em     TIMESTAMP     NOT NULL,
    processado_em TIMESTAMP,
    created_by    VARCHAR(150),
    CONSTRAINT chk_pagamentos_status CHECK (
        status IN ('PENDENTE', 'APROVADO', 'FALHA', 'ESTORNADO')
    ),
    CONSTRAINT chk_pagamentos_metodo CHECK (
        metodo IN ('PIX', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'DINHEIRO')
    )
);
