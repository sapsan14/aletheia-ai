# Crypto Quick Reference — For Beginners

**One place for: what we use, why, and how to generate keys.**

If you're new to cryptography, start with [KINDERGARDEN](../../KINDERGARDEN.md) (Russian) for analogies. This doc is a technical cheat sheet.

---

## Table of contents

- [Algorithms we use](#algorithms-we-use)
- [Why tsaToken, not a simple timestamp?](#why-tsatoken-not-a-simple-timestamp)
- [tsaToken format and trust](#tsatoken-format-and-trust)
- [Keys in the project](#keys-in-the-project)
- [How to generate keys](#how-to-generate-keys)
- [Where to learn more](#where-to-learn-more)

---

## Algorithms we use

| Step | Algorithm | What it does |
|------|-----------|--------------|
| **Hash** | SHA-256 | Produces a 64-character hex "fingerprint" of data. Same input → same output. |
| **Sign** | RSA PKCS#1 v1.5 with SHA-256 | Signs the hash with a private key. Only the key owner can sign; anyone can verify. |
| **Timestamp** | RFC 3161 (Time-Stamp Protocol) | External TSA attests "this data existed at this time". |

**Library:** Bouncy Castle (Java) — `bcpkix`, `bcprov`.

**Why these?** Industry standard, widely trusted, compatible with eIDAS and legal use.

---

## Why tsaToken, not a simple timestamp?

**Problem:** We need to prove that the signature existed at time T — not that someone *claims* it did.

| Approach | Who sets the time? | Can it be faked? |
|----------|--------------------|------------------|
| `"timestamp": "2026-02-01T12:00:00"` in JSON | You | Yes — anyone can write any time |
| Signature + `signedAt` field | Key owner | Yes — they choose the time |
| **tsaToken (RFC 3161)** | External TSA (DigiCert, Sectigo, etc.) | No — a third party attests; you don't control it |

**Idea:** Time must be attested by a **trusted third party** that you don't control. That's the TSA.

**Simple analogy:** A notary stamps your document with the date. You can't change that stamp.

---

## tsaToken format and trust

### What is tsaToken?

A **Time-Stamp Token** (RFC 3161) is a signed bundle containing:

- **genTime** — the time the TSA attested
- **messageImprint** — hash of the data being timestamped (our signature)
- **TSA's signature** — proves the token came from a real TSA
- **TSA's certificate** — links to a trusted CA

**Format:** ASN.1 DER (binary). We store/transfer it as Base64 in JSON.

### Trust chain

1. **TSA** — has a certificate from a Certificate Authority (CA)
2. **Verification** — check TSA's signature, validate certificate chain to a trusted CA
3. **Result** — "TSA certified that this data existed at genTime"

### Why a simple time field isn't enough

- A time string is just text — no cryptographic link to the data
- tsaToken is **cryptographically bound** to the signature (`messageImprint`)
- Forging it would require compromising the TSA's key

### MOCK_TSA vs real TSA

| | MOCK_TSA | Real TSA |
|---|----------|----------|
| Format | Same RFC 3161 | Same RFC 3161 |
| Trust | Tests only | Legal validity (eIDAS, etc.) |
| Time | Fixed (2026-01-01) | Real clock |
| Certificate | Test/mock | Issued by trusted CA |

---

## Keys in the project

| Key | Purpose | Required? | Location |
|-----|---------|-----------|----------|
| **Signing key** (`ai.key`) | Signs the hash of AI responses | Yes for signing | Configurable path |
| **Mock TSA key** | Signs MOCK_TSA tokens (tests/dev) | Yes for MOCK_TSA | `backend/src/main/resources/mock-tsa/` |

**Signing key** — your identity; keep it secret.  
**Mock TSA key** — test fixture; committed to repo; never use in production.

---

## How to generate keys

### 1. Signing key (for real signing)

```bash
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
```

- **Algorithm:** RSA, 2048 bits
- **Format:** PEM (PKCS#8)
- **Usage:** Set `AI_ALETHEIA_SIGNING_KEY_PATH` in `.env`, or pass on startup: `--ai.aletheia.signing.key-path=../ai.key`

### 2. Mock TSA key (already in repo)

The project includes `mock-tsa/mock-tsa.key` and `mock-tsa/mock-tsa.crt`. Regenerate only if needed:

```bash
# Key
openssl genpkey -algorithm RSA -out mock-tsa.key -pkeyopt rsa_keygen_bits:2048

# Certificate (must have Extended Key Usage: timeStamping)
openssl req -new -x509 -key mock-tsa.key -out mock-tsa.crt -days 3650 \
  -config - -extensions ext <<EOF
[req]
distinguished_name = dn
x509_extensions = ext
prompt = no
[dn]
CN = Mock TSA Aletheia Test
[ext]
extendedKeyUsage = critical, timeStamping
EOF
```

See [mock-tsa/README](../../backend/src/main/resources/mock-tsa/README.md).

---

## Where to learn more

- [SIGNING](SIGNING.md) — what we sign, key management
- [TIMESTAMPING](TIMESTAMPING.md) — TSA modes, switching mock/real
- [MOCK_TSA](MOCK_TSA.md) — deterministic TSA for tests
- [TRUST_MODEL](TRUST_MODEL.md) — who attests what
- [KINDERGARDEN](../../KINDERGARDEN.md) — analogies and concepts (Russian)
