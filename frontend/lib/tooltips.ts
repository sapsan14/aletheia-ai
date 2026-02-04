/**
 * Single source of truth for tooltip texts (Plan Phase 3 UI, P3.1).
 * Use by ID: title={TOOLTIPS.verified_ai_response} or aria-label={TOOLTIPS.copy_response}.
 * Do not hard-code tooltip strings in components.
 *
 * @see docs/en/PLAN_PHASE3_UI.md — Tooltip texts (copy-paste ready)
 */
export const TOOLTIPS: Record<string, string> = {
  verified_ai_response:
    "This response was cryptographically signed and timestamped. Its integrity and creation time can be independently verified.",
  integrity_not_altered:
    "The response content has not changed since it was signed.",
  timestamp_trusted:
    "The creation time is certified by an independent Time Stamping Authority.",
  what_is_verified:
    "See exactly which parts of the response are covered by the signature.",
  copy_summary: "Copies a short verification summary to the clipboard.",
  copy_response:
    "Copies the response text only (without signatures or metadata).",
  ai_claim_heading:
    "A structured statement derived from the AI response and included in the signed data.",
  confidence:
    "A subjective confidence score provided by the AI for this claim. This is not a legal guarantee.",
  policy_version:
    "Claim-policy: the policy or regulatory framework the AI used when forming this claim (e.g. AI-ACT-2024, GDPR).",
  policy_coverage:
    "Coverage-policy: share of rules (e.g. aletheia-demo 2026-01) that were evaluated for this response.",
  included_in_signed_payload:
    "This claim is cryptographically protected and cannot be changed without breaking verification.",
  response_hash: "A cryptographic fingerprint of the response content.",
  signature:
    "The digital signature created using the private key of the signing service.",
  timestamp_token:
    "Proof that the hash existed at a specific moment in time, issued by a trusted authority.",
  verify_hash:
    "Recalculates the hash locally and checks it against the signed value.",
  download_evidence:
    "Downloads all cryptographic materials required for independent verification.",
  preview_package:
    "Explore the contents of the evidence package before downloading.",
  verified_offline:
    "No connection to Aletheia AI is required to verify authenticity and integrity.",
  download_verifier:
    "Downloads the offline verifier (JAR) so you can verify Evidence Packages without the Aletheia server.",
  // PQC.6 — Quantum-Resistant badge
  pqc_badge:
    "This response includes a post-quantum (ML-DSA) signature in addition to the classical signature, so it remains verifiable even in a future with large-scale quantum computers.",
  // Phase 4.5 — Ambiguity / scope (3.2.1)
  ambiguity_scope_warning:
    "Coverage applies to this response only. Other conversation context (e.g. earlier turns, external tools) is not signed or checked. We don't certify truth; we certify responsibility.",
  // Phase 4.5 — Trust summary badges (3.4.1)
  trust_badge_signed: "Response is digitally signed; integrity can be verified.",
  trust_badge_timestamped: "Creation time certified by a trusted Time Stamping Authority.",
  trust_badge_policy: "Share of policy rules evaluated for this response.",
  trust_badge_claim: "AI claim (and Claim-policy) included in signed payload.",
  trust_badge_offline: "Evidence Package can be verified without the Aletheia server.",
};
