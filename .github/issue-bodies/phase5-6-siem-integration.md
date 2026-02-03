SIEM export: emit events (response_generated, response_signed, evidence_created) for enterprise audit.

**Acceptance Criteria:**
- Event schema defined (JSON Lines): event_type, timestamp, response_id, hash, policy_id, coverage (PII minimal)
- Backend emits events after sign/ask (and optionally evidence creation) — via structured log or configurable webhook (e.g. ALETHEIA_SIEM_WEBHOOK_URL)
- Documentation: `docs/en/integrations/SIEM.md` — schema, delivery options, example payload

См. `docs/en/PLAN_PHASE5.md` — 5.5, Task P5.5.1.
