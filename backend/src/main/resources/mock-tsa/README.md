# Mock TSA Key and Certificate

Test/mock RSA key and self-signed certificate for MOCK_TSA (deterministic RFC 3161 timestamp simulation).

**Purpose:** Same input → same token. No network. Used in unit tests and dev when `ai.aletheia.tsa.mode=mock`.

**Regenerate (if needed):**
```bash
openssl genpkey -algorithm RSA -out mock-tsa.key -pkeyopt rsa_keygen_bits:2048
# Cert must have Extended Key Usage: timeStamping
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

**Security:** These are test-only credentials. Never use in production.
