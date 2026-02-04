# Quick start (developers)

Environment setup, run backend/frontend, and main commands. For high-level overview see [root README](../../../README.md).

---

## One-time setup

```bash
git clone <repo> && cd aletheia-ai
cp .env.example .env
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
```

In `.env` set:

- `AI_ALETHEIA_SIGNING_KEY_PATH=./ai.key` (or absolute path)

Optional (PQC):

```bash
cd backend
mvn -q compile exec:java -Dexec.mainClass="ai.aletheia.crypto.PqcKeyGen" -Dexec.args="."
```

Then in `.env`: `AI_ALETHEIA_PQC_ENABLED=true`, `AI_ALETHEIA_PQC_KEY_PATH=./backend/ai_pqc.key` (or your path).

---

## Environment variables (main)

| Variable | When needed | Default / note |
|----------|-------------|----------------|
| `AI_ALETHEIA_SIGNING_KEY_PATH` | Signing, POST /api/ai/ask | Path to PEM (e.g. `./ai.key`) |
| `OPENAI_API_KEY` | LLM, POST /api/ai/ask | — |
| `SPRING_DATASOURCE_URL` | DB | `jdbc:h2:file:./data/aletheia` (H2) |
| `AI_ALETHEIA_TSA_MODE` | TSA | `real` (DigiCert) or `mock` (tests) |
| `AI_ALETHEIA_PQC_ENABLED` | PQC signing | `false` |
| `NEXT_PUBLIC_API_URL` | Frontend | `http://localhost:8080` |

Full list: [.env.example](../../../.env.example).

---

## Run backend

```bash
cd backend && mvn spring-boot:run
```

- API: http://localhost:8080  
- Swagger: http://localhost:8080/swagger-ui.html  
- H2 console: http://localhost:8080/h2-console (if H2)

---

## Run frontend

```bash
cd frontend && cp .env.example .env.local && npm install && npm run dev
```

Set `NEXT_PUBLIC_API_URL=http://localhost:8080` in `frontend/.env.local`.  
Open http://localhost:3000.

---

## Run tests

**Backend:**

```bash
cd backend && mvn test
```

**Frontend:**

```bash
cd frontend && npm test
```

---

## Evidence Package and offline verifier

After POST /api/ai/ask or /api/audit/demo, get the response `id`, then:

```bash
curl -o evidence.aep "http://localhost:8080/api/ai/evidence/1"
```

Verify offline (no backend call):

```bash
java -jar backend/target/aletheia-verifier.jar evidence.aep
```

Build verifier JAR: `cd backend && mvn package -Pverifier -DskipTests`.  
See [scripts README](../../../scripts/README.md) for OpenSSL-only option.

---

## Deployment

Ansible + Docker: [deploy/ansible/README.md](../../../deploy/ansible/README.md).
