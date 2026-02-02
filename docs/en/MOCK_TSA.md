# MOCK_TSA — Deterministic TSA Simulator for Testing

**Deterministic implementation of RFC 3161 Time-Stamp Authority for reproducible tests.**

---

## Contents

- [Why MOCK_TSA is Needed](#why-mock_tsa-is-needed)
- [Purpose and Operating Principle](#purpose-and-operating-principle)
- [Key Idea](#key-idea)
- [Architecture](#architecture)
- [Minimal Contract](#minimal-contract)
- [Fixed Elements](#fixed-elements)
  - [Fixed Time](#fixed-time)
  - [Deterministic serialNumber](#deterministic-serialnumber)
  - [Deterministic Signature](#deterministic-signature)
- [Minimal RFC 3161 Response Structure](#minimal-rfc-3161-response-structure)
- [Implementation Example (Pseudocode)](#implementation-example-pseudocode)
- [What Can Be Tested with MOCK_TSA](#what-can-be-tested-with-mock_tsa)
- [Core Philosophy](#core-philosophy)
- [Differences from Real TSA](#differences-from-real-tsa)
- [Pattern: Cryptographic Test Oracle](#pattern-cryptographic-test-oracle)
- [Java Implementation (Bouncy Castle)](#java-implementation-bouncy-castle)
  - [Why Java for Aletheia](#why-java-for-aletheia)
  - [Contract: No Network, Service Only](#contract-no-network-service-only)
  - [Dependencies](#dependencies)
  - [Working Logic with Bouncy Castle](#working-logic-with-bouncy-castle)
  - [Where to Use in the Project](#where-to-use-in-the-project)
  - [Golden Fixtures (Byte-for-Byte)](#golden-fixtures-byte-for-byte)
- [Next Steps (Task 2.4)](#next-steps-task-24)
- [Related Documents](#related-documents)

---

## Why MOCK_TSA is Needed

MOCK_TSA solves a fundamental problem in testing cryptographic systems with timestamps:

**Problem:** Real TSA is by definition **non-deterministic**:
- Each request returns unique time (real clock)
- Signature contains random elements (padding, nonce)
- Requires network connection to external service
- Result changes on every test run

**Solution:** MOCK_TSA — fully **deterministic** simulator:
- Always returns identical token for identical input
- Independent of real time
- No network or external services required
- Guarantees test reproducibility

**Conclusion:**
```
Real TSA    = non-deterministic → flaky tests
MOCK_TSA    = 100% deterministic → reproducible tests
```

---

## Purpose and Operating Principle

### Purpose

For identical input:

```
hash + policy + nonce  →  always the same TimeStampToken
```

### What is Eliminated

MOCK_TSA operates **without**:
- **Clock** — time is fixed
- **Network** — everything local, in-memory
- **Certificates** — uses hardcoded mock certificate
- **Randomness** — all elements deterministic

---

## Key Idea

MOCK_TSA is a **cryptographic simulator**, not a real TSA.

It pretends to be a TSA, but actually:

| Element | Real TSA | MOCK_TSA |
|---------|----------|----------|
| Time | `Date.now()` | Fixed (`2026-01-01T00:00:00Z`) |
| Certificate | Real X.509 from CA | Mock hardcoded cert |
| Signature | With randomness | Deterministic (RSA PKCS#1 v1.5) |
| ASN.1 structure | Fully valid RFC 3161 | Correct but simplified |

**Important:** MOCK_TSA returns valid RFC 3161 structures — parsers and verifiers work with it like a real TSA.

---

## Architecture

```
┌──────────────────────┐
│   Test Code          │
│                      │
│  digest = sha256(x)  │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│   MOCK_TSA           │
│                      │
│ 1. TSQ → TSR         │
│ 2. Fixed time        │
│ 3. Deterministic sig │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ TimeStampToken       │
│ (always identical)   │
└──────────────────────┘
```

**Key:** The same request always produces the same response.

---

## Minimal Contract

```java
public interface MockTsaService {
    /**
     * Responds to RFC 3161 TimeStampRequest with deterministic TimeStampResponse.
     *
     * @param tsqBytes — serialized TimeStampRequest (ASN.1 DER)
     * @return serialized TimeStampResponse (ASN.1 DER)
     */
    byte[] respond(byte[] tsqBytes) throws Exception;
}
```

**Properties:**
- **Input:** RFC 3161 `TimeStampRequest` (TSQ) bytes
- **Output:** RFC 3161 `TimeStampResponse` (TSR) bytes
- **Guarantee:** For identical input → identical output

---

## Fixed Elements

### Fixed Time

```java
private static final Instant FIXED_TIME = Instant.parse("2026-01-01T00:00:00Z");
```

All tokens always contain this time.

### Deterministic serialNumber

```java
BigInteger serial = new BigInteger(1, digest); // from request digest
```

Serial is derived from the request hash → always reproducible.

### Signature and byte-level determinism

- **Algorithm:** RSA PKCS#1 v1.5 with SHA-256
- **Key:** Fixed, hardcoded private key
- **Byte determinism:** RSA PKCS#1 v1.5 uses **random padding** (SecureRandom). Each call produces different signature bytes — even for identical data. This is standard behavior.
- **Semantic determinism:** Same input → same genTime, serial, messageImprint digest. The token bytes may differ; the *meaning* is identical. Our tests assert semantic equivalence (see [CRYPTO_REFERENCE](CRYPTO_REFERENCE.md#rsa-signature-randomness-and-semantic-determinism-learners)).

---

## Minimal RFC 3161 Response Structure

```asn1
TimeStampResp ::= SEQUENCE {
   status         PKIStatusInfo,
   timeStampToken TimeStampToken OPTIONAL
}

TimeStampToken ::= ContentInfo  -- SignedData from RFC 5652

TSTInfo ::= SEQUENCE {
   version        INTEGER,
   policy         TSAPolicyId,
   messageImprint MessageImprint,
   serialNumber   INTEGER,
   genTime        GeneralizedTime,  -- FIXED
   ...
}
```

MOCK_TSA generates minimal valid structure with fixed `genTime`.

---

## Implementation Example (Pseudocode)

```java
public class MockTsaService {

    private final PrivateKey tsaKey;
    private final X509Certificate tsaCert;
    private final Date fixedTime;

    public MockTsaService(PrivateKey tsaKey, X509Certificate tsaCert) {
        this.tsaKey = tsaKey;
        this.tsaCert = tsaCert;
        this.fixedTime = Date.from(Instant.parse("2026-01-01T00:00:00Z"));
    }

    /**
     * Process RFC 3161 request (TSQ bytes) and return deterministic response (TSR bytes).
     */
    public byte[] respond(byte[] tsqBytes) throws Exception {
        TimeStampRequest req = new TimeStampRequest(tsqBytes);
        
        // Extract digest from request
        byte[] digest = req.getMessageImprintDigest();
        
        // Deterministic serial from digest
        BigInteger serial = new BigInteger(1, digest);
        
        // Build TSTInfo with FIXED time
        TSTInfo tstInfo = new TSTInfo(
            req.getReqPolicy(),
            req.getMessageImprint(),
            serial,
            fixedTime,  // ← FIXED
            null, null, null
        );
        
        // Sign with deterministic RSA PKCS#1 v1.5
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
            .build(tsaKey);
        
        SignedData signedData = buildSignedData(tstInfo, signer, tsaCert);
        
        TimeStampResponse resp = new TimeStampResponse(
            PKIStatusInfo.granted,
            new TimeStampToken(signedData)
        );
        
        return resp.getEncoded();
    }
}
```

**Key properties:**
- Same `tsqBytes` → same `fixedTime` → same `serial` → same logical token
- Raw bytes may differ due to RSA signature padding; we test semantic equivalence

---

## What Can Be Tested with MOCK_TSA

✅ **Token structure:**
- Parse `TimeStampToken` → extract `genTime`, `serial`, `policy`
- Verify ASN.1 structure is valid

✅ **Determinism:**
- Call twice with same digest → get semantically identical token (same genTime, serial, digest; bytes may differ due to RSA padding)

✅ **Integration:**
- Backend → MOCK_TSA → store token → verify from DB

✅ **Regression:**
- Compare current token with golden fixture from Git

❌ **What cannot be tested:**
- Real time verification (MOCK_TSA always returns 2026-01-01)
- Real PKI chain verification (mock certificate)
- Network issues and TSA availability

---

## Core Philosophy

MOCK_TSA is not a "fake" or "stub" — it's a **deterministic implementation of RFC 3161**.

**Analogy:**
```
Real TSA      = production database (live, mutable, networked)
MOCK_TSA      = in-memory test database (H2, deterministic, isolated)
```

Both are valid implementations, but MOCK_TSA optimized for **reproducible testing**.

---

## Differences from Real TSA

| Feature | Real TSA | MOCK_TSA |
|---------|----------|----------|
| **Time** | Real clock | Fixed |
| **Serial** | Incremental counter | Hash-derived |
| **Signature** | Random padding (typical) | Same; padding still random (byte-level non-deterministic) |
| **Network** | Required (HTTP/HTTPS) | In-process service |
| **Certificate** | Valid CA chain | Mock cert |
| **Reproducibility** | ❌ Different each time | ✅ Semantically identical (genTime, serial, digest) |
| **CI/CD** | ⚠️ Requires running TSA server | ✅ Works offline |

---

## Pattern: Cryptographic Test Oracle

MOCK_TSA implements the **Cryptographic Oracle** pattern:

> **Oracle** = known correct output for given input, used to verify implementation correctness.

In cryptographic testing:
- **Hash oracle:** `sha256("hello") → e2d0fe...` (known result)
- **Signature oracle:** `sign(hash) → base64...` (deterministic)
- **Timestamp oracle:** `timestamp(hash) → token bytes` (MOCK_TSA)

**Purpose:** Provide reproducible reference output for regression testing.

See: [CRYPTO_ORACLE](CRYPTO_ORACLE.md)

---

## Java Implementation (Bouncy Castle)

### Why Java for Aletheia

Aletheia backend is **Java Spring Boot** with focus on enterprise PKI:
- Native BouncyCastle support
- Strong typing for cryptographic operations
- Production-ready Spring ecosystem
- Easy integration with JUnit for testing

### Contract: No Network, Service Only

```java
@Service
public class MockTsaService {
    byte[] respond(byte[] tsqBytes) throws Exception;
}
```

**Not** an HTTP server — just a service that:
- Takes TSQ bytes as input
- Returns TSR bytes as output
- Works in-memory, no I/O

**Use in tests:**
```java
@Autowired
private MockTsaService mockTsa;

@Test
void testSemanticDeterminism() throws Exception {
    byte[] tsq = createTSQ(sha256("hello"));
    byte[] token1 = mockTsa.respond(tsq);
    byte[] token2 = mockTsa.respond(tsq);

    // Semantic equivalence: same genTime, serial, digest (bytes may differ due to RSA padding)
    TimeStampToken ts1 = new TimeStampToken(new CMSSignedData(token1));
    TimeStampToken ts2 = new TimeStampToken(new CMSSignedData(token2));
    assertEquals(ts1.getTimeStampInfo().getGenTime(), ts2.getTimeStampInfo().getGenTime());
    assertEquals(ts1.getTimeStampInfo().getSerialNumber(), ts2.getTimeStampInfo().getSerialNumber());
    assertArrayEquals(ts1.getTimeStampInfo().getMessageImprintDigest(), ts2.getTimeStampInfo().getMessageImprintDigest());
}
```

### Dependencies

```xml
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcpkix-jdk18on</artifactId>
    <version>1.78.1</version>
</dependency>
```

Includes:
- `org.bouncycastle.tsp.*` — RFC 3161 classes
- `org.bouncycastle.asn1.*` — ASN.1 encoding
- `org.bouncycastle.cms.*` — CMS/PKCS#7 (SignedData)
- `org.bouncycastle.cert.*` — X.509 certificates

### Working Logic with Bouncy Castle

```java
// 1. Signature — only SHA256withRSA (deterministic)
ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(tsaPrivateKey);
DigestCalculatorProvider digestCalc = new JcaDigestCalculatorProviderBuilder().build();
SignerInfoGenerator sigInfoGen = new JcaSignerInfoGeneratorBuilder(digestCalc).build(signer, tsaCert);

// 2. Token generator: fixed key, certificate, policy OID
TimeStampTokenGenerator tokenGen = new TimeStampTokenGenerator(
    sigInfoGen,
    new JcaDigestCalculatorProviderBuilder().build().get(new AlgorithmIdentifier(OID.SHA256)),
    new ASN1ObjectIdentifier("1.2.3.4.5.6")
);
tokenGen.addCertificates(new JcaCertStore(List.of(tsaCert)));

// 3. Response generator
TimeStampResponseGenerator respGen = new TimeStampResponseGenerator(tokenGen, TSPAlgorithms.ALLOWED);

// 4. Serial and time — deterministic; fixedTime passed manually
TimeStampResponse resp = respGen.generate(request, serial, fixedTime);

return resp.getEncoded();
```

**Key points:**
- `SHA256withRSA` — RSA PKCS#1 v1.5; signature padding is random, so byte-level output varies
- `fixedTime` — always `2026-01-01T00:00:00Z`
- `serial` — derived from request digest
- Result: semantically identical output for identical input (same genTime, serial, digest)

### Where to Use in the Project

**Spring profile: `test` or `mock`**

```java
@Profile("test")
@Service
public class MockTsaService implements TimestampService {
    @Override
    public byte[] getTimestamp(byte[] digest) throws Exception {
        byte[] tsq = buildTSQ(digest);
        return respond(tsq);
    }
}
```

**Production profile: `prod`**

```java
@Profile("prod")
@Service
public class RealTsaService implements TimestampService {
    @Override
    public byte[] getTimestamp(byte[] digest) throws Exception {
        // HTTP POST to real TSA
        return httpClient.post(tsaUrl, buildTSQ(digest));
    }
}
```

**Tests automatically use MOCK_TSA:**
```java
@SpringBootTest
@ActiveProfiles("test")
class AuditFlowTest {
    @Autowired TimestampService tsa; // → MockTsaService injected
    
    @Test
    void testFullAuditFlow() { /* ... */ }
}
```

### Golden Fixtures (optional, semantic)

**Structure (if used):**
```
src/test/resources/fixtures/timestamps/
  ├── hello-world.tsq          (request)
  └── hello-world.json         (metadata: expected digest, time, serial)
```

**Metadata (`hello-world.json`):**
```json
{
  "description": "Timestamp token for 'hello world' canonical text",
  "digest_sha256": "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
  "expected_time": "2026-01-01T00:00:00Z",
  "policy_oid": "1.2.3.4.5.6",
  "serial": "83737289194838447048400471564591383726494234456"
}
```

**Test (semantic, not byte-for-byte):** Compare `genTime`, `serialNumber`, and `messageImprintDigest` from the parsed token against expected values from metadata. Do not compare raw token bytes — RSA signature padding varies.

**Note:** See `TimestampServiceTest.timestamp_sameInput_returnsSemanticallyIdenticalToken` for the actual implementation. Byte-for-byte comparison is not used because RSA signature padding varies. Semantic checks (genTime, serial, digest) are sufficient.

---

## Next Steps (Task 2.4)

**Priority roadmap for implementing MOCK_TSA in Aletheia:**

### 1. Implement `MockTsaService` (Java + Bouncy Castle)
- Interface: `byte[] respond(byte[] tsqBytes)`
- Fixed time: `2026-01-01T00:00:00Z`
- Deterministic serial: derived from digest
- Signature: `SHA256withRSA`
- Unit test: same input → semantically identical output (genTime, serial, digest match; bytes may differ due to RSA padding)

**Estimated time:** 4–5 hours (Task 2.4 from plan)

### 2. Spring Bean Integration
- Profile `@Profile("test")` for MOCK_TSA
- Profile `@Profile("prod")` for Real TSA (HTTP client)
- Interface `TimestampService` with method `byte[] getTimestamp(byte[] digest)`
- Dependency injection in tests automatically uses MOCK_TSA

**Estimated time:** 1 hour

### 3. RFC 3161 Test Vectors Repository
- Create `src/test/resources/fixtures/timestamps/`
- Generate 5–10 test vectors: `.tsq` (request), `.tsr` (response), `.json` (metadata)
- Cover: valid hash, invalid hash, different algorithms, edge cases
- Document in `fixtures/README.md`

**Estimated time:** 2 hours

### 4. Golden Fixtures in Git
- Commit test vectors to Git
- Add CI step: run tests against golden fixtures
- On fixture change: require explicit commit + review

**Estimated time:** 1 hour

### 5. Drop-in Replacement for Real TSA
- Ensure MOCK_TSA can be swapped with Real TSA without code changes
- Configuration: `ai.aletheia.tsa.mode=mock|real`
- Integration test: both modes produce parseable tokens

**Estimated time:** 1 hour

**Total estimated time for Task 2.4:** ~9–10 hours

---

## Related Documents

- [TIMESTAMPING](TIMESTAMPING.md) — RFC 3161 overview and test vectors
- [CRYPTO_ORACLE](CRYPTO_ORACLE.md) — Testing philosophy and golden fixtures
- [TESTING_STRATEGY](TESTING_STRATEGY.md) — Full testing approach with MOCK_TSA integration
- [PLAN](PLAN.md) — Task 2.4: TimestampService implementation
- [README](../../README.md) — Project overview

---

**Tags:** `#testing` `#RFC3161` `#deterministic` `#BouncyCastle` `#golden-fixtures` `#TSA` `#mocking`
