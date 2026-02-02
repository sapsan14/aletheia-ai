# Aletheia AI — Documentation index

Documentation is grouped by language in `docs/<lang>/` (en, ru, et). This page gives an overview and suggests where to start.

---

## Where to start

| If you want to… | Read first | Then |
|-----------------|------------|------|
| **Understand the project** | [PoC](en/PoC.md) (architecture & stack) | [Vision & roadmap](en/VISION_AND_ROADMAP.md) |
| **Implement or extend** | [Implementation plan](en/PLAN.md) | [Signing](en/SIGNING.md), [Timestamping](en/TIMESTAMPING.md) |
| **Plan Phase 2 (killer demo)** | [PLAN_PHASE2](en/PLAN_PHASE2.md) | [Vision Phase 2](en/VISION_AND_ROADMAP.md#2-killer-demo--domain-choice) |
| **Understand crypto** | [Crypto reference](en/CRYPTO_REFERENCE.md) (algorithms, keys, why tsaToken) | [Signing](en/SIGNING.md), [Trust model](en/TRUST_MODEL.md) |
| **Test or CI** | [MOCK_TSA](en/MOCK_TSA.md), [Cryptographic Oracle](en/CRYPTO_ORACLE.md) | [Testing strategy](en/TESTING_STRATEGY.md) |
| **Deploy** | [README → Deployment](../README.md#deployment) | [deploy/ansible/README](../deploy/ansible/README.md) (includes [API proxy (Docker)](../deploy/ansible/README.md#api-proxy-docker), ngrok, CORS) |

Same topics exist in **Russian** ([docs/ru/](ru/)) and **Estonian** ([docs/et/](et/)). [Crypto reference](en/CRYPTO_REFERENCE.md) is EN-only; [MOCK_TSA](en/MOCK_TSA.md) has no ET translation.

---

## Documents by topic

| Topic | Description | EN | RU | ET |
|-------|-------------|----|----|-----|
| **PoC** | Architecture, stack, cryptography choices | [PoC](en/PoC.md) | [PoC](ru/PoC.md) | [PoC](et/PoC.md) |
| **Vision & roadmap** | Next steps, Evidence Package, AI Claim | [Vision](en/VISION_AND_ROADMAP.md) | [Видение](ru/VISION_AND_ROADMAP.md) | [Visioon](et/VISION_AND_ROADMAP.md) |
| **Plan** | Step-by-step implementation tasks | [PLAN](en/PLAN.md) | [План](ru/PLAN.md) | [Plaan](et/PLAN.md) |
| **Plan Phase 2** | Killer demo, Evidence Package, offline verifier | [EN](en/PLAN_PHASE2.md) | [RU](ru/PLAN_PHASE2.md) | [ET](et/PLAN_PHASE2.md) |
| **Signing** | What we sign, key, interface, storage | [SIGNING](en/SIGNING.md) | [Подпись](ru/SIGNING.md) | [Allkirjastamine](et/SIGNING.md) |
| **Timestamping** | RFC 3161, TSA, mock/real, storage | [TIMESTAMPING](en/TIMESTAMPING.md) | [Временные метки](ru/TIMESTAMPING.md) | [Ajatemplid](et/TIMESTAMPING.md) |
| **Trust model** | Who attests what, eIDAS mapping | [TRUST_MODEL](en/TRUST_MODEL.md) | [Модель доверия](ru/TRUST_MODEL.md) | [Usaldusmudel](et/TRUST_MODEL.md) |
| **MOCK_TSA** | Deterministic TSA for tests | [MOCK_TSA](en/MOCK_TSA.md) | [MOCK_TSA](ru/MOCK_TSA.md) | — |
| **Crypto Oracle** | Oracle pattern for testing | [CRYPTO_ORACLE](en/CRYPTO_ORACLE.md) | [Оракул](ru/CRYPTO_ORACLE.md) | [Oraakel](et/CRYPTO_ORACLE.md) |
| **Crypto reference** | Algorithms, keys, tsaToken (beginner-friendly) | [CRYPTO_REFERENCE](en/CRYPTO_REFERENCE.md) | — | — |
| **Agent Audit** | Audit model for LLM agents | [AGENT_AUDIT](en/AGENT_AUDIT_MODEL.md) | [Аудит агентов](ru/AGENT_AUDIT_MODEL.md) | [Agentide audit](et/AGENT_AUDIT_MODEL.md) |
| **Testing strategy** | Unit, integration, fixtures, CI | [TESTING_STRATEGY](en/TESTING_STRATEGY.md) | [Стратегия тестирования](ru/TESTING_STRATEGY.md) | [Testimise strateegia](et/TESTING_STRATEGY.md) |
| **Future ideas** (PKI for AI agents, OpenClaw, MCP) | — | [Идеи](ru/ideas/README.md) | [Ideed](et/ideas/README.md) |
| **Legal & regulatory** | EU law: eIDAS, AI Act, GDPR, ETSI timestamps | [legal/README.md](legal/README.md) | | |
| **Diagrams** | Mermaid: pipeline, trust chain, stack | [diagrams/architecture.md](../diagrams/architecture.md) | | |

---

## Cross-references

- From any `docs/<lang>/` file: same-language docs use relative links (e.g. `[SIGNING](SIGNING.md)`). README and diagrams: `../../README.md`, `../../diagrams/architecture.md`.
- Root [README](../README.md) links to `docs/en/`, `docs/ru/`, `docs/et/` and [diagrams/architecture.md](../diagrams/architecture.md).
