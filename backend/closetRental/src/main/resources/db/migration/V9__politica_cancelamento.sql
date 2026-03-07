-- =============================================================================
-- V9: Política de cancelamento por tenant
-- Adiciona 3 percentuais configuráveis em tenants.
--
-- Faixas de dias até a data de início da locação:
--   >= 7 dias  → isento (não há campo — sempre 0%)
--   3–6 dias   → pct_cancelamento_3d (default 30%)
--   1–2 dias   → pct_cancelamento_1d (default 70%)
--   0 dias     → pct_cancelamento_0d (default 100%)
-- =============================================================================

ALTER TABLE tenants
    ADD COLUMN pct_cancelamento_3d NUMERIC(5,2) NOT NULL DEFAULT 30.00,
    ADD COLUMN pct_cancelamento_1d NUMERIC(5,2) NOT NULL DEFAULT 70.00,
    ADD COLUMN pct_cancelamento_0d NUMERIC(5,2) NOT NULL DEFAULT 100.00;

-- Adiciona campo multa_cancelamento em locacoes (separado de multa por atraso)
ALTER TABLE locacoes
    ADD COLUMN multa_cancelamento NUMERIC(10,2);
