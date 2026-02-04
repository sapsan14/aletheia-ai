# Aletheia Demo Policy (2026-01)

This is the **Coverage-policy** for Phase 4 demos and pilots: the versioned set of
rules that define what Aletheia checks (signature, timestamp, model identity, etc.)
and how coverage is reported. It provides a transparent, minimal rule set so that
every demo uses the same policy baseline. (**Claim-policy** is separate: it is the
policy or framework the AI used when forming the claim, e.g. AI-ACT-2024.)

## Purpose

The demo policy is intentionally small (4 rules) and focuses on **verifiability**
and **traceability**. It makes explicit which checks are automated now and which
checks are **not evaluated yet** in Phase 4, so the UI can be honest about
coverage and gaps.

## Rules

| Rule ID | Description | Check status | Notes |
|--------|-------------|--------------|-------|
| R1 | Response is signed and timestamped | `automated` | Requires signature + RFC 3161 TSA token. |
| R2 | Model identity (model_id) is recorded | `automated` | Requires LLM model id. |
| R3 | No medical or legal advice in response | `not_evaluated` | Phase 4 does not run content checks yet. |
| R4 | Human review performed | `not_evaluated` | Phase 4 does not include human review. |

## Why R3 and R4 are `not_evaluated` in Phase 4

Phase 4 focuses on **market validation**, not full policy enforcement. We show
which checks were performed (R1, R2) and clearly mark which checks were not run
(R3, R4). This keeps the demo honest and prevents overstating coverage.

## Where this policy is used in Phase 4.5

In the Phase 4.5 transition, this demo policy is applied consistently across:

- **Backend evaluation:** `PolicyEvaluationService` uses `policy_id = "aletheia-demo"` and
  `policy_version = "2026-01"` when computing `policyCoverage` and per‑rule results.
- **Persistence:** when no explicit `policyVersion` is provided in the request,
  `AuditRecordService` stores the demo policy version on each `AiResponse` record.
- **Verify API:** `GET /api/ai/verify/:id` returns `policyVersion` together with coverage
  and rule results so that the frontend can display which policy was used.
- **Evidence Package:** `metadata.json` includes `policy_version`, `policy_coverage`,
  and `policy_rules_evaluated` for offline verification.
- **UI:** the verify page and the Trust Panel show Coverage-policy (demo) and
  explicitly label it as `aletheia-demo (2026-01)` in the trust summary.
