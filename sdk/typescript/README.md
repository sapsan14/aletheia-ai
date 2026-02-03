# Aletheia TypeScript SDK

Minimal Node/TypeScript client for the Aletheia AI API.

## Install (from repo)

```bash
cd sdk/typescript
npm install
npm run build
```

## Usage

```ts
import { sign, verify, getEvidence } from "aletheia-client";

// Configure base URL via env or pass baseUrl explicitly
// export ALETHEIA_API_URL="http://localhost:8080"

const signed = await sign("Hello from my LLM", { modelId: "external-llm" });
const recordId = signed.id;

const verifyRecord = await verify(recordId);
console.log(verifyRecord.signatureValid);

const data = await getEvidence(recordId);
```

## Environment variables

- `ALETHEIA_API_URL` — base URL of the backend (e.g. `http://localhost:8080`)

## Publish (manual)

```bash
npm publish
```
