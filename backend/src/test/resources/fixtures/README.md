# Test Fixtures for Aletheia AI

This directory contains **golden fixtures** — reference cryptographic outputs used for deterministic regression testing.

---

## Purpose

Golden fixtures ensure:
- **Reproducibility:** Same input always produces same output
- **Regression detection:** Catch unintended changes (library updates, algorithm tweaks)
- **Documentation:** Serve as concrete examples of expected behavior
- **CI/CD validation:** Byte-level verification in automated tests

See: [TESTING_STRATEGY](../../../../docs/en/TESTING_STRATEGY.md), [CRYPTO_ORACLE](../../../../docs/en/CRYPTO_ORACLE.md)

---

## Directory Structure

```
fixtures/
├── README.md                   (this file)
├── hashes/                     (SHA-256 outputs)
│   ├── hello-world-sha256.bin
│   ├── empty-string-sha256.bin
│   └── manifest.json
├── signatures/                 (RSA PKCS#1 v1.5 signatures)
│   ├── test-message-rsa.sig
│   ├── hello-world-rsa.sig
│   └── manifest.json
├── timestamps/                 (RFC 3161 timestamp tokens)
│   ├── hello-world.tsq         (TimeStampRequest)
│   ├── hello-world.tsr         (TimeStampResponse)
│   ├── hello-world.json        (metadata)
│   └── manifest.json
└── test-vectors/               (Full RFC 3161 test vectors)
    ├── valid-sha256.json
    ├── invalid-digest.json
    ├── missing-nonce.json
    └── manifest.json
```

---

## Fixture Types

### 1. Hash Fixtures (`hashes/`)

**Purpose:** Verify SHA-256 determinism.

**Format:** Binary files (32 bytes) or hex strings.

**Example:**
```
hello-world-sha256.bin = b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9
```

**Usage:**
```java
@Test
void testHashFixture() {
    byte[] input = "hello world".getBytes(StandardCharsets.UTF_8);
    byte[] actual = hashService.sha256(input);
    byte[] expected = loadFixture("hashes/hello-world-sha256.bin");
    assertArrayEquals(expected, actual);
}
```

---

### 2. Signature Fixtures (`signatures/`)

**Purpose:** Verify RSA PKCS#1 v1.5 signature determinism.

**Format:** Binary signature files (.sig).

**Example:**
```
test-message-rsa.sig = <binary signature bytes>
```

**Usage:**
```java
@Test
void testSignatureFixture() {
    byte[] hash = sha256("test message");
    byte[] actual = signatureService.sign(hash);
    byte[] expected = loadFixture("signatures/test-message-rsa.sig");
    assertArrayEquals(expected, actual);
}
```

---

### 3. Timestamp Fixtures (`timestamps/`)

**Purpose:** Verify MOCK_TSA deterministic timestamp tokens (RFC 3161).

**Format:** 
- `.tsq` — TimeStampRequest (ASN.1 DER)
- `.tsr` — TimeStampResponse (ASN.1 DER)
- `.json` — Metadata (digest, time, serial, policy OID)

**Example metadata (`hello-world.json`):**
```json
{
  "description": "Timestamp token for 'hello world' canonical text",
  "digest_sha256": "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
  "expected_time": "2026-01-01T00:00:00Z",
  "policy_oid": "1.2.3.4.5.6",
  "serial": "83737289194838447048400471564591383726494234456"
}
```

**Usage:**
```java
@Test
void testTimestampFixture() {
    byte[] tsq = loadFixture("timestamps/hello-world.tsq");
    byte[] actual = mockTsa.respond(tsq);
    byte[] expected = loadFixture("timestamps/hello-world.tsr");
    assertArrayEquals(expected, actual, "Token differs from golden fixture");
}
```

---

### 4. RFC 3161 Test Vectors (`test-vectors/`)

**Purpose:** Full protocol testing with request/response pairs and expected outcomes.

**Format:** JSON files with embedded or referenced binary data.

**Example (`valid-sha256.json`):**
```json
{
  "name": "valid_sha256_request",
  "description": "Valid RFC 3161 request with SHA-256 digest",
  "request": {
    "digest_algorithm": "SHA-256",
    "digest": "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
    "nonce": "123456789",
    "cert_req": true
  },
  "expected_response": {
    "status": "granted",
    "genTime": "2026-01-01T00:00:00Z",
    "serial": "83737289194838447048400471564591383726494234456",
    "policy_oid": "1.2.3.4.5.6"
  },
  "files": {
    "request": "valid-sha256.tsq",
    "response": "valid-sha256.tsr"
  }
}
```

**Usage:**
```java
@ParameterizedTest
@JsonFileSource("fixtures/test-vectors/valid-sha256.json")
void testRFC3161Vector(TestVector vector) {
    byte[] tsq = loadFixture(vector.getRequestFile());
    byte[] tsr = mockTsa.respond(tsq);
    
    TimeStampResponse response = new TimeStampResponse(tsr);
    assertEquals("granted", response.getStatus());
    
    TimeStampToken token = response.getTimeStampToken();
    assertEquals(vector.getExpectedTime(), token.getTimeStampInfo().getGenTime());
}
```

---

## Manifest Files

Each subdirectory contains a `manifest.json` for documentation and validation:

```json
{
  "version": "1.0",
  "generated_at": "2026-01-30T17:00:00Z",
  "generator": "MockTsaService v1.0",
  "fixtures": [
    {
      "name": "hello-world",
      "input": "hello world",
      "algorithm": "SHA-256",
      "files": ["hello-world.tsq", "hello-world.tsr", "hello-world.json"]
    }
  ]
}
```

**Purpose:**
- Document fixture creation process
- Track fixture versions
- Enable automated validation (fixture completeness check)

---

## Generating Fixtures

### Hash Fixtures

```bash
echo -n "hello world" | sha256sum | cut -d' ' -f1 | xxd -r -p > hello-world-sha256.bin
```

### Signature Fixtures

```java
// Run once to generate
@Test
@Disabled("Fixture generator — run manually")
void generateSignatureFixture() throws Exception {
    byte[] hash = sha256("test message");
    byte[] signature = signatureService.sign(hash);
    Files.write(Paths.get("src/test/resources/fixtures/signatures/test-message-rsa.sig"), signature);
}
```

### Timestamp Fixtures

```java
@Test
@Disabled("Fixture generator — run manually")
void generateTimestampFixture() throws Exception {
    byte[] digest = sha256("hello world");
    byte[] tsq = buildTSQ(digest);
    byte[] tsr = mockTsa.respond(tsq);
    
    Files.write(Paths.get("fixtures/timestamps/hello-world.tsq"), tsq);
    Files.write(Paths.get("fixtures/timestamps/hello-world.tsr"), tsr);
    
    // Generate metadata
    TimeStampToken token = new TimeStampResponse(tsr).getTimeStampToken();
    String metadata = """
    {
      "digest_sha256": "%s",
      "expected_time": "%s",
      "serial": "%s"
    }
    """.formatted(
        Hex.toHexString(digest),
        token.getTimeStampInfo().getGenTime(),
        token.getTimeStampInfo().getSerialNumber()
    );
    Files.writeString(Paths.get("fixtures/timestamps/hello-world.json"), metadata);
}
```

---

## Updating Fixtures

**When to update:**
- ✅ Intentional algorithm change (e.g., upgrade from SHA-256 to SHA-3)
- ✅ Library upgrade that changes output format
- ✅ Fixed time update (e.g., change MOCK_TSA fixed time from 2026 to 2027)

**When NOT to update:**
- ❌ Test fails and you "fix" fixture to match broken output
- ❌ Random modification without understanding why output changed

**Process:**
1. Understand why fixture no longer matches
2. If change is intentional: regenerate fixture with clear commit message
3. If change is unintended: fix the code, not the fixture
4. Review fixture changes in PR (byte-level diff review)

---

## CI Integration

**GitHub Actions example:**

```yaml
- name: Verify fixtures are unchanged
  run: |
    ./mvnw test -Dtest=FixtureRegressionTest
    git diff --exit-code src/test/resources/fixtures/
```

**Purpose:** Catch accidental fixture modifications.

---

## Golden Fixtures Branch (Optional)

For large projects, maintain fixtures in separate Git branch:

```bash
git checkout -b golden-fixtures
# Commit only fixtures
git add src/test/resources/fixtures/
git commit -m "chore: add golden fixtures v1.0"
git push origin golden-fixtures
```

**Tests checkout fixtures:**
```yaml
- uses: actions/checkout@v3
  with:
    ref: golden-fixtures
    path: fixtures
```

See: [TESTING_STRATEGY — Golden Fixtures Branch Strategy](../../../../docs/en/TESTING_STRATEGY.md#golden-fixtures-branch-strategy)

---

## Best Practices

1. **Version fixtures:** Include `manifest.json` with version and generation date
2. **Document inputs:** Always store input data alongside output (e.g., `hello-world.txt` + `hello-world-sha256.bin`)
3. **Binary format:** Store as binary (`.bin`, `.sig`, `.tsr`) for exact byte comparison
4. **Metadata files:** Use JSON for human-readable context
5. **Small fixtures:** Keep fixtures minimal (< 1KB when possible)
6. **Review changes:** Treat fixture updates as code changes (require PR review)

---

## Related Documentation

- [TESTING_STRATEGY](../../../../docs/en/TESTING_STRATEGY.md) — Full testing approach
- [CRYPTO_ORACLE](../../../../docs/en/CRYPTO_ORACLE.md) — Oracle pattern philosophy
- [MOCK_TSA](../../../../docs/en/MOCK_TSA.md) — Deterministic TSA implementation
- [TIMESTAMPING](../../../../docs/en/TIMESTAMPING.md) — RFC 3161 overview

---

**Tags:** `#testing` `#golden-fixtures` `#regression-testing` `#RFC3161` `#determinism`
