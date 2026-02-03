Implement sign-only API so external systems can send an already-generated LLM response to Aletheia for signing (no LLM call).

**Acceptance Criteria:**
- Endpoint POST /api/sign with body: `{ "response", "model_id", "policy_id" }` (and optional fields)
- Backend: canonicalize → hash → sign → timestamp → store; return id, signature, tsaToken, claim, etc.
- POST /api/sign documented in OpenAPI and on "For Developers" page
- Integration test: sign then GET /api/ai/verify/:id returns same record

См. `docs/en/PLAN_PHASE5.md` — 5.2, Tasks P5.2.1, P5.2.2.
