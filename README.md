# Aletheia AI

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Aletheia signs external AI outputs for regulated workflows.**

Verifiable AI responses with signing and timestamps. Proof, not just trust.

**For researchers and practitioners in trustworthy AI:** Aletheia provides **cryptographic attestation** of AI-generated content (e.g. LLM responses, agent outputs): digital signature over a canonical representation plus optional TSA timestamp. The result is an **Evidence Package** (hash, signature, timestamp, metadata) that auditors can verify offline. Target use: regulated domains (compliance, legal, healthcare) where you need an audit trail and non-repudiation, not just logging. Stack: Spring Boot backend, Next.js frontend; REST API for “prompt → signed response” and for sign-only flows (e.g. attesting your own model’s output). Designed to integrate with conversational UIs and automated pipelines; see [docs](docs/README.md) for trust model, API, and deployment.

---

## Documentation

| You are… | Start here |
|----------|------------|
| **User** (concepts, trust, demo) | [Users — concepts and trust](docs/README.md#users) |
| **Developer** (API, run, test, deploy) | [Developers — API and setup](docs/README.md#developers) |
| **Partner** (value, integration) | [Partners — value and use cases](docs/README.md#partners) |

Full index and topic list: [docs/README.md](docs/README.md).

---

## Quick start

```bash
git clone <repo> && cd aletheia-ai
cp .env.example .env
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
# In .env set: AI_ALETHEIA_SIGNING_KEY_PATH=./ai.key
```

```bash
cd backend && mvn spring-boot:run
```

```bash
cd frontend && cp .env.example .env.local && npm install && npm run dev
```

- Backend: http://localhost:8080  
- Frontend: http://localhost:3000  
- Set `OPENAI_API_KEY` in `.env` for the AI demo. Set `NEXT_PUBLIC_API_URL=http://localhost:8080` in `frontend/.env.local`.

All other details (env vars, Evidence Package, offline verifier, deployment): [docs/README.md](docs/README.md) → Developers.

---

## License

MIT. See [LICENSE](LICENSE). Authorship: [docs/README.md#authorship](docs/README.md#authorship).
