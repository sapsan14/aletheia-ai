/**
 * Single Page Verification Dashboard (Plan Phase 3 + PQC).
 *
 * Two-column layout: Chat (left) + Trust Panel (right). On "Send & Verify",
 * response and verification evidence appear on the same page; no redirect to /verify.
 */

"use client";

import { PqcBadge, PqcIcon } from "@/app/components/PqcBadge";
import { trackEvent } from "@/lib/analytics";
import { TOOLTIPS } from "@/lib/tooltips";
import Image from "next/image";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

/** Response from POST /api/ai/ask */
interface AiAskResponse {
  response: string;
  responseHash: string;
  signature: string | null;
  tsaToken: string | null;
  id: number;
  model: string;
}

/** Full record from GET /api/ai/verify/:id — same shape as verify page */
interface VerifyRecord {
  id: number;
  prompt: string;
  response: string;
  responseHash: string;
  computedHash?: string | null;
  signature: string | null;
  tsaToken: string | null;
  llmModel: string;
  createdAt: string;
  hashMatch?: boolean;
  signatureValid?: string;
  claim?: string | null;
  confidence?: number | null;
  policyVersion?: string | null;
  policyCoverage?: number | null;
  policyRulesEvaluated?: PolicyRuleResult[] | null;
  signaturePqc?: string | null;
  pqcAlgorithm?: string | null;
}

interface PolicyRuleResult {
  ruleId: string;
  status: "pass" | "not_evaluated";
}

function truncateMiddle(str: string, head = 20, tail = 20): string {
  if (!str || str.length <= head + tail) return str;
  return `${str.slice(0, head)}...${str.slice(-tail)}`;
}

function formatPolicyVersion(value: string | null | undefined): string {
  if (value == null || value === "") return "";
  return value.split("-").map((p) => p.toUpperCase()).join("-");
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

async function sha256Hex(text: string): Promise<string> {
  const bytes = new TextEncoder().encode(text);
  const buf = await crypto.subtle.digest("SHA-256", bytes);
  return Array.from(new Uint8Array(buf))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

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
  const confStr = conf.toFixed(6);
  return `{"claim":"${c}","confidence":${confStr},"model":"${m}","policy_version":"${p}"}`;
}

/** Legacy format (no toFixed) for verifying records saved before %.6f was introduced. */
function claimCanonicalJsonLegacy(
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

/** Small copy icon for copy summary / copy response buttons */
function CopyIcon({ className }: { className?: string }) {
  return (
    <svg
      className={className}
      xmlns="http://www.w3.org/2000/svg"
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden
    >
      <rect width="14" height="14" x="8" y="8" rx="2" ry="2" />
      <path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2" />
    </svg>
  );
}

function StatusItem({
  label,
  ok,
}: {
  label: string;
  ok: boolean;
}) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-xl px-2 py-0.5 text-sm ${
        ok
          ? "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400"
          : "bg-zinc-100 text-zinc-500 dark:bg-zinc-700 dark:text-zinc-400"
      }`}
    >
      {ok ? (
        <span className="text-emerald-600 dark:text-emerald-500" aria-hidden>✓</span>
      ) : (
        <span className="text-zinc-400 dark:text-zinc-500" aria-hidden>—</span>
      )}
      {label}
    </span>
  );
}

function TrustPanel({
  verifyRecord,
  responseData,
  apiBase,
  onDownloadEvidence,
  downloading,
  downloadError,
}: {
  verifyRecord: VerifyRecord | null;
  responseData: AiAskResponse | null;
  apiBase: string;
  onDownloadEvidence: () => void;
  downloading: boolean;
  downloadError: string | null;
}) {
  const [hashMatch, setHashMatch] = useState<boolean | null>(null);
  const [frontendComputedHash, setFrontendComputedHash] = useState<string | null>(null);
  const [hashChecking, setHashChecking] = useState(false);
  const [claimExpanded, setClaimExpanded] = useState(false);
  const [verifierDownloading, setVerifierDownloading] = useState(false);
  const [verifierError, setVerifierError] = useState<string | null>(null);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [previewKeys, setPreviewKeys] = useState<string[] | null>(null);
  const [previewError, setPreviewError] = useState<string | null>(null);

  const record = verifyRecord;
  const hasSignature = !!(record?.signature?.trim());
  const hasTsaToken = !!(record?.tsaToken?.trim());
  const isVerified = hasSignature && hasTsaToken;
  const hasPqc = !!(record?.signaturePqc?.trim());

  const createdUtc =
    record?.createdAt &&
    `${new Date(record.createdAt).toISOString().slice(0, 19).replace("T", " ")} UTC`;

  const integrityLabel =
    record?.hashMatch === true
      ? "Not altered"
      : record?.hashMatch === false
        ? "Altered or unknown"
        : "—";

  const timestampLabel = hasTsaToken ? "Trusted (RFC 3161)" : "—";

  const summaryLine =
    record &&
    [
      isVerified ? "Verified AI Response" : "AI Response",
      createdUtc && `Created: ${createdUtc}`,
      record.llmModel && `Model: ${record.llmModel}`,
      `Integrity: ${integrityLabel}`,
      `Timestamp: ${timestampLabel}`,
      "Coverage-policy: aletheia-demo (2026-01)",
    ]
      .filter(Boolean)
      .join(" — ");

  function handleCopySummary() {
    if (summaryLine) void navigator.clipboard.writeText(summaryLine);
  }

  async function handleVerifyHash() {
    const id = record?.id ?? responseData?.id;
    let dataToUse = record;
    if (id && dataToUse?.response && !dataToUse?.prompt && (dataToUse?.claim == null) && (dataToUse?.policyVersion == null)) {
      try {
        const res = await fetch(`${apiBase}/api/ai/verify/${id}`);
        if (res.ok) {
          const full = (await res.json()) as VerifyRecord;
          dataToUse = full;
        }
      } catch {
        /* use existing dataToUse */
      }
    }
    const responseText = dataToUse?.response ?? responseData?.response ?? "";
    const storedHash = dataToUse?.responseHash ?? responseData?.responseHash ?? "";
    if (!responseText || !storedHash) return;
    setHashChecking(true);
    setHashMatch(null);
    setFrontendComputedHash(null);
    try {
      const responseCanonical = canonicalize(responseText);
      const hasClaim =
        (dataToUse?.claim != null && String(dataToUse.claim).trim() !== "") ||
        (dataToUse?.policyVersion != null && String(dataToUse.policyVersion).trim() !== "");
      const toHash = hasClaim && dataToUse
        ? responseCanonical +
          "\n" +
          claimCanonicalJson(
            dataToUse.claim,
            dataToUse.confidence,
            dataToUse.llmModel,
            dataToUse.policyVersion
          )
        : responseCanonical;
      let computed = await sha256Hex(toHash);
      setFrontendComputedHash(computed);
      let match = computed === storedHash.toLowerCase();
      if (!match && hasClaim && dataToUse) {
        const toHashLegacy =
          responseCanonical +
          "\n" +
          claimCanonicalJsonLegacy(
            dataToUse.claim,
            dataToUse.confidence,
            dataToUse.llmModel,
            dataToUse.policyVersion
          );
        const computedLegacy = await sha256Hex(toHashLegacy);
        match = computedLegacy === storedHash.toLowerCase();
      }
      if (!match) {
        const responseOnlyHash = await sha256Hex(responseCanonical);
        match = responseOnlyHash === storedHash.toLowerCase();
      }
      setHashMatch(match);
    } catch {
      setHashMatch(false);
    } finally {
      setHashChecking(false);
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

  if (!verifyRecord && !responseData) {
    return (
      <section
        className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800"
        aria-label="Verification Evidence"
      >
        <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
          Verification Evidence
        </h2>
        <p className="text-sm text-zinc-500 dark:text-zinc-400">
          Response and verification data will appear here after you send a prompt.
        </p>
      </section>
    );
  }

  return (
    <section
      className="min-w-0 overflow-hidden rounded-xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800"
      aria-label="Verification Evidence"
    >
      <div className="min-w-0 space-y-6">
        {/* Trust Summary */}
        <div
          className="relative min-w-0 rounded-xl border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-600 dark:bg-zinc-700/30"
          aria-label="Trust summary"
        >
          <button
            type="button"
            onClick={handleCopySummary}
            title={TOOLTIPS.copy_summary}
            aria-label="Copy summary"
            className="absolute right-3 top-3 rounded-xl p-1.5 text-zinc-500 hover:bg-zinc-200/80 hover:text-zinc-700 dark:text-zinc-400 dark:hover:bg-zinc-600 dark:hover:text-zinc-200"
          >
            <CopyIcon className="h-4 w-4" />
          </button>
          <div className="mb-3 flex flex-wrap items-center gap-2 pr-8">
            <h2
              className="text-lg font-semibold text-zinc-900 dark:text-zinc-50"
              title={TOOLTIPS.verified_ai_response}
            >
              {isVerified ? "✅ Verified AI Response" : "AI Response"}
            </h2>
            {hasPqc && <PqcBadge variant="landing" />}
          </div>
          <dl className="space-y-1.5 text-sm">
            <div className="flex flex-wrap gap-x-2">
              <dt className="text-zinc-500 dark:text-zinc-400">🕒 Created:</dt>
              <dd className="text-zinc-700 dark:text-zinc-300">{createdUtc || "—"}</dd>
            </div>
            <div className="flex flex-wrap gap-x-2">
              <dt className="text-zinc-500 dark:text-zinc-400">🤖 Model:</dt>
              <dd className="text-zinc-700 dark:text-zinc-300">{record?.llmModel || "—"}</dd>
            </div>
            <div className="flex flex-wrap gap-x-2">
              <dt className="text-zinc-500 dark:text-zinc-400">🛡️ Integrity:</dt>
              <dd className="text-zinc-700 dark:text-zinc-300" title={TOOLTIPS.integrity_not_altered}>
                {integrityLabel}
              </dd>
            </div>
            <div className="flex flex-wrap gap-x-2">
              <dt className="text-zinc-500 dark:text-zinc-400">⏱️ Timestamp:</dt>
              <dd className="text-zinc-700 dark:text-zinc-300" title={TOOLTIPS.timestamp_trusted}>
                {timestampLabel}
              </dd>
            </div>
            <div className="flex flex-wrap gap-x-2">
              <dt className="text-zinc-500 dark:text-zinc-400">📘 Coverage-policy:</dt>
              <dd className="text-zinc-700 dark:text-zinc-300" title={TOOLTIPS.policy_coverage}>
                aletheia-demo (2026-01)
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
                includes an AI claim (claim, confidence, Claim-policy), those are also
                part of the signed payload. You can verify the response offline using the{" "}
                <button
                  type="button"
                  onClick={onDownloadEvidence}
                  className="font-medium text-blue-600 underline hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300"
                >
                  Evidence Package
                </button>
                {" "}and{" "}
                <button
                  type="button"
                  onClick={handleDownloadVerifier}
                  className="font-medium text-blue-600 underline hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300"
                >
                  Verifier utility
                </button>
                .
              </p>
            </details>
          </div>
          <p
            className="mt-3 rounded-lg border border-amber-200 bg-amber-50/80 px-3 py-2 text-xs text-amber-800 dark:border-amber-700/50 dark:bg-amber-900/20 dark:text-amber-200"
            title={TOOLTIPS.ambiguity_scope_warning}
            role="note"
          >
            ⓘ {TOOLTIPS.ambiguity_scope_warning}
          </p>
        </div>

        {/* AI Claim */}
        {(record?.claim != null && String(record.claim).trim() !== "") ||
        (record?.policyVersion != null && String(record.policyVersion).trim() !== "") ? (
          <div
            className="rounded-xl border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-600 dark:bg-zinc-700/30"
            aria-label="AI Claim"
          >
            <h2
              className="mb-3 text-lg font-semibold text-zinc-900 dark:text-zinc-50"
              title={TOOLTIPS.ai_claim_heading}
            >
              🧠 AI Claim
            </h2>
            {record?.claim != null && String(record.claim).trim() !== "" && (
              <div className="mb-3">
                <p className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
                  Claim:
                </p>
                {record.claim.length > 80 && !claimExpanded ? (
                  <>
                    <blockquote className="border-l-2 border-zinc-300 pl-3 text-zinc-700 dark:border-zinc-500 dark:text-zinc-300">
                      &ldquo;{record.claim.slice(0, 80).trim()}…&rdquo;
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
            {record?.confidence != null && (
              <div className="mb-3 flex flex-wrap gap-x-2">
                <span className="text-sm font-medium text-zinc-600 dark:text-zinc-400">
                  Confidence:
                </span>
                <span className="text-sm text-zinc-700 dark:text-zinc-300" title={TOOLTIPS.confidence}>
                  {typeof record.confidence === "number"
                    ? record.confidence >= 0 && record.confidence <= 1
                      ? `${Math.round(record.confidence * 100)}%`
                      : String(record.confidence)
                    : String(record.confidence)}
                </span>
              </div>
            )}
            {record?.policyVersion != null && String(record.policyVersion).trim() !== "" && (
              <div className="mb-3 flex flex-wrap gap-x-2">
                <span className="text-sm font-medium text-zinc-600 dark:text-zinc-400">
                  Claim-policy:
                </span>
                <span className="text-sm text-zinc-700 dark:text-zinc-300" title={TOOLTIPS.policy_version}>
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
          </div>
        ) : null}

        {/* Backend verification */}
        <div>
          <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
            Backend verification
          </h2>
          <div className="flex flex-wrap gap-3">
            <span
              className={
                record?.hashMatch === true
                  ? "text-emerald-600 dark:text-emerald-400"
                  : record?.hashMatch === false
                    ? "text-red-600 dark:text-red-400"
                    : "text-zinc-500"
              }
            >
              Hash match:{" "}
              {record?.hashMatch === true ? "✓ yes" : record?.hashMatch === false ? "✗ no" : "—"}
            </span>
            <span
              className={
                record?.signatureValid === "valid"
                  ? "text-emerald-600 dark:text-emerald-400"
                  : record?.signatureValid === "invalid"
                    ? "text-red-600 dark:text-red-400"
                    : "text-zinc-500"
              }
            >
              Signature:{" "}
              {record?.signatureValid === "valid"
                ? "✓ valid"
                : record?.signatureValid === "invalid"
                  ? "✗ invalid"
                  : "n/a"}
            </span>
            {hasPqc ? (
              <span className="text-emerald-600 dark:text-emerald-400" title={TOOLTIPS.pqc_badge}>
                PQC signature: ✓ present
              </span>
            ) : (
              <span className="text-zinc-500">PQC signature: — not included</span>
            )}
            <span
              className={
                hasTsaToken
                  ? "text-emerald-600 dark:text-emerald-400"
                  : "text-zinc-500"
              }
              title={TOOLTIPS.timestamp_trusted}
            >
              Time-stamped: {hasTsaToken ? "✓ trusted" : "—"}
            </span>
          </div>
        </div>

        {/* Response hash */}
        {(record?.responseHash ?? responseData?.responseHash) && (
          <div>
            <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
              Response hash (SHA-256)
            </h2>
            <p className="break-all font-mono text-sm text-zinc-700 dark:text-zinc-300">
              {record?.responseHash ?? responseData?.responseHash}
            </p>
          </div>
        )}

        {/* Signature */}
        {record?.signature && (
          <div>
            <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
              Signature (Base64)
            </h2>
            <p className="break-all font-mono text-sm text-zinc-700 dark:text-zinc-300">
              {truncateMiddle(record.signature)}
            </p>
          </div>
        )}

        {/* TSA token */}
        {record?.tsaToken && (
          <div>
            <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
              TSA token (Base64)
            </h2>
            <p className="break-all font-mono text-sm text-zinc-700 dark:text-zinc-300">
              {truncateMiddle(record.tsaToken)}
            </p>
          </div>
        )}

        {/* PQC */}
        {hasPqc && record?.signaturePqc && (
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

        {/* Verify hash button */}
        <div className="border-t border-zinc-200 pt-4 dark:border-zinc-600">
          <button
            type="button"
            onClick={handleVerifyHash}
            title={TOOLTIPS.verify_hash}
            disabled={
              hashChecking ||
              !(record?.responseHash ?? responseData?.responseHash) ||
              !(record?.response ?? responseData?.response)
            }
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
            <div className="mt-2 space-y-1 text-sm">
              <p className="text-red-600 dark:text-red-400">
                ✗ Hash mismatch — response may have been altered
              </p>
              <p className="text-xs text-zinc-500 dark:text-zinc-400">
                Compare to find the cause: Stored (saved at ask time) vs Backend (recomputed from DB) vs Frontend (recomputed in browser).
              </p>
              <dl className="mt-2 space-y-0.5 font-mono text-xs">
                <div>
                  <dt className="text-zinc-500">Stored:</dt>
                  <dd className="break-all text-zinc-700 dark:text-zinc-300">
                    {record?.responseHash ?? responseData?.responseHash ?? "—"}
                  </dd>
                </div>
                {record?.computedHash != null && (
                  <div>
                    <dt className="text-zinc-500">Backend computed:</dt>
                    <dd className="break-all text-zinc-700 dark:text-zinc-300">{record.computedHash}</dd>
                  </div>
                )}
                {frontendComputedHash != null && (
                  <div>
                    <dt className="text-zinc-500">Frontend computed:</dt>
                    <dd className="break-all text-zinc-700 dark:text-zinc-300">{frontendComputedHash}</dd>
                  </div>
                )}
              </dl>
            </div>
          )}
        </div>

        {/* Evidence Package */}
        <div
          className="min-w-0 rounded-xl border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-600 dark:bg-zinc-700/30"
          aria-label="Evidence Package"
        >
          <h2 className="mb-3 text-lg font-semibold text-zinc-900 dark:text-zinc-50">
            📦 Evidence Package
          </h2>
          <p className="mb-3 text-sm text-zinc-700 dark:text-zinc-300" title={TOOLTIPS.verified_offline}>
            This response can be verified offline.
          </p>
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={onDownloadEvidence}
              disabled={downloading}
              title={TOOLTIPS.download_evidence}
              className="inline-flex items-center gap-1.5 rounded-xl border border-zinc-200 bg-white px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 disabled:opacity-60 dark:border-zinc-600 dark:bg-zinc-800 dark:text-zinc-300 dark:hover:bg-zinc-700"
            >
              {downloading ? "Downloading…" : "⬇ Download evidence"}
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
          {downloadError && (
            <p className="mt-2 text-sm text-red-600 dark:text-red-400">{downloadError}</p>
          )}
        </div>

        {/* Verifier utility */}
        <div
          className="min-w-0 rounded-xl border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-600 dark:bg-zinc-700/30"
          aria-label="Verifier utility"
        >
          <h2 className="mb-3 text-lg font-semibold text-zinc-900 dark:text-zinc-50">
            🔧 Verifier utility
          </h2>
          <p className="mb-3 break-words text-sm text-zinc-700 dark:text-zinc-300">
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
            className="min-w-0 rounded-xl border border-zinc-200 bg-white p-3 dark:border-zinc-600 dark:bg-zinc-800"
            aria-label="How to use"
          >
            <p className="mb-2 text-sm font-medium text-zinc-700 dark:text-zinc-300">
              How to use
            </p>
            <ol className="list-decimal list-inside space-y-1.5 break-words text-sm text-zinc-600 dark:text-zinc-400">
              <li>Download the verifier JAR above.</li>
              <li>
                Run from a terminal:
                <div className="relative mt-1 min-w-0">
                  <pre className="break-all rounded-xl bg-zinc-100 px-2 py-1.5 pr-10 font-mono text-xs whitespace-pre-wrap dark:bg-zinc-700">
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
                    <CopyIcon className="h-3.5 w-3.5" />
                  </button>
                </div>
                <span className="block break-words text-zinc-500 dark:text-zinc-500">
                  (or path to an extracted Evidence Package folder)
                </span>
              </li>
              <li>No network or backend required.</li>
              <li>Exit code 0 = VALID, 1 = INVALID.</li>
            </ol>
          </div>
        </div>

        {responseData && (
          <Link
            href={`/verify?id=${responseData.id}`}
            className="block text-center text-sm text-blue-600 hover:underline dark:text-blue-400"
          >
            Full verification page →
          </Link>
        )}
      </div>

      {/* Preview package modal */}
      {previewOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
          role="dialog"
          aria-modal="true"
          aria-labelledby="preview-modal-title"
        >
          <div className="max-h-[80vh] w-full max-w-md overflow-auto rounded-xl border border-zinc-200 bg-white p-4 shadow-lg dark:border-zinc-600 dark:bg-zinc-800">
            <h3
              id="preview-modal-title"
              className="mb-3 text-lg font-semibold text-zinc-900 dark:text-zinc-50"
            >
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
              className="rounded-xl border border-zinc-200 bg-zinc-100 px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-200 dark:border-zinc-600 dark:bg-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-600"
            >
              Close
            </button>
          </div>
        </div>
      )}
    </section>
  );
}

export default function Home() {
  const [prompt, setPrompt] = useState("");
  const [responseData, setResponseData] = useState<AiAskResponse | null>(null);
  const [verifyRecord, setVerifyRecord] = useState<VerifyRecord | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [downloading, setDownloading] = useState(false);
  const [downloadError, setDownloadError] = useState<string | null>(null);

  // Use relative /api so requests go to same origin (ngrok or VM:3000); Next.js proxies to backend.
  const apiUrl = process.env.NEXT_PUBLIC_API_URL || "";
  const apiBase = typeof window !== "undefined" ? "" : apiUrl;
  const searchParams = useSearchParams();
  const router = useRouter();
  const idParam = searchParams.get("id");

  useEffect(() => {
    void trackEvent("landing_view");
    void trackEvent("demo_view");
  }, []);

  // Persist current response id so Browser Back from use-cases/verify can restore
  const currentId = verifyRecord?.id ?? responseData?.id;
  useEffect(() => {
    if (typeof window === "undefined" || currentId == null) return;
    try {
      sessionStorage.setItem("aletheia_restore_id", String(currentId));
    } catch {
      // ignore
    }
  }, [currentId]);

  // Restore prompt and response when returning (/?id=... or Browser Back with sessionStorage)
  useEffect(() => {
    const idFromUrl = idParam ?? null;
    const idFromStorage =
      typeof window !== "undefined" && !idFromUrl
        ? sessionStorage.getItem("aletheia_restore_id")
        : null;
    const idToRestore = idFromUrl ?? idFromStorage;
    if (!idToRestore) return;
    const numId = Number(idToRestore);
    if (!Number.isInteger(numId) || numId < 1) return;
    let cancelled = false;
    fetch(`${apiBase}/api/ai/verify/${idToRestore}`)
      .then((res) => {
        if (!res.ok) throw new Error("Failed to load");
        return res.json();
      })
      .then((data: VerifyRecord) => {
        if (cancelled) return;
        setVerifyRecord(data);
        setPrompt(data.prompt ?? "");
        setResponseData({
          id: data.id,
          response: data.response,
          responseHash: data.responseHash,
          signature: data.signature,
          tsaToken: data.tsaToken,
          model: data.llmModel,
        });
        if (idFromUrl) router.replace("/", { scroll: false });
        else if (idFromStorage) {
          try {
            sessionStorage.removeItem("aletheia_restore_id");
          } catch {
            // ignore
          }
        }
      })
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [idParam, apiBase, router]);

  async function handleSend() {
    const trimmed = prompt.trim();
    if (!trimmed) return;

    setIsLoading(true);
    setError(null);
    setDownloadError(null);
    setResponseData(null);
    setVerifyRecord(null);

    try {
      const res = await fetch(`${apiBase}/api/ai/ask`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ prompt: trimmed }),
      });

      const contentType = res.headers.get("content-type") ?? "";
      let data: unknown;
      try {
        data = contentType.includes("application/json")
          ? await res.json()
          : { error: await res.text() || `Request failed (${res.status})` };
      } catch {
        data = { error: await res.text().catch(() => `Request failed (${res.status})`) };
      }

      if (!res.ok) {
        let msg =
          typeof data === "object" && data !== null && "message" in data
            ? String((data as { message?: unknown }).message)
            : typeof data === "object" && data !== null && "error" in data
              ? String((data as { error?: unknown }).error)
              : `Request failed (${res.status})`;
        const bodyStr =
          typeof data === "string"
            ? data
            : typeof data === "object" && data !== null && "error" in data
              ? String((data as { error?: unknown }).error)
              : "";
        if (
          res.status === 501 ||
          bodyStr.includes("Unsupported method") ||
          bodyStr.includes("501")
        ) {
          msg =
            "Server does not support POST. Run the app with npm run dev (Next.js) and start the backend — do not use a static file server.";
        }
        setError(msg || `Request failed (${res.status})`);
        return;
      }

      const askData = data as AiAskResponse;
      setResponseData(askData);

      // Fetch full verification data for Trust Panel (same as verify page)
      try {
        const verifyRes = await fetch(`${apiBase}/api/ai/verify/${askData.id}`);
        if (verifyRes.ok) {
          const verifyData = (await verifyRes.json()) as VerifyRecord;
          setVerifyRecord(verifyData);
        }
      } catch {
        // Trust Panel will show response hash from ask if verify fetch fails
        setVerifyRecord({
          id: askData.id,
          prompt: "",
          response: askData.response,
          responseHash: askData.responseHash,
          signature: askData.signature ?? null,
          tsaToken: askData.tsaToken ?? null,
          llmModel: askData.model,
          createdAt: "",
        });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Network error");
    } finally {
      setIsLoading(false);
    }
  }

  const signed = !!(responseData?.signature?.trim());
  const timestamped = !!(responseData?.tsaToken?.trim());
  const verifiable = signed && timestamped;

  async function handleDownloadEvidence() {
    if (!responseData) return;
    void trackEvent("download_evidence_click", { responseId: responseData.id });
    setDownloading(true);
    setDownloadError(null);
    try {
      const res = await fetch(`${apiBase}/api/ai/evidence/${responseData.id}`);
      if (!res.ok) {
        const text = await res.text();
        const msg =
          res.status === 404
            ? "Response not found"
            : res.status === 503
              ? "Signing key not configured"
              : text || `Download failed (${res.status})`;
        setDownloadError(msg);
        return;
      }
      const blob = await res.blob();
      const disposition = res.headers.get("Content-Disposition");
      let filename = `aletheia-evidence-${responseData.id}.aep`;
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
      setDownloadError(err instanceof Error ? err.message : "Download failed");
    } finally {
      setDownloading(false);
    }
  }

  return (
    <div className="min-h-screen overflow-x-hidden bg-zinc-50 p-4 dark:bg-zinc-900 md:p-6">
      <div className="mx-auto w-full max-w-6xl min-w-0 space-y-6">
        {/* Top dashboard: logo + slogan; Explore use cases button top right */}
        <section
          className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800"
          aria-label="Product"
        >
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div className="flex min-w-0 flex-wrap items-center gap-4">
              <div className="flex shrink-0 rounded-xl bg-white p-1 dark:bg-zinc-800">
                <Image
                  src="/logo.png"
                  alt="Aletheia AI"
                  width={80}
                  height={80}
                  className="rounded-xl dark:bg-zinc-800"
                  unoptimized
                />
              </div>
              <div className="min-w-0">
                <h1 className="text-xl font-bold text-zinc-900 dark:text-zinc-50">
                  Aletheia AI
                </h1>
                <p className="mt-1 text-2xl font-bold text-zinc-900 dark:text-zinc-50 md:text-3xl">
                  AI said it. Prove it.
                </p>
                <p className="mt-1 text-sm text-zinc-600 dark:text-zinc-400">
                  Verifiable AI responses with Quantum-resistant signing and timestamps
                </p>
                <div className="mt-2 flex flex-wrap gap-2">
                  <a
                    href="https://csrc.nist.gov/pubs/fips/204/final"
                    target="_blank"
                    rel="noopener noreferrer"
                    title="Secured by ML-DSA algorithm. Your evidence will remain valid even in the era of quantum computing."
                    className="inline-flex items-center gap-1.5 rounded-xl border border-indigo-400/60 bg-indigo-50 px-2.5 py-1 text-sm font-medium text-indigo-800 hover:bg-indigo-100 dark:border-indigo-600 dark:bg-indigo-950/50 dark:text-indigo-200 dark:hover:bg-indigo-900/50"
                  >
                    <PqcIcon className="h-4 w-4 shrink-0 text-indigo-600 dark:text-indigo-400" />
                    Quantum-Proof Trust Anchor
                  </a>
                  <a
                    href="https://www.rfc-editor.org/rfc/rfc3161"
                    target="_blank"
                    rel="noopener noreferrer"
                    title="RFC 3161 Trusted Timestamp. External attestation that this response existed at this exact point in time."
                    className="inline-flex items-center gap-1.5 rounded-xl border border-amber-400/60 bg-amber-50 px-2.5 py-1 text-sm font-medium text-amber-800 hover:bg-amber-100 dark:border-amber-600 dark:bg-amber-950/50 dark:text-amber-200 dark:hover:bg-amber-900/50"
                  >
                    <span aria-hidden>⏳</span>
                    Non-Repudiable Time-Proof
                  </a>
                </div>
              </div>
            </div>
            <Link
              href={currentId != null ? `/use-cases?fromId=${currentId}` : "/use-cases"}
              onClick={() => {
                void trackEvent("cta_click");
              }}
              className="shrink-0 rounded-xl bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-500"
            >
              Explore use cases
            </Link>
          </div>
        </section>

        {/* Two columns — same total width as sections above (1fr + fixed right column) */}
        <div className="grid min-w-0 grid-cols-1 items-start gap-6 lg:grid-cols-[1fr_minmax(320px,380px)]">
          <main
            id="demo"
            className="min-w-0 space-y-6 rounded-xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800"
          >
          <div>
            <label
              htmlFor="prompt"
              className="mb-2 block text-sm font-medium text-zinc-700 dark:text-zinc-300"
            >
              Prompt
            </label>
            <textarea
              id="prompt"
              rows={4}
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter" && !e.shiftKey) {
                  e.preventDefault();
                  if (prompt.trim() && !isLoading) handleSend();
                }
              }}
              placeholder="Ask anything"
              disabled={isLoading}
              className="w-full rounded-xl border border-zinc-300 px-3 py-2 text-zinc-900 placeholder-zinc-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-60 dark:border-zinc-600 dark:bg-zinc-700 dark:text-zinc-100 dark:placeholder-zinc-500"
            />
          </div>

          <button
            type="button"
            onClick={handleSend}
            disabled={isLoading || !prompt.trim()}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:bg-zinc-400 disabled:cursor-not-allowed dark:disabled:bg-zinc-600"
            aria-label="Send prompt and verify"
          >
            {isLoading ? (
              <>
                <span
                  className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"
                  aria-hidden
                />
                Attesting…
              </>
            ) : (
              <>Send & Verify</>
            )}
          </button>

          <div>
            <label className="mb-2 block text-sm font-medium text-zinc-700 dark:text-zinc-300">
              Response
            </label>
            <div
              className="relative min-h-[120px] rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-4 dark:border-zinc-600 dark:bg-zinc-700/50"
              aria-live="polite"
            >
              {responseData && !error && !isLoading && (
                <button
                  type="button"
                  onClick={() => {
                    if (responseData.response) void navigator.clipboard.writeText(responseData.response);
                  }}
                  title={TOOLTIPS.copy_response}
                  aria-label="Copy response"
                  className="absolute right-2 top-2 rounded-xl p-1.5 text-zinc-500 hover:bg-zinc-200/80 hover:text-zinc-700 dark:text-zinc-400 dark:hover:bg-zinc-600 dark:hover:text-zinc-200"
                >
                  <CopyIcon className="h-4 w-4" />
                </button>
              )}
              {isLoading && (
                <p className="flex items-center gap-2 text-zinc-600 dark:text-zinc-400">
                  <span
                    className="inline-block h-4 w-4 animate-spin rounded-full border-2 border-zinc-400 border-t-transparent"
                    aria-hidden
                  />
                  Working on it…
                </p>
              )}
              {error && !isLoading && (
                <p className="text-red-600 dark:text-red-400">{error}</p>
              )}
              {responseData && !error && !isLoading && (
                <div className="min-w-0 space-y-3 pr-8">
                  <div className="border-b border-zinc-200 pb-3 dark:border-zinc-600">
                    <div className="flex flex-wrap items-center gap-2">
                      <StatusItem label="Signed" ok={signed} />
                      <StatusItem label="Timestamped" ok={timestamped} />
                      <StatusItem label="Verifiable" ok={verifiable} />
                    </div>
                    <p className="mt-1.5 text-xs text-zinc-500 dark:text-zinc-400">
                      Model: {responseData.model} · ID: {responseData.id}
                    </p>
                  </div>
                  <p className="break-words whitespace-pre-wrap text-zinc-700 dark:text-zinc-300">
                    {responseData.response}
                  </p>
                </div>
              )}
              {!responseData && !error && !isLoading && (
                <span className="text-zinc-500 dark:text-zinc-400">
                  Response will appear here after you send a prompt.
                </span>
              )}
            </div>
          </div>
        </main>

          {/* Right: Trust Panel */}
          <aside className="min-w-0">
            <TrustPanel
              verifyRecord={verifyRecord}
              responseData={responseData}
              apiBase={apiBase}
              onDownloadEvidence={handleDownloadEvidence}
              downloading={downloading}
              downloadError={downloadError}
            />
          </aside>
        </div>
      </div>

      <footer className="mx-auto mt-8 max-w-6xl text-center text-xs text-zinc-500 dark:text-zinc-400">
        <p>© 2026 Anton Sokolov &amp; Team 3</p>
        <p className="mt-1">
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
      </footer>
    </div>
  );
}
