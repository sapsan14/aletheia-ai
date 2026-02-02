#!/usr/bin/env bash
# Offline Evidence Package verifier (DP2.2) — uses OpenSSL only.
# Usage: ./scripts/verify-evidence.sh <path-to-dir-or-.aep>
# Exit: 0 = VALID, 1 = INVALID or error.
# Verification order: (1) hash, (2) signature, (3) TSA token (parse + optional verify).

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

usage() {
  echo "Usage: $0 <path-to-evidence-dir-or-.aep>" >&2
  exit 1
}

[[ $# -eq 1 ]] || usage
PATH_ARG="$1"
[[ -e "$PATH_ARG" ]] || { echo "Path does not exist: $PATH_ARG" >&2; exit 1; }

WORKDIR=
cleanup() {
  [[ -n "$WORKDIR" && -d "$WORKDIR" ]] && rm -rf "$WORKDIR"
}
trap cleanup EXIT

if [[ -f "$PATH_ARG" && "$PATH_ARG" == *.aep ]]; then
  WORKDIR=$(mktemp -d)
  unzip -q -o "$PATH_ARG" -d "$WORKDIR"
  PKG_DIR="$WORKDIR"
else
  [[ -d "$PATH_ARG" ]] || { echo "Not a directory or .aep file: $PATH_ARG" >&2; exit 1; }
  PKG_DIR="$PATH_ARG"
fi

HASH_FILE="$PKG_DIR/hash.sha256"
CANONICAL_FILE="$PKG_DIR/canonical.bin"
SIG_FILE="$PKG_DIR/signature.sig"
TSR_FILE="$PKG_DIR/timestamp.tsr"
PUBKEY_FILE="$PKG_DIR/public_key.pem"

report() { echo "$1"; }
fail() {
  echo -e "${RED}INVALID${NC}: $1" >&2
  exit 1
}
ok() {
  echo -e "${GREEN}VALID${NC}"
  exit 0
}

# Required files
[[ -f "$HASH_FILE" ]] || fail "missing hash.sha256"
[[ -f "$CANONICAL_FILE" ]] || fail "missing canonical.bin"
[[ -f "$PUBKEY_FILE" ]] || fail "missing public_key.pem"

HASH_HEX=$(tr -d ' \n\r' < "$HASH_FILE")
[[ ${#HASH_HEX} -eq 64 && "$HASH_HEX" =~ ^[0-9a-fA-F]+$ ]] || fail "hash.sha256 must be 64 hex characters"

# (1) Hash check
COMPUTED=$(openssl dgst -sha256 -binary < "$CANONICAL_FILE" | xxd -p -c 256 | tr -d '\n')
if [[ "$(echo "$HASH_HEX" | tr '[:upper:]' '[:lower:]')" != "$(echo "$COMPUTED" | tr '[:upper:]' '[:lower:]')" ]]; then
  report "hash: MISMATCH"
  fail "hash mismatch (computed != stored)"
fi
report "hash: OK"

# (2) Signature check (RSA over DigestInfo(SHA-256, hash) — same as backend)
# DigestInfo DER for SHA-256: 19-byte prefix + 32-byte hash = 51 bytes
DIGESTINFO_PREFIX="3031300d060960864801650304020105000420"
DIGESTINFO_BIN=$(echo "${DIGESTINFO_PREFIX}${HASH_HEX}" | xxd -r -p)
DIGESTINFO_FILE=$(mktemp)
echo -n "$DIGESTINFO_BIN" > "$DIGESTINFO_FILE"
trap 'rm -f "$DIGESTINFO_FILE" "$SIG_BIN" 2>/dev/null; cleanup' EXIT

SIG_B64=$(tr -d ' \n\r' < "$SIG_FILE")
[[ -n "$SIG_B64" ]] || { report "signature: (empty)"; fail "signature missing"; }
SIG_BIN=$(mktemp)
echo "$SIG_B64" | base64 -d > "$SIG_BIN" 2>/dev/null || fail "signature.sig is not valid Base64"

# OpenSSL pkeyutl -verify: -in is the signed data (DigestInfo), -sigfile is the signature
if ! openssl pkeyutl -verify -inkey "$PUBKEY_FILE" -sigfile "$SIG_BIN" -in "$DIGESTINFO_FILE" >/dev/null 2>&1; then
  report "signature: INVALID"
  fail "signature verification failed"
fi
report "signature: OK"

# (3) TSA token (parse and show genTime; optional: verify with -CAfile if TSA cert available)
if [[ -f "$TSR_FILE" ]]; then
  TSR_B64=$(tr -d ' \n\r' < "$TSR_FILE")
  if [[ -n "$TSR_B64" ]]; then
    TSR_BIN=$(mktemp)
    echo "$TSR_B64" | base64 -d > "$TSR_BIN" 2>/dev/null || true
    if [[ -s "$TSR_BIN" ]]; then
      if openssl ts -reply -in "$TSR_BIN" -text 2>/dev/null | grep -q "Time stamp"; then
        GEN_TIME=$(openssl ts -reply -in "$TSR_BIN" -text 2>/dev/null | grep "Time stamp" | head -1 | sed 's/^[[:space:]]*//')
        report "timestamp: $GEN_TIME"
        # Optional: verify TSA signature (requires TSA cert); skip if not set
        if [[ -n "$TSA_CA_FILE" && -f "$TSA_CA_FILE" ]]; then
          if openssl ts -verify -in "$TSR_BIN" -data "$SIG_BIN" -CAfile "$TSA_CA_FILE" >/dev/null 2>&1; then
            report "timestamp signature: OK"
          else
            report "timestamp signature: INVALID (or TSA cert not trusted)"
          fi
        fi
      else
        report "timestamp: INVALID (parse error)"
      fi
      rm -f "$TSR_BIN"
    else
      report "timestamp: (empty)"
    fi
  else
    report "timestamp: (empty)"
  fi
else
  report "timestamp: (none)"
fi

ok
