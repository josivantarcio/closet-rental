-- V13: Vínculo entre usuário (login) e registro de cliente
-- Permite que ROLE_CLIENTE veja apenas suas próprias locações e pagamentos

ALTER TABLE usuarios
    ADD COLUMN cliente_id BIGINT REFERENCES clientes(id) ON DELETE SET NULL;

CREATE INDEX idx_usuarios_cliente_id ON usuarios(cliente_id);
