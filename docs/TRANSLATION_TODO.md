# Translation status (EN → RU, ET)

**Purpose:** Track which docs exist in EN only and should be translated for RU and ET.  
**Location:** Same path under `users/<lang>/`, `developers/<lang>/`, `internal/<lang>/`, or `partners/<lang>/`.

---

## Done (already have EN + RU + ET)

| Doc | EN | RU | ET |
|-----|----|----|-----|
| Trust model | users/en/trust-model.md | users/ru/trust-model.md | users/et/trust-model.md |
| Signing | developers/en/signing.md | developers/ru/signing.md | developers/et/signing.md |
| Timestamping | developers/en/timestamping.md | developers/ru/timestamping.md | developers/et/timestamping.md |
| PoC architecture | developers/en/poc-architecture.md | developers/ru/poc-architecture.md | developers/et/poc-architecture.md |
| Vision & roadmap | internal/en/vision-roadmap.md | internal/ru/vision-roadmap.md | internal/et/vision-roadmap.md |
| Plan | internal/en/plan.md | internal/ru/plan.md | internal/et/plan.md |
| Plan Phase 2 | internal/en/plan-phase2.md | internal/ru/plan-phase2.md | internal/et/plan-phase2.md |
| Plan Phase 4 | internal/en/plan-phase4.md | internal/ru/plan-phase4.md | internal/et/plan-phase4.md |
| Plan PQC | internal/en/plan-pqc.md | internal/ru/plan-pqc.md | internal/et/plan-pqc.md |
| Agent audit model | internal/en/agent-audit-model.md | internal/ru/agent-audit-model.md | internal/et/agent-audit-model.md |
| Testing strategy | developers/en/testing-strategy.md | developers/ru/testing-strategy.md | developers/et/testing-strategy.md |
| Crypto Oracle | developers/en/crypto-oracle.md | developers/ru/crypto-oracle.md | developers/et/crypto-oracle.md |
| Plan EDU | — | internal/ru/plan-edu.md | internal/et/plan-edu.md |
| Mock TSA | developers/en/mock-tsa.md | developers/ru/mock-tsa.md | — |

---

## To translate (EN only → add RU, ET)

| Doc | Path | Priority |
|-----|------|----------|
| Demo scenario (legal/compliance) | users/en/demo-scenario.md | Done (RU, ET added) |
| Quick start (developers) | developers/en/quickstart.md | High |
| Demo script (step-by-step) | scripts/demo-script.md | High |
| Manual test checklist 4.2 | scripts/manual-test-checklist-4-2.md | Medium |
| Phase 4 analytics | analytics/phase4-analytics.md | Low |
| Plan Phase 3 UI | internal/en/plan-phase3-ui.md | Low |
| Plan Phase 5 | internal/en/plan-phase5.md | Medium |
| Phase 4→5 transition | internal/en/phase4-5-transition.md | Low |
| Crypto reference | developers/en/crypto-reference.md | Medium (technical) |
| Policy lifecycle design | policy/policy-lifecycle-design.md | Low |
| Policy creation & human verification | policy/policy-creation-and-human-verification.md | Low |
| Outreach | outreach/phase4-outreach.md | Low |
| Use case: Trust Lab PQC | use-cases/trust-lab-case-ambiguous-pqc.md | Medium |

---

## Notes

- **RU:** Add files under `users/ru/`, `developers/ru/`, `internal/ru/` with same base name (e.g. `demo-scenario.md`).
- **ET:** Same under `users/et/`, `developers/et/`, `internal/et/`.
- **scripts/**, **analytics/**, **policy/**, **outreach/**, **use-cases/**: No language subfolders; translate by creating e.g. `scripts/demo-script-ru.md` and `scripts/demo-script-et.md`, or add a convention (e.g. `scripts/en/demo-script.md`, `scripts/ru/demo-script.md`) and update docs/README.
