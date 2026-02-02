-- DP2.4.1: Minimal AI Claim — claim, confidence, policy_version in metadata and signed payload.
-- Nullable: only set when compliance inferred from prompt (e.g. GDPR, comply, clause).
-- Compatible with H2 (tests) and PostgreSQL (production).

ALTER TABLE ai_response ADD COLUMN claim TEXT;
ALTER TABLE ai_response ADD COLUMN confidence DOUBLE PRECISION;
ALTER TABLE ai_response ADD COLUMN policy_version VARCHAR(64);
