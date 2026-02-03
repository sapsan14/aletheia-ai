-- Phase 4: policy coverage metadata for demo policy
-- Adds coverage ratio and per-rule evaluation results (JSON stored as TEXT).

ALTER TABLE ai_response
    ADD COLUMN policy_coverage DOUBLE PRECISION;

ALTER TABLE ai_response
    ADD COLUMN policy_rules_evaluated TEXT;
