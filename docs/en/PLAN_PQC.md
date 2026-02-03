# Aletheia AI — PQC Manifest: Post-Quantum Cryptography (Out of Scope / PoC)

**Hybrid cryptographic attestation for AI outputs: classical + post-quantum signatures over the same evidence hash.**

This document is an **out-of-scope, enthusiast-driven** plan: adding a second, additive PQC signature layer without breaking the existing trust chain. It positions Aletheia as **quantum-ready** for long-term evidence and regulatory narrative, while keeping the current RSA + RFC 3161 pipeline fully valid and primary.

**Status:** Draft for meditation & PoC · **Related:** [Signing](SIGNING.md) · [Crypto reference](CRYPTO_REFERENCE.md) · [Trust model](TRUST_MODEL.md) · [Vision & roadmap](VISION_AND_ROADMAP.md)

---

## Table of contents

- [Why PQC and why now](#why-pqc-and-why-now)
- [Standards and references](#standards-and-references)
- [Design principles](#design-principles)
- [Simple explanation (for beginners)](#simple-explanation-for-beginners)
- [Deliverables and tasks (LLM-readable)](#deliverables-and-tasks-llm-readable)
- [Frontend: PQC badge and marketing](#frontend-pqc-badge-and-marketing)
- [Backend changes](#backend-changes)
- [Verifier utility changes](#verifier-utility-changes)
- [Deployment (Ansible)](#deployment-ansible)
- [Completion criteria](#completion-criteria)
- [Risks and notes](#risks-and-notes)

---

## Why PQC and why now

| Driver | Description |
|--------|-------------|
| **Long-term validation** | Evidence stored today may need to be verified in 10–20 years. Classical RSA/ECDSA could become vulnerable to large-scale quantum computers; PQC signatures remain secure in that scenario. |
| **Future-proof narrative** | "Aletheia — AI trust infrastructure ready for the quantum era." Strong message for defence, finance, and regulators (EU AI Act, eIDAS, long-term archiving). |
| **Technical excellence** | Hybrid signing (classical + PQC over the same hash) is the recommended migration path; implementing it demonstrates engineering foresight. |
| **No breakage** | PQC is **additive only**. Existing auditors, lawyers, and verifiers continue to rely on the classical signature and TSA; PQC is an extra layer for future safety. |

**Positioning:** *"Hybrid cryptographic attestation: classical + post-quantum signatures over the same evidence hash."* — Not "we rewrote everything in PQC," but "we added a quantum-resistant layer so evidence remains verifiable for decades."

---

## Standards and references

**Full list with significance and official links:** [Legal & Regulatory References — Post-Quantum Cryptography](../legal/README.md#post-quantum-cryptography-nist-and-related). That section indexes NIST FIPS 203, 204, 205, the NIST PQC project, FAQs, migration guidance, ETSI Technical Reports, and CRYSTALS-Dilithium, with short explanations of each document’s contents and why it matters for Aletheia.

| Resource | Description | URL / Reference |
|----------|-------------|-----------------|
| **NIST PQC standardization** | NIST selected ML-DSA (Dilithium), ML-KEM (Kyber), SLH-DSA (SPHINCS+), etc. | [NIST PQC Project](https://csrc.nist.gov/projects/post-quantum-cryptography) |
| **FIPS 204** | ML-DSA (Module-Lattice-Based Digital Signature Standard) — standard for Dilithium-based signatures | [FIPS 204 (2024)](https://csrc.nist.gov/pubs/fips/204/final) |
| **FIPS 205** | SLH-DSA (Stateless Hash-Based Digital Signature Standard) | [FIPS 205 (2024)](https://csrc.nist.gov/pubs/fips/205/final) |
| **CRYSTALS-Dilithium** | Original algorithm name; NIST standardised as ML-DSA | [CRYSTALS-Dilithium](https://pq-crystals.org/dilithium/) |
| **Bouncy Castle PQC** | Java implementation of PQC algorithms (Dilithium, Kyber, etc.) | [Bouncy Castle PQC](https://www.bouncycastle.org/pqc_jce.html) · Maven: `org.bouncycastle:bcpkix-jdk18on` + PQC provider |
| **ETSI / long-term archiving** | ETSI TS 101 733, PAdES; PQC for long-term signature validity | [ETSI](https://www.etsi.org/) |
| **NIST PQC migration** | NIST guidance on hybrid and migration | [NIST PQC FAQ](https://csrc.nist.gov/projects/post-quantum-cryptography/faqs) |

**Algorithm choice for PoC:** **ML-DSA (Dilithium)** — NIST standard, well-documented, available in Bouncy Castle; Falcon can be added later if needed.

---

## Design principles

1. **Dual-signing over the same hash**  
   `canonical.bin → SHA-256 → [RSA signature] (classical) + [ML-DSA signature] (PQC) → RFC 3161 timestamp` (timestamp remains over classical flow; TSA PQC is out of scope until TSAs adopt PQC).

2. **Evidence Package remains backward-compatible**  
   New files: `signature_pqc.sig`, `pqc_public_key.pem`, `pqc_algorithm.json`. Existing `signature.sig`, `public_key.pem`, etc. unchanged. Verifiers that do not support PQC ignore the new files and still verify classically.

3. **SignatureService abstraction**  
   Introduce (or extend) an abstraction so that multiple signers can coexist: classical (current RSA) and PQC (ML-DSA). Same hash in, two signatures out.

4. **Optional at runtime**  
   PQC signing is enabled only if a PQC key is configured (e.g. `ai.aletheia.signing.pqc-key-path`). If not set, behaviour is identical to today (no PQC files in Evidence Package).

---

## Simple explanation (for beginners)

**What is an ML-DSA (Dilithium) key pair?**

A **key pair** is like two matching parts: a **secret key** (only you have it) and a **public key** (you can share it with everyone).

- The **secret key** is like your personal key to a safe. You use it to **sign**: “I wrote this.”
- The **public key** is like a copy of the lock. Anyone with the public key can **verify** that a signature was made by the owner of the secret key, but they cannot forge your signature.

**ML-DSA (Dilithium)** is the name of the “rules” used to create these keys. These rules are designed so that even future quantum computers cannot forge the signature. **Bouncy Castle** is a Java library that knows how to generate such key pairs and how to sign and verify with them.

**What is the difference between `ai_pqc.key` and `ai_pqc_public.pem`?**

| File | What it is | Who may see it | Used for |
|------|------------|----------------|----------|
| **ai_pqc.key** | The **secret** (private) key | Only you / your server. Never share. | **Signing** — creating the PQC signature. |
| **ai_pqc_public.pem** | The **public** key | Anyone (e.g. in the Evidence Package, or in docs). | **Verifying** — checking that a signature was made with your secret key. |

If someone steals the secret key, they can sign in your name. The public key is safe to share; it only allows others to verify your signatures.

---

## Configuration and local run

Backend loads configuration from the project root `.env` (see [README Quick start](../../README.md#quick-start) and [.env.example](../../.env.example)).

| Variable | Description | Example |
|----------|-------------|---------|
| `AI_ALETHEIA_PQC_ENABLED` | Set to `true` to enable ML-DSA signing alongside classical RSA. | `true` |
| `AI_ALETHEIA_PQC_KEY_PATH` | Path to ML-DSA private key file. Relative to backend working directory when running `mvn spring-boot:run` from `backend/`. | `./ai_pqc.key` |

**Generate PQC key pair (one-time):**

```bash
cd backend
mvn -q compile exec:java -Dexec.mainClass="ai.aletheia.crypto.PqcKeyGen" -Dexec.args="."
```

This creates `ai_pqc.key` and `ai_pqc_public.pem` in `backend/`. Do not commit them. In `.env` at project root set:

- `AI_ALETHEIA_PQC_ENABLED=true`
- `AI_ALETHEIA_PQC_KEY_PATH=./ai_pqc.key`

Then run the backend from `backend/` so the path resolves. Frontend needs `NEXT_PUBLIC_API_URL=http://localhost:8080` in `frontend/.env.local` to call the API.

---

## Marketing copy and badge asset (PQC.7)

**Tagline (README, Vision doc):**  
*PQC layer: hybrid classical + post-quantum (ML-DSA) signatures for long-term evidence verification.*

**Quantum-Resistant** — Evidence is also signed with a post-quantum (ML-DSA) signature so it remains verifiable even if classical cryptography is broken by future quantum computers.

**Badge asset:** Reusable SVG at [frontend/public/pqc-badge.svg](../../frontend/public/pqc-badge.svg): rounded rectangle, teal accent, atom icon, text “Quantum-Resistant”. Use on the landing page, verify page, README, or docs. The SVG includes `role="img"` and `aria-label="Quantum-Resistant: post-quantum signature for long-term verification"` for accessibility.

---

## Deliverables and tasks (LLM-readable)

---

### PQC.1 — Backend: Add Bouncy Castle PQC dependency and PQC key configuration

| Field | Value |
|-------|--------|
| **Est.** | 0.5 h |
| **Description** | Add Bouncy Castle PQC (or bcprov with PQC support) and configuration properties for PQC key path. |

**Coding prompt (LLM-readable):**

- In `backend/pom.xml`, add dependency for Bouncy Castle PQC. Use the same Bouncy Castle version family as existing `bcpkix-jdk18on` / `bcprov-jdk18on`. If your project uses `bouncy-castle-bcpkix` and `bouncy-castle-bcprov`, add the PQC provider module (e.g. `bcutil` and the PQC-specific JAR, or the all-in-one that includes Dilithium). Check [Bouncy Castle PQC](https://www.bouncycastle.org/pqc_jce.html) for correct artifact names (e.g. `org.bouncycastle:bcpqc-jdk18on` or similar). Ensure the JCE provider registers algorithms such as `Dilithium` or `MLDSA`.
- In `application.yml` or `application.properties`, add optional properties: `ai.aletheia.signing.pqc-enabled` (boolean, default `false`) and `ai.aletheia.signing.pqc-key-path` (string, path to PEM or key file for ML-DSA private key). Document in `README` or `docs/en/SIGNING.md` that PQC is optional and keys can be generated with OpenSSL or Bouncy Castle key generation utilities.
- Do not remove or change existing `ai.aletheia.signing.key-path` (classical RSA). Both keys can coexist.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Build | Maven | `mvn compile` succeeds with new dependency. |
| Config | Application | Application starts with `pqc-enabled=false` and no PQC key path; no PQC signing attempted. |

**Note:** `pqc-enabled` and `pqc-key-path` are **Spring Boot application properties** (used at runtime). Do not pass them to Maven: use `mvn compile` for build; set `AI_ALETHEIA_PQC_ENABLED` and `AI_ALETHEIA_PQC_KEY_PATH` when running the app (e.g. `mvn spring-boot:run` or `java -jar`).

---

### PQC.2 — Backend: Generate and store ML-DSA (Dilithium) key pair

| Field | Value |
|-------|--------|
| **Est.** | 1 h |
| **Description** | Provide a way to generate an ML-DSA key pair and store it next to `ai.key` (or in a configured directory). |

**Coding prompt (LLM-readable):**

- Add a utility class or main method (e.g. `ai.aletheia.crypto.PqcKeyGen` or script under `scripts/`) that generates an ML-DSA (Dilithium) key pair using Bouncy Castle PQC. Use parameter set equivalent to Dilithium3 (NIST level 3) for a balance of security and performance; document the chosen parameter set in `docs/en/PLAN_PQC.md` or in code comments (e.g. `DilithiumParameterSpec.DILITHIUM_3` or the OID/name used by your BC version).
- Output: private key and public key in PEM or format readable by Bouncy Castle. Save private key to a file (e.g. `ai_pqc.key`) and public key to `ai_pqc_public.pem`. Document in README: "To enable PQC signing, generate keys with `...` and set `ai.aletheia.signing.pqc-key-path` to the private key file."
- Do not commit private keys to the repository. Add `*_pqc*.key`, `ai_pqc*.pem` (private) to `.gitignore` if not already covered.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | Key generation | Running the utility produces a private and public key file; key format is loadable by Bouncy Castle. |

---

### PQC.3 — Backend: Introduce PqcSignatureService and dual-signing pipeline

| Field | Value |
|-------|--------|
| **Est.** | 2–3 h |
| **Description** | Implement a PQC signature service that signs the same hash as the classical path; integrate into the pipeline so that both classical and PQC signatures are produced when PQC is enabled. |

**Coding prompt (LLM-readable):**

- Create interface `PqcSignatureService` (or extend a generic `SignatureProvider` interface) with methods: `byte[] sign(byte[] hashBytes)` and `boolean verify(byte[] hashBytes, byte[] signatureBytes)`, and `String getPublicKeyPem()` (or equivalent for PQC public key export). Implement `PqcSignatureServiceImpl` using Bouncy Castle ML-DSA (Dilithium): load private key from `ai.aletheia.signing.pqc-key-path` at startup (only if `pqc-enabled` is true); sign the 32-byte SHA-256 hash (ML-DSA signs a message; treat the hash as the message, or use the hash as input to the signing process as per BC API). Export public key as PEM or raw for inclusion in Evidence Package.
- In the pipeline where `SignatureService.sign(hashBytes)` is called (e.g. in `AuditRecordService` or the controller that builds the signed response), after obtaining the classical signature: if PQC is enabled and `PqcSignatureService` is available, call `pqcSignatureService.sign(hashBytes)` with the **same** hash bytes. Store both signatures: existing `signature` (classical) and new `signaturePqc` (byte[] or Base64 string) in the entity (add column `signature_pqc` to `ai_response` or equivalent). Use a Flyway/Liquibase migration: add column `signature_pqc` (TEXT or BLOB) nullable.
- Ensure the **same** canonical bytes and hash are used for both classical and PQC signing. Do not change the order of operations: canonicalize → hash → classical sign → PQC sign (optional) → timestamp (over classical signature as today).
- If PQC signing fails (e.g. key not loaded), log a warning and continue without PQC; do not fail the whole request. Response still returns classical signature and id; `signaturePqc` may be null.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Unit | PqcSignatureService | Given 32-byte hash, sign returns non-empty bytes; verify(sameHash, signature) returns true. |
| Integration | Pipeline | With PQC enabled, saved entity has both `signature` and `signature_pqc` set. With PQC disabled, `signature_pqc` is null. |

---

### PQC.4 — Backend: Extend Evidence Package with PQC artifacts

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Add to the Evidence Package: `signature_pqc.sig`, `pqc_public_key.pem`, and `pqc_algorithm.json` (algorithm name and parameter set). |

**Coding prompt (LLM-readable):**

- In `EvidencePackageServiceImpl` (and interface `EvidencePackageService`), extend `buildPackage` to accept optional parameters: `byte[] signaturePqcBytes`, `String pqcPublicKeyPem`, `String pqcAlgorithmName` (e.g. `"ML-DSA"` or `"Dilithium3"`). Add overloaded method or add these parameters to the existing method with null allowed. When non-null:
  - Add to the package map: `signature_pqc.sig` → Base64-encoded (or raw) PQC signature bytes.
  - Add: `pqc_public_key.pem` → UTF-8 bytes of the PQC public key PEM string.
  - Add: `pqc_algorithm.json` → JSON object with keys `"algorithm"` (e.g. `"ML-DSA"`), `"parameter_set"` (e.g. `"Dilithium3"`), `"standard"` (e.g. `"FIPS 204"`). Single line or pretty-printed is fine.
- When `signaturePqcBytes` is null, do **not** add these entries to the package (backward compatibility: old verifiers and older .aep files have no PQC files).
- In the caller (e.g. where `buildPackage` is invoked after saving the response), pass the PQC signature and public key from the entity and from `PqcSignatureService.getPublicKeyPem()` when PQC was used. Read `signature_pqc` and PQC public key from the service when building the package for GET /api/ai/evidence/:id.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Unit/Integration | Evidence Package | When PQC is enabled, .aep contains `signature_pqc.sig`, `pqc_public_key.pem`, `pqc_algorithm.json`. When disabled, these files are absent. |
| Doc | Format | Document in PLAN_PQC or Evidence Package spec the names and formats of the new files. |

**Evidence Package PQC files (PQC.4):**

| File | Content |
|------|---------|
| `signature_pqc.sig` | Base64-encoded ML-DSA signature over the same 32-byte SHA-256 hash as the classical signature. |
| `pqc_public_key.pem` | PEM of the ML-DSA (Dilithium) public key (UTF-8). |
| `pqc_algorithm.json` | JSON: `algorithm` (e.g. `"ML-DSA (Dilithium3)"`), `parameter_set` (`"Dilithium3"`), `standard` (`"FIPS 204"`). |

When PQC is disabled or the record has no `signature_pqc`, these files are **not** added to the .aep (backward compatibility).

**How to get PQC files in your .aep (required for testing)**

PQC files are added only when the **record was saved with PQC enabled**. You must:

1. **Generate a PQC key pair** (once):
   ```bash
   cd backend && mvn -q exec:java -Dexec.mainClass="ai.aletheia.crypto.PqcKeyGen" -Dexec.args="."
   ```
   This creates `ai_pqc.key` and `ai_pqc_public.pem` in the current directory.

2. **Start the backend with PQC enabled** when creating the response:
   ```bash
   cd backend
   export AI_ALETHEIA_PQC_ENABLED=true
   export AI_ALETHEIA_PQC_KEY_PATH=/absolute/path/to/ai_pqc.key
   mvn spring-boot:run
   ```
   (Or set `ai.aletheia.signing.pqc-enabled=true` and `ai.aletheia.signing.pqc-key-path` in `application.properties` or env.)

3. **Create a response** (e.g. send a prompt via the frontend or POST to `/api/ai/ask`, or use `/api/audit/demo`). The saved record will have `signature_pqc` and `pqc_public_key_pem` stored.

4. **Download the Evidence Package** (GET `/api/ai/evidence/:id` or “Download evidence” on the frontend). The .aep will contain `signature_pqc.sig`, `pqc_public_key.pem`, and `pqc_algorithm.json`. The PQC public key is now stored in the database when the record is saved, so the package includes PQC even if you later run the backend without PQC enabled.

**How to verify that an .aep package uses PQC**

The .aep file is a ZIP archive. To check that it includes PQC:

1. **List contents** (any of these):
   ```bash
   unzip -l aletheia-evidence-123.aep
   ```
   or
   ```bash
   jar tf aletheia-evidence-123.aep
   ```
   Look for: `signature_pqc.sig`, `pqc_public_key.pem`, `pqc_algorithm.json`. If all three are present, the package was built with PQC enabled.

2. **One-liner** (exit 0 only if all three PQC files exist):
   ```bash
   unzip -l aletheia-evidence-123.aep | grep -q signature_pqc.sig && \
   unzip -l aletheia-evidence-123.aep | grep -q pqc_public_key.pem && \
   unzip -l aletheia-evidence-123.aep | grep -q pqc_algorithm.json && echo "PQC present"
   ```

3. **Extract and inspect**:
   ```bash
   unzip aletheia-evidence-123.aep -d /tmp/aep
   ls -la /tmp/aep/signature_pqc.sig /tmp/aep/pqc_public_key.pem /tmp/aep/pqc_algorithm.json
   cat /tmp/aep/pqc_algorithm.json   # e.g. {"algorithm":"ML-DSA (Dilithium3)","parameter_set":"Dilithium3","standard":"FIPS 204"}
   ```

4. **Frontend**: On the verify page (`/verify?id=<id>`), if the response has a PQC signature, the **Quantum-Resistant** badge is shown. Use **Preview package** to see the list of files including the PQC entries.

The offline verifier JAR (PQC.8) now also verifies the PQC signature when `signature_pqc.sig` and `pqc_public_key.pem` are present, and reports "PQC signature: valid", "PQC signature: INVALID", or "PQC signature: not present". The result includes `pqcValid` (Boolean) for scripted use.

---

### PQC.5 — Backend: Expose PQC status and signature in API (GET /api/ai/verify/:id)

| Field | Value |
|-------|--------|
| **Est.** | 0.5 h |
| **Description** | Add to the verify response DTO optional fields: `signaturePqc` (Base64 or null), `pqcVerified` (boolean or null), `pqcAlgorithm` (string). |

**Coding prompt (LLM-readable):**

- In `AiVerifyResponse` (or equivalent DTO for GET /api/ai/verify/:id), add optional fields: `signaturePqc` (String, Base64, null if not present), `pqcAlgorithm` (String, e.g. `"ML-DSA (Dilithium3)"`, null if not present). Optionally add `pqcVerified` (Boolean) if you verify PQC on the server when building the response; otherwise the verifier does it offline.
- Populate these from the stored entity (signature_pqc column, and algorithm from config or constant). Ensure the frontend can show "PQC signature present" and "Quantum-Resistant" badge when `signaturePqc != null`.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Integration | GET /api/ai/verify/:id | When response has PQC, JSON includes signaturePqc and pqcAlgorithm. When not, fields are null or omitted. |

---

## Frontend: PQC badge and marketing

### PQC.6 — UI: Quantum-Resistant badge and short copy

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | On the verify page and optionally on the landing/developers page, show a clear, elegant "Quantum-Resistant" / "PQC Verified" indicator when the response includes a PQC signature. |

**Coding prompt (LLM-readable):**

- On the verify page (`frontend/app/verify/page.tsx` or component that displays verification result), when the API returns `signaturePqc` (or `pqcAlgorithm`) non-null, display a **PQC badge**:
  - **Text:** "Quantum-Resistant" or "PQC Verified" (choose one and use consistently). Subtext optional: "Post-quantum signature included for long-term verification."
  - **Visual:** Use a small, elegant badge or pill: e.g. shield + atom icon (⚛️) or a minimal "PQC" label. Prefer a single colour (e.g. teal or blue) and clear typography; avoid noisy gradients. Ensure it is accessible (contrast, aria-label).
- Add a **tooltip or short explanation** on hover/focus: "This response includes a post-quantum (ML-DSA) signature in addition to the classical signature, so it remains verifiable even in a future with large-scale quantum computers."
- Optional: on the main landing or "For Developers" page, add one line: "Hybrid attestation: classical + post-quantum signatures for long-term evidence." with a small PQC badge or icon. Link to `docs/en/PLAN_PQC.md` or a short `/pqc` or `/trust#pqc` section.
- **Assets:** Prefer inline SVG or a simple emoji (⚛️) plus text to avoid new image dependencies; if you add an SVG, keep it minimal (e.g. atom symbol or shield with "PQC").

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | Verify page with PQC | Badge "Quantum-Resistant" or "PQC Verified" visible when signaturePqc is present. |
| Manual | Verify page without PQC | No PQC badge when signaturePqc is null. |
| A11y | Badge | Tooltip and aria-label present; contrast sufficient. |

---

### PQC.7 — Marketing copy and logo/badge asset (optional)

| Field | Value |
|-------|--------|
| **Est.** | 0.5 h |
| **Description** | One-line tagline and optional reusable badge asset for use on landing, README, or docs. |

**Coding prompt (LLM-readable):**

- Add to `README.md` or Vision doc one sentence: "Optional PQC layer: hybrid classical + post-quantum (ML-DSA) signatures for long-term evidence verification." Link to `docs/en/PLAN_PQC.md`.
- Optional: add a small SVG or PNG badge (e.g. "Quantum-Resistant" or "PQC Ready") under `frontend/public/` or `docs/static/` for use on the website or in docs. Design: cool, elegant, straightforward — e.g. shield + ⚛️ or text "PQC" in a rounded rectangle. Do not overload with colours; one accent colour is enough.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | README / Vision | PQC sentence and link present. |
| Optional | Badge asset | Badge exists and is used on verify page or landing. |

---

## Verifier utility changes

### PQC.8 — Verifier: Read and verify PQC signature from Evidence Package ✅

| Field | Value |
|-------|--------|
| **Est.** | 2–3 h |
| **Status** | Implemented: `EvidenceVerifierImpl` verifies ML-DSA when PQC files present; `VerificationResult.pqcValid`; report line "PQC signature: valid/invalid/not present". |
| **Description** | In the offline verifier (JAR), when an .aep contains `signature_pqc.sig` and `pqc_public_key.pem`, verify the PQC signature over the same hash and report result in the verification report. |

**Coding prompt (LLM-readable):**

- In `EvidenceVerifierImpl` (or equivalent in `backend/src/main/java/ai/aletheia/verifier/`), after verifying the classical signature and TSA:
  - Check for presence of files `signature_pqc.sig`, `pqc_public_key.pem` (and optionally `pqc_algorithm.json`) in the unpacked Evidence Package. Use the same constants as in `EvidencePackageServiceImpl` (e.g. `SIGNATURE_PQC_SIG`, `PQC_PUBLIC_KEY_PEM`, `PQC_ALGORITHM_JSON`); define these in the evidence package constants if not already.
- If `signature_pqc.sig` is present: read the hash from `hash.sha256` (same as for classical verification). Load the PQC public key from `pqc_public_key.pem` using Bouncy Castle PQC. Verify the PQC signature bytes (decode Base64 if stored as Base64) against the hash (as message). Use the same algorithm as backend (ML-DSA / Dilithium). Add to the verification report a line: "PQC signature: valid" or "PQC signature: invalid" or "PQC signature: not present."
- If PQC files are absent, report "PQC signature: not present" and do not fail the overall verification (classical path is sufficient). Overall result: VALID if classical (and TSA) verification passes; PQC is informational. Optionally extend `VerificationResult` to include a `pqcValid` (Boolean) or `pqcStatus` (String) field for UI or scripted use.
- Ensure the verifier JAR includes Bouncy Castle PQC on the classpath (same as backend). Update `VerifierMain` or build so that the standalone JAR has the PQC provider available.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Unit/Integration | Verifier with .aep containing PQC | Report includes PQC signature status; when valid, pqcValid is true. |
| Unit/Integration | Verifier with .aep without PQC | No failure; report shows "PQC signature: not present". |
| Manual | JAR | `java -jar aletheia-verifier.jar file.aep` prints PQC line when applicable. |

---

## Deployment (Ansible)

### PQC.9 — Ansible: Optional PQC key and env

| Field | Value |
|-------|--------|
| **Est.** | 0.5 h |
| **Description** | When deploying with Ansible, support optional PQC key path and `pqc-enabled` flag so that production can enable PQC without code change. |

**Coding prompt (LLM-readable):**

- In `deploy/ansible/templates/.env.j2` (or the template that sets backend env vars), add optional variables: `ALETHEIA_PQC_ENABLED` (true/false) and `ALETHEIA_PQC_KEY_PATH` (path to PQC private key file on the server). Document in `deploy/ansible/README.md` that these are optional; if not set, backend behaves as before (no PQC). Ensure the backend reads these from the environment (e.g. `ai.aletheia.signing.pqc-enabled` from `ALETHEIA_PQC_ENABLED`) so that the same application.yml works and Ansible only injects env.
- If the playbook copies keys (e.g. `ai.key`), add an optional task: when a PQC key file is provided (e.g. from a vault or secure copy), copy it to the target path and set `ALETHEIA_PQC_KEY_PATH` to that path. Mark the task as optional and document that PQC key must be generated separately (see PLAN_PQC or backend README).

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | Ansible README | PQC env vars and optional key deployment documented. |
| Manual | Deploy with PQC disabled | Backend starts; no PQC. With PQC enabled and key path set, backend produces PQC signatures. |

---

## Completion criteria

| # | Criterion | Status |
|---|-----------|--------|
| 1 | Bouncy Castle PQC dependency added; PQC config (key path, enabled) in place | [ ] |
| 2 | ML-DSA key generation utility and key files (private/public) documented | [ ] |
| 3 | PqcSignatureService implemented; dual-signing in pipeline; signature_pqc stored in DB | [ ] |
| 4 | Evidence Package includes signature_pqc.sig, pqc_public_key.pem, pqc_algorithm.json when PQC enabled | [ ] |
| 5 | GET /api/ai/verify/:id exposes signaturePqc and pqcAlgorithm when present | [ ] |
| 6 | Verify page shows "Quantum-Resistant" / "PQC Verified" badge when PQC signature present | [ ] |
| 7 | Offline verifier reads and verifies PQC signature; report includes PQC status | [ ] |
| 8 | Ansible (or deployment) supports optional PQC key and env | [ ] |

---

## Risks and notes

| Risk | Mitigation |
|------|------------|
| Bouncy Castle PQC API changes | Pin dependency version; document exact artifact and version in PLAN_PQC. |
| PQC key size / performance | Dilithium3 is acceptable for PoC; measure sign time and document. If needed, offer Dilithium2 for lighter use. |
| Verifier JAR size | PQC provider may increase JAR size; document and accept for PoC. |
| Regulatory claims | Use wording "quantum-resistant" / "post-quantum" and "for long-term verification"; do not claim certification. |

---

## References

- [Signing](SIGNING.md) — current RSA signing and key path.
- [Crypto reference](CRYPTO_REFERENCE.md) — algorithms and keys.
- [Trust model](TRUST_MODEL.md) — who attests what.
- [Vision & roadmap](VISION_AND_ROADMAP.md) — product direction.
- [NIST PQC](https://csrc.nist.gov/projects/post-quantum-cryptography) · [FIPS 204 (ML-DSA)](https://csrc.nist.gov/pubs/fips/204/final) · [Bouncy Castle PQC](https://www.bouncycastle.org/pqc_jce.html).

**Translations:** [RU](../ru/PLAN_PQC.md) · [ET](../et/PLAN_PQC.md)
