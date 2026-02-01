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

Docs are grouped by language in `docs/<lang>/` (en, ru, et). Core documents are available in all three languages.

| Topic | EN | RU | ET |
|-------|----|----|-----|
| **PoC (architecture & stack)** | [PoC](docs/en/PoC.md) | [PoC (архитектура)](docs/ru/PoC.md) | [PoC (arhitektuur)](docs/et/PoC.md) |
| **Vision & roadmap** (next steps, AI Claim, evidence package) | [Vision & roadmap](docs/en/VISION_AND_ROADMAP.md) | [Видение и дорожная карта](docs/ru/VISION_AND_ROADMAP.md) | [Visioon ja teekond](docs/et/VISION_AND_ROADMAP.md) |
| **Implementation plan** | [Implementation plan](docs/en/plan.md) | [План реализации](docs/ru/plan.md) | [Rakendusplaan](docs/et/plan.md) |
| **Signing** | [Signing](docs/en/SIGNING.md) | [Подпись](docs/ru/SIGNING.md) | [Allkirjastamine](docs/et/SIGNING.md) |
| **Timestamping** | [Timestamping](docs/en/TIMESTAMPING.md) | [Временные метки](docs/ru/TIMESTAMPING.md) | [Ajatemplid](docs/et/TIMESTAMPING.md) |
| **Trust model & eIDAS** | [Trust model](docs/en/TRUST_MODEL.md) | [Модель доверия](docs/ru/TRUST_MODEL.md) | [Usaldusmudel](docs/et/TRUST_MODEL.md) |
| **MOCK_TSA (testing)** | [MOCK_TSA](docs/en/MOCK_TSA.md) | [MOCK_TSA (тестирование)](docs/ru/MOCK_TSA.md) | — |
| **Cryptographic Oracle** | [Crypto Oracle](docs/en/CRYPTO_ORACLE.md) | [Криптографический оракул](docs/ru/CRYPTO_ORACLE.md) | [Krüptograafiline oraakel](docs/et/CRYPTO_ORACLE.md) |
| **Agent Audit Model** | [Agent Audit](docs/en/AGENT_AUDIT_MODEL.md) | [Модель аудита агентов](docs/ru/AGENT_AUDIT_MODEL.md) | [Agentide auditi mudel](docs/et/AGENT_AUDIT_MODEL.md) |
| **Testing Strategy** | [Testing Strategy](docs/en/TESTING_STRATEGY.md) | [Стратегия тестирования](docs/ru/TESTING_STRATEGY.md) | [Testimise strateegia](docs/et/TESTING_STRATEGY.md) |
| **Crypto reference** (algorithms, keys, why tsaToken) | [Crypto reference](docs/en/CRYPTO_REFERENCE.md) | — | — |
| **Architecture diagrams** | [Architecture diagrams](diagrams/architecture.md) (Mermaid: pipeline, trust chain, stack) | | |

### README contents

- [Design: PKI chain and RFC 3161](#design-pki-chain-and-rfc-3161)
- [Prerequisites](#prerequisites)
- [Environment variables](#environment-variables)
- [Setup from scratch](#setup-from-scratch)
- [Quick Start (minimal)](#quick-start-minimal)
- [Run backend](#run-backend)
- [H2 (default) — file-based DB](#h2-default--file-based-db)
- [Run PostgreSQL](#run-postgresql-or-docker)
- [Main AI endpoint](#main-ai-endpoint)
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

**RFC 3161** is a real cryptographic standard used in eIDAS, legal evidence, archival storage, and enterprise PKI — not a toy. This PoC uses the same timestamping mechanism as production PKI systems.

**What we timestamp:** The TSA timestamp is applied to the **signature bytes**, not to the original AI response text. The chain is:

```
AI response text  →  hash(text)  →  sign(hash)  →  timestamp(signature)
                         ↑               ↑                  ↑
                   content digest   we attest content   TSA attests time
```

So: we attest *what* was said (signature over hash); the TSA attests *when* it was signed. This is the classic PKI trust chain.

**In short:** *Timestamp is applied to the signature bytes, not to the original AI response text.*

**BouncyCastle TSP** is used for RFC 3161 requests — the standard, eIDAS-compatible approach. The TSA token is stored as **opaque bytes**; verification of the token is out of scope for this PoC (no need to reimplement PKI).

For details: [Signing](docs/en/SIGNING.md), [Timestamping](docs/en/TIMESTAMPING.md), [Trust model & eIDAS mapping](docs/en/TRUST_MODEL.md), [diagrams (trust chain)](diagrams/architecture.md#6-trust-chain).

---

## Prerequisites

| Requirement | Version | Required |
|-------------|---------|----------|
| Java | 21+ | ✓ backend |
| Node.js | 18+ | ✓ frontend |
| OpenSSL | any | ✓ key generation |
| Maven | 3.6+ (or use `./mvnw`) | ✓ backend |
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

Full list: [.env.example](.env.example). Architecture: [PoC](docs/en/PoC.md), [plan](docs/en/plan.md).

---

## Setup from scratch

**1. Clone and env:**
```bash
git clone <repo> && cd aletheia-ai
cp .env.example .env
```

**2. Generate signing key:**
```bash
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
```
Set in `.env`: `AI_ALETHEIA_SIGNING_KEY_PATH=../ai.key` (or absolute path).

**3. Run backend:**
```bash
cd backend && ./mvnw spring-boot:run
```
- H2 DB created at `backend/data/` (no PostgreSQL)
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console

**4. Run frontend:**
```bash
cd frontend && npm install && npm run dev
```
- Create `frontend/.env.local` with `NEXT_PUBLIC_API_URL=http://localhost:8080` (or copy from `frontend/.env.example`)
- Open http://localhost:3000

**5. Test:** On the main page, enter a prompt and click Send. Requires `OPENAI_API_KEY` in `.env`. For LLM-free test: `curl -X POST http://localhost:8080/api/audit/demo -H "Content-Type: application/json" -d '{"text":"hello"}'`.

---

## Quick Start (minimal)

```bash
cp .env.example .env
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
# Edit .env: AI_ALETHEIA_SIGNING_KEY_PATH=../ai.key
cd backend && ./mvnw spring-boot:run
# In another terminal:
cd frontend && cp .env.example .env.local && npm install && npm run dev
```
Open http://localhost:3000. For POST /api/ai/ask, add `OPENAI_API_KEY` to `.env`.

---

## Run PostgreSQL (optional)

Use when you want PostgreSQL instead of H2. **Option A — Docker Compose:**

```bash
docker-compose up -d postgres
```

**Option B — Single container:**

```bash
docker run -d --name aletheia-db -e POSTGRES_DB=aletheia -e POSTGRES_USER=aletheia -e POSTGRES_PASSWORD=local -p 5432:5432 postgres:15-alpine
```

**Migrations:** Flyway runs automatically when the backend starts. The `ai_response` table is created on first run. No manual migration step needed. For manual SQL, see `backend/src/main/resources/db/schema-ai_response-standalone.sql`. We may switch to Liquibase later for multi-DB support or rollbacks (see `backend/src/main/resources/db/migration/README.md`).

**Using PostgreSQL:** Set in `.env`:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/aletheia
SPRING_DATASOURCE_USERNAME=aletheia
SPRING_DATASOURCE_PASSWORD=local
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
```

---

## Run backend

```bash
cd backend
./mvnw spring-boot:run
# Or: java -jar target/aletheia-backend.jar
```

**.env loading:** Spring Boot loads `.env` from the project root automatically (`spring.config.import`). Set `SPRING_DATASOURCE_URL`, signing key path, TSA mode, etc. in `.env`.

**Default DB:** H2 file-based at `backend/data/aletheia.mv.db` — data persists between runs. Override with `SPRING_DATASOURCE_URL` for PostgreSQL. Default API: `http://localhost:8080`.

---

## H2 (default) — file-based DB

**No PostgreSQL needed** for local development. H2 stores data in `backend/data/aletheia.mv.db`; the folder is created on first run (and ignored by git).

**H2 Console** — inspect the DB at `http://localhost:8080/h2-console` while the backend is running:

| Field | Value |
|-------|-------|
| **JDBC URL** | `jdbc:h2:file:./data/aletheia` |
| **User Name** | `sa` |
| **Password** | *(leave empty)* |

Path `./data/aletheia` is relative to the backend process working directory (`backend/`). **Run backend from `backend/`** so the path resolves correctly.

**H2 Console shows 0 rows but API returns data?** The console must use the **same** JDBC URL as the app. Check `SPRING_DATASOURCE_URL` in `.env` — if it's `jdbc:h2:mem:aletheia`, the console must use that too (not `file:`). For file-based, use `jdbc:h2:file:./data/aletheia`.

**Signing key (required for backend):** PEM path in `ai.aletheia.signing.key-path` or env. Generate:
```bash
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
```
Then set `ai.aletheia.signing.key-path=/path/to/ai.key` (or equivalent env).

**TSA mode:** `AI_ALETHEIA_TSA_MODE=real` (default, DigiCert TSA) or `mock` (deterministic, no network; use for tests or offline dev). DigiCert URL is the default; alternatives: Sectigo, GlobalSign. For a self-hosted RFC 3161 TSA, see [TIMESTAMPING](docs/en/TIMESTAMPING.md).

**Command-line arguments (override .env):** Spring Boot accepts `--property=value`. Useful for one-off runs or CI:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--ai.aletheia.signing.key-path=../ai.key --ai.aletheia.tsa.mode=real --ai.aletheia.tsa.url=http://timestamp.digicert.com"
```

Or with JAR: `java -jar backend.jar --ai.aletheia.signing.key-path=/path/to/ai.key --ai.aletheia.tsa.mode=real --ai.aletheia.tsa.url=http://timestamp.digicert.com`

CLI args override env vars and `application.properties`.

**API documentation (Swagger):** When the backend is running, available at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). OpenAPI JSON at `/v3/api-docs`.

---

## Main AI endpoint

**POST /api/ai/ask** — full flow: prompt → LLM → canonicalize → hash → sign → timestamp → save to DB. Requires `OPENAI_API_KEY` and signing key. Returns verifiable response with `id` for GET /api/ai/verify/:id.

```bash
curl -X POST http://localhost:8080/api/ai/ask -H "Content-Type: application/json" -d '{"prompt":"What is 2+2?"}'
# → { "response": "...", "responseHash": "...", "signature": "...", "tsaToken": "...", "id": 1, "model": "gpt-4" }

curl http://localhost:8080/api/ai/verify/1
# → full record for verification page
```

---

## LLM (OpenAI)

**POST /api/llm/demo** — test LLM completion only (no persistence). Requires `OPENAI_API_KEY` in `.env`. Returns `{"responseText", "modelId"}`.

```bash
curl -X POST http://localhost:8080/api/llm/demo -H "Content-Type: application/json" -d '{"prompt":"What is 2+2?"}'
# → { "responseText": "2+2 equals 4.", "modelId": "gpt-4" }
```

**Env vars:** `OPENAI_API_KEY` (required for real calls), `OPENAI_MODEL` (default: gpt-4). See `.env.example`.

---

## Audit demo (tangible test)

**POST /api/audit/demo** — crypto pipeline + save to DB. Accepts `{ "text": "..." }`, runs canonicalize → hash → sign → timestamp, persists the record, returns the id. Use to verify the full flow without LLM.

```bash
curl -X POST http://localhost:8080/api/audit/demo -H "Content-Type: application/json" -d '{"text":"hello world"}'
# → { "id": 1, "hash": "...", "signature": "...", "tsaToken": "...", ... }

curl http://localhost:8080/api/ai/verify/1
# → full record for verification page
```

Check H2 console (`http://localhost:8080/h2-console`) to see the saved row in `ai_response`.

---

## Crypto demo endpoint

Exposes the crypto pipeline: canonicalize → hash → sign → timestamp. **Works without signing key** — returns hash; signature and `tsaToken` are `null` when key not configured.

**Manual test:**

```bash
# Start backend first (see Run backend above)
cd backend && ./mvnw spring-boot:run

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
- **Response** — AI answer, status (Signed, Timestamped, Verifiable), link to verify page
- **Verify page** — `/verify?id=...` shows hash, signature, TSA token, model, date; backend verification (hashMatch, signatureValid); "Verify hash" for client-side check

**Required:** Set `NEXT_PUBLIC_API_URL=http://localhost:8080` in `frontend/.env.local` (or copy from `frontend/.env.example`). Start the backend first. CORS allows `http://localhost:3000` by default; override with `CORS_ALLOWED_ORIGINS` if needed.

---

## Run tests

**Backend (from `backend/`):**
```bash
./mvnw test
# or: mvn test
```
Runs JUnit 5 tests: `HealthControllerTest` (GET /health → 200, `{"status":"UP"}`), `AletheiaBackendApplicationTests` (context load), `CanonicalizationServiceTest`, `HashServiceTest`, and `SignatureServiceTest` (sign/verify, tampered signature). Uses H2 in-memory for tests; signing tests use in-memory RSA keys (no PEM file required).

**Golden Fixtures:** Test fixtures (hash outputs, signatures, RFC 3161 tokens) are stored in `backend/src/test/resources/fixtures/` for deterministic regression testing. See [fixtures README](backend/src/test/resources/fixtures/README.md) for details.

**Frontend:** `npm test` when test script is added.

Detailed test scope and acceptance criteria per step: see [plan (EN)](docs/en/plan.md#testing-by-step), [plan (RU)](docs/ru/plan.md#тестирование-по-шагам), [plan (ET)](docs/et/plan.md#testimine-sammude-kaupa).

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

`NEXT_PUBLIC_API_URL` must be set at **build time** (baked into the client bundle):

```bash
cd frontend
docker build --build-arg NEXT_PUBLIC_API_URL=http://localhost:8080 -t aletheia-frontend .
docker run -p 3000:3000 aletheia-frontend
```

For production, use the backend URL: `--build-arg NEXT_PUBLIC_API_URL=https://api.example.com`

### .dockerignore

Both `backend/` and `frontend/` have `.dockerignore` that exclude `node_modules`, `.next`, `target`, `.git`, and `.env`. Secrets are passed via env or volumes at runtime.

---

## Deployment

**Chosen approach:** Full stack (Docker + Ansible + GitHub Actions) for automated deployment to a target VM (e.g. `ssh ubuntu@193.40.157.132`).

| Component | Purpose |
|-----------|---------|
| **Docker** | Containerize backend and frontend; docker-compose with PostgreSQL |
| **Ansible** | VM setup (Docker install), .env template, `docker-compose up` |
| **GitHub Actions** | On push to main: tests → build → deploy via SSH/Ansible |

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

**Result:** Frontend at `http://VM:3000`, Backend at `http://VM:8080`. See [deploy/ansible/README.md](deploy/ansible/README.md) for variables, troubleshooting (ai.key directory fix, frontend TypeScript), and verified flow.

**Alternatives:** Ansible-only (no containers), script-only (bash over SSH), Docker Compose only. See [plan Step 8](docs/en/plan.md#step-8--deployment-cicd) for detailed tasks and LLM-readable implementation prompts.

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
