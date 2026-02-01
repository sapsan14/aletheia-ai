# Aletheia AI — Vision and Roadmap (English)

Product vision and next big steps: from cryptographically verifiable AI responses to **AI attestation** and **trust infrastructure**. This document describes the strategic direction beyond the current PoC and demo.

**Related:** [PoC (architecture)](PoC.md) · [Implementation plan](plan.md) · [Trust model](TRUST_MODEL.md)

---

## Table of contents

- [Current state: what we have](#current-state-what-we-have)
- [Positioning for the outside world](#positioning-for-the-outside-world)
- [Big step 1: AI Claim (attestable assertion)](#big-step-1-ai-claim-attestable-assertion)
- [Big step 2: Verification outside our system](#big-step-2-verification-outside-our-system)
- [Big step 3: Key and trust model](#big-step-3-key-and-trust-model)
- [Big step 4: Trusted time, not just a timestamp](#big-step-4-trusted-time-not-just-a-timestamp)
- [Big step 5: AI Evidence Package](#big-step-5-ai-evidence-package)
- [Big step 6: AI vs AI verification](#big-step-6-ai-vs-ai-verification)
- [Big step 7: Choose a killer domain](#big-step-7-choose-a-killer-domain)
- [Summary and next actions](#summary-and-next-actions)

---

## Current state: what we have

| Area | Status |
|------|--------|
| **PoC** | ✅ Complete |
| **Demo** | ✅ Working end-to-end |
| **Crypto pipeline** | ✅ Canonicalize → hash → sign → timestamp (RFC 3161) |
| **RFC 3161 TSA** | ✅ Implemented (rare in AI projects) |
| **Trust chain** | ✅ Signed response + TSA token; verifiable on our backend |

We have closed the hardest part: a **cryptographically strict trust chain for AI**. Many startups fail exactly here. The next level is to turn “signed AI response” into **docusable, audit-grade AI attestation** and **verification without our server**.

---

## Positioning for the outside world

We can already state:

> **Aletheia AI provides cryptographically verifiable AI statements with trusted timestamps — suitable for audit, legal, and compliance use.**

This positions the project beyond “chatbot security” and toward **AI attestation** and **trust infrastructure**.

---

## Big step 1: AI Claim (attestable assertion)

**Current:** “The AI answered like this, and it is signed + timestamped.”

**Next:** “The AI made **assertion X** under **conditions Y**, and this is **provable**.”

### Claim object (formalized response)

Sign not just raw text, but a structured **claim** plus context:

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
| **Outcome** | Legal / compliance / audit-grade attestation |

→ We move from “chatbot security” to **AI attestation**.

---

## Big step 2: Verification outside our system

**Current:** Verification = our backend.

**Next:** Verification **without** our system.

### Offline verification

| Deliverable | Purpose |
|-------------|---------|
| **CLI tool** | `aletheia verify response.json` — run locally |
| **Pure JS verifier** | Browser or Node — no backend call |
| **Export bundle** | `response.json`, `signature.pem`, `tsa.tsr`, `public_key.pem` |

Auditors, courts, or enterprises can verify **hash**, **signature**, and **timestamp** without our server. This is when the project becomes **trust infrastructure**.

---

## Big step 3: Key and trust model

**Current:** Effectively one key for signing.

**Next:** Explicit **key and trust model**.

### Minimum

| Element | Description |
|---------|-------------|
| **key_id** | In every record; link to public key |
| **Public key registry** | JSON or JWKS |
| **Metadata** | `purpose`, `algorithm`, `valid_from` / `valid_to` |

### Later

- **Key rotation** without breaking old evidence
- **Key provenance** — who issued the key and why
- **Domain-specific keys** — e.g. legal, medical, financial AI

→ This is **PKI for AI**, not just “a signature.”

See also: [Trust model & eIDAS](TRUST_MODEL.md).

---

## Big step 4: Trusted time, not just a timestamp

**Current:** TSA = technical “time of signing.”

**Next:** **Proof of moment in history** — trusted time.

### Ideas

| Mechanism | Purpose |
|-----------|---------|
| **Multi-TSA** | 2–3 independent TSAs for redundancy and trust |
| **Anchor to public sources** | Bitcoin block hash, Ethereum calldata, or RFC 9162 Roughtime (optional) |

Outcome: “This AI response existed **before** event X” — legally and scientifically strong.

See also: [Timestamping](TIMESTAMPING.md).

---

## Big step 5: AI Evidence Package

Bundle everything into one **evidence object**:

```
Aletheia Evidence Package (.aep)
├── response.txt
├── canonical.bin
├── hash.sha256
├── signature.sig
├── timestamp.tsr
├── metadata.json
└── public_key.pem
```

### Use cases

- Courts
- Audits
- Regulators
- Corporate investigations
- Scientific publications

→ We define a **new evidence format for AI**.

---

## Big step 6: AI vs AI verification

**Idea:** One AI answers; another AI **verifies** the assertion. Both answers are signed and timestamped.

| Role | Example |
|------|---------|
| **AI-1** | “This code is secure.” |
| **AI-2** | “I confirm the assertion is correct.” |

→ **Multi-agent trust** — closer to scientific verification.

---

## Big step 7: Choose a killer domain

We are ready to act as **architect**, not only engineer. Focus on **one** primary domain first.

### Strong candidate niches

| Domain | Why |
|--------|-----|
| **Legal / compliance AI** | Contracts, clauses, regulatory checks |
| **Medical AI opinions** | Second opinion, documentation |
| **AI-generated contracts** | Signing and attestation of AI-drafted terms |
| **Scientific AI results** | Reproducibility, citations |
| **AI journalism / fact claims** | Verifiable sources and claims |

Important: not “chat,” but **assertions with consequences**.

---

## Summary and next actions

### Already done

- ✅ PoC
- ✅ Demo
- ✅ Full crypto pipeline (canonicalize, hash, sign, timestamp)
- ✅ RFC 3161 (rare in AI projects)

### Next big steps (in order of impact)

1. **AI Claim** — structured assertion + context instead of plain text
2. **Offline / third-party verification** — CLI + JS verifier, export bundle
3. **Trust & key model** — key_id, registry, rotation, provenance
4. **Strong timestamp anchoring** — multi-TSA, public anchors
5. **Evidence package** — .aep format for courts and audits
6. **Killer domain** — pick one (e.g. legal, medical, contracts)

### Optional follow-ups

- One-page **product vision**
- **Killer demo** for investors
- **EU-style roadmap** (PoC → Pilot → Regulation)

---

*This document is part of the Aletheia AI docs. See [README](../../README.md) and [plan](plan.md) for implementation details.*
