# Aletheia AI — Plan Phase 5: API Platform & Integrations (2026)

This document describes **Phase 5** of the roadmap: turning Aletheia from a demo product into an **API platform** that can be embedded in corporate pipelines, agent frameworks (MCP, LangChain, LlamaIndex), HR/legal/compliance systems, and internal LLM infrastructures. It is based on completed Phases 1–4 and the chosen strategy after market validation.

**Status:** Draft for review  
**Related:** [Vision & roadmap](VISION_AND_ROADMAP.md) · [Plan Phase 4](PLAN_PHASE4.md) · [NEXT.md](../tmp/NEXT.md)

---

## Table of contents

- [Phase 5 goal and scope](#phase-5-goal-and-scope)
- [Deliverables and development steps](#deliverables-and-development-steps)
- [Out of scope (Phase 6+)](#out-of-scope-phase-6)
- [Completion criteria](#completion-criteria)
- [Timeline](#timeline)
- [Risks and mitigation](#risks-and-mitigation)
- [References](#references)

---

## Phase 5 goal and scope

**Goal:** Turn Aletheia into an **API platform** that can be integrated into:

- Corporate pipelines,
- Agent frameworks (MCP, LangChain, LlamaIndex),
- HR / legal / compliance systems,
- Internal LLM infrastructures of companies.

Phase 5 is the transition from “pilots” to “integrations”.

**In scope:**

- Public API with OpenAPI 3.0 documentation (5.1).
- Sign-only API for signing external LLM responses (5.2).
- SDKs: Python and TypeScript/Node (5.3).
- MCP attestation: document and support agent attestation (5.4).
- Integrations: SIEM export and optional blockchain anchoring (5.5).
- Partner scenarios: 1–2 real integrations (5.6).

**Out of scope for Phase 5:** Full Policy Registry (versioning), Policy Evaluation Pipeline, time-travel verify, human/hybrid review, enterprise dashboards, multi-tenant deployments (deferred to Phase 6+).

---

## Deliverables and development steps

Each deliverable is broken down into tasks with **LLM-readable coding prompts** and acceptance criteria.

---

### 5.1 Public API (OpenAPI 3.0)

**Goal:** A stable public API with documentation so that external developers can integrate without a call.

**Deliverables:**

- OpenAPI 3.0 specification (`openapi.yaml` or `openapi.json`) describing all public endpoints.
- Documented endpoints: `POST /api/ai/ask`, `GET /api/ai/verify/:id`, `GET /api/ai/evidence/:id`, `GET /api/ai/verifier`.
- Request/response examples and error codes.
- “For Developers” page on the landing site linking to the spec and examples.

#### Task P5.1.1 — Generate and maintain OpenAPI 3.0 spec

| Field | Value |
|-------|--------|
| **Est.** | 2–3 h |
| **Description** | Create a canonical OpenAPI 3.0 file for the public API and keep it in sync with the backend. |

**Coding prompt (LLM-readable):**

- Create file `openapi.yaml` (or `openapi.json`) at the repository root or in `docs/api/`. Use OpenAPI 3.0.2 or 3.1.
- Define `info.title`: "Aletheia AI API", `info.version`: "1.0.0" (or match backend version), `info.description`: short paragraph on purpose (verifiable AI responses, signing, evidence).
- Define `servers`: at least one entry with `url` (e.g. `https://api.aletheia.example` or `http://localhost:8080` for dev). Document that base URL is configurable.
- Define paths and operations:
  - **POST /api/ai/ask**  
    - Summary: "Generate AI response, sign it, store it, return id and verification data."  
    - Request body: `application/json` with schema: `prompt` (string, required), optional: `systemPrompt`, `temperature`, `model`. Align with `AiAskRequest` (e.g. `ai/aletheia/api/dto/AiAskRequest.java`).  
    - Responses: `200` — body with `id`, `response`, `responseHash`, `signature`, `tsaToken`, `claim`, `confidence`, `policyVersion`, etc. (align with `AiAskResponse`). `400` — validation error. `500` — server/LLM error.  
    - Include example request and example response in `examples` or in schema.
  - **GET /api/ai/verify/{id}**  
    - Summary: "Get verification record by response id."  
    - Path parameter: `id` (integer or string, required).  
    - Response `200`: JSON with full verify payload (id, prompt, response, responseHash, signature, tsaToken, llmModel, createdAt, claim, confidence, policyVersion, hashMatch, signatureValid, policyCoverage, policyRulesEvaluated if Phase 4 done).  
    - Response `404`: not found.  
    - Include example response.
  - **GET /api/ai/evidence/{id}**  
    - Summary: "Download Evidence Package (.aep) by response id."  
    - Path parameter: `id`.  
    - Response `200`: `application/zip` (or documented media type) with binary body.  
    - Response `404`: not found.  
    - Optional: query param `Accept: application/json` for metadata-only (if supported).
  - **GET /api/ai/verifier**  
    - Summary: "Download offline verifier (JAR)."  
    - Response `200`: `application/java-archive` or `application/octet-stream` with binary body.
- Add a **components** section: reusable schemas for `AiAskRequest`, `AiAskResponse`, `AiVerifyResponse`, and error object (e.g. `code`, `message`). Reference existing DTOs in `backend/src/main/java/ai/aletheia/api/dto/`.
- Define common error response schema: e.g. `{ "code": "VALIDATION_ERROR", "message": "..." }` for 400; `{ "code": "NOT_FOUND", "message": "..." }` for 404; `{ "code": "INTERNAL_ERROR", "message": "..." }` for 500. Document that clients should rely on HTTP status and optionally parse body for details.
- If the backend uses SpringDoc OpenAPI (swagger-ui), ensure the generated spec matches this file or that this file is the source of truth; otherwise document that the spec is maintained by hand and must be updated when API changes.
- Add a short README in `docs/api/` (or next to openapi.yaml): "Public API spec: openapi.yaml. Base URL: see servers in spec. Changelog: link to releases or CHANGELOG."

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | openapi.yaml | Valid OpenAPI 3.x; all four endpoints documented with request/response. |
| Manual | Spec vs backend | Actual request/response from backend conform to described schemas. |

#### Task P5.1.2 — Document error codes and examples

| Field | Value |
|-------|--------|
| **Est.** | 1 h |
| **Description** | Add explicit error codes and request/response examples to the spec or a separate doc. |

**Coding prompt (LLM-readable):**

- In `openapi.yaml` (or `docs/api/README.md`), add a section "Error codes" or extend each operation's `responses`: list possible codes (e.g. `VALIDATION_ERROR`, `NOT_FOUND`, `LLM_UNAVAILABLE`, `TIMESTAMP_ERROR`, `INTERNAL_ERROR`) with HTTP status and short description.
- For POST /api/ai/ask and GET /api/ai/verify/:id, add at least one `example` in the schema or in `content.example`. Example for ask: `{ "prompt": "What is 2+2?", "temperature": 0.2 }`. Example for verify: minimal JSON with id, prompt, response, hashMatch, signatureValid.
- Ensure the backend returns error bodies that match the documented shape (e.g. use `ErrorResponse` or similar DTO in `api/dto/`). If not, add a note in the spec: "Error body format may vary; always check HTTP status first."

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | Error docs | Error codes and at least one request/response example are documented. |

#### Task P5.1.3 — "For Developers" page on landing

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Add a "For Developers" (or "API") page on the frontend that links to the API spec and gives a minimal quickstart. |

**Coding prompt (LLM-readable):**

- Create a new page route, e.g. `frontend/app/developers/page.tsx` or `frontend/app/api-docs/page.tsx`. Title: "For Developers" or "API".
- Content: (1) Short intro: "Integrate Aletheia into your pipeline. Use the API to generate and sign AI responses, or to sign responses from your own LLM." (2) Link to the OpenAPI spec: "OpenAPI 3.0 spec: [openapi.yaml](link)". If the spec is served by the backend (e.g. /v3/api-docs), use that URL; otherwise use a link to the repo or a deployed static URL. (3) Quickstart: 3–4 steps. Step 1: "POST /api/ai/ask with your prompt to get a signed response and id." Step 2: "GET /api/ai/verify/:id to retrieve the full verification record." Step 3: "GET /api/ai/evidence/:id to download the Evidence Package." Step 4: "Use the offline verifier to validate the .aep file." (4) Optional: curl examples for each endpoint (with placeholder base URL and id). (5) Link to Sign-only API (POST /api/sign) once it exists (Task P5.2.1).
- Add a link to this page from the main navigation or footer (e.g. "Developers" or "API").
- Ensure the page is accessible and works on mobile (readable text, no wide code blocks without scroll).

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | Developers page | Page exists, links to OpenAPI spec, contains quickstart steps. |
| Manual | Navigation | "Developers" or "API" link is visible from main/footer. |

---

### 5.2 Sign-only API

**Goal:** Allow external systems to send an already-generated LLM response to Aletheia for signing only (no LLM call). Aletheia becomes a universal "signer" for any pipeline.

**Deliverables:**

- New endpoint `POST /api/sign` with body `{ "response", "model_id", "policy_id" }` (and optional fields).
- Backend: canonicalize → hash → sign → timestamp → store; return id, signature, tsaToken, claim, etc., without calling any LLM.

#### Task P5.2.1 — Implement POST /api/sign endpoint

| Field | Value |
|-------|--------|
| **Est.** | 3–4 h |
| **Description** | Add sign-only endpoint and reuse existing canonicalize/sign/timestamp/store logic. |

**Coding prompt (LLM-readable):**

- Create a new DTO for the sign request, e.g. `backend/src/main/java/ai/aletheia/api/dto/SignRequest.java`. Fields: `response` (String, required), `modelId` (String, optional, default e.g. "external"), `policyId` (String, optional, default e.g. "aletheia-demo-2026-01"). Optional: `prompt` (String, for audit), `requestId` (String). Use validation annotations (@NotBlank on response).
- Create response DTO, e.g. `SignResponse.java`. Fields: `id` (Long), `responseHash` (String), `signature` (String), `tsaToken` (String), `claim` (String), `confidence` (Double), `policyVersion` (String), `createdAt` (Instant). Align with what the verify endpoint returns so that GET /api/ai/verify/:id works for sign-only records.
- Create a new controller, e.g. `SignController.java`, with `@RequestMapping("/api")` and `@PostMapping(value = "/sign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)`. Inject: CanonicalizationService, HashService, SignatureService, TimestampService, AuditRecordService (or a new service that only stores "sign-only" records), ComplianceInferenceService (for coverage if Phase 4 done).
- In the controller method: (1) Validate request body (response not blank). (2) Canonicalize the response string (use CanonicalizationService). (3) Compute hash (HashService). (4) Sign the hash (SignatureService). (5) Get TSA timestamp (TimestampService). (6) Build claim and policy coverage: use ComplianceInferenceService or a minimal version (e.g. policy_version = request.getPolicyId(), coverage from fixed rules: signature+tsa = pass, model recorded = pass if modelId set, rest not_evaluated). (7) Persist entity: create AiResponse (or equivalent) with prompt null or request.getPrompt(), response = request.getResponse(), responseHash, signature, tsaToken, llmModel = request.getModelId(), policy_version, policy_coverage, etc. Do not call LLMClient. (8) Return SignResponse with id, responseHash, signature, tsaToken, claim, confidence, policyVersion, createdAt.
- Reuse existing AuditRecordService.save(...) if it can accept a pre-built response/hash/signature/tsa; otherwise add a method like `saveSignOnly(SignRequest, hash, signature, tsaToken, claim, coverage)` that creates and saves the entity.
- Handle TimestampException and SignatureException: return 502 or 503 with clear error code (e.g. TIMESTAMP_UNAVAILABLE, SIGNING_ERROR). Document in OpenAPI.
- Add integration test: POST /api/sign with valid body, assert 200, assert response contains id, signature, tsaToken; GET /api/ai/verify/:id returns the same record. Add test for invalid body (empty response) → 400.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Integration | POST /api/sign | 200, body has id, signature, tsaToken; no LLM call. |
| Integration | GET /api/ai/verify/:id | Record created by sign is retrievable and verifiable. |
| Integration | Invalid body | 400 for empty or missing response. |

#### Task P5.2.2 — Add sign-only API to OpenAPI and developers page

| Field | Value |
|-------|--------|
| **Est.** | 0.5 h |
| **Description** | Document POST /api/sign in OpenAPI and on the Developers page. |

**Coding prompt (LLM-readable):**

- In `openapi.yaml`, add path **POST /api/sign**. Request body: `application/json` with schema: `response` (string, required), `modelId` (string, optional), `policyId` (string, optional), `prompt` (string, optional). Response 200: body with id, responseHash, signature, tsaToken, claim, confidence, policyVersion, createdAt. Responses 400, 502/503 as in Task P5.1.1.
- On the "For Developers" page, add a subsection "Sign-only API": "If you already have an LLM response, you can send it to Aletheia for signing only. POST /api/sign with body { \"response\": \"...\", \"modelId\": \"...\", \"policyId\": \"...\" }. You receive the same verification data (id, signature, tsaToken) and can use GET /api/ai/verify/:id and GET /api/ai/evidence/:id as usual."

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | OpenAPI | POST /api/sign is documented. |
| Manual | Developers page | Sign-only section describes usage. |

---

### 5.3 SDKs (Python + TypeScript)

**Goal:** Minimal client libraries so that ML pipelines and backend services can integrate with Aletheia without writing HTTP by hand.

**Deliverables:**

- **Python:** package with at least: `aletheia.sign(text, model_id=..., policy_id=...)`, `aletheia.verify(id)`, `aletheia.get_evidence(id)` (or download to path). Publishable to PyPI.
- **TypeScript/Node:** package with same logical API: `sign()`, `verify()`, `getEvidence()`. Publishable to npm.

#### Task P5.3.1 — Python SDK: project layout and sign/verify/get_evidence

| Field | Value |
|-------|--------|
| **Est.** | 3–4 h |
| **Description** | Create a Python package that wraps POST /api/sign, GET /api/ai/verify/:id, GET /api/ai/evidence/:id with a simple API. |

**Coding prompt (LLM-readable):**

- Create directory `sdk/python/` (or `clients/python/`) at repo root. Inside: `pyproject.toml` (PEP 517/518) with project name e.g. `aletheia-client`, version `0.1.0`, dependencies: `requests` (or `httpx`). Add `[tool.setuptools.packages.find]` or equivalent so that package `aletheia` or `aletheia_client` is found.
- Create module structure, e.g. `aletheia/__init__.py`, `aletheia/client.py`. In `__init__.py`, export: `sign`, `verify`, `get_evidence` (or `get_evidence_path` to download to file).
- Implement `client.py`: class `AletheiaClient` with constructor `__init__(self, base_url: str, timeout: float = 30.0)`. Methods:
  - **sign(response: str, model_id: str | None = None, policy_id: str | None = None) -> dict**: POST `base_url/api/sign` with JSON body `{"response": response, "modelId": model_id, "policyId": policy_id}`. Return parsed JSON (id, responseHash, signature, tsaToken, etc.). Raise a custom exception on non-2xx (e.g. `AletheiaAPIError` with status_code and message).
  - **verify(id: int | str) -> dict**: GET `base_url/api/ai/verify/{id}`. Return parsed JSON. Raise on 404.
  - **get_evidence(id: int | str) -> bytes**: GET `base_url/api/ai/evidence/{id}` with Accept application/zip (or omit). Return response bytes. Raise on 404.
  - Optional: **get_evidence_path(id, path: str)**: call get_evidence(id) and write bytes to path; return path.
- In `__init__.py`, expose a default client factory, e.g. `def sign(text, model_id=None, policy_id=None, base_url=None): ...` that uses `base_url` from env `ALETHEIA_API_URL` if not provided, and calls `AletheiaClient(base_url).sign(text, model_id, policy_id)`. Same for `verify(id)` and `get_evidence(id)`.
- Add a minimal README in `sdk/python/README.md`: install with `pip install .` or from repo; usage example: `import aletheia; r = aletheia.sign("Hello"); print(r["id"]); v = aletheia.verify(r["id"]); print(v["signatureValid"])`. Document env var `ALETHEIA_API_URL`.
- Add tests (pytest): mock requests to POST /api/sign and GET /api/ai/verify/:id; assert correct URL and body; assert return value shape. Optionally one integration test against local backend if available.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Unit | Python client | sign(), verify(), get_evidence() call correct URLs and return expected shape. |
| Manual | pip install | Package installs and runs against real backend (or mocked). |

#### Task P5.3.2 — Publish Python SDK to PyPI (or prepare for publish)

| Field | Value |
|-------|--------|
| **Est.** | 1 h |
| **Description** | Make the package publishable to PyPI (build, version, optional CI). |

**Coding prompt (LLM-readable):**

- In `pyproject.toml`, set `version`, `description`, `authors`, `license`, `readme`, `classifiers` (Python 3.8+, etc.). Ensure `build-system` uses setuptools or hatch.
- Add a brief publishing note in `sdk/python/README.md`: "To publish: `python -m build`, `twine upload dist/*`." Or add a GitHub Action workflow that builds and publishes on tag (e.g. `v0.1.0`) to PyPI. Prefer manual publish for Phase 5 unless automation is already in place.
- Optional: add `aletheia-client` or chosen name to PyPI; document in main repo README that the Python client is available as `pip install aletheia-client`.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | Build | `python -m build` produces wheel and sdist. |

#### Task P5.3.3 — TypeScript/Node SDK: project layout and sign/verify/get_evidence

| Field | Value |
|-------|--------|
| **Est.** | 3–4 h |
| **Description** | Create a TypeScript/Node package with the same logical API as the Python SDK. |

**Coding prompt (LLM-readable):**

- Create directory `sdk/typescript/` (or `clients/ts/`). Initialize npm package: `package.json` with name e.g. `@aletheia/client` or `aletheia-client`, version `0.1.0`, main/types pointing to built output. Dependencies: `node-fetch` or native `fetch`, or `axios`. DevDependencies: `typescript`, `@types/node`, `tsup` or `tsc` for build.
- Create `src/client.ts` (or `index.ts`). Export class `AletheiaClient` with constructor `(baseUrl: string, options?: { timeout?: number })`. Methods:
  - **sign(response: string, options?: { modelId?: string; policyId?: string }): Promise<SignResponse>**: POST `baseUrl/api/sign` with JSON body. Return typed object (id, responseHash, signature, tsaToken, ...). Throw on non-2xx.
  - **verify(id: number | string): Promise<VerifyResponse>**: GET `baseUrl/api/ai/verify/${id}`. Return typed object.
  - **getEvidence(id: number | string): Promise<Buffer | ArrayBuffer>**: GET `baseUrl/api/ai/evidence/${id}`. Return binary body. Throw on 404.
- Define TypeScript interfaces for SignResponse and VerifyResponse (id, responseHash, signature, tsaToken, claim, confidence, etc.) so that consumers get type safety.
- Export a convenience API: `createClient(baseUrl?: string)` that reads baseUrl from env `ALETHEIA_API_URL` if not provided; export `sign`, `verify`, `getEvidence` as functions that use the default client.
- Add README: install (`npm install aletheia-client`), usage example (sign, verify, getEvidence). Document env `ALETHEIA_API_URL`.
- Add unit tests (jest or vitest): mock fetch; assert correct URL and body for sign and verify; assert return types.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Unit | TS client | sign(), verify(), getEvidence() call correct URLs and return typed data. |
| Manual | npm install | Package builds and runs against real backend (or mocked). |

#### Task P5.3.4 — Publish TypeScript SDK to npm (or prepare for publish)

| Field | Value |
|-------|--------|
| **Est.** | 1 h |
| **Description** | Make the package publishable to npm. |

**Coding prompt (LLM-readable):**

- In `package.json`, set `name`, `version`, `description`, `license`, `repository`, `keywords`. Ensure `main`, `types`, and `files` are set so that npm pack includes only needed files.
- Add note in README: "To publish: npm publish." Or add CI that publishes on tag. For Phase 5, manual publish is acceptable.
- Optional: publish to npm; document in main repo README that the Node/TS client is available as `npm install aletheia-client`.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | Build | `npm run build` produces dist; `npm pack` works. |

---

### 5.4 MCP Attestation

**Goal:** Position Aletheia as an attestation layer for agent outputs (e.g. MCP). Document how an agent can call Aletheia to sign its output; metadata may include agent_id, tool_usage, policy_id, timestamp.

**Deliverables:**

- Documentation: `docs/en/mcp/attestation.md` describing the flow and metadata.
- Optional: extend POST /api/sign (or a dedicated endpoint) to accept agent_id and tool_usage in the request body and store them in metadata/Evidence Package.

#### Task P5.4.1 — Document MCP attestation flow and metadata

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Write the attestation doc for MCP/agent ecosystem. |

**Coding prompt (LLM-readable):**

- Create directory `docs/en/mcp/` if it does not exist. Create file `docs/en/mcp/attestation.md`.
- Content structure: (1) **Purpose**: "Aletheia can attest agent outputs. An agent (e.g. MCP server) generates a response; it sends the response to Aletheia for signing. The signed result and Evidence Package provide cryptographic proof of what the agent said and when." (2) **Flow**: Step 1 — Agent produces output. Step 2 — Agent calls Aletheia (POST /api/sign or POST /api/ai/ask). Step 3 — Aletheia returns id, signature, tsaToken. Step 4 — Agent or client can retrieve verification and Evidence Package via GET /api/ai/verify/:id and GET /api/ai/evidence/:id. (3) **Metadata**: List recommended metadata for attestation: `agent_id` (identifier of the agent/MCP server), `tool_usage` (optional: which tools were used), `policy_id` (which policy was applied), `timestamp` (from TSA). Document that these can be passed in the request body (e.g. POST /api/sign with optional fields agentId, toolUsage) and stored in the record and/or Evidence Package metadata. (4) **Example**: Minimal JSON example for POST /api/sign with response, modelId, policyId, agentId, toolUsage. (5) **References**: Link to OpenAPI spec and to MCP (Model Context Protocol) if relevant.
- If the backend does not yet accept agent_id/tool_usage in POST /api/sign, add a note: "Phase 5.4.2 will add these fields to the API and Evidence Package."

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | attestation.md | Document exists, describes flow and metadata; example request included. |

#### Task P5.4.2 — Add agent_id and tool_usage to sign API and Evidence Package (optional)

| Field | Value |
|-------|--------|
| **Est.** | 2 h |
| **Description** | Extend POST /api/sign and storage/Evidence Package to support agent_id and tool_usage. |

**Coding prompt (LLM-readable):**

- In `SignRequest` (or equivalent), add optional fields: `agentId` (String), `toolUsage` (String or JSON string, or List<String>). In the entity (e.g. AiResponse), add columns or a JSON blob for `agent_id`, `tool_usage` (or store in existing metadata column). Add Flyway/Liquibase migration if needed.
- In SignController, when saving the record, set agent_id and tool_usage from the request. In GET /api/ai/verify/:id response DTO, add optional fields `agentId`, `toolUsage`. In EvidencePackageServiceImpl, when building metadata.json, add `agent_id` and `tool_usage` when present.
- Update OpenAPI: document optional request body fields agentId, toolUsage; document response and metadata.json contents.
- Update docs/en/mcp/attestation.md: "Supported in API: pass agentId and toolUsage in POST /api/sign; they appear in GET /api/ai/verify/:id and in Evidence Package metadata."

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Integration | POST /api/sign with agentId | Stored and returned in verify and in .aep metadata. |
| Doc | attestation.md | Doc states fields are supported. |

---

### 5.5 Integrations (SIEM, Blockchain)

**Goal:** SIEM export for enterprise; optional blockchain anchoring for "proof of existence".

**Deliverables:**

- **SIEM:** Events in a defined format (e.g. JSON Lines); events: response_generated, response_signed, evidence_created. Export via webhook or log/file.
- **Blockchain (optional):** Document or implement recording of Evidence Package hash (or response hash) to a public blockchain (Ethereum/Bitcoin); document as proof of existence.

#### Task P5.5.1 — SIEM: define event schema and emit response_generated / response_signed / evidence_created

| Field | Value |
|-------|--------|
| **Est.** | 2–3 h |
| **Description** | Define a JSON event schema and emit events from the backend when a response is generated/signed and when evidence is created. |

**Coding prompt (LLM-readable):**

- Create doc `docs/en/integrations/SIEM.md`. Describe: (1) Purpose: "Aletheia can emit events for SIEM integration (audit, compliance)." (2) Event types: `response_generated` (when POST /api/ai/ask or POST /api/sign completes), `response_signed` (same, or when TSA timestamp is received), `evidence_created` (when Evidence Package is first built or downloaded — optional to avoid duplicate). (3) Event format: JSON Lines (one JSON object per line). Each event: `event_type`, `timestamp` (ISO 8601), `response_id`, `hash` (response hash), optional `prompt_hash` or no PII, optional `policy_id`, `coverage`. Keep PII minimal; document that deployers may redact. (4) Delivery: Option A — backend writes events to a log file or stdout in JSON Lines format; SIEM agent tails the file. Option B — backend calls a configurable webhook URL (env ALETHEIA_SIEM_WEBHOOK_URL) with POST body = JSON array of events or single event. Option C — both. For Phase 5, implement at least Option A (structured log) or Option B (webhook).
- In the backend, after saving a new AiResponse (in AuditRecordService or after POST /api/ai/ask and POST /api/sign), build an event object (event_type, timestamp, response_id, hash, policy_id, coverage). If Option A: log it as a single line JSON (e.g. use a dedicated logger or append to a file). If Option B: if ALETHEIA_SIEM_WEBHOOK_URL is set, HTTP POST the event to that URL (async or fire-and-forget to avoid blocking). Use a small helper or Spring Event listener so that the main flow stays clean.
- Document in SIEM.md: how to enable webhook, env var name, example payload. Document that SIEM connectors may need to parse JSON Lines from log or handle webhook payload.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Unit/Integration | Event emission | After sign or ask, an event with event_type and response_id is emitted (log or webhook). |
| Doc | SIEM.md | Event schema and delivery options documented. |

#### Task P5.5.2 — Blockchain anchoring (optional): document or implement

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h (doc only) or 4+ h (implement) |
| **Description** | Document blockchain anchoring as "proof of existence"; optionally implement a minimal path (e.g. store hash in a public chain). |

**Coding prompt (LLM-readable):**

- Create doc `docs/en/integrations/BLOCKCHAIN.md`. Content: (1) Purpose: "Recording the hash of an Evidence Package (or response) on a public blockchain provides a timestamp and proof of existence that does not depend on Aletheia's TSA." (2) Options: Ethereum (smart contract or simple tx with hash in data), Bitcoin (OP_RETURN or similar). (3) Flow: After signing, optionally send hash to a configured blockchain endpoint or service; store tx id or block number in metadata (or separate table). (4) For Phase 5, implementation is optional: if time permits, add a configurable "blockchain anchor" step (e.g. call external API that writes to chain) and store anchor info in AiResponse or metadata; otherwise only document the intended design and defer implementation to Phase 6.
- If implementing: add env ALETHEIA_BLOCKCHAIN_ANCHOR_URL or similar; after TSA timestamp, call anchor service with hash; persist anchor_tx_id or anchor_block in entity and in Evidence Package metadata. Document in BLOCKCHAIN.md.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | BLOCKCHAIN.md | Document exists; purpose and options described. |
| Optional | Implementation | If implemented: hash is sent to anchor service; anchor id stored and returned. |

---

### 5.6 Partner scenarios

**Goal:** 1–2 real partner integrations (legal tech, HR platform, MCP provider, or corporate LLM platform) to produce case studies and market trust.

**Deliverables:**

- At least one documented partner integration or pilot: who, what was integrated (API/SDK/sign-only), outcome (e.g. "Pilot: signed responses in HR workflow").
- Optional: joint press release or case study doc.

#### Task P5.6.1 — Document partner integration and outcome

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h (doc structure) + ongoing (partnership) |
| **Description** | Create a template and document at least one partner scenario. |

**Coding prompt (LLM-readable):**

- Create file `docs/en/partners/README.md` or `docs/partners.md`. Section: "Partner integrations". For each partner (or placeholder): name (or "Partner A" if confidential), segment (legal tech / HR / MCP / corporate LLM), what was integrated (e.g. "Sign-only API in their pipeline"), outcome (e.g. "Pilot: 100 signed responses/day; feedback collected"). For Phase 5 completion, at least one real or anonymized entry is required: "Pilot with [X]: integrated POST /api/sign; outcome: ...".
- Optional: add `docs/en/partners/CASE_STUDY_TEMPLATE.md` — structure for a one-page case study (problem, solution, integration points, result, quote). Fill it for one partner when available.
- Link from main README or VISION_AND_ROADMAP: "Partner integrations: see docs/partners."

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | partners | At least one partner/pilot documented with integration type and outcome. |

---

## Out of scope (Phase 6+)

The following are explicitly **out of scope** for Phase 5 and deferred to Phase 6 or later:

- Full Policy Registry (versioning, multiple policies, API to list/get policies).
- Policy Evaluation Pipeline (automated rule engine for all rule types).
- Time-travel verify (as_of date, historical policy).
- Human / hybrid review (human_verified flag, Proof of Human).
- Enterprise dashboards (multi-tenant, org management).
- Multi-tenant deployments and tenant isolation.

---

## Completion criteria

Phase 5 is complete when all of the following are true:

| # | Criterion | Status |
|---|-----------|--------|
| 1 | Public API documented (OpenAPI 3.0) | [ ] |
| 2 | "For Developers" page published | [ ] |
| 3 | Sign-only API (POST /api/sign) works | [ ] |
| 4 | Python SDK published or build-ready for PyPI | [ ] |
| 5 | TypeScript SDK published or build-ready for npm | [ ] |
| 6 | MCP attestation documented (docs/en/mcp/attestation.md) | [ ] |
| 7 | SIEM export implemented (events + doc) | [ ] |
| 8 | (Optional) Blockchain anchoring documented or implemented | [ ] |
| 9 | ≥1 partner integration or pilot documented | [ ] |

---

## Timeline

Suggested 6–8 week split:

| Week | Focus |
|------|--------|
| 1–2 | OpenAPI spec (P5.1.1, P5.1.2); "For Developers" page (P5.1.3) |
| 3 | Sign-only API (P5.2.1, P5.2.2) |
| 4 | Python SDK (P5.3.1, P5.3.2); TypeScript SDK (P5.3.3, P5.3.4) |
| 5 | MCP attestation doc and optional API extension (P5.4.1, P5.4.2) |
| 6 | SIEM events and doc (P5.5.1); blockchain doc or impl (P5.5.2) |
| 7–8 | Partner scenario (P5.6.1); polish, tests, docs |

---

## Risks and mitigation

| Risk | Mitigation |
|------|-------------|
| API drift from OpenAPI spec | Treat openapi.yaml as source of truth; review on every API change; optional CI that checks backend against spec. |
| SDK maintenance burden | Keep SDK surface minimal (sign, verify, get_evidence); avoid optional parameters until requested. |
| SIEM/blockchain scope creep | SIEM: one event schema, one delivery method (log or webhook). Blockchain: doc-only or one anchor provider. |
| No partner in time | Document "target partners" and integration pattern; count as done when one pilot or LOI is documented. |

---

## References

- [Vision & roadmap](VISION_AND_ROADMAP.md) — Phases 1–6; Phase 5 = API platform & integrations.
- [Plan Phase 4](PLAN_PHASE4.md) — Market validation & policy foundation.
- [NEXT.md](../tmp/NEXT.md) — Strategy and direction.

**Translations:** RU and ET versions can be added when Phase 5 is approved.
