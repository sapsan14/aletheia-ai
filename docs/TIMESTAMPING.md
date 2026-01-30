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

## TSA Endpoint

The TSA (Time-Stamp Authority) endpoint is **external** to the backend. It may be:

- a **local RFC 3161 server** (e.g. OpenTSA, OpenSSL TSA)
- a **test stub** (deterministic mock for unit tests and CI)
- a **public TSA** (future)
- an **eIDAS-qualified TSA** (future)

The backend sends timestamp requests to the configured URL and stores the returned token as opaque bytes.

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

## Related documents

- [Signing](SIGNING.md) — what we sign; signature bytes are what we timestamp.
- [Trust model](TRUST_MODEL.md) — who attests what, eIDAS mapping.
- [Architecture diagrams](../diagrams/architecture.md) — pipeline and trust chain.
- [README](../README.md) — design overview, run instructions.
