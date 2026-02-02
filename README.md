# Aletheia AI

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![RFC 3161](https://img.shields.io/badge/RFC-3161-purple)](https://www.ietf.org/rfc/rfc3161.txt)

**Verifiable AI responses with signing and timestamps.**

PoC for cryptographically signed and timestamped LLM answers so that responses can be proven, not just trusted.

Stack (PoC): Next.js, Java Spring Boot, PostgreSQL, OpenSSL/BouncyCastle, RFC 3161 TSA, one LLM (OpenAI/Gemini/Mistral).

---

## Documentation

Docs are grouped by language in `docs/<lang>/` (en, ru, et). **Overview and where to start:** [docs/README.md](docs/README.md). Core documents are available in all three languages.

| Topic | EN | RU | ET |
|-------|----|----|-----|
| **PoC (architecture & stack)** | [PoC](docs/en/PoC.md) | [PoC (архитектура)](docs/ru/PoC.md) | [PoC (arhitektuur)](docs/et/PoC.md) |
| **Vision & roadmap** (next steps, AI Claim, evidence package) | [Vision & roadmap](docs/en/VISION_AND_ROADMAP.md) | [Видение и дорожная карта](docs/ru/VISION_AND_ROADMAP.md) | [Visioon ja teekond](docs/et/VISION_AND_ROADMAP.md) |
| **Implementation plan** | [PLAN](docs/en/PLAN.md) | [План реализации](docs/ru/PLAN.md) | [Rakendusplaan](docs/et/PLAN.md) |
| **Plan Phase 2** (killer demo, Evidence Package, offline verifier) | [EN](docs/en/PLAN_PHASE2.md) | [RU](docs/ru/PLAN_PHASE2.md) | [ET](docs/et/PLAN_PHASE2.md) |
| **Signing** | [Signing](docs/en/SIGNING.md) | [Подпись](docs/ru/SIGNING.md) | [Allkirjastamine](docs/et/SIGNING.md) |
| **Timestamping** | [Timestamping](docs/en/TIMESTAMPING.md) | [Временные метки](docs/ru/TIMESTAMPING.md) | [Ajatemplid](docs/et/TIMESTAMPING.md) |
| **Trust model & eIDAS** | [Trust model](docs/en/TRUST_MODEL.md) | [Модель доверия](docs/ru/TRUST_MODEL.md) | [Usaldusmudel](docs/et/TRUST_MODEL.md) |
| **MOCK_TSA (testing)** | [MOCK_TSA](docs/en/MOCK_TSA.md) | [MOCK_TSA (тестирование)](docs/ru/MOCK_TSA.md) | — |
| **Cryptographic Oracle** | [Crypto Oracle](docs/en/CRYPTO_ORACLE.md) | [Криптографический оракул](docs/ru/CRYPTO_ORACLE.md) | [Krüptograafiline oraakel](docs/et/CRYPTO_ORACLE.md) |
| **Agent Audit Model** | [Agent Audit](docs/en/AGENT_AUDIT_MODEL.md) | [Модель аудита агентов](docs/ru/AGENT_AUDIT_MODEL.md) | [Agentide auditi mudel](docs/et/AGENT_AUDIT_MODEL.md) |
| **Testing Strategy** | [Testing Strategy](docs/en/TESTING_STRATEGY.md) | [Стратегия тестирования](docs/ru/TESTING_STRATEGY.md) | [Testimise strateegia](docs/et/TESTING_STRATEGY.md) |
| **Future ideas** (PKI for AI agents, OpenClaw, MCP) | — | [Будущие идеи](docs/ru/ideas/README.md) | [Tuleviku ideed](docs/et/ideas/README.md) |
| **Crypto reference** (algorithms, keys, why tsaToken) | [Crypto reference](docs/en/CRYPTO_REFERENCE.md) | — | — |
| **Legal & regulatory** (eIDAS, EU AI Act, GDPR, ETSI) | [Legal docs](docs/legal/README.md) | | |
| **Architecture diagrams** | [Architecture diagrams](diagrams/architecture.md) (Mermaid: pipeline, trust chain, stack) | | |

### README contents

- [Design: PKI chain and RFC 3161](#design-pki-chain-and-rfc-3161)
- [Prerequisites](#prerequisites)
- [Environment variables](#environment-variables)
- [Quick start](#quick-start)
- [Backend & database](#backend--database)
- [Main AI endpoint](#main-ai-endpoint)
- [Evidence Package & offline verification](#evidence-package--offline-verification)
- [Killer demo (Phase 2)](#killer-demo-phase-2)
- [Works with MCP / any agent](#works-with-mcp--any-agent)
- [LLM (OpenAI)](#llm-openai)
- [Audit demo (tangible test)](#audit-demo-tangible-test)
- [Crypto demo endpoint](#crypto-demo-endpoint)
- [Run frontend](#run-frontend)
- [Run tests](#run-tests)
- [Docker build](#docker-build)
- [Deployment](#deployment)
- [Authorship and License](#authorship-and-license)

---

## Design: PKI chain and RFC 3161

**RFC 3161** is the same standard used in eIDAS, legal evidence, and enterprise PKI. This PoC timestamps the **signature bytes** (not the raw AI text): we attest *what* was said; the TSA attests *when* it was signed. BouncyCastle TSP is used for requests; the TSA token is stored as opaque bytes.

**Chain:** `AI response → canonicalize → hash → sign(hash) → timestamp(signature) → store`

For details: [Signing](docs/en/SIGNING.md), [Timestamping](docs/en/TIMESTAMPING.md), [Trust model & eIDAS](docs/en/TRUST_MODEL.md), [diagrams (trust chain)](diagrams/architecture.md#6-trust-chain). Doc index: [docs/README.md](docs/README.md).

---

## Prerequisites

| Requirement | Version | Required |
|-------------|---------|----------|
| Java | 21+ | ✓ backend |
| Node.js | 18+ | ✓ frontend |
| OpenSSL | any | ✓ key generation |
| Maven | 3.6+ | ✓ backend |
| PostgreSQL | 15+ | optional (default: H2) |
| Docker | any | optional (for PostgreSQL) |

---

## Environment variables

**Backend config from `.env`** — copy `.env.example` to `.env` to override defaults. Key variables:

| Variable | When needed | Default / note |
|----------|-------------|----------------|
| `AI_ALETHEIA_SIGNING_KEY_PATH` | signing, POST /api/ai/ask | path to PEM (e.g. `../ai.key`) |
| `OPENAI_API_KEY` | LLM, POST /api/ai/ask | — |
| `OPENAI_MODEL` | LLM | `gpt-4` |
| `OPENAI_TEMPERATURE` | LLM | `1.0` (0–2) |
| `OPENAI_MAX_TOKENS` | LLM | `2000` |
| `SPRING_DATASOURCE_URL` | DB | `jdbc:h2:file:./data/aletheia` |
| `AI_ALETHEIA_TSA_MODE` | TSA | `real` (default, DigiCert) or `mock` (tests/offline) |
| `AI_ALETHEIA_TSA_URL` | when mode=real | `http://timestamp.digicert.com` (default) |
| `NEXT_PUBLIC_API_URL` | frontend | `http://localhost:8080` |

Full list: [.env.example](.env.example). Architecture: [PoC](docs/en/PoC.md), [PLAN](docs/en/PLAN.md).

---

## Quick start

**One-time setup:** copy env, generate signing key, set key path in `.env`.

```bash
git clone <repo> && cd aletheia-ai
cp .env.example .env
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
# In .env set: AI_ALETHEIA_SIGNING_KEY_PATH=../ai.key  (or absolute path)
```

**Run backend** (from project root or `backend/`):

```bash
cd backend && mvn spring-boot:run
```

- H2 DB is created at `backend/data/` (no PostgreSQL required). API: http://localhost:8080, Swagger: http://localhost:8080/swagger-ui.html, H2 console: http://localhost:8080/h2-console.

**Run frontend** (in another terminal):

```bash
cd frontend && cp .env.example .env.local && npm install && npm run dev
```

Open http://localhost:3000. For POST /api/ai/ask set `OPENAI_API_KEY` in `.env`. LLM-free test: `curl -X POST http://localhost:8080/api/audit/demo -H "Content-Type: application/json" -d '{"text":"hello"}'`.

---

## Backend & database

Spring Boot loads `.env` from the project root automatically. Default: **H2** file-based at `backend/data/aletheia.mv.db` (data persists). Override with [Environment variables](#environment-variables) for PostgreSQL.

**Run backend:** `cd backend && mvn spring-boot:run` (or `java -jar target/aletheia-backend.jar`). API: http://localhost:8080.

**H2 console** (while backend is running): http://localhost:8080/h2-console. Use the **same** JDBC URL as in `SPRING_DATASOURCE_URL` (e.g. `jdbc:h2:file:./data/aletheia` for file-based). User: `sa`, password: empty. Run backend from `backend/` so `./data/aletheia` resolves.

**PostgreSQL (optional):** `docker-compose up -d postgres` or single container: `docker run -d --name aletheia-db -e POSTGRES_DB=aletheia -e POSTGRES_USER=aletheia -e POSTGRES_PASSWORD=local -p 5432:5432 postgres:15-alpine`. Then in `.env`: `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/aletheia`, `SPRING_DATASOURCE_USERNAME=aletheia`, `SPRING_DATASOURCE_PASSWORD=local`, `SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect`. Flyway runs on startup; see `backend/src/main/resources/db/migration/README.md` and `schema-ai_response-standalone.sql` for manual SQL.

**Signing key:** required for signing/TSA. Generate once (see [Quick start](#quick-start)); set `AI_ALETHEIA_SIGNING_KEY_PATH` in `.env`. Override via CLI: `mvn spring-boot:run -Dspring-boot.run.arguments="--ai.aletheia.signing.key-path=../ai.key"`.

**TSA:** `AI_ALETHEIA_TSA_MODE=real` (default, DigiCert) or `mock` (tests/offline). See [TIMESTAMPING](docs/en/TIMESTAMPING.md).

**Swagger:** http://localhost:8080/swagger-ui.html, OpenAPI JSON: `/v3/api-docs`.

---

## Main AI endpoint

**POST /api/ai/ask** — full flow: prompt → LLM → canonicalize → hash → sign → timestamp → save to DB. Requires `OPENAI_API_KEY` and signing key. Returns verifiable response with `id` for GET /api/ai/verify/:id.

```bash
curl -X POST http://localhost:8080/api/ai/ask -H "Content-Type: application/json" -d '{"prompt":"What is 2+2?"}'
# → { "response": "...", "responseHash": "...", "signature": "...", "tsaToken": "...", "id": 1, "model": "gpt-4" }

curl http://localhost:8080/api/ai/verify/1
# → full record for verification page
```

### Evidence Package & offline verification

Each signed response can be exported as an **Evidence Package** (`.aep`) and verified **offline** with the verifier CLI — no Aletheia backend call. See [Plan Phase 2](docs/en/PLAN_PHASE2.md) for the format and [scripts/README.md](scripts/README.md) for verifier usage (JAR, Java+Maven, OpenSSL-only).

**GET /api/ai/evidence/:id** — build and download the Evidence Package (.aep) for a stored response. Returns a ZIP archive containing the seven components required for offline verification (response.txt, canonical.bin, hash.sha256, signature.sig, timestamp.tsr, metadata.json, public_key.pem). Requires a configured signing key. Use `?format=json` to get JSON with base64-encoded file contents instead of ZIP.

```bash
# After POST /api/ai/ask or /api/audit/demo, get the response id (e.g. 1)
curl -o evidence-1.aep "http://localhost:8080/api/ai/evidence/1"
# → saves aletheia-evidence-1.aep (ZIP)

curl "http://localhost:8080/api/ai/evidence/1?format=json"
# → JSON with keys: response.txt, canonical.bin, hash.sha256, signature.sig, timestamp.tsr, metadata.json, public_key.pem (values are base64)
```

See [Plan Phase 2](docs/en/PLAN_PHASE2.md) for the Evidence Package format (DP2.1.1) and offline verifier.

### Offline verification (DP2.2)

You can verify an Evidence Package **without calling the Aletheia backend**: use the Java verifier (backend) or the OpenSSL script.

**Verification order:** (1) **Hash** — recompute SHA-256 of `canonical.bin` and compare with `hash.sha256`. (2) **Signature** — load `public_key.pem`, verify `signature.sig` over the hash (RSA PKCS#1 v1.5 over DigestInfo(SHA-256, hash)). (3) **TSA token** — parse `timestamp.tsr` (RFC 3161), read `genTime`, optionally verify TSA signature.

**Shell script (OpenSSL only):**
```bash
./scripts/verify-evidence.sh /path/to/package-dir
# or
./scripts/verify-evidence.sh /path/to/evidence.aep
```
Exit 0 = VALID, 1 = INVALID. Requires `openssl`, `xxd`, `base64`, `unzip`. Optional: set `TSA_CA_FILE` to a TSA CA PEM to verify the timestamp signature.

**Java verifier (CLI):**
```bash
# Standalone JAR (no Maven at runtime)
cd backend && mvn package -Pverifier -DskipTests
java -jar backend/target/aletheia-verifier.jar /path/to/package-dir
java -jar backend/target/aletheia-verifier.jar /path/to/evidence.aep

# Or via Maven (from repo root)
./scripts/verify-evidence-java.sh /path/to/package-dir
# Or from backend/: mvn exec:java -Dexec.mainClass="ai.aletheia.verifier.VerifierMain" -Dexec.args="/path/to/package"
```
Exit 0 = VALID, 1 = INVALID. No backend server or network call. The `-Pverifier` profile builds a fat JAR (verifier + BouncyCastle only) at `backend/target/aletheia-verifier.jar`. Programmatic use: `new EvidenceVerifierImpl().verify(path)`. Unit tests: `EvidenceVerifierTest`. See [scripts/README.md](scripts/README.md) for all verifier options (JAR, Java+Maven, OpenSSL-only).

### Killer demo (Phase 2)

One reproducible scenario: **legal/compliance** — user asks AI a compliance question (e.g. “Is this clause GDPR-compliant?”), backend returns signed + timestamped response, user exports Evidence Package, auditor runs the offline verifier and sees VALID. Reproducible in ≤5 minutes. See [Demo script](docs/DEMO_SCRIPT.md) for step-by-step instructions and [Plan Phase 2](docs/en/PLAN_PHASE2.md) for scope and implementation status.

### Works with MCP / any agent

**Aletheia can sign and timestamp outputs from any agent** (e.g. MCP, OpenClaw); verification remains offline. The Evidence Package and verifier do not depend on a specific LLM or UI. See [Vision Phase 2](docs/en/VISION_AND_ROADMAP.md#2-killer-demo--domain-choice) and [ideas: PKI for AI agents](docs/ru/ideas/PKI_FOR_AI_AGENTS.md) (RU).

---

## LLM (OpenAI)

**POST /api/llm/demo** — test LLM completion only (no persistence). Returns `{"responseText", "modelId"}`. Requires `OPENAI_API_KEY`; see [Environment variables](#environment-variables).

```bash
curl -X POST http://localhost:8080/api/llm/demo -H "Content-Type: application/json" -d '{"prompt":"What is 2+2?"}'
# → { "responseText": "2+2 equals 4.", "modelId": "gpt-4" }
```

---

## Audit demo (tangible test)

**POST /api/audit/demo** — crypto pipeline + save to DB. Accepts `{ "text": "..." }`, runs canonicalize → hash → sign → timestamp, persists the record, returns the id. Use to verify the full flow without LLM.

```bash
curl -X POST http://localhost:8080/api/audit/demo -H "Content-Type: application/json" -d '{"text":"hello world"}'
# → { "id": 1, "hash": "...", "signature": "...", "tsaToken": "...", ... }

curl http://localhost:8080/api/ai/verify/1
# → full record for verification page
```

Check [H2 console](http://localhost:8080/h2-console) to see the saved row in `ai_response`.

---

## Crypto demo endpoint

Exposes the crypto pipeline: canonicalize → hash → sign → timestamp. **Works without signing key** — returns hash; signature and `tsaToken` are `null` when key not configured.

**Manual test:**

```bash
# Start backend first (see [Backend & database](#backend--database))
cd backend && mvn spring-boot:run

# In another terminal — hash only (no key needed):
curl -X POST http://localhost:8080/api/crypto/demo \
  -H "Content-Type: application/json" \
  -d '{"text":"hello world"}'
```

**Expected response (without signing key — works out of the box):**
```json
{
  "text": "hello world",
  "canonicalBase64": "aGVsbG8gd29ybGQK",
  "hash": "a948904f2f0f479b8f8197694b30184b0d2ed1c1cd2a1ec0fb85d299a192a447",
  "signature": null,
  "signatureStatus": "KEY_NOT_CONFIGURED",
  "tsaToken": null,
  "tsaStatus": "NO_SIGNATURE"
}
```

**With signing key:** `signature` = Base64 RSA signature, `signatureStatus` = `"SIGNED"`, `tsaToken` = RFC 3161 timestamp token (Base64), `tsaStatus` = `"REAL_TSA"` (default, DigiCert) or `"MOCK_TSA"` when using mock. See [TIMESTAMPING](docs/en/TIMESTAMPING.md) for TSA mode switching.

You can verify the hash using any SHA-256 tool (e.g. `echo -n "hello world" | shasum -a 256` — note: canonical form adds trailing newline, so hash may differ). See [SIGNING](docs/en/SIGNING.md) for canonicalization rules.

---

## Run frontend

```bash
# From frontend directory
cd frontend
npm install
npm run dev
```

Open http://localhost:3000. You should see:
- **Prompt** — text area for entering questions
- **Send** — button (connects to POST /api/ai/ask)
- **Response** — AI answer, status (Signed, Timestamped, Verifiable), link to verify page, **Download evidence** button
- **Download evidence** — after asking a question, click **Download evidence** to get the Evidence Package (`.aep`) for offline verification; the file is saved as `aletheia-evidence-<id>.aep`
- **Verify page** — `/verify?id=...` shows hash, signature, TSA token, model, date; backend verification (hashMatch, signatureValid); "Verify hash" for client-side check

**Required:** `NEXT_PUBLIC_API_URL=http://localhost:8080` in `frontend/.env.local` (see [Environment variables](#environment-variables)). Start the backend first. CORS allows `http://localhost:3000` by default.

---

## Run tests

**Backend (from `backend/`):**
```bash
mvn test
```
Runs JUnit 5 tests: `HealthControllerTest` (GET /health → 200, `{"status":"UP"}`), `AletheiaBackendApplicationTests` (context load), `CanonicalizationServiceTest`, `HashServiceTest`, and `SignatureServiceTest` (sign/verify, tampered signature). Uses H2 in-memory for tests; signing tests use in-memory RSA keys (no PEM file required).

**Golden Fixtures:** Test fixtures (hash outputs, signatures, RFC 3161 tokens) are stored in `backend/src/test/resources/fixtures/` for deterministic regression testing. See [fixtures README](backend/src/test/resources/fixtures/README.md) for details.

**Frontend:** `npm test` when test script is added.

Detailed test scope and acceptance criteria per step: see [plan (EN)](docs/en/PLAN.md#testing-by-step), [plan (RU)](docs/ru/PLAN.md#тестирование-по-шагам), [plan (ET)](docs/et/PLAN.md#testimine-sammude-kaupa).

---

## Docker build

### Quick start (docker-compose)

```bash
# 1. Generate signing key (once) — must exist before first run
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048

# 2. Add OPENAI_API_KEY to .env (or cp .env.example .env and edit)

# 3. Run everything
docker-compose up -d
```

Open http://localhost:3000 (frontend) and http://localhost:8080 (backend API, Swagger).

**Note:** If `ai.key` did not exist when compose first ran, Docker may create it as a directory; backend will fail. Fix: `rm -rf ai.key`, recreate the file, then `docker compose down && docker compose up -d`. See [deploy/ansible/README.md#troubleshooting](deploy/ansible/README.md#troubleshooting).

### Standalone builds

Backend and frontend have multi-stage Dockerfiles. Use env vars at runtime; do **not** commit `.env` or keys to the image.

### Backend

```bash
cd backend
docker build -t aletheia-backend .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/aletheia \
  -e AI_ALETHEIA_SIGNING_KEY_PATH=/app/ai.key \
  -e AI_ALETHEIA_TSA_MODE=real \
  -e AI_ALETHEIA_TSA_URL=http://timestamp.digicert.com \
  -e OPENAI_API_KEY=sk-your-key \
  -v /path/to/ai.key:/app/ai.key:ro \
  aletheia-backend
```

**Signing key:** Mount your PEM key at runtime (`-v /host/ai.key:/app/ai.key`) and set `AI_ALETHEIA_SIGNING_KEY_PATH=/app/ai.key`. Never copy keys into the image.

### Frontend

`NEXT_PUBLIC_API_URL` must be set at **build time** (baked into the client bundle). Empty = relative `/api` URLs (used when behind ngrok; [frontend/app/api/[...path]/route.ts](frontend/app/api/[...path]/route.ts) proxies to the backend at runtime using `BACKEND_INTERNAL_URL`). See [deploy/ansible/README.md](deploy/ansible/README.md#api-proxy-docker) and [CORS when using ngrok](deploy/ansible/README.md#cors-when-opening-app-via-ngrok-fetch-to-localhost8080-blocked).

```bash
cd frontend
docker build --build-arg NEXT_PUBLIC_API_URL=http://localhost:8080 -t aletheia-frontend .
docker run -p 3000:3000 aletheia-frontend
```

For production (no ngrok), use the backend URL: `--build-arg NEXT_PUBLIC_API_URL=http://YOUR_VM_IP:8080`. For ngrok, build with empty: `--build-arg NEXT_PUBLIC_API_URL=`.

### .dockerignore

Both `backend/` and `frontend/` have `.dockerignore` that exclude `node_modules`, `.next`, `target`, `.git`, and `.env`. Secrets are passed via env or volumes at runtime.

---

## Deployment

**Chosen approach:** Full stack (Docker + Ansible + GitHub Actions) for automated deployment to a target VM (e.g. `ssh ubuntu@193.40.157.132`).

| Component | Purpose |
|-----------|---------|
| **Docker** | Containerize backend and frontend; docker-compose with PostgreSQL |
| **Ansible** | VM setup (Docker install), .env template, `docker compose up`, optional ngrok systemd service |
| **GitHub Actions** | On push to main: tests → build → deploy via Ansible; see [.github/workflows/deploy.yml](.github/workflows/deploy.yml) |

### Ansible deploy (manual, verified)

One command deploys postgres, backend, frontend to a target VM. **Verified** on Ubuntu 22.04.

```bash
# From project root; ensure ai.key exists (or playbook fails with instructions)
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048

ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml

# With secrets (production)
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml \
  -e postgres_password=SECURE_PASS \
  -e openai_api_key=sk-xxx \
  -e next_public_api_url=http://YOUR_VM_IP:8080
```

**Result:** Frontend at `http://VM:3000`, Backend at `http://VM:8080`. Variables, troubleshooting: [deploy/ansible/README.md](deploy/ansible/README.md).

**ngrok (firewall / university):** One command exposes the app via ngrok (free plan = 1 endpoint). Add `NGROK_AUTHTOKEN` to `.env`, then: `ansible-playbook ... -e ngrok_enabled=true`. Next.js rewrites `/api/*` to the backend — no second tunnel or CORS.

### GitHub Actions (CI/CD)

Push to `main` triggers: **test** → **build** → **deploy** via Ansible. Manual run: Actions → Deploy → Run workflow.

**Required secrets** (Settings → Secrets and variables → Actions):

| Secret | Description |
|--------|-------------|
| `DEPLOY_HOST` | Target VM IP or hostname |
| `DEPLOY_USER` | SSH user (e.g. `ubuntu`) |
| `SSH_PRIVATE_KEY` | SSH private key content (`cat ~/.ssh/id_ed25519`) |
| `SIGNING_KEY` | PEM content of `ai.key` (`cat ai.key`) |

**Optional** (for production): `POSTGRES_PASSWORD`, `OPENAI_API_KEY`, `NEXT_PUBLIC_API_URL` (defaults to `http://DEPLOY_HOST:8080`).

Workflow: [.github/workflows/deploy.yml](.github/workflows/deploy.yml)

**Alternatives:** Ansible-only (no containers), script-only (bash over SSH), Docker Compose only. See [plan Step 8](docs/en/PLAN.md#step-8--deployment-cicd) for detailed tasks and LLM-readable implementation prompts.

---

## Authorship and License

The **Aletheia AI** project was initiated and primarily developed by **Anton Sokolov** 
as part of the TalTech continuing education program 
"Noorem-tarkvaraarendajast vanemarendajaks" (From Junior to Senior Software Developer).

The project is licensed under the **MIT License**.  
You are free to use, copy, modify, and distribute the code with attribution.

The author retains the right to continue development of the project independently 
of the course or project group.

For full details, see [LICENSE](LICENSE).

## Private Development / Future Work

This project is open for exploration, experimentation, and contributions.  
**Please note:** the original author intends to continue developing certain features independently in future branches (`future/` or `dev/`).  

When forking or contributing:  
- Always retain proper attribution to the original author.  
- Do **not modify, merge, or claim ownership** of ongoing private work in `future/` or `dev/`.  
- Contributions to main or feature branches outside these reserved areas are welcome and appreciated.  

This approach helps the community experiment, while preserving the author's right to continue personal development.
