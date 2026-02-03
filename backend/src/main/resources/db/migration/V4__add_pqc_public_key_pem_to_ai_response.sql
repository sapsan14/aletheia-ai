-- PQC: Store PQC public key PEM when saving so Evidence Package can include PQC files
-- even when backend is later run without PQC enabled (no need for PqcSignatureService at download time).
ALTER TABLE ai_response ADD COLUMN pqc_public_key_pem TEXT;
