# Aletheia AI

**Verifiable AI responses with signing and timestamps.**

PoC for cryptographically signed and timestamped LLM answers so that responses can be proven, not just trusted.

Stack (PoC): Next.js, Java Spring Boot, PostgreSQL, OpenSSL/BouncyCastle, RFC 3161 TSA, one LLM (OpenAI/Gemini/Mistral).

---

## Documentation

Docs are grouped by language in `docs/<lang>/` (en, ru, et). Same filenames in each folder: `PoC.md`, `plan.md`, `SIGNING.md`, `TIMESTAMPING.md`, `TRUST_MODEL.md`.

| Topic | EN | RU | ET |
|-------|----|----|-----|
| **PoC (architecture & stack)** | [docs/en/PoC.md](docs/en/PoC.md) | [docs/ru/PoC.md](docs/ru/PoC.md) | [docs/et/PoC.md](docs/et/PoC.md) |
| **Implementation plan** | [docs/en/plan.md](docs/en/plan.md) | [docs/ru/plan.md](docs/ru/plan.md) | [docs/et/plan.md](docs/et/plan.md) |
| **Signing** | [docs/en/SIGNING.md](docs/en/SIGNING.md) | [docs/ru/SIGNING.md](docs/ru/SIGNING.md) | [docs/et/SIGNING.md](docs/et/SIGNING.md) |
| **Timestamping** | [docs/en/TIMESTAMPING.md](docs/en/TIMESTAMPING.md) | [docs/ru/TIMESTAMPING.md](docs/ru/TIMESTAMPING.md) | [docs/et/TIMESTAMPING.md](docs/et/TIMESTAMPING.md) |
| **Trust model & eIDAS** | [docs/en/TRUST_MODEL.md](docs/en/TRUST_MODEL.md) | [docs/ru/TRUST_MODEL.md](docs/ru/TRUST_MODEL.md) | [docs/et/TRUST_MODEL.md](docs/et/TRUST_MODEL.md) |
| **Architecture diagrams** | [diagrams/architecture.md](diagrams/architecture.md) (Mermaid: pipeline, trust chain, stack) | | |

### README contents

- [Design: PKI chain and RFC 3161](#design-pki-chain-and-rfc-3161)
- [Prerequisites](#prerequisites)
- [Run PostgreSQL](#run-postgresql-or-docker)
- [Run backend](#run-backend)
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

## Prerequisites

- **Java 21+** (backend)
- **Node.js 18+** (frontend)
- **PostgreSQL 15+** (or Docker)
- **OpenSSL** (key generation, optional local TSA)
- Env: LLM API key (OpenAI/Gemini/Mistral), DB URL, TSA URL, signing key path — see [PoC](docs/en/PoC.md) and [plan](docs/en/plan.md) for details.

---

## Run PostgreSQL (or Docker)

**Docker:**

```bash
docker run -d --name aletheia-db -e POSTGRES_DB=aletheia -e POSTGRES_USER=aletheia -e POSTGRES_PASSWORD=local -p 5432:5432 postgres:15-alpine
```

**Local:** install PostgreSQL 15+, create database `aletheia`, run migrations (see backend repo / Flyway/Liquibase when available).

---

## Run backend

```bash
# From backend directory (when implemented)
./mvnw spring-boot:run
# Or: java -jar target/aletheia-backend.jar
```

Set env: `SPRING_DATASOURCE_URL`, `OPENAI_API_KEY` (or GEMINI/Mistral), TSA URL, signing key path. Default API: `http://localhost:8080`.

**Signing key (required for backend):** PEM path in `ai.aletheia.signing.key-path` or env. Generate:
```bash
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
```
Then set `ai.aletheia.signing.key-path=/path/to/ai.key` (or equivalent env).

---

## Run frontend

```bash
# From frontend directory (when implemented)
npm install
npm run dev
```

Set `NEXT_PUBLIC_API_URL=http://localhost:8080` (or backend URL). Open http://localhost:3000.

---

## Run tests

**Backend (from `backend/`):**
```bash
./mvnw test
# or: mvn test
```
Runs JUnit 5 tests: `HealthControllerTest` (GET /health → 200, `{"status":"UP"}`), `AletheiaBackendApplicationTests` (context load), `CanonicalizationServiceTest`, `HashServiceTest`, and `SignatureServiceTest` (sign/verify, tampered signature). Uses H2 in-memory for tests; signing tests use in-memory RSA keys (no PEM file required).

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
