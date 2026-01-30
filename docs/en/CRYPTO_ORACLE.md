# Cryptographic Test Oracle

**Deterministic reference outputs for verifying correctness of cryptographic implementations.**

---

## Table of Contents

- [What is a Cryptographic Test Oracle](#what-is-a-cryptographic-test-oracle)
- [Why Cryptographic Oracles Are Needed](#why-cryptographic-oracles-are-needed)
- [What an Oracle Is (and Is Not)](#what-an-oracle-is-and-is-not)
- [Example: Timestamp Oracle](#example-timestamp-oracle)
- [Relationship to MOCK_TSA](#relationship-to-mock_tsa)
- [Oracle Pattern in Practice](#oracle-pattern-in-practice)
- [Analogy: Dashcam for Cryptography](#analogy-dashcam-for-cryptography)
- [Why This Matters for AI / LLM Agents](#why-this-matters-for-ai--llm-agents)
- [Oracle Types in Aletheia](#oracle-types-in-aletheia)
- [Implementation Strategy](#implementation-strategy)
- [Summary](#summary)
- [Related Documents](#related-documents)

---

## What is a Cryptographic Test Oracle

A **cryptographic test oracle** is a deterministic and reproducible reference output used to verify correctness of cryptographic implementations.

In cryptography, many operations are **non-deterministic by design**:

- Digital signatures use random nonces (ECDSA, RSA-PSS)
- Timestamps depend on wall-clock time
- Entropy sources differ per system
- External services (TSA, HSM, LLMs) produce variable results
- Padding schemes introduce randomness

Because of this, traditional assertions like:

```java
assertEquals(expected, actual);
```

are often **impossible** without an oracle.

**The oracle defines what is considered "correct".**

---

## Why Cryptographic Oracles Are Needed

Without a cryptographic oracle, tests usually degrade into shallow checks:

```java
// ❌ Weak tests without oracle
assertNotNull(response);              // "response is not null"
assertTrue(token.canBeParsed());      // "token can be parsed"
assertTrue(verifySignature(token));   // "signature verification returns true"
```

These checks validate **structure**, but not **correctness**.

A cryptographic oracle allows testing at a deeper level:

| Test Level | Without Oracle | With Oracle |
|------------|----------------|-------------|
| **Structure** | ✅ Can parse ASN.1 | ✅ Can parse ASN.1 |
| **Correctness** | ❌ Unknown if output is correct | ✅ Byte-level match with reference |
| **Regression** | ❌ Changes go unnoticed | ✅ Any change breaks test |
| **Compatibility** | ❌ No cross-version check | ✅ Same oracle across versions |

**Benefits:**

- **Byte-level reproducibility** — exact output for given input
- **Deterministic verification** — same oracle works forever
- **Long-term regression safety** — changes are detected immediately
- **Cryptographic compatibility** — ensures implementations remain compatible across versions

**This is especially important for:**

- PKI systems (X.509, certificate chains)
- RFC 3161 timestamping
- CMS / ASN.1 structures
- Signature validation
- Agent signing and audit trails
- Non-repudiable outputs

---

## What an Oracle Is (and Is Not)

### ✅ Oracle IS:

- **Deterministic** — same input always produces same output
- **Reproducible** — can be regenerated if needed
- **Stable across time** — works today, tomorrow, in 10 years
- **Independent from live services** — no network, no external dependencies
- **Suitable for CI/CD** — fast, reliable, no flaky tests

### ❌ Oracle is NOT:

- **A real TSA** — does not prove real time
- **A production CA** — does not issue trusted certificates
- **A live cryptographic authority** — does not provide legal guarantees
- **A security boundary** — not for production use
- **A replacement for real services** — only for testing

**Key principle:**

> An oracle is a **testing truth source**, not a **trust anchor**.

---

## Example: Timestamp Oracle

### Input

```
digest = SHA-256("hello world")
       = b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9
```

### Oracle Defines

The oracle stores a **fixed RFC 3161 timestamp token** for this digest:

```
timestamp_token = BASE64(
  308203a1020101300d06092a864886f70d01010105000482012f...
)
```

This token is:

- **Generated once** — by a deterministic MOCK_TSA
- **Stored as test fixture** (golden fixture) — e.g., `src/test/resources/fixtures/hello-world.tsr`
- **Reused forever** — in all future tests

**Note:** This is an example of both an **RFC 3161 test vector** (input hash + expected output) and a **golden fixture** (stored reference token). See [MOCK_TSA](MOCK_TSA.md) for details on generating and using test vectors and fixtures in Aletheia.

### What the Oracle Validates

Every future test verifies:

| Aspect | What Oracle Tests |
|--------|-------------------|
| **Parsing** | Token can be parsed as ASN.1 / RFC 3161 |
| **Structure** | Contains valid `TSTInfo`, `SignedData`, `ContentInfo` |
| **Signature** | Signature verification logic works |
| **Timestamp** | `genTime` field matches expected fixed time |
| **Message imprint** | Digest matches input |
| **RFC 3161 compliance** | All required fields present |

**If the bytes differ — implementation changed.**

This is a regression signal: either a bug was introduced, or the cryptographic library changed behavior.

---

## Relationship to MOCK_TSA

**Conceptual distinction:**

| Concept | Role |
|---------|------|
| **MOCK_TSA** | Implementation mechanism — generates deterministic tokens |
| **Cryptographic Oracle** | Conceptual role — defines what is "correct" |

**In Aletheia AI:**

1. **MOCK_TSA** generates deterministic timestamp tokens (RFC 3161).
2. Those tokens **act as cryptographic oracles** — they define expected outputs.
3. Tests validate behavior **against the oracle** (byte-level comparison or structural validation).

**Example flow:**

```
Input (digest) → MOCK_TSA → TimeStampToken (bytes)
                                    ↓
                             Store as oracle
                                    ↓
             Future tests compare against oracle
```

This allows testing timestamp logic **without requiring**:

- Real TSA endpoint
- Network access
- System clock dependency
- External cryptographic services

---

## Oracle Pattern in Practice

### 1. Generation Phase (One-Time)

```java
// Generate oracle once
MockTsaService mockTsa = new MockTsaService(fixedKey, fixedCert);
byte[] digest = sha256("hello world");
byte[] oracleToken = mockTsa.respond(createTSQ(digest));

// Store in test resources
Files.write(Paths.get("fixtures/hello-world.tsr"), oracleToken);
```

### 2. Test Phase (Repeated Forever)

```java
@Test
void timestampServiceMatchesOracle() throws Exception {
    // Load oracle
    byte[] expectedToken = loadFixture("hello-world.tsr");
    
    // Generate token with same input
    byte[] actualToken = timestampService.timestamp("hello world");
    
    // Compare against oracle
    assertArrayEquals(expectedToken, actualToken);
}
```

**If test fails:**

- Either implementation changed (regression)
- Or oracle needs updating (intentional change)

### 3. Multi-Level Validation

Oracles can be used at different levels:

| Level | Validation |
|-------|------------|
| **Byte-level** | `assertArrayEquals(expected, actual)` — strictest |
| **Structural** | Parse both, compare fields (`genTime`, `serial`, `policy`) |
| **Semantic** | Verify signature, check hash, validate ASN.1 — loosest |

**For maximum regression safety: use byte-level.**

---

## Analogy: Dashcam for Cryptography

A cryptographic oracle is like a **dashcam** for cryptography.

**It does not decide:**

- Who is guilty
- What should have happened
- What is "right" in an abstract sense

**It records:**

> "This is **exactly** what happened at this cryptographic moment."

Later, implementations can be verified against it:

- Did the output change?
- Is the new behavior compatible?
- Can we reproduce the same result?

**The oracle is the witness, not the judge.**

---

## Why This Matters for AI / LLM Agents

As LLM-based agents become autonomous, they will:

- Issue decisions
- Generate outputs
- Trigger actions
- Interact with external systems (APIs, databases, users)

For **auditability** and **liability separation**, it becomes critical to know:

| Question | Why It Matters |
|----------|----------------|
| **What did the agent say?** | Content of the response |
| **When did it say it?** | Timestamp (non-repudiable) |
| **Under which model/config?** | Context: model version, temperature, prompt |
| **Can history be rewritten?** | No — cryptographic proof |

### Cryptographic Oracles Enable:

1. **Deterministic replay** — reproduce exact agent output in tests
2. **Verifiable audit logs** — prove what agent said and when
3. **Non-repudiable agent outputs** — agent cannot deny past statements
4. **Long-term accountability** — audit trail survives years

### Example: Agent Output Oracle

```
Input:
  prompt = "Summarize contract X"
  model  = "gpt-4"
  
Oracle:
  response     = "Contract X states..."
  signature    = 0x4a3f2e...
  timestamp    = 2026-01-01T00:00:00Z (TSA token)
  
Test:
  replay(prompt, model) → must match oracle response
```

This ensures:

- Agent behavior is reproducible
- Tests catch unintended model changes
- Audit trail is tamper-proof

**For Aletheia AI:** Every signed and timestamped LLM response can serve as an oracle for regression testing and audit verification.

---

## Oracle Types in Aletheia

| Oracle Type | Purpose | Example Fixture |
|-------------|---------|-----------------|
| **Canonicalization Oracle** | Verify text normalization | `canonical-text.txt` |
| **Hash Oracle** | Verify SHA-256 output | `hash-golden.bin` (32 bytes) |
| **Signature Oracle** | Verify RSA PKCS#1 v1.5 signature | `signature-golden.sig` |
| **Timestamp Oracle** | Verify RFC 3161 token | `timestamp-golden.tsr` |
| **RFC 3161 Test Vector** | Full TSQ/TSR pair for protocol testing | `test-vector.json` + `.tsq`/`.tsr` files |
| **LLM Response Oracle** | Verify agent output (for deterministic models) | `llm-response-golden.json` |

Each oracle lives in `src/test/resources/fixtures/` and is versioned in Git.

**Note on RFC 3161 Test Vectors:** Unlike simple golden fixtures (single output file), test vectors include both input (`TimeStampRequest` / `.tsq`) and expected output (`TimeStampResponse` / `.tsr`), plus metadata (expected time, serial, policy OID). They are used to test the full RFC 3161 protocol compliance, not just output reproducibility. See [MOCK_TSA](MOCK_TSA.md) for details on generating test vectors with deterministic MOCK_TSA.

---

## Implementation Strategy

### Step 1: Generate Oracles

Use deterministic implementations (MOCK_TSA, fixed keys, fixed time) to generate reference outputs:

```java
MockTsaService mockTsa = new MockTsaService(fixedKey, fixedCert);
byte[] oracle = mockTsa.respond(tsq);
saveFixture("oracle.tsr", oracle);
```

### Step 2: Store in Repository

```
backend/src/test/resources/fixtures/
  ├── canonical-text-1.txt
  ├── hash-golden-1.bin
  ├── signature-golden-1.sig
  └── timestamp-golden-1.tsr
```

Commit to Git — oracles become part of the codebase.

### Step 3: Write Tests

```java
@Test
void matchesOracle() {
    byte[] expected = loadFixture("oracle.tsr");
    byte[] actual = service.process(input);
    assertArrayEquals(expected, actual);
}
```

### Step 4: Maintain Oracles

- **On intentional change** (algorithm update, library upgrade): regenerate oracle and commit.
- **On accidental change** (bug, regression): test fails → fix code, not oracle.

**Golden rule:** Oracle should only change when you **intentionally** change cryptographic behavior.

---

## Summary

Cryptographic test oracles provide:

| Benefit | Description |
|---------|-------------|
| **Deterministic verification** | Same input always produces same output |
| **Reproducible testing** | Tests work forever, no flakiness |
| **Cryptographic confidence** | Byte-level correctness, not just structure |
| **Future-proof regression control** | Any change is detected immediately |

**They are foundational for building trustworthy systems involving:**

- **PKI** (certificates, signatures, chains)
- **Timestamping** (RFC 3161, TSA)
- **Signing** (RSA, ECDSA, content signing)
- **Autonomous AI agents** (audit trails, non-repudiation, accountability)

**For Aletheia AI:** Cryptographic oracles ensure that every component (canonicalization, hashing, signing, timestamping, LLM responses) is tested against deterministic reference outputs, enabling long-term auditability and verifiability.

---

## Related Documents

- [MOCK_TSA](MOCK_TSA.md) — Deterministic TSA implementation for generating timestamp oracles
- [Timestamping (TIMESTAMPING)](TIMESTAMPING.md) — RFC 3161, TSA endpoint, what we timestamp
- [Signing (SIGNING)](SIGNING.md) — RSA PKCS#1 v1.5, deterministic signatures
- [Trust Model (TRUST_MODEL)](TRUST_MODEL.md) — Who attests what, eIDAS mapping
- [Implementation Plan (plan)](plan.md) — Task 2.4: TimestampService (where oracles are used)
- [Architecture Diagrams](../../diagrams/architecture.md) — Pipeline with crypto layer and trust chain
- [README](../../README.md) — Project overview, design, run instructions

---

**Status:** Conceptual document. Oracle-based testing is planned for Task 2.4 (TimestampService) and beyond.

**License:** MIT (per Aletheia AI project).
