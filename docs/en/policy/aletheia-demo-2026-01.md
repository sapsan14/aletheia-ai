# Aletheia Demo Policy (2026-01)

This is the **canonical demo policy** for Phase 4 demos and pilots. It provides a
transparent, minimal rule set so that every demo uses the same policy baseline.

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
