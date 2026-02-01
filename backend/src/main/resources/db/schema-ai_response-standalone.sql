-- Standalone SQL for ai_response table (PostgreSQL)
-- Use when running migrations manually without the app.
-- Flyway runs this automatically on app startup (see db/migration/V1__*.sql).

CREATE TABLE IF NOT EXISTS ai_response (
    id              BIGSERIAL PRIMARY KEY,
    prompt          TEXT NOT NULL,
    response        TEXT NOT NULL,
    response_hash   VARCHAR(64) NOT NULL,
    signature       VARCHAR(2048),
    tsa_token       VARCHAR(4096),
    llm_model       VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_id      VARCHAR(64),
    temperature     DOUBLE PRECISION,
    system_prompt   TEXT,
    version         INTEGER DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_ai_response_created_at ON ai_response (created_at);
CREATE INDEX IF NOT EXISTS idx_ai_response_request_id ON ai_response (request_id);
