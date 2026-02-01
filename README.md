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
| **PoC (architecture & stack)** | [docs/en/PoC.md](docs/en/PoC.md) | [docs/ru/PoC.md](docs/ru/PoC.md) | [docs/et/PoC.md](docs/et/PoC.md) |
| **Implementation plan** | [docs/en/plan.md](docs/en/plan.md) | [docs/ru/plan.md](docs/ru/plan.md) | [docs/et/plan.md](docs/et/plan.md) |
| **Signing** | [docs/en/SIGNING.md](docs/en/SIGNING.md) | [docs/ru/SIGNING.md](docs/ru/SIGNING.md) | [docs/et/SIGNING.md](docs/et/SIGNING.md) |
| **Timestamping** | [docs/en/TIMESTAMPING.md](docs/en/TIMESTAMPING.md) | [docs/ru/TIMESTAMPING.md](docs/ru/TIMESTAMPING.md) | [docs/et/TIMESTAMPING.md](docs/et/TIMESTAMPING.md) |
| **Trust model & eIDAS** | [docs/en/TRUST_MODEL.md](docs/en/TRUST_MODEL.md) | [docs/ru/TRUST_MODEL.md](docs/ru/TRUST_MODEL.md) | [docs/et/TRUST_MODEL.md](docs/et/TRUST_MODEL.md) |
| **MOCK_TSA (testing)** | [docs/en/MOCK_TSA.md](docs/en/MOCK_TSA.md) | [docs/ru/MOCK_TSA.md](docs/ru/MOCK_TSA.md) | — |
| **Cryptographic Oracle** | [docs/en/CRYPTO_ORACLE.md](docs/en/CRYPTO_ORACLE.md) | [docs/ru/CRYPTO_ORACLE.md](docs/ru/CRYPTO_ORACLE.md) | [docs/et/CRYPTO_ORACLE.md](docs/et/CRYPTO_ORACLE.md) |
| **Agent Audit Model** | [docs/en/AGENT_AUDIT_MODEL.md](docs/en/AGENT_AUDIT_MODEL.md) | [docs/ru/AGENT_AUDIT_MODEL.md](docs/ru/AGENT_AUDIT_MODEL.md) | [docs/et/AGENT_AUDIT_MODEL.md](docs/et/AGENT_AUDIT_MODEL.md) |
| **Testing Strategy** | [docs/en/TESTING_STRATEGY.md](docs/en/TESTING_STRATEGY.md) | [docs/ru/TESTING_STRATEGY.md](docs/ru/TESTING_STRATEGY.md) | [docs/et/TESTING_STRATEGY.md](docs/et/TESTING_STRATEGY.md) |
| **Crypto reference** (algorithms, keys, why tsaToken) | [docs/en/CRYPTO_REFERENCE.md](docs/en/CRYPTO_REFERENCE.md) | — | — |
| **Architecture diagrams** | [diagrams/architecture.md](diagrams/architecture.md) (Mermaid: pipeline, trust chain, stack) | | |

### README contents

- [Design: PKI chain and RFC 3161](#design-pki-chain-and-rfc-3161)
- [Prerequisites](#prerequisites)
- [Run backend](#run-backend)
- [H2 (default) — file-based DB](#h2-default--file-based-db)
- [Run PostgreSQL](#run-postgresql-or-docker)
- [Audit demo (tangible test)](#audit-demo-tangible-test)
- [Crypto demo endpoint](#crypto-demo-endpoint)
- [Run frontend](#run-frontend)
- [Run tests](#run-tests)
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

## Quick Start

1. **Copy environment template:**
   ```bash
   cp .env.example .env
   # Edit .env: at minimum, set AI_ALETHEIA_SIGNING_KEY_PATH=../ai.key
   ```

2. **Generate signing key:**
   ```bash
   openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
   ```

3. **Run backend:**
   ```bash
   cd backend && ./mvnw spring-boot:run
   ```
   Uses H2 file-based DB by default — no PostgreSQL needed for local dev.

4. **Run frontend:**
   ```bash
   cd frontend && npm install && npm run dev
   ```
   Open http://localhost:3000

**Optional:** For PostgreSQL, start `docker-compose up -d postgres` and set `SPRING_DATASOURCE_URL` in `.env` (see [Run PostgreSQL](#run-postgresql-or-docker)).

---

## Prerequisites

- **Java 21+** (backend)
- **Node.js 18+** (frontend)
- **OpenSSL** (key generation)
- **PostgreSQL 15+** (optional — only when using PostgreSQL; default is H2)
- Env: LLM API key (OpenAI/Gemini/Mistral), signing key path — see [PoC](docs/en/PoC.md) and [plan](docs/en/plan.md) for details.

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

**TSA mode:** `AI_ALETHEIA_TSA_MODE=mock` (default, deterministic) or `real` (requires `AI_ALETHEIA_TSA_URL`). See [TIMESTAMPING](docs/en/TIMESTAMPING.md).

**Command-line arguments (override .env):** Spring Boot accepts `--property=value`. Useful for one-off runs or CI:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--ai.aletheia.signing.key-path=../ai.key --ai.aletheia.tsa.mode=real --ai.aletheia.tsa.url=http://timestamp.digicert.com"
```

Or with JAR: `java -jar backend.jar --ai.aletheia.signing.key-path=/path/to/ai.key --ai.aletheia.tsa.mode=real --ai.aletheia.tsa.url=http://timestamp.digicert.com`

CLI args override env vars and `application.properties`.

**API documentation (Swagger):** When implemented (see [plan — Task 7.3](docs/en/plan.md#task-73--swagger--openapi-implement-when-needed)), available at `http://localhost:8080/swagger-ui.html`.

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

**With signing key:** `signature` = Base64 RSA signature, `signatureStatus` = `"SIGNED"`, `tsaToken` = RFC 3161 timestamp token (Base64), `tsaStatus` = `"MOCK_TSA"` (default) or `"REAL_TSA"` when using external TSA. See [TIMESTAMPING](docs/en/TIMESTAMPING.md) for TSA mode switching.

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
- **Prompt** — text area for entering questions (Task 1.3)
- **Send — Coming soon** — button (disabled; will connect to backend in Step 6)
- **Response** — area where AI answer will appear

Set `NEXT_PUBLIC_API_URL=http://localhost:8080` when connecting to backend (Step 6).

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
