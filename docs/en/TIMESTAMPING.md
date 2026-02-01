# Timestamping in Aletheia AI

**RFC 3161 timestamps make AI output provable in time.**

This document describes how timestamping is integrated into the Aletheia backend: TSA endpoint options, error handling, testing strategy, storage, and scope.

---

## Table of contents

- [TSA Endpoint](#tsa-endpoint)
- [Error Handling](#error-handling)
- [Testing Strategy](#testing-strategy)
- [Storage Model](#storage-model)
- [Security and Scope](#security-and-scope)
- [Conceptual Summary](#conceptual-summary)
- [Future Work (Out of Scope)](#future-work-out-of-scope)
- [Final Note](#final-note)
- [Related documents](#related-documents)

---

## Why tsaToken, not a simple timestamp?

A plain `"timestamp": "2026-02-01T12:00:00"` field can be faked — anyone can write any time. The **tsaToken** is an RFC 3161 structure signed by an external TSA (Time-Stamp Authority). A trusted third party attests *when* the data existed; you cannot forge that. See [Crypto Reference — Why tsaToken](CRYPTO_REFERENCE.md#why-tsatoken-not-a-simple-timestamp) for a noob-friendly explanation.

---

## TSA Endpoint

The TSA (Time-Stamp Authority) endpoint is **external** to the backend. It may be:

- a **local RFC 3161 server** (e.g. OpenTSA, OpenSSL TSA)
- a **test stub** (deterministic mock for unit tests and CI)
- a **public TSA** (DigiCert, Sectigo, GlobalSign, FreeTSA)
- an **eIDAS-qualified TSA** (future)

The backend sends timestamp requests to the configured URL and stores the returned token as opaque bytes.

---

## Switching MOCK_TSA / REAL_TSA

Configuration is prepared in `.env.example`:

```
AI_ALETHEIA_TSA_MODE=mock
AI_ALETHEIA_TSA_URL=http://timestamp.digicert.com
```

Typical implementation in `TimestampService`:

```java
@Value("${ai.aletheia.tsa.mode:mock}")  // default: mock
private String tsaMode;

@Value("${ai.aletheia.tsa.url:}")
private String tsaUrl;
```

Selection logic:

- **mode=mock** → `MockTsaServiceImpl` (no network, deterministic)
- **mode=real** → `RealTsaServiceImpl` (HTTP POST to `tsaUrl`)

Spring Boot reads `AI_ALETHEIA_TSA_MODE`, `AI_ALETHEIA_TSA_URL` via `application.properties`:

```properties
ai.aletheia.tsa.mode=${AI_ALETHEIA_TSA_MODE:mock}
ai.aletheia.tsa.url=${AI_ALETHEIA_TSA_URL:}
```

Mode can be set via `.env`, environment variables, or **command-line arguments**:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--ai.aletheia.tsa.mode=real --ai.aletheia.tsa.url=http://timestamp.digicert.com"
```

CLI args override env and `application.properties`.

---

## Real TSA Options

| Option | Description | Complexity |
|--------|-------------|------------|
| **Public TSA** | DigiCert, Sectigo, GlobalSign, etc. | Low |
| **Local TSA** | OpenTSA, OpenSSL TSA, EJBCA | Medium |
| **eIDAS-qualified** | Commercial providers for legal validity | High |

### A. Public TSA (simplest)

- **DigiCert:** `http://timestamp.digicert.com`
- **Sectigo (Comodo):** `http://timestamp.sectigo.com`
- **GlobalSign:** `http://timestamp.globalsign.com`
- **FreeTSA:** `http://freetsa.org/tsr`

Only the URL is needed; the same client code works. No registration required.

### B. Local TSA (dev / isolation)

- **OpenTSA** — RFC 3161 server, can be run in Docker.
- **OpenSSL TSA** — `tsget` + config; requires certificates and `openssl.cnf`.
- **EJBCA** — Full PKI; typically for enterprise use.
- **Docker Compose** — Optional TSA service:

```yaml
tsa:
  image: ghcr.io/digicert/timestamp-authority:latest
  profiles:
    - with-tsa
```

Ensure the image exists and note the port (e.g. 3180). For local use, set `AI_ALETHEIA_TSA_URL=http://localhost:3180`.

### Recommendation

1. **Dev / tests:** `AI_ALETHEIA_TSA_MODE=mock` (default).
2. **Integration checks:** use a public TSA:
   ```bash
   AI_ALETHEIA_TSA_MODE=real
   AI_ALETHEIA_TSA_URL=http://timestamp.digicert.com
   ```
3. **Local TSA (optional):** run OpenTSA or similar, then:
   ```bash
   AI_ALETHEIA_TSA_URL=http://localhost:3180
   ```

Public TSAs require no signup and are suitable for initial RFC 3161 pipeline validation.

---

## Error Handling

The `TimestampService` must handle:

- **connection failures** (timeout, unreachable host)
- **invalid TSA responses** (non-2xx, wrong content type)
- **malformed tokens** (unparseable bytes)

The implementation may choose either:

- **checked or runtime exceptions** (fail fast, caller handles), or  
- **Optional-style responses** (e.g. `Optional<byte[]>` or `Result` type)

The chosen approach must be **clearly documented in code** (e.g. in the service interface and implementation Javadoc).

---

## Testing Strategy

Two testing modes are supported.

### 1. Mock / Stub TSA (default for unit tests)

A deterministic stub may return:

- a fixed byte sequence, or  
- a reproducible mock token

Used for:

- fast unit tests  
- CI pipelines (no external TSA required)  
- isolated verification of pipeline logic  

### 2. Local RFC 3161 TSA (integration tests)

Optional integration testing using:

- OpenSSL TSA  
- OpenTSA  
- minimal local RFC 3161 server  

Test expectations:

- timestamp token is returned  
- token is **parsable** by BouncyCastle  
- generation time (`genTime`) can be extracted (and optionally logged)  

The TSA server itself is **out of scope** for this task — we consume the endpoint, we do not implement it.

---

## Storage Model

The timestamp token is stored together with:

- AI response (text)  
- response hash (SHA-256)  
- digital signature  
- metadata (model, parameters, time)  

Example database field:

```sql
timestamp_token BYTEA
```

The token is **immutable** and must never be modified after storage.

---

## Security and Scope

This PoC **intentionally**:

- does **not** claim legal validity  
- does **not** implement qualified trust services  
- does **not** perform certificate path validation  

However, the **architecture** is designed to be fully compatible with:

- eIDAS-qualified timestamp authorities  
- long-term archival validation  
- regulatory audit scenarios  

So the same pipeline can later be wired to a qualified TSA without redesign.

---

## Conceptual Summary

This timestamping model does **not** attempt to make AI “truthful”.

It makes AI output **provable**.

The system answers one precise question:

> *“Can we prove that this exact output existed at this exact time?”*

That capability becomes essential when AI systems evolve from chat tools into autonomous agents influencing real-world decisions.

---

## Future Work (Out of Scope)

The following are **intentionally excluded** from the PoC to keep scope controlled:

- Timestamp **verification** service  
- Qualified TSA integration  
- Long-term validation (LTV)  
- Evidence Record Syntax (ERS / RFC 4998)  
- Regulatory trust profiles  

They remain as natural extensions for a future phase.

---

## Final Note

Timestamping is not about shifting responsibility.

It is about **preserving reality**.

Once an AI response is signed and timestamped, history can no longer be rewritten.

---

## Testing with RFC 3161 Test Vectors

For implementation testing, **RFC 3161 test vectors** and **golden fixtures** provide deterministic reference outputs:

- **Test vectors** = concrete examples: given hash/digest → expected TSA response
- **Golden fixtures** = stored reference tokens for regression testing

**Example test vector:**

```json
{
  "digest_sha256": "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
  "tst_token_base64": "MIIC...",
  "expected_time": "2026-01-01T00:00:00Z",
  "policy_oid": "1.2.3.4.5.6.7.8"
}
```

**Purpose:**
- Verify RFC 3161 client/server behaves correctly
- Include valid cases and edge cases (invalid hashes, wrong algorithms)
- Enable byte-level regression testing against known-good outputs

**Sources:**
- BouncyCastle `tsp` test suites
- Open-source TSA implementations
- Aletheia MOCK_TSA (see [MOCK_TSA](MOCK_TSA.md))

For deterministic testing in Aletheia, use MOCK_TSA to generate reproducible tokens. See [Cryptographic Oracle](CRYPTO_ORACLE.md) for the testing philosophy.

---

## Related documents

- [Crypto Reference](CRYPTO_REFERENCE.md) — algorithms, keys, key generation, why tsaToken (noob-friendly).
- [Signing](SIGNING.md) — what we sign; signature bytes are what we timestamp.
- [Trust model](TRUST_MODEL.md) — who attests what, eIDAS mapping.
- [MOCK_TSA](MOCK_TSA.md) — deterministic TSA for testing, RFC 3161 test vectors.
- [Cryptographic Oracle](CRYPTO_ORACLE.md) — oracle pattern for timestamp testing.
- [Architecture diagrams](../../diagrams/architecture.md) — pipeline and trust chain.
- [README](../../README.md) — design overview, run instructions.
