# Killer demo script (legal/compliance)

One end-to-end scenario: operator asks AI a compliance question → backend returns signed + timestamped response → operator exports Evidence Package → auditor runs offline verifier. **Target: ≤5 minutes.**

**Related:** [Plan Phase 2](en/PLAN_PHASE2.md) (Step DP2.3) · [Offline verification](../README.md#offline-verification-dp22) · [Evidence Package](../README.md#evidence-package)

---

## Prerequisites

| Requirement | Notes |
|-------------|--------|
| **Backend running** | `cd backend && mvn spring-boot:run` (or `java -jar target/backend-*.jar`). API: http://localhost:8080 |
| **Signing key** | Set `AI_ALETHEIA_SIGNING_KEY_PATH` in `.env` (e.g. path to `ai.key`). Required for signature and Evidence Package. |
| **TSA** | Default: real (DigiCert). For offline demo use mock: `AI_ALETHEIA_TSA_MODE=mock` in `.env`. |
| **OpenAI API key** | Set `OPENAI_API_KEY` in `.env` if using POST /api/ai/ask (LLM). For LLM-free test use POST /api/audit/demo with fixed text. |
| **Frontend (optional)** | `cd frontend && npm run dev` → http://localhost:3000. Set `NEXT_PUBLIC_API_URL=http://localhost:8080` in `frontend/.env.local`. |

---

## Step 1 — Operator asks the AI (compliance question)

**Option A — Frontend**

1. Open http://localhost:3000.
2. In the prompt field, paste the example below (or your own clause + question).
3. Click **Send**. Wait for the response; note the **response id** (e.g. from the verify link or UI).

**Option B — curl (no frontend)**

```bash
curl -X POST http://localhost:8080/api/ai/ask \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Does the following clause comply with GDPR?\n\nThe data controller shall process personal data only for specified, explicit and legitimate purposes (purpose limitation)."}'
```

From the JSON response, note **`id`** (e.g. `1`). You need it for Step 3.

**Example prompt (copy-paste):**

```
Does the following clause comply with GDPR?

The data controller shall process personal data only for specified, explicit and legitimate purposes (purpose limitation).
```

**Compliance claim (DP2.4):** If the prompt mentions GDPR, compliance, clause, AI Act, legal, or similar, the backend infers a compliance claim and adds `claim`, `confidence`, and `policy_version` to the Evidence Package metadata; the offline verifier will display them when reporting VALID. **Note:** `claim` is taken from the model response (first sentence); `confidence` is a fixed 0.85 and `policy_version` is inferred from the prompt only (e.g. "gdpr" → gdpr-2024). They do not come from the model output. See [Plan Phase 2 — DP2.4](en/PLAN_PHASE2.md#step-dp24--minimal-ai-claim-optional-for-demo) for how these fields are set and how they could be derived from the model in the future.

**LLM-free alternative (no OpenAI):** use audit demo with fixed text; you still get a signed response and an id:

```bash
curl -X POST http://localhost:8080/api/audit/demo \
  -H "Content-Type: application/json" \
  -d '{"text":"The data controller shall process personal data only for specified, explicit and legitimate purposes."}'
```

Note the **`id`** from the response.

---

## Step 2 — Backend returns signed + timestamped response

The response includes:

- **`id`** — store this for exporting the Evidence Package.
- **`response`** — AI (or audit) text.
- **`responseHash`** — SHA-256 of canonical form.
- **`signature`** — Base64 RSA signature (null if key not configured).
- **`tsaToken`** — Base64 RFC 3161 timestamp token (null if TSA not used).

If `signature` and `tsaToken` are present, the Evidence Package will be fully verifiable.

---

## Step 3 — Operator exports Evidence Package

Replace `ID` with the response id from Step 1 (e.g. `1`).

**curl (save as .aep):**

```bash
curl -o evidence-ID.aep "http://localhost:8080/api/ai/evidence/ID"
# Example:
curl -o evidence-1.aep "http://localhost:8080/api/ai/evidence/1"
```

**Frontend:** Click **Download evidence** after the response; the file will download (e.g. `aletheia-evidence-1.aep`).

The file is a ZIP containing the seven components: `response.txt`, `canonical.bin`, `hash.sha256`, `signature.sig`, `timestamp.tsr`, `metadata.json`, `public_key.pem`.

---

## Step 4 — Auditor receives the .aep

Transfer the `.aep` file to the auditor (e.g. email, USB, shared folder). The auditor does **not** need access to the Aletheia backend or the internet for verification.

---

## Step 5 — Auditor runs offline verifier

On a machine with **no** Aletheia backend (and no network to it), run one of:

**Standalone JAR (Java 21+ only):**

```bash
java -jar backend/target/aletheia-verifier.jar /path/to/evidence-1.aep
```

**Or script (from repo root):**

```bash
./scripts/verify-evidence-java.sh /path/to/evidence-1.aep
# Or OpenSSL-only (no Java):
./scripts/verify-evidence.sh /path/to/evidence-1.aep
```

**Expected output (VALID):**

```
hash: OK
signature: OK
timestamp: 2026-01-01T12:00:00Z
VALID
```

Exit code **0** = VALID. Exit code **1** = INVALID (e.g. tampered or missing file).

---

## Time target

| Step | Typical time |
|------|----------------|
| 1 — Ask | ~30 s (frontend or curl) |
| 2 — Response | Included in step 1 |
| 3 — Export .aep | ~5 s (curl or button) |
| 4 — Transfer | Depends on channel (e.g. local copy: instant) |
| 5 — Verify | ~5 s (run verifier) |
| **Total** | **≤5 min** (excluding transfer if remote) |

---

## Quick copy-paste sequence (operator + auditor on same machine)

**Operator (backend running, id = 1):**

```bash
curl -o evidence-1.aep "http://localhost:8080/api/ai/evidence/1"
```

**Auditor (any machine with repo or JAR):**

```bash
java -jar backend/target/aletheia-verifier.jar evidence-1.aep
```

If you see `VALID` and a timestamp, the killer demo is complete.
