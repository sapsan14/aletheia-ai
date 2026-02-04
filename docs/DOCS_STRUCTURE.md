# Documentation structure and audience

**Internal.** Defines folder layout, naming, and audience for all project docs.

---

## Audience

| Audience | Folder | Purpose |
|----------|--------|---------|
| **Users** | `docs/users/<lang>/` | Definitions, concepts, how to use the product, trust model |
| **Developers & testers** | `docs/developers/<lang>/` | API, testing, signing, timestamping, deployment, scripts |
| **Partners** | `docs/partners/<lang>/` | Value proposition, integration, use cases, business |
| **Internal** | `docs/internal/<lang>/` | Vision, roadmap, phase plans (not for external distribution) |

---

## Naming

- **All filenames:** lowercase, words separated by hyphens (e.g. `trust-model.md`, `plan-phase4.md`).
- **Cross-references:** use human-readable link text, not raw filename. Example: `[Trust model and eIDAS](trust-model.md)`.

---

## Folder layout

```
docs/
  README.md                 # Index: who you are → where to go (users / developers / partners)
  users/
    en/ ru/ et/
      trust-model.md
      concepts.md           # Definitions (evidence package, coverage policy, etc.)
      demo-scenario.md     # Phase 4 legal/compliance scenario
  developers/
    en/ ru/ et/
      api.md               # API overview + link to OpenAPI
      signing.md
      timestamping.md
      testing-strategy.md
      mock-tsa.md
      crypto-oracle.md
      crypto-reference.md  # EN-only if not translated
      deployment.md        # From README + deploy/ansible
  partners/
    en/ ru/ et/
      value-proposition.md
      use-cases.md
      integration.md       # How to integrate (high-level)
  internal/
    en/ ru/ et/
      vision-roadmap.md    # Marked: "Internal — vision and roadmap"
      plan.md
      plan-phase2.md
      plan-phase3-ui.md
      plan-phase4.md
      plan-phase5.md
      plan-pqc.md
      plan-edu.md          # RU, ET
      phase4-5-transition.md
      agent-audit-model.md
  policy/
    aletheia-demo-2026-01.json
    aletheia-demo-2026-01.md
    policy-lifecycle-design.md
    policy-creation-and-human-verification.md
  api/
    openapi.yaml
    readme.md
  legal/
    README.md
    download.sh
  outreach/
    phase4-outreach.md
  scripts/                 # Demo script, manual test checklist (developer-facing)
    demo-script.md
    manual-test-checklist-4-2.md
  analytics/
    phase4-analytics.md
  use-cases/               # Detailed use case narratives
    trust-lab-case-ambiguous-pqc.md
  diagrams/                # At repo root: diagrams/architecture.md
```

---

## File mapping (current → new)

| Current | Audience | New path |
|---------|----------|----------|
| en/TRUST_MODEL.md | users | users/en/trust-model.md |
| en/PoC.md | developers | developers/en/poc-architecture.md |
| en/SIGNING.md | developers | developers/en/signing.md |
| en/TIMESTAMPING.md | developers | developers/en/timestamping.md |
| en/MOCK_TSA.md | developers | developers/en/mock-tsa.md |
| en/CRYPTO_ORACLE.md | developers | developers/en/crypto-oracle.md |
| en/CRYPTO_REFERENCE.md | developers | developers/en/crypto-reference.md |
| en/TESTING_STRATEGY.md | developers | developers/en/testing-strategy.md |
| en/AGENT_AUDIT_MODEL.md | internal | internal/en/agent-audit-model.md |
| en/VISION_AND_ROADMAP.md | internal | internal/en/vision-roadmap.md |
| en/PLAN.md | internal | internal/en/plan.md |
| en/PLAN_PHASE2.md | internal | internal/en/plan-phase2.md |
| en/PLAN_PHASE3_UI.md | internal | internal/en/plan-phase3-ui.md |
| en/PLAN_PHASE4.md | internal | internal/en/plan-phase4.md |
| en/PLAN_PHASE5.md | internal | internal/en/plan-phase5.md |
| en/PLAN_PQC.md | internal | internal/en/plan-pqc.md |
| en/PHASE4_5_TRANSITION.md | internal | internal/en/phase4-5-transition.md |
| en/policy/aletheia-demo-2026-01.* | policy | policy/aletheia-demo-2026-01.* |
| en/policy/POLICY_*.md | policy | policy/policy-lifecycle-design.md, policy-creation-and-human-verification.md |
| DEMO_SCRIPT.md | developers | scripts/demo-script.md |
| DEMO_SCENARIO_PHASE4.md | users | users/en/demo-scenario.md |
| MANUAL_TEST_CHECKLIST_4_2.md | developers | scripts/manual-test-checklist-4-2.md |
| PHASE4_ANALYTICS.md | developers | analytics/phase4-analytics.md |
| api/README.md | developers | api/readme.md |
| outreach/PHASE4_OUTREACH.md | internal | outreach/phase4-outreach.md |
| use_cases/Aletheia_Trust_Lab_*.md | partners/users | use-cases/trust-lab-case-ambiguous-pqc.md |

Same mapping for `ru/` and `et/` where files exist.

---

## Root README.md

- **Minimal:** project name, tagline, badges, license.
- **Documentation:** single table or list: "For **users** (concepts, trust) → [docs/users](docs/README.md#users)". "For **developers** (API, run, test) → [docs/developers](docs/README.md#developers)". "For **partners** (value, integration) → [docs/partners](docs/README.md#partners)".
- **Quick start:** 3–5 bullets or one code block (clone, env, run backend, run frontend). All detailed steps, env table, deployment → moved to `docs/developers/en/quickstart.md` or similar.
- **No long paragraphs mixed with commands.** Commands in fenced code blocks only.

---

## Internal docs

- Every doc under `docs/internal/` must start with: **"Internal — [topic]. For project planning only."**
- `vision-roadmap.md`, all `plan-*.md`, `phase4-5-transition.md` are internal.

---

## Translations

- After restructure: ensure each logical doc has `users/<lang>/`, `developers/<lang>/`, `partners/<lang>/`, `internal/<lang>/` for en, ru, et where applicable.
- Link from docs/README.md with clear titles: "[Trust model (EN)](users/en/trust-model.md)", "[Модель доверия (RU)](users/ru/trust-model.md)", etc.
