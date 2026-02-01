# Aletheia AI — Architecture Diagrams

Detailed Mermaid diagrams for the PoC: verifiable AI responses with signing and RFC 3161 timestamps.

---

## 1. High-level system architecture

```mermaid
graph TB
  subgraph User["User"]
    U[Browser]
  end

  subgraph Frontend["Frontend Next.js React"]
    direction TB
    P[Prompt input]
    B[Send button]
    R[Response display]
    S[Status signed timestamped verifiable]
    V[Verify this response link]
    P --> B
    B --> R
    R --> S
    S --> V
  end

  subgraph Backend["Backend API Java Spring Boot"]
    direction TB
    API[AiController]
    API --> LLM_C[LLMClient]
    API --> CRYPTO[Crypto layer]
    API --> AUDIT[AuditRecordService]
    CRYPTO --> H[HashService]
    CRYPTO --> SIG[SignatureService]
    CRYPTO --> TSA_S[TimestampService]
  end

  subgraph External["External services"]
    LLM[LLM API - OpenAI Gemini Mistral]
    TSA[RFC 3161 TSA - local or public]
  end

  subgraph DB["PostgreSQL"]
    direction TB
    T[(ai_response)]
    T --- T1[prompt, response]
    T --- T2[response_hash, signature]
    T --- T3[tsa_token, llm_model]
    T --- T4[created_at, metadata]
  end

  U <-->|"1. POST prompt"| Frontend
  Frontend <-->|"2. POST ask - 3. Response hash signature tsaToken id"| Backend
  Backend -->|"prompt to completion"| LLM
  LLM -->|"response text"| Backend
  Backend -->|"hash to timestamp request"| TSA
  TSA -->|"TSA token"| Backend
  Backend <-->|"4. Store load audit record"| DB
  U <-->|"5. GET verify by id"| Frontend
  Frontend <-->|"6. Fetch verification data"| Backend
```

---

## 2. Backend request pipeline (7 steps)

```mermaid
graph LR
  subgraph In["Input"]
    A[Prompt]
  end

  subgraph Pipeline["Backend pipeline"]
    direction LR
    S1["1. Send prompt to LLM"]
    S2["2. Receive response"]
    S3["3. Canonicalize text"]
    S4["4. Hash SHA-256"]
    S5["5. Sign hash"]
    S6["6. Timestamp RFC 3161"]
    S7["7. Store in DB"]
    S1 --> S2 --> S3 --> S4 --> S5 --> S6 --> S7
  end

  subgraph Out["Output"]
    O[response hash signature tsaToken id]
  end

  A --> S1
  S7 --> O
```

---

## 3. Backend module structure

```mermaid
graph TB
  subgraph API["api"]
    CTRL[AiController - POST ask GET verify]
  end

  subgraph LLM_M["llm"]
    LLM_CLIENT[LLMClient - complete prompt to response modelId]
  end

  subgraph CRYPTO_M["crypto"]
    HASH[HashService - hash bytes to hex]
    SIG_SVC[SignatureService - sign and verify]
    TSA_SVC[TimestampService - getTimestamp digest to token]
  end

  subgraph AUDIT_M["audit"]
    AUDIT_SVC[AuditRecordService - save record to id]
  end

  subgraph DB_M["db"]
    REPO[AiResponseRepository - JPA ai_response table]
  end

  CTRL --> LLM_CLIENT
  CTRL --> HASH
  CTRL --> SIG_SVC
  CTRL --> TSA_SVC
  CTRL --> AUDIT_SVC
  AUDIT_SVC --> REPO
  HASH --> SIG_SVC
  SIG_SVC --> TSA_SVC
```

---

## 4. Data flow: Ask & Verify

```mermaid
sequenceDiagram
  participant U as User
  participant F as Frontend
  participant API as AiController
  participant LLM as LLM Client
  participant ExtLLM as LLM API
  participant Crypto as Hash Sign TSA
  participant TSA as TSA Server
  participant DB as PostgreSQL

  Note over U,DB: Ask flow
  U->>F: Enter prompt, click Send
  F->>API: POST api ask with prompt
  API->>LLM: complete(prompt)
  LLM->>ExtLLM: request
  ExtLLM-->>LLM: response text, modelId
  LLM-->>API: responseText, modelId
  API->>Crypto: canonicalize hash sign timestamp
  Crypto->>TSA: RFC 3161 request
  TSA-->>Crypto: token
  Crypto-->>API: hash, signature, tsaToken
  API->>DB: save(prompt, response, hash, signature, tsaToken, model)
  DB-->>API: id
  API-->>F: response responseHash signature tsaToken id model
  F-->>U: Show response and status signed timestamped verifiable

  Note over U,DB: Verify flow
  U->>F: Click "Verify this response"
  F->>API: GET api verify by id
  API->>DB: findById(id)
  DB-->>API: record
  API-->>F: prompt response hash signature tsaToken model createdAt
  F-->>U: Show verification data recompute hash verify signature
```

---

## 5. Crypto layer detail

```mermaid
graph LR
  subgraph Input["Response text"]
    T[LLM response]
  end

  subgraph Canon["Canonicalization"]
    C[NFC newline trim]
  end

  subgraph Hash["HashService"]
    H[SHA-256]
  end

  subgraph Sign["SignatureService"]
    S[RSA ECDSA BouncyCastle ai.key]
  end

  subgraph Stamp["TimestampService"]
    TS[RFC 3161 request to TSA]
  end

  T --> C
  C -->|"canonical bytes"| H
  H -->|"hex 64 chars"| S
  S -->|"signature bytes"| TS
  H -->|"digest for TSA"| TS
  TS -->|"tsa_token"| Out[Store with record]
  S --> Out
  H --> Out
```

---

## 6. Trust chain

Who attests what: we attest *content* (signature over hash); TSA attests *time* (timestamp over signature bytes). See [Trust model (EN)](../docs/en/TRUST_MODEL.md).

```mermaid
graph LR
  subgraph Content["Content attestation (backend)"]
    T[AI text]
    C[Canonicalize]
    H[Hash SHA-256]
    S[Sign]
    T --> C --> H --> S
  end

  subgraph Time["Time attestation (TSA)"]
    TS[Timestamp RFC 3161]
    ST[Store]
    S --> TS --> ST
  end

  H -.->|"we attest: digest = content"| S
  S -.->|"TSA attests: existed at T"| TS
```

---

## 7. Stack overview

```mermaid
graph TB
  subgraph Stack["PoC stack"]
    direction TB
    subgraph FE["Frontend"]
      N[Next.js]
    end
    subgraph BE["Backend"]
      J[Java Spring Boot]
    end
    subgraph CR["Crypto"]
      O[OpenSSL + BouncyCastle]
      K[RSA ECDSA key]
      T[RFC 3161 local TSA]
    end
    subgraph Data["Data"]
      P[(PostgreSQL)]
    end
    subgraph AI["LLM"]
      L[Gemini or OpenAI or Mistral]
    end
  end

  FE <--> BE
  BE <--> CR
  BE <--> Data
  BE <--> AI
```

---

## 8. RSA PKCS#1 v1.5 signature padding (why tokens differ)

RSA signatures use random padding. Same hash + same key → different padding → different bytes. See [CRYPTO_REFERENCE — RSA signature randomness](../docs/en/CRYPTO_REFERENCE.md#rsa-signature-randomness-and-semantic-determinism-learners).

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│  PKCS#1 v1.5 Signature Block (256 bytes for 2048-bit key)                            │
├──────┬──────┬─────────────────────────────┬──────┬───────────────────┬───────────────┤
│ [00] │ [01] │  [FF FF FF ... FF]          │ [00] │ [hash_algorithm]  │ [hash_value]  │
│      │      │  ← RANDOM (SecureRandom)    │      │ (OID)             │ (32 bytes)    │
└──────┴──────┴─────────────────────────────┴──────┴───────────────────┴───────────────┘
  block  sig     padding varies each call       sep    fixed              fixed
  start  type
```

**Flow:** `sign(hash)` → SecureRandom fills padding → RSA applied → different bytes every time.

---

## Related documentation

- **PoC:** [EN](../docs/en/PoC.md) · [RU](../docs/ru/PoC.md) · [ET](../docs/et/PoC.md)
- **Plan:** [EN](../docs/en/plan.md) · [RU](../docs/ru/plan.md) · [ET](../docs/et/plan.md)
- **Signing:** [EN](../docs/en/SIGNING.md) · [RU](../docs/ru/SIGNING.md) · [ET](../docs/et/SIGNING.md)
- **Timestamping:** [EN](../docs/en/TIMESTAMPING.md) · [RU](../docs/ru/TIMESTAMPING.md) · [ET](../docs/et/TIMESTAMPING.md)
- **Trust model & eIDAS:** [EN](../docs/en/TRUST_MODEL.md) · [RU](../docs/ru/TRUST_MODEL.md) · [ET](../docs/et/TRUST_MODEL.md)
- **MOCK_TSA:** [EN](../docs/en/MOCK_TSA.md) · [RU](../docs/ru/MOCK_TSA.md)
- **Crypto reference** (algorithms, padding, digest, serial): [EN](../docs/en/CRYPTO_REFERENCE.md)
- **Cryptographic Oracle:** [EN](../docs/en/CRYPTO_ORACLE.md) · [RU](../docs/ru/CRYPTO_ORACLE.md) · [ET](../docs/et/CRYPTO_ORACLE.md)
- **Agent Audit Model:** [EN](../docs/en/AGENT_AUDIT_MODEL.md) · [RU](../docs/ru/AGENT_AUDIT_MODEL.md) · [ET](../docs/et/AGENT_AUDIT_MODEL.md)
- **Testing Strategy:** [EN](../docs/en/TESTING_STRATEGY.md) · [RU](../docs/ru/TESTING_STRATEGY.md) · [ET](../docs/et/TESTING_STRATEGY.md)
- **README:** [../README.md](../README.md)
