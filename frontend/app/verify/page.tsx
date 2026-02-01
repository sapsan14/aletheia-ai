/**
 * Task 6.3 — Verify page
 *
 * Fetches GET /api/ai/verify/:id, displays prompt, response, hash, signature,
 * tsaToken, model, date. Optional "Verify hash" button for client-side check.
 */

"use client";

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

function truncateMiddle(str: string, head = 20, tail = 20): string {
  if (!str || str.length <= head + tail) return str;
  return `${str.slice(0, head)}...${str.slice(-tail)}`;
}

function VerifyContent() {
  const searchParams = useSearchParams();
  const id = searchParams.get("id");
  const [record, setRecord] = useState<VerifyRecord | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [hashMatch, setHashMatch] = useState<boolean | null>(null);
  const [hashChecking, setHashChecking] = useState(false);

  const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

  useEffect(() => {
    if (!id) {
      setError("Missing id in URL (e.g. /verify?id=1)");
      setIsLoading(false);
      return;
    }
    fetch(`${apiUrl}/api/ai/verify/${id}`)
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
  }, [id, apiUrl]);

  async function handleVerifyHash() {
    if (!record) return;
    setHashChecking(true);
    setHashMatch(null);
    try {
      const canonical = canonicalize(record.response);
      const computed = await sha256Hex(canonical);
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

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap gap-2 text-sm">
        <span className="text-zinc-500 dark:text-zinc-400">Model:</span>
        <span className="text-zinc-700 dark:text-zinc-300">{record.llmModel}</span>
        <span className="text-zinc-400">·</span>
        <span className="text-zinc-500 dark:text-zinc-400">Created:</span>
        <span className="text-zinc-700 dark:text-zinc-300">
          {new Date(record.createdAt).toLocaleString()}
        </span>
      </div>

      <div>
        <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
          Prompt
        </h2>
        <p className="whitespace-pre-wrap rounded bg-zinc-100 px-3 py-2 text-zinc-800 dark:bg-zinc-700 dark:text-zinc-200">
          {record.prompt}
        </p>
      </div>

      <div>
        <h2 className="mb-1 text-sm font-medium text-zinc-600 dark:text-zinc-400">
          Response
        </h2>
        <p className="whitespace-pre-wrap rounded bg-zinc-100 px-3 py-2 text-zinc-800 dark:bg-zinc-700 dark:text-zinc-200">
          {record.response}
        </p>
      </div>

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

      <div className="border-t border-zinc-200 pt-4 dark:border-zinc-600">
        <button
          type="button"
          onClick={handleVerifyHash}
          disabled={hashChecking}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60 dark:bg-blue-500 dark:hover:bg-blue-600"
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
      <main className="w-full max-w-2xl space-y-6 rounded-lg border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
        <div className="flex items-center gap-4">
          <Link href="/" className="flex shrink-0">
            <Image
              src="/logo.png"
              alt="Aletheia AI"
              width={48}
              height={48}
              className="rounded-lg bg-white dark:bg-zinc-800"
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
        </footer>
      </main>
    </div>
  );
}
