# Aletheia AI — Documentation

Docs are organized by **audience**. Use the table that matches your role.

---

## Users

Concepts, trust model, and how to use the product.

| Document | EN | RU | ET |
|----------|----|----|-----|
| **Trust model and eIDAS** | [Trust model](users/en/trust-model.md) | [Модель доверия](users/ru/trust-model.md) | [Usaldusmudel](users/et/trust-model.md) |
| **Demo scenario (legal/compliance)** | [Demo scenario](users/en/demo-scenario.md) | [Сценарий (RU)](users/ru/demo-scenario.md) | [Stsenaarium (ET)](users/et/demo-scenario.md) |

---

## Developers and testers

API, setup, crypto, testing, deployment.

| Document | EN | RU | ET |
|----------|----|----|-----|
| **Quick start (env, run, deploy)** | [Quick start](developers/en/quickstart.md) | — | — |
| **API overview and OpenAPI** | [API reference](api/README.md) | — | — |
| **PoC architecture and stack** | [PoC architecture](developers/en/poc-architecture.md) | [Архитектура](developers/ru/poc-architecture.md) | [Arhitektuur](developers/et/poc-architecture.md) |
| **Signing (what we sign, keys)** | [Signing](developers/en/signing.md) | [Подпись](developers/ru/signing.md) | [Allkirjastamine](developers/et/signing.md) |
| **Timestamping (RFC 3161, TSA)** | [Timestamping](developers/en/timestamping.md) | [Временные метки](developers/ru/timestamping.md) | [Ajatemplid](developers/et/timestamping.md) |
| **Testing strategy** | [Testing strategy](developers/en/testing-strategy.md) | [Стратегия тестирования](developers/ru/testing-strategy.md) | [Testimise strateegia](developers/et/testing-strategy.md) |
| **Mock TSA (tests)** | [Mock TSA](developers/en/mock-tsa.md) | [Mock TSA](developers/ru/mock-tsa.md) | — |
| **Crypto Oracle (tests)** | [Crypto Oracle](developers/en/crypto-oracle.md) | [Оракул](developers/ru/crypto-oracle.md) | [Oraakel](developers/et/crypto-oracle.md) |
| **Crypto reference (algorithms)** | [Crypto reference](developers/en/crypto-reference.md) | — | — |
| **Demo script (step-by-step)** | [Demo script](scripts/demo-script.md) | — | — |
| **Manual test checklist (4.2)** | [Manual test checklist](scripts/manual-test-checklist-4-2.md) | — | — |
| **Phase 4 analytics** | [Analytics](analytics/phase4-analytics.md) | — | — |
| **Deployment (Ansible)** | [Deploy README](../deploy/ansible/README.md) | — | — |
| **Legal and regulatory (eIDAS, PQC)** | [Legal docs](legal/README.md) | — | — |
| **Architecture diagrams** | [Diagrams](../diagrams/architecture.md) | — | — |

---

## Partners

Value proposition, use cases, integration.

| Document | EN | RU | ET |
|----------|----|----|-----|
| **Use case: legal/compliance** | [Demo scenario](users/en/demo-scenario.md) | — | — |
| **Use case: Trust Lab (PQC)** | [Trust Lab case](use-cases/trust-lab-case-ambiguous-pqc.md) | — | — |
| **Outreach (targets, template)** | [Phase 4 outreach](outreach/phase4-outreach.md) | — | — |

---

## Internal (project planning only)

Vision, roadmap, phase plans. Not for external distribution.

| Document | EN | RU | ET |
|----------|----|----|-----|
| **Vision and roadmap** | [Vision and roadmap](internal/en/vision-roadmap.md) | [Видение](internal/ru/vision-roadmap.md) | [Visioon](internal/et/vision-roadmap.md) |
| **Implementation plan** | [Plan](internal/en/plan.md) | [План](internal/ru/plan.md) | [План](internal/et/plan.md) |
| **Plan Phase 2 (killer demo)** | [Plan Phase 2](internal/en/plan-phase2.md) | [RU](internal/ru/plan-phase2.md) | [ET](internal/et/plan-phase2.md) |
| **Plan Phase 3 (UI)** | [Plan Phase 3 UI](internal/en/plan-phase3-ui.md) | — | — |
| **Plan Phase 4 (market, policy)** | [Plan Phase 4](internal/en/plan-phase4.md) | [RU](internal/ru/plan-phase4.md) | [ET](internal/et/plan-phase4.md) |
| **Plan Phase 5 (API)** | [Plan Phase 5](internal/en/plan-phase5.md) | — | — |
| **Plan PQC (post-quantum)** | [Plan PQC](internal/en/plan-pqc.md) | [RU](internal/ru/plan-pqc.md) | [ET](internal/et/plan-pqc.md) |
| **Plan EDU (education)** | — | [Plan EDU](internal/ru/plan-edu.md) | [Plan EDU](internal/et/plan-edu.md) |
| **Phase 4→5 transition** | [Phase 4–5 transition](internal/en/phase4-5-transition.md) | — | — |
| **Agent audit model** | [Agent audit](internal/en/agent-audit-model.md) | [RU](internal/ru/agent-audit-model.md) | [ET](internal/et/agent-audit-model.md) |
| **Future ideas (PKI, MCP)** | — | [Идеи](internal/ru/ideas/readme.md) | [Ideed](internal/et/ideas/readme.md) |

---

## Policy

Demo policy and policy design (shared across audiences).

| Document | Path |
|----------|------|
| **Demo policy (rules R1–R4)** | [aletheia-demo-2026-01](policy/aletheia-demo-2026-01.md) · [JSON](policy/aletheia-demo-2026-01.json) |
| **Policy lifecycle design** | [Policy lifecycle](policy/policy-lifecycle-design.md) |
| **Policy creation and human verification** | [Policy creation](policy/policy-creation-and-human-verification.md) |

---

## Authorship

The Aletheia AI project was initiated by **Anton Sokolov** (TalTech continuing education).  
Licensed under **MIT**. See [LICENSE](../LICENSE).

---

## Translations

Documents are provided in **English (EN)**, **Russian (RU)**, and **Estonian (ET)** where applicable. EN is the primary language; RU and ET exist for users, developers, and internal plans as listed in the tables above. New docs are added in EN first; translations can be added to the same path under `users/<lang>/`, `developers/<lang>/`, `internal/<lang>/`, or `partners/<lang>/`.

---

## Structure and naming

- **Folders:** `users/`, `developers/`, `partners/`, `internal/` by audience; `policy/`, `api/`, `scripts/`, `analytics/`, `legal/`, `outreach/`, `use-cases/` by topic.
- **Filenames:** lowercase, hyphens (e.g. `trust-model.md`, `plan-phase4.md`).
- **Translations:** See [TRANSLATION_TODO.md](TRANSLATION_TODO.md) for status and docs that still need RU/ET.
- **Legacy:** Old `docs/en/`, `docs/ru/`, `docs/et/` have been removed; all content lives in the structure above.
