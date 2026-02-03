/**
 * Task 6.3 + Plan Phase 3 UI — Verify page
 *
 * Fetches GET /api/ai/verify/:id, displays Trust Summary (P3.2), prompt, response,
 * hash, signature, tsaToken, model, date. Optional "Verify hash" button for client-side check.
 */

"use client";

import { PqcBadge } from "@/app/components/PqcBadge";
import { trackEvent } from "@/lib/analytics";
import { TOOLTIPS } from "@/lib/tooltips";
import Image from "next/image";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";

interface VerifyRecord {
  id: number;
  prompt: string;
  response: string;
  responseHash: string;
  signature: string | null;
  tsaToken: string | null;
  llmModel: string;
  createdAt: string;
  hashMatch?: boolean;
  signatureValid?: string; // "valid" | "invalid" | "n_a"
  claim?: string | null;
  confidence?: number | null;
  policyVersion?: string | null;
  policyCoverage?: number | null;
  policyRulesEvaluated?: PolicyRuleResult[] | null;
  /** PQC.5/PQC.6: Base64 ML-DSA signature when PQC enabled */
  signaturePqc?: string | null;
  /** e.g. "ML-DSA (Dilithium3)" */
  pqcAlgorithm?: string | null;
}

interface PolicyRuleResult {
  ruleId: string;
  status: "pass" | "not_evaluated";
}

/** Canonicalize text (same rules as backend). */
function canonicalize(text: string): string {
  if (!text) return "";
  const nfc = text.normalize("NFC");
  const linesOnly = nfc.replace(/\r\n/g, "\n").replace(/\r/g, "\n");
  const parts = linesOnly.split("\n");
  const out: string[] = [];
  let lastWasBlank = false;
  for (const part of parts) {
    const trimmed = part.trim();
    const blank = trimmed === "";
    if (blank) {
      if (!lastWasBlank) out.push("");
      lastWasBlank = true;
    } else {
      out.push(trimmed);
      lastWasBlank = false;
    }
  }
  if (out.length > 0 && out[out.length - 1] === "") out.pop();
  let joined = out.join("\n");
  if (joined !== "") joined += "\n";
  return joined;
}

/** Compute SHA-256 hex of UTF-8 bytes. */
async function sha256Hex(text: string): Promise<string> {
  const bytes = new TextEncoder().encode(text);
  const buf = await crypto.subtle.digest("SHA-256", bytes);
  return Array.from(new Uint8Array(buf))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

/**
 * Build claim canonical JSON (same format as backend ClaimCanonical.toCanonicalBytes).
 * Keys alphabetically: claim, confidence, model, policy_version. Used when record has claim.
 */
function claimCanonicalJson(
  claim: string | null | undefined,
  confidence: number | null | undefined,
  model: string | null | undefined,
  policyVersion: string | null | undefined
): string {
  const escapeJson = (s: string) =>
    (s ?? "")
      .replace(/\\/g, "\\\\")
      .replace(/"/g, '\\"')
      .replace(/\n/g, "\\n")
      .replace(/\r/g, "\\r")
      .replace(/\t/g, "\\t");
  const c = escapeJson(claim ?? "");
  const m = escapeJson(model ?? "");
  const p = escapeJson(policyVersion ?? "");
  const conf = confidence != null ? Number(confidence) : 0;
  return `{"claim":"${c}","confidence":${conf},"model":"${m}","policy_version":"${p}"}`;
}

function truncateMiddle(str: string, head = 20, tail = 20): string {
  if (!str || str.length <= head + tail) return str;
  return `${str.slice(0, head)}...${str.slice(-tail)}`;
}

/** Format policy version for display (e.g. "gdpr-2024" → "GDPR-2024"). */
function formatPolicyVersion(value: string | null | undefined): string {
  if (value == null || value === "") return "";
  return value
    .split("-")
    .map((part) => part.toUpperCase())
    .join("-");
}

/** P3.9 — Short descriptions for Evidence Package file names (preview modal). PQC.4: PQC artifacts. */
const EVIDENCE_FILE_DESCRIPTIONS: Record<string, string> = {
  "response.txt": "Response text",
  "canonical.bin": "Canonical bytes",
  "hash.sha256": "SHA-256 hash",
  "signature.sig": "Digital signature",
  "timestamp.tsr": "Timestamp token",
  "metadata.json": "Claim metadata",
  "public_key.pem": "Public key",
  "signature_pqc.sig": "Post-quantum (ML-DSA) signature",
  "pqc_public_key.pem": "PQC public key",
  "pqc_algorithm.json": "PQC algorithm (e.g. ML-DSA Dilithium3)",
};

const DEMO_POLICY_RULES: Record<string, string> = {
  R1: "Response is signed and timestamped",
  R2: "Model identity is recorded",
  R3: "No medical or legal advice in response",
  R4: "Human review performed",
};

const DEMO_POLICY_TOTAL_RULES = 4;

function VerifyContent() {
  const searchParams = useSearchParams();
  const id = searchParams.get("id");
  const [record, setRecord] = useState<VerifyRecord | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [hashMatch, setHashMatch] = useState<boolean | null>(null);
  const [hashChecking, setHashChecking] = useState(false);
  const [claimExpanded, setClaimExpanded] = useState(false);
  const [evidenceDownloading, setEvidenceDownloading] = useState(false);
  const [evidenceError, setEvidenceError] = useState<string | null>(null);
  const [verifierDownloading, setVerifierDownloading] = useState(false);
  const [verifierError, setVerifierError] = useState<string | null>(null);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [previewKeys, setPreviewKeys] = useState<string[] | null>(null);
  const [previewError, setPreviewError] = useState<string | null>(null);
  const [whyNotExpanded, setWhyNotExpanded] = useState(false);

  const apiUrl = process.env.NEXT_PUBLIC_API_URL || "";
  const apiBase = typeof window !== "undefined" ? "" : apiUrl;

  useEffect(() => {
    if (!id) {
      setError("Missing id in URL (e.g. /verify?id=1)");
      setIsLoading(false);
      return;
    }
    fetch(`${apiBase}/api/ai/verify/${id}`)
      .then((res) => {
        if (!res.ok) {
          if (res.status === 404) throw new Error("Record not found");
          throw new Error(`Request failed (${res.status})`);
        }
        return res.json();
      })
      .then(setRecord)
      .catch((err) => setError(err.message))
      .finally(() => setIsLoading(false));
  }, [id, apiBase]);

  useEffect(() => {
    void trackEvent("demo_view");
  }, []);

  async function handleVerifyHash() {
    if (!record) return;
    setHashChecking(true);
    setHashMatch(null);
    try {
      const responseCanonical = canonicalize(record.response);
      const hasClaim =
        (record.claim != null && String(record.claim).trim() !== "") ||
        (record.policyVersion != null && String(record.policyVersion).trim() !== "");
      const toHash = hasClaim
        ? responseCanonical + "\n" + claimCanonicalJson(record.claim, record.confidence, record.llmModel, record.policyVersion)
        : responseCanonical;
      const computed = await sha256Hex(toHash);
      const stored = (record.responseHash || "").toLowerCase();
      setHashMatch(computed === stored);
    } catch {
      setHashMatch(false);
    } finally {
      setHashChecking(false);
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center gap-2 text-zinc-600 dark:text-zinc-400">
        <span
          className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-zinc-400 border-t-transparent"
          aria-hidden
        />
        Loading…
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-4">
        <p className="text-red-600 dark:text-red-400">{error}</p>
        <Link
          href="/"
          className="text-sm text-blue-600 hover:underline dark:text-blue-400"
        >
          ← Back to home
        </Link>
      </div>
    );
  }

  if (!record) return null;

  const hasSignature = !!(record.signature?.trim());
  const hasTsaToken = !!(record.tsaToken?.trim());
  const isVerified = hasSignature && hasTsaToken;

  const createdUtc =
    record.createdAt &&
    `${new Date(record.createdAt).toISOString().slice(0, 19).replace("T", " ")} UTC`;

  const integrityLabel =
    record.hashMatch === true
      ? "Not altered"
      : record.hashMatch === false
        ? "Altered or unknown"
        : "—";

  const timestampLabel = hasTsaToken ? "Trusted (RFC 3161)" : "—";

  const summaryLine = [
    isVerified ? "Verified AI Response" : "AI Response",
    createdUtc && `Created: ${createdUtc}`,
    record.llmModel && `Model: ${record.llmModel}`,
    `Integrity: ${integrityLabel}`,
    `Timestamp: ${timestampLabel}`,
  ]
    .filter(Boolean)
    .join(" — ");

  const policyRules = record.policyRulesEvaluated ?? [];
  const hasPolicyCoverage =
    record.policyCoverage != null || policyRules.length > 0;
  const passRules = policyRules.filter((rule) => rule.status === "pass");
  const notEvaluatedRules = policyRules.filter(
    (rule) => rule.status !== "pass"
  );
  const coveragePercent =
    record.policyCoverage != null
      ? Math.round(record.policyCoverage * 100)
      : policyRules.length > 0
        ? Math.round((passRules.length / DEMO_POLICY_TOTAL_RULES) * 100)
        : null;
  const rulesChecked =
    policyRules.length > 0
      ? passRules.length
      : record.policyCoverage != null
        ? Math.round(record.policyCoverage * DEMO_POLICY_TOTAL_RULES)
        : 0;

  function handleCopySummary() {
    if (summaryLine) void navigator.clipboard.writeText(summaryLine);
  }

  function handleCopyResponse() {
    if (!record) return;
    if (record.response) void navigator.clipboard.writeText(record.response);
  }

  async function handleDownloadEvidence() {
    if (!record) return;
    void trackEvent("download_evidence_click", { responseId: record.id });
    setEvidenceDownloading(true);
    setEvidenceError(null);
    try {
      const res = await fetch(`${apiBase}/api/ai/evidence/${record.id}`);
      if (!res.ok) {
        const text = await res.text();
        const msg =
          res.status === 404
            ? "Response not found"
            : res.status === 503
              ? "Signing key not configured"
              : text || `Download failed (${res.status})`;
        setEvidenceError(msg);
        return;
      }
      const blob = await res.blob();
      const disposition = res.headers.get("Content-Disposition");
      let filename = `aletheia-evidence-${record.id}.aep`;
      if (disposition) {
        const match = /filename[*]?=(?:UTF-8'')?"?([^";\n]+)"?/i.exec(disposition);
        if (match) filename = match[1].trim();
      }
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      a.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      setEvidenceError(err instanceof Error ? err.message : "Download failed");
    } finally {
      setEvidenceDownloading(false);
    }
  }

  async function handleDownloadVerifier() {
    setVerifierDownloading(true);
    setVerifierError(null);
    try {
      const res = await fetch(`${apiBase}/api/ai/verifier`);
      if (!res.ok) {
        const text = await res.text();
        let msg = `Download failed (${res.status})`;
        try {
          const json = JSON.parse(text);
          if (json.message) msg = json.message;
          else if (json.error) msg = json.error;
        } catch {
          if (text) msg = text;
        }
        setVerifierError(msg);
        return;
      }
      const blob = await res.blob();
      const disposition = res.headers.get("Content-Disposition");
      let filename = "aletheia-verifier.jar";
      if (disposition) {
        const match = /filename[*]?=(?:UTF-8'')?"?([^";\n]+)"?/i.exec(disposition);
        if (match) filename = match[1].trim();
      }
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      a.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      setVerifierError(err instanceof Error ? err.message : "Download failed");
    } finally {
      setVerifierDownloading(false);
    }
  }

  async function handlePreviewPackage() {
    if (!record) return;
    setPreviewError(null);
    setPreviewKeys(null);
    setPreviewOpen(true);
    try {
      const res = await fetch(`${apiBase}/api/ai/evidence/${record.id}?format=json`);
      if (!res.ok) {
        const text = await res.text();
        setPreviewError(
          res.status === 404
            ? "Response not found"
            : res.status === 503
              ? "Signing key not configured"
              : text || `Request failed (${res.status})`
        );
        return;
      }
      const data = (await res.json()) as Record<string, string>;
      setPreviewKeys(Object.keys(data).sort());
    } catch (err) {
      setPreviewError(err instanceof Error ? err.message : "Failed to load preview");
    }
  }

  return (
    <div className="space-y-6">
      {/* P3.2 — Trust Summary Card (Section 1) */}
      <section
        className="rounded-xl border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-600 dark:bg-zinc-700/30"
        aria-label="Trust summary"
      >
        <div className="mb-3 flex flex-wrap items-center gap-2">
          <h2
            className="text-lg font-semibold text-zinc-900 dark:text-zinc-50"
            title={TOOLTIPS.verified_ai_response}
          >
            {isVerified ? "✅ Verified AI Response" : "AI Response"}
          </h2>
          {record.signaturePqc != null && record.signaturePqc.trim() !== "" && (
            <PqcBadge variant="default" />
          )}
        </div>
        <dl className="space-y-1.5 text-sm">
          <div className="flex flex-wrap gap-x-2">
            <dt className="text-zinc-500 dark:text-zinc-400">🕒 Created:</dt>
            <dd className="text-zinc-700 dark:text-zinc-300">{createdUtc || "—"}</dd>
          </div>
          <div className="flex flex-wrap gap-x-2">
            <dt className="text-zinc-500 dark:text-zinc-400">🤖 Model:</dt>
            <dd className="text-zinc-700 dark:text-zinc-300">{record.llmModel || "—"}</dd>
          </div>
          <div className="flex flex-wrap gap-x-2">
            <dt className="text-zinc-500 dark:text-zinc-400">🛡️ Integrity:</dt>
            <dd
              className="text-zinc-700 dark:text-zinc-300"
              title={TOOLTIPS.integrity_not_altered}
            >
              {integrityLabel}
            </dd>
          </div>
          <div className="flex flex-wrap gap-x-2">
            <dt className="text-zinc-500 dark:text-zinc-400">⏱️ Timestamp:</dt>
            <dd
              className="text-zinc-700 dark:text-zinc-300"
              title={TOOLTIPS.timestamp_trusted}
            >
              {timestampLabel}
            </dd>
          </div>
        </dl>
        <div className="mt-3 flex flex-wrap gap-2">
          <details className="rounded-xl border border-zinc-200 bg-white px-3 py-2 dark:border-zinc-600 dark:bg-zinc-800">
            <summary
              className="cursor-pointer list-none text-sm font-medium text-blue-600 dark:text-blue-400"
              title={TOOLTIPS.what_is_verified}
            >
              🔎 What is verified?
            </summary>
            <p className="mt-2 text-xs text-zinc-600 dark:text-zinc-400">
              The signature covers the response text in canonical form. If this response
              includes an AI claim (claim, confidence, policy version), those are also
              part of the signed payload. You can verify the response offline using the
              Evidence Package.
            </p>
          </details>
          <button
            type="button"
            onClick={handleCopySummary}
            title={TOOLTIPS.copy_summary}
            className="inline-flex items-center gap-1.5 rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 dark:border-zinc-600 dark:bg-zinc-800 dark:text-zinc-300 dark:hover:bg-zinc-700"
          >
            📋 Copy summary
          </button>
        </div>
      </section>

      {/* P3.3 — Section 2: Prompt & Response with Copy response */}
      <section aria-label="Prompt and Response" className="space-y-4">
        <div>
          <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Prompt
          </h2>
          <p className="whitespace-pre-wrap rounded-xl bg-zinc-100 px-3 py-2 text-zinc-800 dark:bg-zinc-700 dark:text-zinc-200">
            {record.prompt}
          </p>
        </div>
        <div>
          <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Response
          </h2>
          <p className="whitespace-pre-wrap rounded-xl bg-zinc-100 px-3 py-2 text-zinc-800 dark:bg-zinc-700 dark:text-zinc-200">
            {record.response}
          </p>
          <button
            type="button"
            onClick={handleCopyResponse}
            title={TOOLTIPS.copy_response}
            className="mt-2 inline-flex items-center gap-1.5 rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 dark:border-zinc-600 dark:bg-zinc-800 dark:text-zinc-300 dark:hover:bg-zinc-700"
          >
            📋 Copy response
          </button>
        </div>
      </section>

      {/* P3.4 — Section 3: AI Claim (only when claim or policyVersion present) */}
      {(record.claim != null && String(record.claim).trim() !== "") ||
      (record.policyVersion != null && String(record.policyVersion).trim() !== "") ? (
        <section
          className="rounded-xl border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-600 dark:bg-zinc-700/30"
          aria-label="AI Claim"
        >
          <h2
            className="mb-3 text-lg font-semibold text-zinc-900 dark:text-zinc-50"
            title={TOOLTIPS.ai_claim_heading}
          >
            🧠 AI Claim
          </h2>
          {record.claim != null && String(record.claim).trim() !== "" && (
            <div className="mb-3">
              <p className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
                Claim:
              </p>
              {record.claim.length > 200 && !claimExpanded ? (
                <>
                  <blockquote className="border-l-2 border-zinc-300 pl-3 text-zinc-700 dark:border-zinc-500 dark:text-zinc-300">
                    &ldquo;{record.claim.slice(0, 200)}…&rdquo;
                  </blockquote>
                  <button
                    type="button"
                    onClick={() => setClaimExpanded(true)}
                    className="mt-1 rounded-xl text-sm text-blue-600 hover:underline dark:text-blue-400"
                  >
                    Show more
                  </button>
                </>
              ) : (
                <blockquote className="border-l-2 border-zinc-300 pl-3 text-zinc-700 dark:border-zinc-500 dark:text-zinc-300">
                  &ldquo;{record.claim}&rdquo;
                </blockquote>
              )}
            </div>
          )}
          {record.confidence != null && (
            <div className="mb-3 flex flex-wrap gap-x-2">
              <span className="text-sm font-medium text-zinc-600 dark:text-zinc-400">
                Confidence:
              </span>
              <span
                className="text-sm text-zinc-700 dark:text-zinc-300"
                title={TOOLTIPS.confidence}
              >
                {typeof record.confidence === "number"
                  ? record.confidence >= 0 && record.confidence <= 1
                    ? `${Math.round(record.confidence * 100)}%`
                    : String(record.confidence)
                  : String(record.confidence)}
              </span>
            </div>
          )}
          {record.policyVersion != null && String(record.policyVersion).trim() !== "" && (
            <div className="mb-3 flex flex-wrap gap-x-2">
              <span className="text-sm font-medium text-zinc-600 dark:text-zinc-400">
                Policy version:
              </span>
              <span
                className="text-sm text-zinc-700 dark:text-zinc-300"
                title={TOOLTIPS.policy_version}
              >
                {formatPolicyVersion(record.policyVersion) || record.policyVersion}
              </span>
            </div>
          )}
          <p
            className="inline-flex items-center gap-1.5 rounded-xl bg-zinc-200/80 px-2 py-1 text-xs font-medium text-zinc-700 dark:bg-zinc-600/80 dark:text-zinc-300"
            title={TOOLTIPS.included_in_signed_payload}
          >
            🔐 Included in signed payload
          </p>
        </section>
      ) : null}

      {/* Phase 4 — Policy coverage */}
      {hasPolicyCoverage && (
        <section
          className="rounded-xl border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-600 dark:bg-zinc-700/30"
          aria-label="Policy coverage"
        >
          <div className="mb-3 flex flex-wrap items-center gap-2">
            <h2
              className="text-lg font-semibold text-zinc-900 dark:text-zinc-50"
              title={TOOLTIPS.policy_coverage}
            >
              📘 Policy coverage (demo)
            </h2>
            <span className="text-sm text-zinc-600 dark:text-zinc-400">
              {coveragePercent != null
                ? `${coveragePercent}% — ${rulesChecked} of ${DEMO_POLICY_TOTAL_RULES} rules checked`
                : "Coverage not available"}
            </span>
          </div>

          {policyRules.length > 0 ? (
            <ul className="space-y-2 text-sm text-zinc-700 dark:text-zinc-300">
              {policyRules.map((rule) => {
                const statusOk = rule.status === "pass";
                return (
                  <li key={rule.ruleId} className="flex flex-wrap items-center gap-2">
                    <span className="font-mono text-zinc-800 dark:text-zinc-200">
                      {rule.ruleId}
                    </span>
                    <span className="text-zinc-500 dark:text-zinc-400">
                      {DEMO_POLICY_RULES[rule.ruleId] || "Policy rule"}
                    </span>
                    <span
                      className={
                        statusOk
                          ? "ml-auto text-emerald-600 dark:text-emerald-400"
                          : "ml-auto text-zinc-500 dark:text-zinc-400"
                      }
                    >
                      {statusOk ? "✓ Passed" : "Not checked"}
                    </span>
                  </li>
                );
              })}
            </ul>
          ) : (
            <p className="text-sm text-zinc-500 dark:text-zinc-400">
              Policy rule details are not available for this record.
            </p>
          )}

          <div className="mt-3">
            <button
              type="button"
              onClick={() => setWhyNotExpanded((prev) => !prev)}
              aria-expanded={whyNotExpanded}
              aria-controls="why-not-100"
              className="text-sm font-medium text-blue-600 hover:underline dark:text-blue-400"
            >
              Why is confidence not 100%?
            </button>
            {whyNotExpanded && (
              <div
                id="why-not-100"
                className="mt-3 rounded-xl border border-zinc-200 bg-white p-3 text-sm text-zinc-700 dark:border-zinc-600 dark:bg-zinc-800 dark:text-zinc-300"
              >
                <p className="mb-2">
                  Confidence reflects how many of the declared policy checks were
                  performed for this response.
                </p>
                {policyRules.length > 0 ? (
                  <>
                    <p className="mb-1 font-medium text-zinc-600 dark:text-zinc-400">
                      Checked:
                    </p>
                    <ul className="mb-2 list-disc pl-5">
                      {passRules.length > 0 ? (
                        passRules.map((rule) => (
                          <li key={`checked-${rule.ruleId}`}>
                            {rule.ruleId}: {DEMO_POLICY_RULES[rule.ruleId] || "Policy rule"}
                          </li>
                        ))
                      ) : (
                        <li>None</li>
                      )}
                    </ul>
                    <p className="mb-1 font-medium text-zinc-600 dark:text-zinc-400">
                      Not checked:
                    </p>
                    <ul className="mb-2 list-disc pl-5">
                      {notEvaluatedRules.length > 0 ? (
                        notEvaluatedRules.map((rule) => (
                          <li key={`unchecked-${rule.ruleId}`}>
                            {rule.ruleId}: {DEMO_POLICY_RULES[rule.ruleId] || "Policy rule"}
                          </li>
                        ))
                      ) : (
                        <li>None</li>
                      )}
                    </ul>
                  </>
                ) : (
                  <p className="mb-2">
                    Rule-level details were not provided, but coverage indicates that
                    not all policy checks were run.
                  </p>
                )}
                <p className="text-zinc-500 dark:text-zinc-400">
                  We do not certify truth; we show what was checked.
                </p>
              </div>
            )}
          </div>
        </section>
      )}

      <div>
        <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
          Backend verification
        </h2>
        <div className="flex flex-wrap gap-3">
          <span
            className={
              record.hashMatch === true
                ? "text-emerald-600 dark:text-emerald-400"
                : record.hashMatch === false
                  ? "text-red-600 dark:text-red-400"
                  : "text-zinc-500"
            }
          >
            Hash match: {record.hashMatch === true ? "✓ yes" : record.hashMatch === false ? "✗ no" : "—"}
          </span>
          <span
            className={
              record.signatureValid === "valid"
                ? "text-emerald-600 dark:text-emerald-400"
                : record.signatureValid === "invalid"
                  ? "text-red-600 dark:text-red-400"
                  : "text-zinc-500"
            }
          >
            Signature: {record.signatureValid === "valid" ? "✓ valid" : record.signatureValid === "invalid" ? "✗ invalid" : "n/a"}
          </span>
          {record.signaturePqc != null && record.signaturePqc.trim() !== "" ? (
            <span className="text-emerald-600 dark:text-emerald-400" title={TOOLTIPS.pqc_badge}>
              PQC signature: ✓ present
            </span>
          ) : (
            <span className="text-zinc-500">PQC signature: — not included</span>
          )}
        </div>
      </div>

      <div>
        <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
          Response hash (SHA-256)
        </h2>
        <p className="break-all font-mono text-sm text-zinc-700 dark:text-zinc-300">
          {record.responseHash}
        </p>
      </div>

      {record.signature && (
        <div>
          <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Signature (Base64)
          </h2>
          <p className="break-all font-mono text-sm text-zinc-700 dark:text-zinc-300">
            {truncateMiddle(record.signature)}
          </p>
        </div>
      )}

      {record.tsaToken && (
        <div>
          <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
            TSA token (Base64)
          </h2>
          <p className="break-all font-mono text-sm text-zinc-700 dark:text-zinc-300">
            {truncateMiddle(record.tsaToken)}
          </p>
        </div>
      )}

      {record.signaturePqc != null && record.signaturePqc.trim() !== "" && (
        <>
          <div>
            <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
              PQC signature (Base64)
            </h2>
            <p
              className="break-all font-mono text-sm text-zinc-700 dark:text-zinc-300"
              title={TOOLTIPS.pqc_badge}
            >
              {truncateMiddle(record.signaturePqc)}
            </p>
          </div>
          {record.pqcAlgorithm && (
            <div>
              <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
                PQC algorithm
              </h2>
              <p className="font-mono text-sm text-zinc-700 dark:text-zinc-300">
                {record.pqcAlgorithm}
              </p>
            </div>
          )}
        </>
      )}

      <div className="border-t border-zinc-200 pt-4 dark:border-zinc-600">
        <button
          type="button"
          onClick={handleVerifyHash}
          disabled={hashChecking}
          className="rounded-xl bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60 dark:bg-blue-500 dark:hover:bg-blue-600"
        >
          {hashChecking ? "Verifying…" : "Verify hash"}
        </button>
        {hashMatch === true && (
          <p className="mt-2 text-emerald-600 dark:text-emerald-400">
            ✓ Hash match — response was not altered
          </p>
        )}
        {hashMatch === false && (
          <p className="mt-2 text-red-600 dark:text-red-400">
            ✗ Hash mismatch — response may have been altered
          </p>
        )}
      </div>

      {/* P3.6 — Section 5: Evidence Package */}
      <section
        className="rounded-xl border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-600 dark:bg-zinc-700/30"
        aria-label="Evidence Package"
      >
        <h2 className="mb-3 text-lg font-semibold text-zinc-900 dark:text-zinc-50">
          📦 Evidence Package
        </h2>
        <p
          className="mb-3 text-sm text-zinc-700 dark:text-zinc-300"
          title={TOOLTIPS.verified_offline}
        >
          This response can be verified offline.
        </p>
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={handleDownloadEvidence}
            disabled={evidenceDownloading}
            title={TOOLTIPS.download_evidence}
            className="inline-flex items-center gap-1.5 rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 disabled:opacity-60 dark:border-zinc-600 dark:bg-zinc-800 dark:text-zinc-300 dark:hover:bg-zinc-700"
          >
            {evidenceDownloading ? "Downloading…" : "⬇ Download evidence"}
          </button>
          <button
            type="button"
            onClick={handlePreviewPackage}
            title={TOOLTIPS.preview_package}
            className="inline-flex items-center gap-1.5 rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 dark:border-zinc-600 dark:bg-zinc-800 dark:text-zinc-300 dark:hover:bg-zinc-700"
          >
            👀 Preview package
          </button>
        </div>
        {evidenceError && (
          <p className="mt-2 text-sm text-red-600 dark:text-red-400">{evidenceError}</p>
        )}
      </section>

      {/* P3.10 — Section 5b: Verifier utility download and usage box */}
      <section
        className="rounded-xl border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-600 dark:bg-zinc-700/30"
        aria-label="Verifier utility"
      >
        <h2 className="mb-3 text-lg font-semibold text-zinc-900 dark:text-zinc-50">
          🔧 Verifier utility
        </h2>
        <p className="mb-3 text-sm text-zinc-700 dark:text-zinc-300">
          Verify Evidence Packages without the Aletheia server. Requires Java 21+.
        </p>
        <button
          type="button"
          onClick={handleDownloadVerifier}
          disabled={verifierDownloading}
          title={TOOLTIPS.download_verifier}
            className="mb-4 inline-flex items-center gap-1.5 rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 disabled:opacity-60 dark:border-zinc-600 dark:bg-zinc-800 dark:text-zinc-300 dark:hover:bg-zinc-700"
          >
            {verifierDownloading ? "Downloading…" : "⬇ Download verifier"}
        </button>
        {verifierError && (
          <p className="mb-4 text-sm text-red-600 dark:text-red-400">{verifierError}</p>
        )}
        <div
          className="rounded-xl border border-zinc-200 bg-white p-3 dark:border-zinc-600 dark:bg-zinc-800"
          aria-label="How to use"
        >
          <p className="mb-2 text-sm font-medium text-zinc-700 dark:text-zinc-300">
            How to use
          </p>
          <ol className="list-decimal list-inside space-y-1.5 text-sm text-zinc-600 dark:text-zinc-400">
            <li>Download the verifier JAR above.</li>
            <li>
              Run from a terminal:
              <div className="relative mt-1">
                <pre className="overflow-x-auto rounded-xl bg-zinc-100 px-2 py-1.5 pr-10 font-mono text-xs dark:bg-zinc-700">
                  java -jar aletheia-verifier.jar /path/to/your.evidence.aep
                </pre>
                <button
                  type="button"
                  onClick={() => {
                    void navigator.clipboard.writeText("java -jar aletheia-verifier.jar /path/to/your.evidence.aep");
                  }}
                  title="Copy command"
                  aria-label="Copy command"
                  className="absolute right-1.5 top-1.5 rounded-xl p-1.5 text-zinc-500 hover:bg-zinc-200/80 hover:text-zinc-700 dark:text-zinc-400 dark:hover:bg-zinc-600 dark:hover:text-zinc-200"
                >
                  <svg className="h-3.5 w-3.5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
                    <rect width="14" height="14" x="8" y="8" rx="2" ry="2" />
                    <path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2" />
                  </svg>
                </button>
              </div>
              <span className="text-zinc-500 dark:text-zinc-500">
                (or path to an extracted Evidence Package folder)
              </span>
            </li>
            <li>No network or backend required.</li>
            <li>Exit code 0 = VALID, 1 = INVALID.</li>
          </ol>
        </div>
      </section>

      {/* Preview package modal */}
      {previewOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
          role="dialog"
          aria-modal="true"
          aria-labelledby="preview-modal-title"
        >
          <div className="max-h-[80vh] w-full max-w-md overflow-auto rounded-xl border border-zinc-200 bg-white p-4 shadow-lg dark:border-zinc-600 dark:bg-zinc-800">
            <h3 id="preview-modal-title" className="mb-3 text-lg font-semibold text-zinc-900 dark:text-zinc-50">
              Package contents
            </h3>
            {previewError && (
              <p className="mb-3 text-sm text-red-600 dark:text-red-400">{previewError}</p>
            )}
            {previewKeys && previewKeys.length > 0 && (
              <ul className="mb-4 space-y-2 text-sm text-zinc-700 dark:text-zinc-300">
                {previewKeys.map((key) => (
                  <li key={key} className="flex flex-wrap items-baseline gap-2">
                    <span className="font-mono text-zinc-800 dark:text-zinc-200">{key}</span>
                    {EVIDENCE_FILE_DESCRIPTIONS[key] && (
                      <span className="text-zinc-500 dark:text-zinc-400">
                        — {EVIDENCE_FILE_DESCRIPTIONS[key]}
                      </span>
                    )}
                  </li>
                ))}
              </ul>
            )}
            {previewKeys && previewKeys.length === 0 && !previewError && (
              <p className="mb-4 text-sm text-zinc-500">No files in package.</p>
            )}
            <button
              type="button"
              onClick={() => {
                setPreviewOpen(false);
                setPreviewKeys(null);
                setPreviewError(null);
              }}
              className="rounded border border-zinc-200 bg-zinc-100 px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-200 dark:border-zinc-600 dark:bg-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-600"
            >
              Close
            </button>
          </div>
        </div>
      )}

      <Link
        href="/"
        className="inline-block text-sm text-blue-600 hover:underline dark:text-blue-400"
      >
        ← Back to home
      </Link>
    </div>
  );
}

export default function VerifyPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 p-8 dark:bg-zinc-900">
      <main className="w-full max-w-2xl space-y-6 rounded-xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
        <div className="flex items-center gap-4">
          <Link href="/" className="flex shrink-0">
            <Image
              src="/logo.png"
              alt="Aletheia AI"
              width={48}
              height={48}
              className="rounded-xl bg-white dark:bg-zinc-800"
              unoptimized
            />
          </Link>
          <div>
            <h1 className="text-xl font-bold text-zinc-900 dark:text-zinc-50">
              Verify response
            </h1>
            <p className="text-sm text-zinc-600 dark:text-zinc-400">
              Cryptographic verification data
            </p>
          </div>
        </div>

        <Suspense fallback={<div>Loading…</div>}>
          <VerifyContent />
        </Suspense>

        <footer className="mt-8 border-t border-zinc-200 pt-4 text-center text-xs text-zinc-500 dark:border-zinc-600 dark:text-zinc-400">
          <p>© 2026 Anton Sokolov &amp; Team 3</p>
          <p className="mt-1">
            <Link
              href="/use-cases"
              className="underline hover:text-zinc-700 dark:hover:text-zinc-300"
            >
              Use cases
            </Link>
          </p>
          <p>
            <a
              href="https://taltech.ee/vanemarendajaks"
              target="_blank"
              rel="noopener noreferrer"
              className="underline hover:text-zinc-700 dark:hover:text-zinc-300"
            >
              Koolitus „Noorem-tarkvaraarendajast vanemarendajaks“
            </a>
            {" — "}
            Tallinna Tehnikaülikool
          </p>
          {/* P3.7 — Section 6: Footer line */}
          <p className="mt-2">Designed for audit, compliance, and long-term verification</p>
        </footer>
      </main>
    </div>
  );
}
