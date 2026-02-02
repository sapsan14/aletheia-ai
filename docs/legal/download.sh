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

echo "Done. ETSI EN 319421/319422, RFC 3161: download manually if script yields empty files (see README)."
ls -la "$OUT"
