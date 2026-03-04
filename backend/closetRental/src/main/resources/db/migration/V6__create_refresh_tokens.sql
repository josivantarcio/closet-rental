-- V6: Tabela de refresh tokens
-- Access token: configurável (padrão 15min = 900s)
-- Refresh token: UUID opaco, válido por 7 dias, associado ao usuário

CREATE TABLE refresh_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    token       VARCHAR(36)  NOT NULL,
    username    VARCHAR(50)  NOT NULL,
    expira_em   TIMESTAMP    NOT NULL,
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW(),
    revogado    BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uq_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX        idx_refresh_tokens_username ON refresh_tokens (username);
CREATE INDEX        idx_refresh_tokens_expira_em ON refresh_tokens (expira_em);
