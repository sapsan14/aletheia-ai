# Aletheia AI — Plan Phase 2 (Killer demo)

This document describes **Phase 2** of the roadmap: the killer demo in legal/compliance, Evidence Package, offline verification, and a single reproducible scenario. It includes a clear description of **opportunities**, development steps with **LLM-readable coding prompts**, and testing criteria.

**Related:** [Vision & roadmap](VISION_AND_ROADMAP.md) (Phase 2 = Killer demo) · [Implementation plan](PLAN.md) (PoC steps) · [Trust model](TRUST_MODEL.md)

---

## Table of contents

- [Opportunities: why this direction](#opportunities-why-this-direction)
- [Phase 2 goal and scope](#phase-2-goal-and-scope)
- [Development steps (with LLM-readable prompts)](#development-steps-with-llm-readable-prompts)
- [Implementation status (DP2.1 & DP2.2)](#implementation-status-dp21--dp22)
- [Phase 2 completion criteria](#phase-2-completion-criteria)
- [References](#references)

---

## Opportunities: why this direction

### Market and regulatory context

- **AI Trust / TRiSM market:** ~$2.34B (2024) → ~$7.44B (2030), CAGR ~21.6%. Drivers: regulation, explainability, audit, governance.
- **EU AI Act:** In force Aug 2024; phased to 2026–2027. High-risk systems need traceability, conformity, documentation. Delays in harmonized standards and conformity bodies create demand for tools that provide **proof** for regulators.
- **Legal tech / contract AI:** TermScout (Certify™ AI), AlsoCheck (clause-level “Trust Objects” for GDPR, AI Act, ISO 27001), iCertis (contract risk/compliance). Trend: not only “AI analyzed” but **provable output with time and policy binding** — for courts and auditors.
- **Fintech/regtech:** MAS (Singapore), HKMA, FINRA expect AI model risk management, audit trails, attestation. Signed, timestamped model output fits “attestation of model output” and audit trail.

### Recommended niche: legal / compliance AI

We focus Phase 2 on **legal / compliance AI** (contracts, clauses, EU AI Act, audit) because:

1. **Mandatory demand** — EU AI Act and contract audits require demonstrable evidence; “who said what, when” is a direct fit.
2. **Differentiator** — Existing players offer certification or trust badges; few offer **RFC 3161 + offline verification** without calling their server. Aletheia’s Evidence Package is verifiable by anyone with the public key and TSA chain.
3. **Willingness to pay** — Legal and compliance teams pay for audit-grade evidence; one signed assertion (e.g. “this clause is GDPR-compliant”) is a clear product slice.
4. **One demo, many uses** — A single scenario (signed AI assertion on a clause or policy) serves investors, pilots, and regulators.

### Our differentiator in one sentence

**Aletheia turns AI outputs into cryptographically verifiable, offline-checkable evidence (signed + RFC 3161 timestamped) — so auditors, courts, and enterprises can prove what the AI stated and when, without relying on our backend.**

---

## Phase 2 goal and scope

**Goal:** Deliver one reproducible **killer demo** in legal/compliance: an AI assertion (e.g. clause compliance) that is signed and timestamped, bundled as an Evidence Package, and verifiable offline. Update narrative to “works with MCP / any agent framework.”

**Out of scope for Phase 2:** Full AI Claim schema in production, PKI key registry, multi-TSA, HSM. Minimal AI Claim (claim + confidence + policy_version) is in scope only to support the demo.

**Deliverables:**

| # | Deliverable | Purpose |
|---|-------------|---------|
| 1 | Evidence Package (minimal) | Single request → one .aep (or equivalent) with response/canonical, hash, signature, tsa_token, public_key, metadata. |
| 2 | Offline verifier (CLI or script) | `aletheia verify <path>` runs without our server; outputs valid/invalid + report. |
| 3 | One killer demo scenario | Legal/compliance: e.g. “AI states this clause is GDPR-compliant” → signed + timestamped → auditor verifies offline; reproducible in ≤5 min. |
| 4 | Minimal AI Claim in bundle | Optional structured claim (claim, confidence, policy_version) in signed payload for demo. |
| 5 | Narrative and docs | README block “Works with MCP / OpenClaw / any agent”; link to [Vision](VISION_AND_ROADMAP.md) Phase 2; investor-facing one-liner updated. |

---

## Development steps (with LLM-readable prompts)

### Step DP2.1 — Evidence Package generation

**Goal:** For each signed AI response (or for a dedicated endpoint), produce an Evidence Package: a set of files or a single archive that contains everything needed for offline verification.

**Est.** 4–6 h

#### Task DP2.1.1 — Define Evidence Package format

| Field | Value |
|-------|--------|
| **Est.** | 1 h |
| **Description** | Document and implement the minimal .aep structure: which files, naming, and contents. |

**Coding prompt (LLM-readable):**
- Define Evidence Package format. Minimal set: (1) `response.txt` — raw AI response text. (2) `canonical.bin` or `canonical.txt` — canonical form used for hashing. (3) `hash.sha256` — 64-char hex SHA-256 of canonical bytes. (4) `signature.sig` — Base64 signature. (5) `timestamp.tsr` — Base64 RFC 3161 TSA token. (6) `metadata.json` — JSON with at least: model, created_at (ISO 8601), request_id or response_id if available. (7) `public_key.pem` — PEM of the public key used for signing (so verifier does not depend on our server for key). Document the format in README or docs/en/PLAN_PHASE2.md. No backend change yet; only spec and possibly a Java/TypeScript type or constant names for file names.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | Format spec | All seven components listed; naming and content type (text/binary/JSON) defined. |

**Evidence Package format (.aep) — minimal set**

An Evidence Package is a directory or a `.aep` archive (e.g. ZIP) containing exactly the following seven components. All filenames are fixed.

| # | Filename        | Description                                      | Content type / format                    |
|---|-----------------|--------------------------------------------------|------------------------------------------|
| 1 | `response.txt`  | Raw AI response text as returned to the client  | UTF-8 text                               |
| 2 | `canonical.bin` | Canonical form of the content used for hashing  | Binary (exact bytes fed to SHA-256)      |
| 3 | `hash.sha256`   | SHA-256 of the canonical bytes                  | 64 ASCII hex chars (lowercase or mixed)  |
| 4 | `signature.sig` | Signature over the hash (or canonical bytes)    | Base64-encoded binary                    |
| 5 | `timestamp.tsr` | RFC 3161 TSA token                              | Base64-encoded binary                    |
| 6 | `metadata.json`| Model, timestamp, optional ids                  | JSON (e.g. model, created_at ISO 8601)   |
| 7 | `public_key.pem`| Public key used for signing                     | PEM (so verifier needs no backend)      |

Verification uses: `canonical.bin` → recompute hash and compare with `hash.sha256`; load `public_key.pem` and verify `signature.sig` over the hash; parse `timestamp.tsr` and validate TSA token. `response.txt` and `metadata.json` are for display and audit.

---

#### Task DP2.1.2 — Backend: generate Evidence Package for a response

| Field | Value |
|-------|--------|
| **Est.** | 2–3 h |
| **Description** | Add service or endpoint that, given a stored response id (or the response + signature + tsa_token), writes an Evidence Package to a directory or returns a ZIP/base64 bundle. |

**Coding prompt (LLM-readable):**
- Add an `EvidencePackageService` (or equivalent) that takes: response text, canonical bytes, hash, signature bytes, tsa token bytes, model name, created_at, and public key PEM. It produces the Evidence Package as: (1) a Map or list of (filename, content) entries, or (2) writing files to a given directory, or (3) a single ZIP byte array. Use the format from Task DP2.1.1 (response.txt, canonical.bin, hash.sha256, signature.sig, timestamp.tsr, metadata.json, public_key.pem). For canonical.bin use the exact bytes used when computing the hash. For metadata.json include at least model, created_at (ISO 8601), and optionally response_id. Public key: read from the same key file used for signing, export public part as PEM. If backend does not store canonical bytes, recompute from response using CanonicalizationService. Add unit test: given fixed response + mock signature + mock tsa token, generate package and assert all files present and hash.sha256 matches computed hash.
- Optional: add GET `/api/ai/evidence/:id` that loads the response by id from DB, gathers signature and tsa_token from DB, loads public key, builds Evidence Package, and returns either ZIP (Content-Type application/zip) or JSON with base64-encoded file contents. Document in README.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Unit | EvidencePackageService | Input response + signature + tsa + metadata → output contains all 7 components; hash in package equals hash of canonical bytes. |
| Integration | GET /api/ai/evidence/:id (if implemented) | 200; response is ZIP or JSON with all required fields; unzipped/content matches format. |
| Manual | Generate package for existing response | Unzip or open directory; verify response.txt, hash.sha256, signature.sig, timestamp.tsr, public_key.pem, metadata.json exist. |

---

### Step DP2.2 — Offline verifier (CLI or script)

**Goal:** A small program or script that, given a path to an Evidence Package (directory or .aep file), verifies signature and TSA token without calling Aletheia backend.

**Est.** 4–6 h

#### Task DP2.2.1 — Verifier logic (signature + TSA)

| Field | Value |
|-------|--------|
| **Est.** | 2–3 h |
| **Description** | Implement verification: (1) load canonical bytes and hash from package; (2) load public key from public_key.pem; (3) verify signature over hash (or over canonical bytes, consistent with backend); (4) parse RFC 3161 token from timestamp.tsr and validate (signature verification of TSA token; optionally check certificate chain). Output: valid/invalid and short report (e.g. hash OK, signature OK, timestamp time). |

**Coding prompt (LLM-readable):**
- Implement verifier in the language of choice (Java, Node, or script calling OpenSSL). Input: path to directory or .aep (ZIP). Steps: (1) Read hash.sha256 (64-char hex), canonical.bin or response.txt (and canonicalize if only response), signature.sig (Base64), timestamp.tsr (Base64), public_key.pem. (2) Verify signature: decode signature, load public key, verify that signature is valid for the hash (or for SHA-256 hash of canonical bytes). Use BouncyCastle (Java) or crypto (Node) or OpenSSL CLI. (3) Parse RFC 3161 token from timestamp.tsr: extract TimeStampToken, verify TSA signature, read genTime. (4) Output: VALID or INVALID; if invalid, state which check failed (signature, timestamp, or file missing). Write unit tests: valid package → VALID; tampered signature → INVALID; tampered hash → INVALID. Document verification algorithm in README (what is verified and in what order).
- TSA certificate chain verification: at minimum verify the token is parseable and genTime is present; full chain validation to a root CA is optional for Phase 2 but recommended if time permits.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Unit | Valid package | Output VALID; report includes hash OK, signature OK, timestamp time. |
| Unit | Tampered signature | Output INVALID; message indicates signature failure. |
| Unit | Tampered hash | Output INVALID. |
| Manual | Run on real .aep | Verifier exits 0 for valid package, non-zero for invalid; no backend call. |

---

#### Task DP2.2.2 — CLI entrypoint

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Expose verifier as a CLI command, e.g. `aletheia verify <path-to-aep-or-dir>`. |

**Coding prompt (LLM-readable):**
- Add CLI entrypoint: `aletheia verify <path>` or `npm run verify -- <path>` (Node) or `./scripts/verify.sh <path>` (shell wrapping Java/Node). Argument: path to directory containing Evidence Package files, or path to .aep (ZIP). Call the verifier logic from Task DP2.2.1; print result to stdout (VALID/INVALID + report). Exit code 0 if valid, 1 if invalid or error. Document in README: “Offline verification: run `aletheia verify /path/to/package` (or equivalent); no network required.”
- If backend is Java: consider adding a separate module or main class `VerifierMain` that reads path from args and runs verification; or provide a shell script that uses curl to download package and then runs a small Node/Java verifier. Prefer a single command that works on a folder or .aep file.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | `aletheia verify <path>` | Valid path → exit 0, VALID printed; invalid path or tampered → exit 1, INVALID printed. |
| Doc | README | Section “Offline verification” describes command and that no backend call is needed. |

---

### Step DP2.3 — Killer demo scenario (legal/compliance)

**Goal:** One end-to-end scenario: user asks AI a compliance question (e.g. “Is this clause GDPR-compliant?”); backend returns signed + timestamped response; user exports Evidence Package; auditor runs offline verifier. Reproducible in ≤5 minutes.

**Est.** 2–4 h (mostly narrative and flow; backend/UI may already support it)

#### Task DP2.3.1 — Define and document demo script

| Field | Value |
|-------|--------|
| **Est.** | 1 h |
| **Description** | Write a short, step-by-step script for the killer demo: prompt text, how to get response, how to export Evidence Package, how to run verifier. |

**Coding prompt (LLM-readable):**
- Create a markdown file `docs/DEMO_SCRIPT.md` (or a section in PLAN_PHASE2.md) titled “Killer demo script (legal/compliance)”. Steps: (1) Operator opens frontend (or uses curl to POST /api/ai/ask with a prompt such as “Does the following clause comply with GDPR? [paste clause]”). (2) Backend returns response with signature and tsa_token; store response id. (3) Operator exports Evidence Package: e.g. GET /api/ai/evidence/:id and save as .aep, or use a “Download evidence” button that calls the same. (4) Auditor receives .aep (e.g. by email or USB). (5) Auditor runs `aletheia verify /path/to/package` on a machine with no access to Aletheia backend; sees VALID and timestamp. Total time target: ≤5 minutes. Include example prompt text (one short clause + question). Document any prerequisites (backend running, key and TSA configured).
- No code change required if export and verifier already exist; only document the flow and example prompt.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | DEMO_SCRIPT or section | Steps 1–5 clear; example prompt included; time ≤5 min achievable. |
| Manual | Run through once | Follow script; obtain .aep; run verifier; get VALID. |

**→ Implemented:** [Killer demo script (legal/compliance)](../DEMO_SCRIPT.md) — steps 1–5, example prompt, prerequisites, ≤5 min target.

---

#### Task DP2.3.2 — Optional: “Download evidence” in frontend

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | If not already present, add a way in the UI to download the Evidence Package for the last response (or by response id). |

**Coding prompt (LLM-readable):**
- Add “Download evidence” (or “Export .aep”) button or link on the page that shows the AI response. On click: call GET /api/ai/evidence/:id (or POST with id) and save the response as a file (e.g. `aletheia-evidence-<id>.aep` or `.zip`). Use the current response id from the last /api/ai/ask result. If backend returns ZIP, trigger browser download with Content-Disposition or blob. Document in README: “After asking a question, click ‘Download evidence’ to get the Evidence Package for offline verification.”
- Optional: show a short “Verified” / “Not verified” status on the same page by running a small in-browser or local verifier (e.g. Web Crypto + JS); Phase 2 can rely on CLI verifier only.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | Click “Download evidence” | File downloads; unzip/open yields Evidence Package format; verifier accepts it. |

---

### Step DP2.4 — Minimal AI Claim (optional for demo)

**Goal:** Support a structured “claim” in the signed payload for the demo: e.g. claim text, confidence, policy_version. This aligns with [Vision § AI Claim](VISION_AND_ROADMAP.md#3-ai-claim-attestable-assertion).

**Est.** 2–3 h

#### Task DP2.4.1 — Claim structure in metadata and signed payload

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Extend metadata (or a dedicated claim field) to include claim, confidence, policy_version; ensure the signed content includes this so the verifier can show “what was claimed” and under which policy. |

**Coding prompt (LLM-readable):**
- Define a minimal claim structure: `{ "claim": "string", "confidence": number in [0,1], "policy_version": "string" }`. Either (a) store this in metadata.json and sign the same canonical representation of the full response+metadata, or (b) add a separate claim.json in the Evidence Package and sign a canonical form that includes claim. Prefer (a) for simplicity: metadata.json already exists; add claim, confidence, policy_version to it; ensure the backend signs the same bytes that include this metadata (e.g. sign hash of canonical response + canonical metadata, or append metadata to canonical response before hash). Document: “For legal/compliance demo, metadata includes claim, confidence, policy_version; these are part of the signed payload.”
- Backend: when building Evidence Package, if the AI endpoint was called with a “compliance” mode or a specific prompt template, parse or generate claim/confidence/policy_version and add to metadata. For Phase 2, a simple approach: metadata.model, metadata.created_at, metadata.claim (copy of response or first sentence), metadata.confidence (e.g. 0.85), metadata.policy_version (e.g. “gdpr-2024”). Verifier: when reporting VALID, optionally print claim and policy_version from metadata.json.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Unit | Package with claim in metadata | metadata.json contains claim, confidence, policy_version; signature still verifies. |
| Manual | Verifier output | Can display claim and policy_version from package. |

---

### Step DP2.5 — Narrative and documentation updates

**Goal:** Update README and Vision references so Phase 2 is clearly described and “works with MCP / any agent” is stated.

**Est.** 1–2 h

#### Task DP2.5.1 — README and Vision links

| Field | Value |
|-------|--------|
| **Est.** | 1 h |
| **Description** | Add README section “Evidence Package & offline verification” and “Killer demo (Phase 2)”; add “Works with MCP / OpenClaw / any agent framework” with one diagram or sentence; link to PLAN_PHASE2.md and DEMO_SCRIPT. |

**Coding prompt (LLM-readable):**
- In root README.md add section “Evidence Package & offline verification”: one short paragraph explaining that each signed response can be exported as an Evidence Package (.aep) and verified offline with `aletheia verify <path>`; link to [Plan Phase 2](docs/en/PLAN_PHASE2.md). Add section “Killer demo (Phase 2)”: one paragraph on legal/compliance demo (signed AI assertion on clause/policy); link to [Demo script](docs/DEMO_SCRIPT.md) or the script section in PLAN_PHASE2. Add bullet or block “Works with MCP / OpenClaw / any agent framework”: “Aletheia can sign and timestamp outputs from any agent (e.g. MCP, OpenClaw); verification remains offline.” Link to [Vision](docs/en/VISION_AND_ROADMAP.md) Phase 2 and, if present, [ideas/PKI for AI agents](docs/ru/ideas/PKI_FOR_AI_AGENTS.md).
- In docs/README.md ensure “Documents by topic” has Plan Phase 2 | Killer demo, Evidence Package, offline verifier | [EN](en/PLAN_PHASE2.md) · [RU](ru/PLAN_PHASE2.md) · [ET](et/PLAN_PHASE2.md). Ensure PLAN_PHASE2.md is linked from VISION_AND_ROADMAP.md in the “Killer demo” / Phase 2 section.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | README | Sections Evidence Package, Killer demo, Works with MCP present; links to PLAN_PHASE2 and demo script. |
| Doc | docs/README | PLAN_PHASE2 listed in topic table (EN, RU, ET). |

---

## Implementation status (DP2.1 & DP2.2)

The following tasks are implemented. Use this section to find code and entrypoints.

| Task | Implemented in | How to use / verify |
|------|----------------|---------------------|
| **DP2.1.1** — Evidence Package format | Format table in this doc (above); file names in `EvidencePackageServiceImpl` | See [Evidence Package format](#evidence-package-format-aep--minimal-set) |
| **DP2.1.2** — Backend: generate package | `EvidencePackageService`, `AiEvidenceController` | GET `/api/ai/evidence/:id` → ZIP or `?format=json`; root [README](../../README.md#evidence-package) |
| **DP2.2.1** — Verifier logic | `EvidenceVerifierImpl` (Java), `scripts/verify-evidence.sh` (OpenSSL) | Java: `EvidenceVerifier.verify(path)`; script: `./scripts/verify-evidence.sh <path>`; unit tests: `EvidenceVerifierTest` |
| **DP2.2.2** — CLI entrypoint | `VerifierMain`, `scripts/verify-evidence-java.sh`, standalone JAR | `java -jar backend/target/aletheia-verifier.jar <path>`; or `./scripts/verify-evidence-java.sh <path>`; root [README — Offline verification](../../README.md#offline-verification-dp22) |

**Standalone verifier JAR:** From repo root, run `cd backend && mvn package -Pverifier -DskipTests`; then `java -jar backend/target/aletheia-verifier.jar <path-to-dir-or-.aep>`. No backend server or network required. See also [scripts/README.md](../../scripts/README.md).

---

## Phase 2 completion criteria

Phase 2 is complete when all of the following are true:

| # | Criterion | How to verify |
|---|-----------|----------------|
| 1 | Evidence Package is generated for every signed response (or via dedicated endpoint) | Generate package for a response; unzip/open; all 7 components present. |
| 2 | Offline verifier runs without backend | Run `aletheia verify <path>` on a machine with no Aletheia backend; valid package → VALID. |
| 3 | Killer demo script is documented and reproducible in ≤5 min | Follow docs/DEMO_SCRIPT (or equivalent); complete flow in ≤5 min. |
| 4 | Narrative updated | README and docs reference Phase 2, Evidence Package, “works with MCP/any agent.” |
| 5 | Optional: one pilot feedback or LOI | External validation (legal/compliance team or auditor) has run the demo and provided feedback or letter of intent. |

---

## References

- [Vision & roadmap](VISION_AND_ROADMAP.md) — Phase 2 = Killer demo (legal/compliance); Evidence Package; AI Claim.
- [Implementation plan](PLAN.md) — PoC steps (Steps 1–8); crypto pipeline, /api/ai/ask, deploy.
- [Trust model](TRUST_MODEL.md) — Who attests what; eIDAS mapping.
- [Signing](SIGNING.md), [Timestamping](TIMESTAMPING.md) — Backend signing and TSA.
- [Diagrams](../../diagrams/architecture.md) — Pipeline and trust chain.

**Translations:** [RU](../ru/PLAN_PHASE2.md) · [ET](../et/PLAN_PHASE2.md)
