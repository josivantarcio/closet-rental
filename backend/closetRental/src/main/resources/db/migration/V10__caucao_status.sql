-- Fase 3.2 — Gestão de caução robusta
-- Adiciona status do ciclo de vida da caução e valor efetivamente retido

ALTER TABLE locacoes
    ADD COLUMN status_caucao  VARCHAR(20)    NOT NULL DEFAULT 'PENDENTE',
    ADD COLUMN caucao_retida  NUMERIC(10, 2);

-- Locações já devolvidas ou canceladas recebem status adequado retroativamente
UPDATE locacoes SET status_caucao = 'LIBERADA'      WHERE status = 'DEVOLVIDO';
UPDATE locacoes SET status_caucao = 'RETIDA_TOTAL'  WHERE status = 'CANCELADO' AND caucao > 0;

CREATE INDEX idx_locacoes_status_caucao ON locacoes (status_caucao);
