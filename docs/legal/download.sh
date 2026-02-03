#!/bin/bash
# Download EU legal documents (English PDFs) to docs/legal/downloads/
# Run from project root: ./docs/legal/download.sh

set -e
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT="${DIR}/downloads"
mkdir -p "$OUT"

echo "Downloading EU regulations to $OUT ..."

curl -sSL -o "${OUT}/eidas1_regulation_910_2014.pdf" \
  "https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=CELEX:32014R0910"

curl -sSL -o "${OUT}/eidas2_regulation_2024_1183.pdf" \
  "https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=OJ:L_202401183"

curl -sSL -o "${OUT}/eu_ai_act_regulation_2024_1689.pdf" \
  "https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=OJ:L_202401689"

curl -sSL -o "${OUT}/gdpr_regulation_2016_679.pdf" \
  "https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=CELEX:32016R0679"

curl -sSL -o "${OUT}/rfc3161_timestamp_protocol.pdf" \
  "https://www.rfc-editor.org/rfc/pdfrfc/rfc3161.txt.pdf"

echo "Downloading NIST Post-Quantum Cryptography (PQC) standards to $OUT ..."

curl -sSL -o "${OUT}/nist_fips_203_mlkem.pdf" \
  "https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.203.pdf"

curl -sSL -o "${OUT}/nist_fips_204_mldsa.pdf" \
  "https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.204.pdf"

curl -sSL -o "${OUT}/nist_fips_205_slhdsa.pdf" \
  "https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.205.pdf"

echo "Done. EU regulations, RFC 3161, NIST FIPS 203/204/205 (PQC) saved to $OUT."
echo "ETSI EN 319421/319422 and ETSI TR (PQC): download manually from www.etsi.org if needed (see README)."
ls -la "$OUT"
