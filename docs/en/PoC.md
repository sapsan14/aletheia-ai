# Proof of Concept: Aletheia AI Architecture

This document describes the proposed PoC architecture for a system of verifiable AI responses with cryptographic signing and timestamps.

---

## Architecture

```
┌────────────┐
│  Frontend  │  (Next.js / React)
│            │
│ prompt →   │
└─────┬──────┘
      │
      ▼
┌──────────────────────┐
│ Backend API           │
│ (Node / Java / Python)│
│                       │
│ 1. send prompt to LLM │
│ 2. receive response   │
│ 3. canonicalize text  │
│ 4. hash (SHA-256)     │
│ 5. sign hash          │
│ 6. timestamp          │
│ 7. store in DB        │
└─────┬────────────────┘
      │
      ▼
┌──────────────┐
│ PostgreSQL   │
│              │
│ prompt       │
│ response     │
│ hash         │
│ signature    │
│ timestamp    │
│ metadata     │
└──────────────┘
```

**Summary:** this is already a strong PoC.

---

## Cryptography: what to use in practice

### Signing

#### OpenSSL — best start

**Why:**

- minimal
- standard
- understandable to legal
- easy to verify

**Example:**

```bash
openssl dgst -sha256 -sign ai.key response.txt > signature.bin
```

- ✔ ideal for PoC  
- ✔ easy to explain  
- ✔ no infrastructure pain  
- ➡️ **best choice #1**

#### Smallstep — second stage

Use if you want to show “PKI thinking”:

- `step-ca`
- X.509 cert
- automation

**But:** for PoC this is already “level 2”. Start with OpenSSL, then you can replace the backend module.

---

### Timestamping (TSA)

Options by increasing complexity:

| Level | Option | Description |
|-------|--------|-------------|
| 🟢 | **Local RFC 3161 TSA** | Ideal for PoC |

#### 🟢 Option 1 — local RFC 3161 TSA (ideal for PoC)

You can run a local TSA.

**Tools:**

- OpenSSL TSA
- OpenTSA
- simple RFC 3161 server

**Pros:**

- fully offline
- ideal to demonstrate the idea
- RFC 3161 standard

**Cons:**

- trust = your server (but this is a PoC!)

➡️ **Ideal option to start.**

#### 🟡 Option 2 — public TSA

E.g.:

- DigiCert TSA
- GlobalSign TSA
- FreeTSA (limited)

**Issues:** limits, rate limits, sometimes paid. Can leave for “future work”.

#### 🔴 eIDAS Qualified TSA

- ❌ not now  
- ❌ not for PoC  
- ❌ expensive  

You can fairly state in the architecture: *“Architecture compatible with eIDAS Qualified TSA”* — and that is correct.

---

## Database

**PostgreSQL** — yes, 100%.

**Table structure:**

```sql
ai_response (
  id,
  prompt,
  response,
  response_hash,
  signature,
  tsa_token,
  llm_model,
  created_at
)
```

**Optionally add:**

- `request_id`
- `temperature`
- `system_prompt`
- `version`

That is already a full audit trail.

---

## LLM — keep it simple

- **One LLM** at the start.

**Choice:**

- OpenAI (free credits)
- Gemini (often generous free tier)
- Mistral (open/free)

Start with one.

**Important:** log `model name`, `version`, `parameters` — it will be very useful for audit later.

---

## Backend — what fits you

Given a PKI / enterprise profile:

### Java + Spring Boot

**Pros:**

- you are “at home”
- crypto is native (BouncyCastle)
- Timestamp support

**Con:** slightly heavier start.

### Node.js

**Pros:**

- fast
- OpenSSL via shell
- easy to run

**Con:** crypto less “canonical” for enterprise.

### Python

Good for prototype, less “enterprise”.

---

**Recommendation:** Java. Given depth in PKI it makes sense to use BouncyCastle.

---

## Backend module structure

```
backend/
├── llm/
│   └── LLMClient.java
├── crypto/
│   ├── HashService
│   ├── SignatureService
│   └── TimestampService
├── audit/
│   └── AuditRecordService
├── api/
│   └── AiController
└── db/
```

This is architecture, not a demo.

---

## Frontend (minimum)

- **prompt** field
- **Send** button
- AI response
- status block:
  - ✔ signed  
  - ✔ timestamped  
  - ✔ verifiable  

And a **“Verify this response”** link where you can:

- recompute hash
- verify signature
- show TSA token

➡️ that gives a strong effect.

---

## Dagster?

➡️ **Not needed now.**

Dagster is pipeline orchestration. It makes sense to add later if you have:

- batch analysis
- scheduled AI verification
- nightly audit

For PoC — overkill.

---

## Recommended PoC stack

Minimal but powerful:

| Layer | Technology |
|-------|------------|
| Frontend | Next.js |
| Backend | Java Spring Boot |
| Crypto | OpenSSL + BouncyCastle |
| Signing | local RSA/ECDSA key |
| Timestamp | RFC 3161 TSA (default: DigiCert) |
| DB | PostgreSQL |
| LLM | one (Gemini / OpenAI / Mistral) |

---

## Deployment

**Chosen approach:** Full stack (Docker + Ansible + GitHub Actions) for automated deployment to a target VM (e.g. `ssh ubuntu@193.40.157.132`).

- **Docker:** Backend and frontend containerized; docker-compose with PostgreSQL.
- **Ansible:** VM setup (Docker install), .env template, `docker-compose up`.
- **GitHub Actions:** On push to main: tests → build → deploy via SSH/Ansible.

**Alternatives:** Ansible-only (no containers), script-only (bash over SSH), Docker Compose only. See [plan.md](plan.md) Step 8 for detailed tasks and LLM-readable prompts.

---

## Why this PoC is strong

You are not building “AI that tells truth”, but **AI whose answers can be proven**.

That is a fundamental difference — and the direction EU regulation is actually moving.

---

## Next steps

1. Architecture diagram (Mermaid)
2. README PoC as GitHub project
3. Example RFC 3161 timestamp flow
4. Example BouncyCastle timestamp verification
5. Roadmap: PoC → demo → EU-style product
