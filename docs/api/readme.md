# Aletheia AI — Public API

OpenAPI 3.0 specification for the Aletheia backend (Phase 4.5).

- **Spec:** [openapi.yaml](openapi.yaml)
- **Base URL:** See `servers` in the spec (e.g. `http://localhost:8080` when running the backend locally).
- **Swagger UI:** When the backend is running, open `http://localhost:8080/swagger-ui.html` for interactive docs.

## Main endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/ai/ask | Prompt → LLM → sign → timestamp → store; returns id and verification data |
| GET | /api/ai/verify/{id} | Full verification record (includes policy coverage and rule results) |
| GET | /api/ai/evidence/{id} | Download Evidence Package (.aep) for offline verification |
| GET | /api/ai/verifier | Download offline verifier JAR |

## Policy coverage (Phase 4.5)

`GET /api/ai/verify/{id}` returns `policyCoverage` (0–1) and `policyRulesEvaluated` (list of ruleId + status) for the demo Coverage-policy **aletheia-demo (2026-01)**. These fields are also included in the Evidence Package `metadata.json`.
