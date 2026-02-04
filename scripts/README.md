# Scripts

Helper scripts for Aletheia AI. Run from the **repository root** unless noted.

---

## Offline Evidence Package verifier (DP2.2)

Verify an Evidence Package (directory or `.aep` file) **without calling the Aletheia backend**. Exit 0 = VALID, 1 = INVALID.

### Option 1 — Standalone JAR (Java, no Maven at runtime)

Build once, then run anywhere you have Java 21+:

```bash
cd backend && mvn package -Pverifier -DskipTests
java -jar backend/target/aletheia-verifier.jar /path/to/package-dir
java -jar backend/target/aletheia-verifier.jar /path/to/evidence.aep
```

JAR path: `backend/target/aletheia-verifier.jar`. No network required.

### Option 2 — Java via Maven (from repo root)

Uses Maven to run `VerifierMain`; requires Maven and the backend build:

```bash
./scripts/verify-evidence-java.sh /path/to/package-dir
# or
./scripts/verify-evidence-java.sh /path/to/evidence.aep
```

### Option 3 — OpenSSL-only script (no Java)

Uses only OpenSSL, `xxd`, `base64`, `unzip` (no JVM):

```bash
./scripts/verify-evidence.sh /path/to/package-dir
# or
./scripts/verify-evidence.sh /path/to/evidence.aep
```

Optional: set `TSA_CA_FILE` to a TSA CA PEM path to verify the timestamp signature.

---

## Verification order

1. **Hash** — Recompute SHA-256 of `canonical.bin`; compare with `hash.sha256`.
2. **Signature** — Load `public_key.pem`; verify `signature.sig` over the hash (RSA PKCS#1 v1.5 over DigestInfo(SHA-256, hash)).
3. **TSA token** — Parse `timestamp.tsr` (RFC 3161); read `genTime`; optionally verify TSA signature.

See [Plan Phase 2](docs/internal/en/plan-phase2.md) (Evidence Package format, DP2.2) and root [README — Offline verification](README.md#offline-verification-dp22).
