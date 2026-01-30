# Aletheia AI

**Verifiable AI responses with signing and timestamps.**

PoC for cryptographically signed and timestamped LLM answers so that responses can be proven, not just trusted. See **[PoC (architecture & stack)](docs/PoC.ru.md)** for full spec — also [docs/PoC.et.md](docs/PoC.et.md), [plan](docs/plan.en.md), [diagrams](diagrams/architecture.md).

Stack (PoC): Next.js, Java Spring Boot, PostgreSQL, OpenSSL/BouncyCastle, RFC 3161 TSA, one LLM (OpenAI/Gemini/Mistral).

---

## Prerequisites

- **Java 17+** (backend)
- **Node.js 18+** (frontend)
- **PostgreSQL 15+** (or Docker)
- **OpenSSL** (key generation, optional local TSA)
- Env: LLM API key (OpenAI/Gemini/Mistral), DB URL, TSA URL, signing key path — see [PoC](docs/PoC.ru.md) and plan for details.

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

Set env: `SPRING_DATASOURCE_URL`, `OPENAI_API_KEY` (or GEMINI/Mistral), TSA URL, key path. Default API: `http://localhost:8080`.

---

## Run frontend

```bash
# From frontend directory (when implemented)
npm install
npm run dev
```

Set `NEXT_PUBLIC_API_URL=http://localhost:8080` (or backend URL). Open http://localhost:3000.

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
