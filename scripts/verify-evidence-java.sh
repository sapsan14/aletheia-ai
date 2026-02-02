#!/usr/bin/env bash
# Run the Java offline verifier (VerifierMain). Use from repo root.
# Usage: ./scripts/verify-evidence-java.sh <path-to-dir-or-.aep>
# Exit: 0 = VALID, 1 = INVALID or error.

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKEND_DIR="$REPO_ROOT/backend"

[[ $# -eq 1 ]] || { echo "Usage: $0 <path-to-evidence-dir-or-.aep>" >&2; exit 1; }
PATH_ARG="$1"

cd "$BACKEND_DIR"
exec mvn -q exec:java \
  -Dexec.mainClass="ai.aletheia.verifier.VerifierMain" \
  -Dexec.args="$PATH_ARG"
