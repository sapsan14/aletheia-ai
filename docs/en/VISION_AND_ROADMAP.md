# Aletheia AI — Vision and Roadmap (English)

Product vision and next steps: from cryptographically verifiable AI responses to **AI attestation** and **trust infrastructure**. Strategic direction beyond the current PoC and demo.

**Related:** [PoC (architecture)](PoC.md) · [Implementation plan](PLAN.md) · [Trust model](TRUST_MODEL.md)

---

## Table of contents

- [Current state](#current-state)
- [Positioning](#positioning)
- [Extended vision](#extended-vision)
- [Strategic roadmap](#strategic-roadmap)
- [Focus areas (detailed)](#focus-areas-detailed)
- [Production readiness: Cloud & HSM](#production-readiness-cloud--hsm)
- [Investor pitch](#investor-pitch)

---

## Current state

| Area | Status |
|------|--------|
| **PoC** | ✅ Complete |
| **Demo** | ✅ Working end-to-end |
| **Crypto pipeline** | ✅ Canonicalize → hash → sign → timestamp (RFC 3161) |
| **RFC 3161 TSA** | ✅ Implemented (rare in AI projects) |
| **Trust chain** | ✅ Signed response + TSA token; verifiable on our backend |
| **Deploy** | ✅ Docker, docker-compose, Ansible |
| **PQC (optional)** | ✅ Hybrid classical + post-quantum (ML-DSA) signatures for long-term evidence verification ([Plan PQC](PLAN_PQC.md)) |

The hardest part is done: a **cryptographically strict trust chain for AI**. The next level is turning “signed AI response” into **audit-grade AI attestation** and **verification without our server**.

---

## Positioning

> **Aletheia AI provides cryptographically verifiable AI statements with trusted timestamps — suitable for audit, legal, and compliance use.**

This positions the project beyond “chatbot security” toward **AI attestation** and **trust infrastructure**.

---

## Extended vision

Beyond response-level attestation, the trust infrastructure can extend to:

| Scope | What we attest | Why it matters |
|-------|----------------|----------------|
| **Models** | Model weights, architecture, checkpoint integrity | Reproducibility, supply chain; EU AI Act traceability for high-risk systems |
| **Agents** | Agent identity, capabilities, tool usage | Multi-agent systems; who did what |
| **Datasets & data** | Training data provenance, lineage, consent | Compliance, bias audit, copyright |
| **AI-to-AI via MCP** | Attestation over Model Context Protocol | One agent certifies another; interoperable AI trust layer |

**Signing neural network weights** — cryptographic binding of model checkpoint to a specific version and provenance. Verifiers can confirm the exact model that produced a given output.

These are strategic perspectives: they broaden Aletheia from "signed responses" to **full-stack AI trust** — models, data, agents, and responses. Near-term roadmap stays on response attestation; this is the horizon.

### Complementary trust layers

Trust in **what was done** (who did what, when) is complemented by other layers. **Proof of Human** (e.g. [World ID](https://worldcoin.org) / Orb and similar systems) answers “who is human” without revealing personal data. Aletheia answers “what was done, by whom, and when.” These are orthogonal axes: one layer is human identity; the other is cryptographic proof of the result. Proof of Human can be used as an optional input when issuing agent certificates (e.g. “agent under control of a verified human”). More: [idea: World ID and Aletheia](../ru/ideas/WORLD_ID_AND_ALETHEIA.md) (RU).

---

## Strategic roadmap

Single prioritized plan (phases by impact and dependency):

| Phase | Focus | Status | Purpose |
|-------|-------|--------|---------|
| **1** | Evidence Package + offline verification | ✅ Done | Trust infrastructure; verification without our server |
| **2** | Killer demo (legal/compliance) | ✅ Done | Product-market fit; investor story; choose primary domain |
| **3** | AI Claim (structured attestation) + Verify UI | ✅ Done | Audit-grade assertions with context and confidence |
| **4** | Key and trust model | Next | PKI for AI; key_id, registry, rotation |
| **5** | Trusted time anchoring | — | Multi-TSA, public anchors (Bitcoin, Ethereum, Roughtime) |
| **6** | Production: Cloud + HSM path | — | Scalability, SLA, enterprise readiness |

**Future perspectives:**

- **AI vs AI verification** — one AI asserts, another AI verifies; multi-agent trust (aligns with MCP / extended vision)
- **EU-style roadmap** — PoC → Pilot → Regulation alignment
- **Post-quantum (PQC)** — optional hybrid classical + ML-DSA signatures over the same evidence hash for long-term verification; see [Plan PQC](PLAN_PQC.md) (out-of-scope PoC / enthusiast track).

---

## Focus areas (detailed)

### 1. Evidence Package & offline verification

**Current:** Verification = our backend only.

**Goal:** Verification **without** our system.

| Deliverable | Purpose |
|-------------|---------|
| **Evidence Package (.aep)** | response.txt, canonical.bin, hash.sha256, signature.sig, timestamp.tsr, metadata.json, public_key.pem |
| **CLI tool** | `aletheia verify response.json` — run locally |
| **Pure JS verifier** | Browser or Node — no backend call |

Auditors, courts, or enterprises can verify hash, signature, and timestamp offline. This is when the project becomes **trust infrastructure**.

---

### 2. Killer demo & domain choice

Focus on **one** primary domain first. Recommended: **legal / compliance AI** (EU AI Act, contracts, audit).

| Domain | Why |
|--------|-----|
| **Legal / compliance AI** | Contracts, clauses, regulatory checks |
| **Medical AI opinions** | Second opinion, documentation |
| **AI-generated contracts** | Signing and attestation of AI-drafted terms |
| **Scientific AI results** | Reproducibility, citations |
| **AI journalism / fact claims** | Verifiable sources and claims |

Important: not “chat,” but **assertions with consequences**.

**Concrete plan:** Step-by-step tasks (Evidence Package, offline verifier, one killer demo scenario, LLM-readable coding prompts) are in [Plan Phase 2](PLAN_PHASE2.md). Run the demo in ≤5 min: [Demo script](../DEMO_SCRIPT.md). Plan Phase 2 also describes [opportunities](PLAN_PHASE2.md#opportunities-why-this-direction) (market, niche, differentiator) in plain language.

---

### 3. AI Claim (attestable assertion)

**Current:** “The AI answered like this, and it is signed + timestamped.”

**Goal:** “The AI made **assertion X** under **conditions Y**, and this is **provable**.”

Sign a structured **claim** plus context instead of raw text:

```json
{
  "claim": "This contract clause is GDPR-compliant",
  "confidence": 0.82,
  "reasoning_hash": "...",
  "model": "gpt-4.1",
  "policy_version": "gdpr-2024-05"
}
```

| Aspect | Benefit |
|--------|---------|
| **Signed content** | Claim + context (model, policy version) |
| **Confidence** | Explicit uncertainty for legal/compliance |
| **Policy version** | Audit trail: which rules were in effect |

---

### 4. Key and trust model

**Current:** Effectively one key for signing.

**Goal:** Explicit **key and trust model** — PKI for AI.

| Element | Description |
|---------|-------------|
| **key_id** | In every record; link to public key |
| **Public key registry** | JSON or JWKS |
| **Metadata** | purpose, algorithm, valid_from / valid_to |
| **Later** | Key rotation, key provenance, domain-specific keys (legal, medical, financial) |

**PKI / CA options** — signing keys can be issued and managed by established PKI:

| Solution | Use case |
|----------|----------|
| **Smallstep (step-ca)** | Lightweight CA; ACME, OIDC; short-lived certs; DevOps-friendly |
| **EJBCA** | Enterprise PKI; CA/RA/VA; Common Criteria; regulated industries |
| **HashiCorp Vault** | Transit engine: sign hashes via API (keys never leave Vault); PKI engine for cert issuance; secrets for API keys, DB, TSA creds |

**HashiCorp Vault in practice** — Backend calls Vault Transit to sign the response hash instead of using a local key file. Keys stay in Vault; signing is an API call. Vault can also issue X.509 certs (PKI engine) and store secrets (OpenAI key, TSA config). Common in enterprises; fits alongside or instead of HSM for software-based key protection.

→ Aletheia signing certificates issued by Smallstep or EJBCA; or signing via Vault Transit / HSM; full chain of trust, revocation, lifecycle.

See: [Trust model & eIDAS](TRUST_MODEL.md).

---

### 5. Trusted time, not just a timestamp

**Current:** TSA = technical “time of signing.”

**Goal:** **Proof of moment in history** — trusted time.

| Mechanism | Purpose |
|-----------|---------|
| **Multi-TSA** | 2–3 independent TSAs for redundancy and trust |
| **Public anchors** | Bitcoin block hash, Ethereum calldata, RFC 9162 Roughtime (optional) |

Outcome: “This AI response existed **before** event X” — legally and scientifically strong.

See: [Timestamping](TIMESTAMPING.md).

---

### 6. AI vs AI verification (future)

One AI answers; another AI **verifies** the assertion. Both answers are signed and timestamped.

| Role | Example |
|------|---------|
| **AI-1** | “This code is secure.” |
| **AI-2** | “I confirm the assertion is correct.” |

→ **Multi-agent trust** — closer to scientific verification.

---

## Production readiness: Cloud & HSM

**Cloud:** Stack (Docker, Spring Boot, Next.js) is cloud-ready. Recommended: AWS (ECS/EKS, RDS), GCP (Cloud Run, Cloud SQL), or Azure (Container Apps, PostgreSQL).

**HSM:** Current = file-based `ai.key`. Production = signing via HSM (AWS KMS, CloudHSM, Azure Key Vault HSM, Google Cloud HSM). Abstract `SignatureService` — one impl for file, another for HSM/KMS. “Enterprise: switch from file to HSM in one config change.”

---

## Investor pitch

> **Aletheia AI — Trust Infrastructure for AI**
>
> We turn AI outputs into cryptographically verifiable evidence: signed, RFC 3161 timestamped, verifiable offline.
>
> **Problem:** Impossible to prove what an AI said, when, and in what context.
>
> **Solution:** Signed AI claims with trusted timestamps; verifiable without our server.
>
> **Market:** Legal tech, compliance, fintech, regulators (EU AI Act).
>
> **Status:** PoC complete; RFC 3161; eIDAS-compatible architecture; path to HSM and qualified TSA.
>
> **Next:** Evidence Package, offline verification, pilot in legal/compliance.

---

*Part of Aletheia AI docs. See [README](../../README.md), [PLAN](PLAN.md), and [doc index](../README.md) for implementation details and where to start.*
