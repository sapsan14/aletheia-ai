# Aletheia AI — Implementation Plan (English)

Step-by-step plan for building the PoC: verifiable AI responses with cryptographic signing and RFC 3161 timestamps. Each task includes **coding prompt** instructions for LLM or developer implementation.

**Stack (from PoC):** Next.js, Java Spring Boot, PostgreSQL, OpenSSL/BouncyCastle, one LLM (OpenAI/Gemini/Mistral), local RFC 3161 TSA.

---

## Step 1 — Project setup and skeleton

**Goal:** Repository structure, backend and frontend projects, DB schema stub.  
**Est. total:** 6–8 h

### Task 1.1 — Repository and docs

| Field | Value |
|-------|--------|
| **Est.** | 1 h |
| **Description** | Initialize repo (if needed), add README with project goal and how to run backend/frontend/DB. Add Mermaid architecture diagram matching PoC (frontend → backend API → PostgreSQL). |

**Coding prompt (LLM-readable):**
- Create or update README.md: project name "Aletheia AI", one-line goal "Verifiable AI responses with signing and timestamps". Sections: Prerequisites, Run backend, Run frontend, Run PostgreSQL (or Docker). Add link to docs/PoC.
- Add docs/architecture.md or section in README with one Mermaid diagram: flow from Frontend (Next.js) to Backend API (steps: send prompt → LLM → canonicalize → hash → sign → timestamp → store) to PostgreSQL. Use rectangles and arrows; label each step. Do not implement code; only documentation.

---

### Task 1.2 — Backend skeleton (Java Spring Boot)

| Field | Value |
|-------|--------|
| **Est.** | 2 h |
| **Description** | Create Spring Boot app with package layout: `llm`, `crypto`, `audit`, `api`, `db`. No business logic yet; one health/readiness endpoint. |

**Coding prompt (LLM-readable):**
- Create a new Spring Boot 3.x project (Maven or Gradle). Java 21+. Base package e.g. `ai.aletheia`.
- Create empty package structure: `ai.aletheia.llm`, `ai.aletheia.crypto`, `ai.aletheia.audit`, `ai.aletheia.api`, `ai.aletheia.db` (or equivalent). No classes required yet except one REST controller under `api` that exposes GET /actuator/health or GET /health returning 200 and a simple JSON like `{"status":"UP"}`.
- Add dependency: spring-boot-starter-web, spring-boot-starter-data-jpa (for later), and BouncyCastle (bcpkix, bcprov). Do not implement LLM, crypto, or DB logic in this task.

---

### Task 1.3 — Frontend skeleton (Next.js)

| Field | Value |
|-------|--------|
| **Est.** | 1.5 h |
| **Description** | Next.js app with one page: placeholder for prompt input and response area. No API calls yet. |

**Coding prompt (LLM-readable):**
- Create Next.js app (App Router). Single page at "/" with: a text area or input labeled "Prompt", a button "Send", and a div for "Response". Do not connect to backend; button can be disabled or show "Coming soon". Use minimal styling (Tailwind or plain CSS). Ensure app runs with `npm run dev` and shows the layout. No environment variables for API URL required yet.

---

### Task 1.4 — PostgreSQL and schema

| Field | Value |
|-------|--------|
| **Est.** | 2 h |
| **Description** | Define and apply schema for `ai_response` table. Optional: Docker Compose for local Postgres. |

**Coding prompt (LLM-readable):**
- Define SQL schema for table `ai_response` with columns: id (UUID or bigserial PK), prompt (text), response (text), response_hash (varchar 64 or bytea), signature (bytea or text/base64), tsa_token (bytea or text), llm_model (varchar), created_at (timestamptz). Optionally add request_id, temperature, system_prompt, version as in PoC.
- Provide either: (a) a Flyway/Liquibase migration script that creates this table, or (b) a standalone SQL file that can be run manually. If using Docker: add docker-compose.yml with one service `postgres`, image postgres:15-alpine, expose 5432, optional env POSTGRES_DB=aletheia. Document in README how to start DB and run migrations.

---

## Step 2 — Crypto layer

**Goal:** Canonicalization, hash, sign, timestamp. All logic behind services; no REST yet.  
**Est. total:** 10–14 h

### Task 2.1 — Text canonicalization

| Field | Value |
|-------|--------|
| **Est.** | 2 h |
| **Description** | Implement deterministic canonical form for LLM response text so the same logical content always yields the same bytes before hashing. |

**Coding prompt (LLM-readable):**
- Implement a canonicalization function (e.g. in a utility or `CanonicalizationService`). Input: string (LLM response). Output: byte array (UTF-8) that is deterministic. Rules: (1) Normalize Unicode to NFC. (2) Normalize line endings to \\n. (3) Trim trailing whitespace from each line and collapse multiple consecutive blank lines to one. (4) No trailing newline at end of file, or exactly one — choose one rule and document it. Use UTF-8 for final bytes. Write unit tests: same input string must always produce same byte array; two strings that differ only by \\r\\n vs \\n must produce same result. Language: Java (or same as backend). Do not integrate with REST or DB in this task.

---

### Task 2.2 — HashService (SHA-256)

| Field | Value |
|-------|--------|
| **Est.** | 1 h |
| **Description** | Service that hashes canonical bytes with SHA-256 and returns hex string (or fixed format). |

**Coding prompt (LLM-readable):**
- Create HashService (interface + impl). Method: hash(canonicalBytes: byte[]) → String (64-char hex). Use standard MessageDigest (SHA-256); BouncyCastle not required for hashing. No external I/O. Unit test: hash known string (e.g. "hello\\n" after canonicalization) and assert against known SHA-256 hex value. Integrate with canonicalization: input string → canonical bytes → hash; document that callers must pass already canonical bytes or a single method that accepts string and does canonicalize+hash.

---

### Task 2.3 — SignatureService (BouncyCastle, RSA or ECDSA)

| Field | Value |
|-------|--------|
| **Est.** | 3 h |
| **Description** | Sign the hash (or canonical bytes) with a private key; provide verification that takes hash + signature and returns valid/invalid. |

**Coding prompt (LLM-readable):**
- Create SignatureService. Load private key from file or env (e.g. PEM path or key material). Use BouncyCastle for RSA or ECDSA. Methods: (1) sign(hashHex: String or hashBytes: byte[]) → byte[] or Base64 string. (2) verify(hashHex, signature) → boolean. Key format: PEM, either generated by OpenSSL or by code. Document how to generate key (e.g. openssl genpkey -algorithm RSA -out ai.key). Unit tests: sign a known hash, verify returns true; tampered signature returns false. Do not call TSA in this task. PoC: one key is enough; key rotation is out of scope.

---

### Task 2.4 — TimestampService (RFC 3161 local TSA)

| Field | Value |
|-------|--------|
| **Est.** | 4–5 h |
| **Description** | Request RFC 3161 timestamp from a TSA (local server or stub). Input: hash of the response (or of the signature). Output: TSA response token (bytes or Base64). |

**Coding prompt (LLM-readable):**
- Create TimestampService that requests an RFC 3161 timestamp from a configurable TSA URL (e.g. http://localhost:3180). Input: digest (SHA-256 hash bytes of the data to be timestamped — e.g. the response hash or the signature, choose one and document). Output: timestamp token as byte[] or Base64 String. Use BouncyCastle's TSP (Time-Stamp Protocol) APIs: generate request with the digest, send HTTP POST to TSA, parse response and extract token. Handle connection errors and invalid response (return Optional or throw; document). If no TSA server is available: (a) provide a stub/mock that returns a fixed byte sequence for tests, or (b) document how to run a simple RFC 3161 server (e.g. OpenTSA or similar) in README. Unit test: with mock TSA or real local TSA, request timestamp for a known hash and assert token is non-empty and parseable. Do not implement the TSA server itself in this task unless it is a minimal stub.

---

## Step 3 — LLM integration

**Goal:** One client to call a single LLM provider; return plain text and model metadata.  
**Est. total:** 4–6 h

### Task 3.1 — LLM client (one provider)

| Field | Value |
|-------|--------|
| **Est.** | 3 h |
| **Description** | Integrate one LLM API: OpenAI, or Gemini, or Mistral. Accept prompt string; return response text and model identifier. |

**Coding prompt (LLM-readable):**
- Create LLMClient (interface + impl). Interface: method like complete(prompt: String) → LLMResult where LLMResult contains: responseText: String, modelId: String (e.g. "gpt-4", "gemini-pro"). Implementation: choose one provider (OpenAI, Gemini, or Mistral). Use official SDK or HTTP client. Read API key from environment variable (e.g. OPENAI_API_KEY or GEMINI_API_KEY). Do not canonicalize or hash in this task; only call LLM and return text + model. Handle errors (rate limit, timeout, invalid key) with clear exceptions or Result type. Unit test: use mock or real API with a short prompt; assert response is non-empty and modelId is set. Document in README which env vars are required.

---

### Task 3.2 — Log model name, version, parameters

| Field | Value |
|-------|--------|
| **Est.** | 1 h |
| **Description** | Ensure every LLM call stores model identifier and optional parameters for audit. |

**Coding prompt (LLM-readable):**
- Extend LLMResult or the caller to capture: model name/id, and if available from the API: model version or parameters (e.g. temperature). These fields will be stored in ai_response (llm_model and optionally version/parameters in metadata). Add a single place (e.g. AuditRecord or request context) where these values are set from LLMResult. No new API; only ensure data is available for the audit layer. Optional: log to slf4j for each request (model, prompt length, response length) for debugging.

---

## Step 4 — Audit and persistence

**Goal:** Save prompt, response, hash, signature, tsa_token, llm_model, created_at to PostgreSQL.  
**Est. total:** 4–6 h

### Task 4.1 — ai_response entity and repository

| Field | Value |
|-------|--------|
| **Est.** | 2 h |
| **Description** | JPA entity (or equivalent) and repository for table ai_response. |

**Coding prompt (LLM-readable):**
- Create JPA entity AiResponse (or same name as table) with fields: id (UUID or Long), prompt, response, responseHash, signature (byte[] or Base64 string), tsaToken (byte[] or string), llmModel, createdAt. Map to table ai_response. Use Spring Data JPA repository: AiResponseRepository with save and findById. Add Flyway/Liquibase migration if not done in Step 1. Unit test: save one entity, findById, assert all fields match. Use H2 or Testcontainers for tests if needed.

---

### Task 4.2 — AuditRecordService (orchestrate save)

| Field | Value |
|-------|--------|
| **Est.** | 2–3 h |
| **Description** | Service that receives prompt, response, hash, signature, tsa_token, llm_model (and optional metadata) and persists one row. |

**Coding prompt (LLM-readable):**
- Create AuditRecordService with method save(request: AuditRecordRequest): AuditRecordId. AuditRecordRequest contains: prompt, response, responseHash, signature, tsaToken, llmModel, and optionally requestId, temperature, systemPrompt, version. Service generates id and created_at, maps to AiResponse entity, calls repository.save. Return saved id. No LLM or crypto in this service; only persistence. Unit test: save one record, load by id, assert all fields. This service will be called from the API layer after hash/sign/timestamp are computed.

---

## Step 5 — Backend API

**Goal:** REST endpoint: accept prompt, call LLM, canonicalize, hash, sign, timestamp, store, return response and verification info.  
**Est. total:** 6–8 h

### Task 5.1 — POST /api/ai/ask (or /chat)

| Field | Value |
|-------|--------|
| **Est.** | 4–5 h |
| **Description** | Single endpoint: body { "prompt": "..." }; flow: LLM → canonicalize → hash → sign → timestamp → store; response: { "response", "responseHash", "signature", "tsaToken", "id", "model" }. |

**Coding prompt (LLM-readable):**
- Create REST controller (e.g. AiController). POST /api/ai/ask (or /api/chat). Request body: JSON with field "prompt" (string). Flow: (1) Call LLMClient.complete(prompt) → get responseText and modelId. (2) Canonicalize responseText → canonical bytes. (3) HashService.hash(canonicalBytes) → responseHash. (4) SignatureService.sign(responseHash) → signature. (5) TimestampService.getTimestamp(responseHash bytes or signature) → tsaToken. (6) AuditRecordService.save(prompt, response, responseHash, signature, tsaToken, modelId, ...) → id. (7) Return JSON: response (original or canonical string), responseHash (hex), signature (Base64), tsaToken (Base64), id, model (modelId). Use DTOs for request/response. On LLM or TSA failure return 502 or 503 with clear message. Integration test: call endpoint with a short prompt, assert 200, response body contains response, responseHash, signature, tsaToken, id; assert DB has one row with same id and hash.

---

### Task 5.2 — GET /api/ai/verify/:id (optional but recommended)

| Field | Value |
|-------|--------|
| **Est.** | 2 h |
| **Description** | Return stored record by id for verification page: prompt, response, hash, signature, tsa_token, model, created_at. |

**Coding prompt (LLM-readable):**
- Add GET /api/ai/verify/{id}. Load AiResponse by id from repository. Return JSON: prompt, response, responseHash, signature, tsaToken, llmModel, createdAt. If not found return 404. No verification logic in this endpoint; only data retrieval for the frontend verification page.

---

## Step 6 — Frontend

**Goal:** UI: prompt input, Send, display response and status (signed, timestamped, verifiable); link to verify page.  
**Est. total:** 6–8 h

### Task 6.1 — Prompt input and Send button

| Field | Value |
|-------|--------|
| **Est.** | 2 h |
| **Description** | Connect prompt field and Send button to POST /api/ai/ask; show loading state and errors. |

**Coding prompt (LLM-readable):**
- In Next.js app: on "Send" click, read prompt from input/textarea. POST to backend (e.g. /api/ai/ask), body { prompt }. Use env variable for API base URL (e.g. NEXT_PUBLIC_API_URL). Show loading state (disabled button or spinner) while request in progress. On success store response JSON in state. On 4xx/5xx show error message to user. Do not yet render response content or verification; only ensure data is fetched and error handling works.

---

### Task 6.2 — Display response and status (signed, timestamped, verifiable)

| Field | Value |
|-------|--------|
| **Est.** | 2 h |
| **Description** | Show AI response text and a status block: ✔ signed, ✔ timestamped, ✔ verifiable (based on presence of signature and tsaToken). Link "Verify this response" to /verify?id=... |

**Coding prompt (LLM-readable):**
- After successful POST: display response.response in a dedicated area. Show status block with three checkmarks (or icons): "Signed", "Timestamped", "Verifiable" — show as positive if response.signature and response.tsaToken are non-empty. Add link or button "Verify this response" that navigates to /verify?id={response.id} (or store id in state and pass to verify page). Use the id returned by the API. No verification logic (hash check, signature check) in this task; only display and navigation.

---

### Task 6.3 — Verify page: show hash, signature, TSA token

| Field | Value |
|-------|--------|
| **Est.** | 2–3 h |
| **Description** | Page /verify?id=... that fetches GET /api/ai/verify/:id and displays prompt, response, hash, signature (e.g. Base64 snippet), TSA token (snippet), model, date. Optional: client-side hash check (re-canonicalize response and compute SHA-256, compare to stored hash). |

**Coding prompt (LLM-readable):**
- Create page route /verify. Read id from query (e.g. ?id=...). Fetch GET /api/ai/verify/{id}. Display: prompt, response text, responseHash (full or truncated), signature (Base64, first/last 20 chars + "..."), tsaToken (same), llmModel, createdAt. Optional: add "Verify hash" button that (1) canonicalizes displayed response in browser (same rules as backend), (2) computes SHA-256 (use Web Crypto API or a small library), (3) compares to stored responseHash and shows "Match" or "Mismatch". This gives the "wow" effect of proving the response was not altered. Do not implement full signature verification in browser unless you add a small crypto lib; displaying data is enough for PoC.

---

## Step 7 — Verification flow and docs

**Goal:** Optional backend verification endpoint; README and run instructions.  
**Est. total:** 2–4 h

### Task 7.1 — Backend: verify hash and signature (optional)

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | GET /api/ai/verify/:id could optionally return verification result: hashMatch (recompute hash from stored response and compare), signatureValid (SignatureService.verify). |

**Coding prompt (LLM-readable):**
- Optionally extend GET /api/ai/verify/:id response (or add GET /api/ai/verify/:id/check) to include: hashMatch (boolean: recompute hash from stored response using same canonicalization, compare to stored responseHash), signatureValid (boolean: call SignatureService.verify(storedHash, storedSignature)). Return JSON: { ...record, hashMatch, signatureValid }. Frontend can then show "Hash match: yes/no", "Signature valid: yes/no". Implement only if time permits; otherwise leave for future.

---

### Task 7.2 — README and run instructions

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | README: how to generate key, set env vars, run PostgreSQL, run backend, run frontend, run local TSA (if any). List required env: API keys, DB URL, TSA URL, key path. |

**Coding prompt (LLM-readable):**
- Update README with: (1) Prerequisites: Java 21+, Node 18+, PostgreSQL 15+, optional Docker. (2) Environment variables: OPENAI_API_KEY or GEMINI_API_KEY (or Mistral), DB URL, TSA URL, path to private key for signing. (3) How to generate key: openssl genpkey -algorithm RSA -out ai.key. (4) How to run DB: docker-compose up -d or local install, run migrations. (5) How to run backend: ./mvnw spring-boot:run or equivalent, with env set. (6) How to run frontend: npm run dev, set NEXT_PUBLIC_API_URL. (7) Optional: how to run a local RFC 3161 TSA for development. Keep sections short; link to docs/PoC for architecture.

---

## Summary — Estimated total

| Step | Est. hours |
|------|------------|
| 1 — Project setup and skeleton | 6–8 |
| 2 — Crypto layer | 10–14 |
| 3 — LLM integration | 4–6 |
| 4 — Audit and persistence | 4–6 |
| 5 — Backend API | 6–8 |
| 6 — Frontend | 6–8 |
| 7 — Verification and docs | 2–4 |
| **Total** | **38–54** |

---

## Testing (by step)

Detailed test scope and acceptance criteria for each step. Run backend tests with `mvn test` (from `backend/`); frontend with `npm test` when available.

| Step | Test type | What to test | Acceptance criteria |
|------|-----------|--------------|---------------------|
| **1.1** | Manual / doc | README and diagram | README has Prerequisites, Run backend/frontend/DB; Mermaid diagram renders; link to docs/PoC works. |
| **1.2** | Unit | Health endpoint | `GET /health` returns 200 and JSON `{"status":"UP"}`. Use `@WebMvcTest(HealthController.class)` + MockMvc. |
| **1.2** | Integration | Context load | `@SpringBootTest` context loads (no missing beans). |
| **1.3** | Manual | Frontend | App runs with `npm run dev`; page shows Prompt, Send, Response placeholders. |
| **1.4** | Manual / migration | DB schema | Liquibase/Flyway runs; table `ai_response` exists with expected columns. |
| **2.1** | Unit | Canonicalization | Same string → same bytes; `\r\n` vs `\n` → same result. |
| **2.2** | Unit | HashService | Known input → known SHA-256 hex (64 chars). |
| **2.3** | Unit | SignatureService | sign(hash) then verify(hash, signature) → true; tampered signature → false. |
| **2.4** | Unit | TimestampService | Mock TSA: request returns non-empty token; invalid response handled. |
| **3.1** | Unit | LLMClient | Mock: complete(prompt) returns non-empty text and modelId. |
| **3.2** | — | Data flow | LLMResult / audit context contains model id and optional params. |
| **4.1** | Unit | Repository | save(entity); findById(id); assert fields. Use H2 or Testcontainers. |
| **4.2** | Unit | AuditRecordService | save(request) → id; load by id, assert all fields. |
| **5.1** | Integration | POST /api/ai/ask | 200; body has response, responseHash, signature, tsaToken, id, model; DB has one row. |
| **5.2** | Unit / Integration | GET /api/ai/verify/:id | 200 with record; 404 for unknown id. |
| **6.1** | Manual / E2E | Frontend → backend | Send prompt → response and status displayed; errors shown on failure. |
| **6.2** | Manual | UI | Signed, timestamped, verifiable badges; "Verify" link with id. |
| **6.3** | Manual | Verify page | Fetches by id; shows hash, signature, TSA token; optional client-side hash check. |
| **7.1** | Unit | Verification | hashMatch and signatureValid in response when implemented. |
| **7.2** | Manual | README | All run instructions and env vars documented. |

**Backend test command:** From `backend/`: `./mvnw test` or `mvn test`. Ensure H2 is available for tests (default Spring Boot test profile uses in-memory DB).

---

**Coding prompt convention:** Each task's "Coding prompt (LLM-readable)" block is intended to be copy-pasted or fed to an LLM/developer to implement that task. It specifies: what to build, inputs/outputs, tech stack, tests, and what not to do. Adjust estimates and scope per your team.
