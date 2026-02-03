-- PQC.3: Optional ML-DSA (Dilithium) signature over the same hash. Null when PQC disabled.
-- See docs/en/PLAN_PQC.md.

ALTER TABLE ai_response ADD COLUMN signature_pqc TEXT;
