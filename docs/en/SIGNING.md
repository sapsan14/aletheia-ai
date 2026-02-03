# Signing in Aletheia AI

**Digital signatures make AI output attributable and tamper-evident.**

This document describes how signing is integrated into the Aletheia backend: what we sign, key management, error handling, testing, storage, and scope. For algorithms and key generation, see [Crypto reference](CRYPTO_REFERENCE.md).

---

## Table of contents

- [What we sign](#what-we-sign)
- [Key management](#key-management)
- [Interface and algorithm](#interface-and-algorithm)
- [Error handling](#error-handling)
- [Testing strategy](#testing-strategy)
- [Storage model](#storage-model)
- [Security and scope](#security-and-scope)
- [Related documents](#related-documents)

---

## What we sign

We sign the **hash** of the canonical AI response, not the raw text.

The chain is:

```
AI response text  →  canonicalize  →  hash (SHA-256)  →  sign (RSA)
                                                              ↓
                                              signature bytes (used for timestamping)
```

- **Canonicalization** ensures the same logical content always yields the same bytes (Unicode NFC, line endings, whitespace).
- **Hash** (SHA-256) produces a fixed-size digest; we sign that digest.
- **Signature** binds the digest to our private key; the TSA then timestamps the **signature bytes** (see [Timestamping](TIMESTAMPING.md)).

So we attest *what* was said; the TSA attests *when* it was signed. See [Trust model](TRUST_MODEL.md) and [diagrams](../../diagrams/architecture.md).

---

## Key management

- **Format:** PEM (RSA private key). Generate with OpenSSL:
  ```bash
  openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
  ```
- **Location:** Configurable path via `ai.aletheia.signing.key-path` (file or `classpath:...`). Optional at startup; sign/verify fail with a clear message if the key is not loaded.
- **PoC scope:** One key is enough; key rotation is out of scope. No HSM; key is loaded from file or classpath.

**Post-quantum (PQC):** Optionally, a second ML-DSA (Dilithium) signature can be produced over the same hash for long-term verification. Set `ai.aletheia.signing.pqc-enabled=true` and `ai.aletheia.signing.pqc-key-path` to the path of an ML-DSA private key. PQC keys can be generated with a Bouncy Castle PQC key generation utility (see [Plan PQC](PLAN_PQC.md)). If not set, behaviour is unchanged (classical RSA only).

---

## Interface and algorithm

- **Sign:** `sign(hashHex)` or `sign(hashBytes)` → signature as byte[] or Base64.
- **Verify:** `verify(hashHex, signatureBase64)` or `verify(hashBytes, signatureBytes)` → boolean.

Algorithm: **RSA with SHA-256** (PKCS#1 v1.5, DigestInfo). Implemented with BouncyCastle; the public key is derived from the private key for verification.

The service is **neutral**: it signs and verifies byte digests. The pipeline passes the hash of the canonical response; no AI-specific types in the interface.

---

## Error handling

The `SignatureService` must handle:

- **key not configured** (empty path or missing file) — fail at first sign/verify with a clear message.
- **invalid input** (null hash, wrong length, non-hex) — `IllegalArgumentException`.
- **key load failure** (bad PEM, unsupported format) — fail at load or first use; document in Javadoc.

The chosen behaviour (optional key at startup vs. required) is documented in code and README.

---

## Testing strategy

- **Unit tests:** In-memory RSA key pair (no PEM file); or test PEM in `src/test/resources` with `classpath:test-signing-key.pem`. Tests cover: sign then verify succeeds; tampered signature returns false; invalid input throws.
- **CI:** No external key required; tests use in-memory keys or a committed test PEM (test key only).
- **Integration:** If the app runs with a real key path, sign/verify use that key; no separate “signing server”.

---

## Storage model

The signature is stored together with:

- AI response (text)
- response hash (SHA-256 hex or bytes)
- timestamp token (opaque bytes — see [Timestamping](TIMESTAMPING.md))
- metadata (model, parameters, time)

Example database field:

```sql
signature BYTEA
```

The signature is **immutable** and must never be modified after storage.

---

## Security and scope

This PoC **intentionally**:

- does **not** implement qualified electronic signatures (QES) under eIDAS
- does **not** use an HSM or key ceremony
- uses a **single** key; rotation is out of scope

The **architecture** is designed so that the same pipeline can later be wired to:

- qualified trust service providers
- eIDAS-compliant signing (see [Trust model — eIDAS mapping](TRUST_MODEL.md#eidas-mapping-non-qualified--qualified))

---

## Related documents

- [Crypto Reference](CRYPTO_REFERENCE.md) — algorithms, keys, key generation (noob-friendly).
- [Timestamping](TIMESTAMPING.md) — what we timestamp (signature bytes), TSA, storage.
- [Trust model](TRUST_MODEL.md) — trust chain, who attests what, eIDAS mapping.
- [Plan PQC](PLAN_PQC.md) — optional post-quantum (ML-DSA) hybrid signing; dual-signing pipeline and Evidence Package extension.
- [Architecture diagrams](../../diagrams/architecture.md) — pipeline and crypto layer.
- [README](../../README.md) — design overview, run instructions.
