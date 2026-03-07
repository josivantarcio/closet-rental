-- Fase 4 — Log de notificações enviadas

CREATE TABLE notificacoes (
    id           BIGSERIAL       PRIMARY KEY,
    tenant_id    UUID,
    tipo         VARCHAR(50)     NOT NULL,
    canal        VARCHAR(20)     NOT NULL DEFAULT 'EMAIL',
    destinatario VARCHAR(255)    NOT NULL,
    assunto      VARCHAR(255)    NOT NULL,
    status       VARCHAR(20)     NOT NULL,
    erro         TEXT,
    enviado_em   TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notificacoes_tenant    ON notificacoes (tenant_id);
CREATE INDEX idx_notificacoes_tipo      ON notificacoes (tipo);
CREATE INDEX idx_notificacoes_status    ON notificacoes (status);
CREATE INDEX idx_notificacoes_enviado   ON notificacoes (enviado_em);
