-- =============================================================================
-- V7: Multi-tenancy — Shared Database, Shared Schema
-- Estratégia: tenant_id UUID em todas as tabelas de domínio.
-- UUID fixo do tenant padrão: a0000000-0000-0000-0000-000000000001
-- =============================================================================

-- Tabela de tenants (lojas/ateliers)
CREATE TABLE tenants (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nome       VARCHAR(255) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    plano      VARCHAR(20)  NOT NULL DEFAULT 'FREE' CHECK (plano IN ('FREE', 'BASIC', 'PRO')),
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_tenants_slug  ON tenants(slug);
CREATE INDEX idx_tenants_ativo ON tenants(ativo);

-- Tenant padrão para dados existentes (dev/demo)
INSERT INTO tenants (id, nome, slug, plano, ativo)
VALUES ('a0000000-0000-0000-0000-000000000001', 'Closet Rental Demo', 'default', 'FREE', TRUE);

-- =============================================================================
-- Adicionar tenant_id nas tabelas de domínio
-- Inicialmente nullable com DEFAULT para não quebrar dados existentes
-- =============================================================================

ALTER TABLE clientes
    ADD COLUMN tenant_id UUID REFERENCES tenants(id)
        NOT NULL DEFAULT 'a0000000-0000-0000-0000-000000000001';

ALTER TABLE produtos
    ADD COLUMN tenant_id UUID REFERENCES tenants(id)
        NOT NULL DEFAULT 'a0000000-0000-0000-0000-000000000001';

ALTER TABLE locacoes
    ADD COLUMN tenant_id UUID REFERENCES tenants(id)
        NOT NULL DEFAULT 'a0000000-0000-0000-0000-000000000001';

ALTER TABLE movimentacoes_estoque
    ADD COLUMN tenant_id UUID REFERENCES tenants(id)
        NOT NULL DEFAULT 'a0000000-0000-0000-0000-000000000001';

ALTER TABLE pagamentos
    ADD COLUMN tenant_id UUID REFERENCES tenants(id)
        NOT NULL DEFAULT 'a0000000-0000-0000-0000-000000000001';

-- usuarios: nullable pois SUPER_ADMIN não pertence a nenhum tenant
ALTER TABLE usuarios
    ADD COLUMN tenant_id UUID REFERENCES tenants(id);

-- Vincular usuários existentes ao tenant padrão
UPDATE usuarios SET tenant_id = 'a0000000-0000-0000-0000-000000000001'
WHERE role IN ('ADMIN', 'ATENDENTE', 'CLIENTE');

-- =============================================================================
-- Índices compostos (tenant_id primeiro para partition pruning O(log n))
-- =============================================================================

CREATE INDEX idx_clientes_tenant                  ON clientes(tenant_id);
CREATE INDEX idx_produtos_tenant                   ON produtos(tenant_id);
CREATE INDEX idx_locacoes_tenant                   ON locacoes(tenant_id);
CREATE INDEX idx_locacoes_tenant_produto_periodo   ON locacoes(tenant_id, produto_id, data_inicio, data_fim);
CREATE INDEX idx_pagamentos_tenant                 ON pagamentos(tenant_id);
CREATE INDEX idx_estoque_tenant                    ON movimentacoes_estoque(tenant_id);
CREATE INDEX idx_usuarios_tenant                   ON usuarios(tenant_id);

-- =============================================================================
-- Remover DEFAULT após migração (dados já foram migrados)
-- =============================================================================

ALTER TABLE clientes              ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE produtos              ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE locacoes              ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE movimentacoes_estoque ALTER COLUMN tenant_id DROP DEFAULT;
ALTER TABLE pagamentos            ALTER COLUMN tenant_id DROP DEFAULT;

-- =============================================================================
-- Atualizar CHECK do role em usuarios para incluir SUPER_ADMIN
-- =============================================================================

ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_role_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_role_check
    CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'ATENDENTE', 'CLIENTE'));
