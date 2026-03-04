-- V5: Tabela de usuários — substitui autenticação in-memory
-- Roles: ADMIN, ATENDENTE, CLIENTE

CREATE TABLE usuarios (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    username    VARCHAR(50)  NOT NULL,
    email       VARCHAR(150) NOT NULL,
    senha_hash  VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'ATENDENTE', 'CLIENTE')),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);

CREATE UNIQUE INDEX uq_usuarios_username ON usuarios (username);
CREATE UNIQUE INDEX uq_usuarios_email    ON usuarios (email);
CREATE INDEX        idx_usuarios_role    ON usuarios (role);
CREATE INDEX        idx_usuarios_ativo   ON usuarios (ativo);

-- Usuários iniciais (senhas hasheadas com BCrypt — custo 10)
-- admin123      → $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH (placeholder)
-- Senhas serão inseridas pela aplicação no primeiro boot via DataInitializer
