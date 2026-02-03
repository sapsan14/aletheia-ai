/**
 * Task 1.3 + 6.1 + 6.2 — Frontend (Next.js)
 *
 * Main page: prompt input, Send button, loading indicator,
 * response display with status (signed, timestamped, verifiable),
 * link to verify page.
 */

"use client";

import { PqcBadge } from "@/app/components/PqcBadge";
import Image from "next/image";
import Link from "next/link";
import { useState } from "react";

/** Response from POST /api/ai/ask */
interface AiAskResponse {
  response: string;
  responseHash: string;
  signature: string | null;
  tsaToken: string | null;
  id: number;
  model: string;
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
      className={`inline-flex items-center gap-1.5 rounded px-2 py-0.5 text-sm ${
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

export default function Home() {
  const [prompt, setPrompt] = useState("");
  const [responseData, setResponseData] = useState<AiAskResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [downloading, setDownloading] = useState(false);
  const [downloadError, setDownloadError] = useState<string | null>(null);

  const apiUrl = process.env.NEXT_PUBLIC_API_URL || "";

  async function handleSend() {
    const trimmed = prompt.trim();
    if (!trimmed) return;

    setIsLoading(true);
    setError(null);
    setDownloadError(null);
    setResponseData(null);

    try {
      const res = await fetch(`${apiUrl}/api/ai/ask`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ prompt: trimmed }),
      });

      // Backend may return 500/502/503 with plain text or HTML; avoid res.json() throwing.
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

      setResponseData(data as AiAskResponse);
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
    setDownloading(true);
    setDownloadError(null);
    try {
      const res = await fetch(
        `${apiUrl}/api/ai/evidence/${responseData.id}`
      );
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
        const match = /filename[*]?=(?:UTF-8'')?"?([^";\n]+)"?/i.exec(
          disposition
        );
        if (match) filename = match[1].trim();
      }
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      a.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      setDownloadError(
        err instanceof Error ? err.message : "Download failed"
      );
    } finally {
      setDownloading(false);
    }
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 p-8 dark:bg-zinc-900">
      <main className="w-full max-w-2xl space-y-6 rounded-lg border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
        <div className="flex items-center gap-4">
          <div className="flex shrink-0 rounded-xl bg-white dark:bg-zinc-800 p-1">
            <Image
              src="/logo.png"
              alt="Aletheia AI"
              width={144}
              height={144}
              className="rounded-lg bg-white dark:bg-zinc-800"
              unoptimized
            />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-zinc-900 dark:text-zinc-50">
              Aletheia AI
            </h1>
            <p className="text-sm text-zinc-600 dark:text-zinc-400">
              Verifiable AI responses with signing and timestamps
            </p>
            <p className="mt-1.5 flex flex-wrap items-center gap-2 text-xs text-zinc-500 dark:text-zinc-400">
              <span>Hybrid attestation: classical + post-quantum signatures for long-term evidence.</span>
              <PqcBadge variant="compact" />
            </p>
          </div>
        </div>

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
            placeholder="Enter your question here..."
            disabled={isLoading}
            className="w-full rounded-md border border-zinc-300 px-3 py-2 text-zinc-900 placeholder-zinc-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-60 dark:border-zinc-600 dark:bg-zinc-700 dark:text-zinc-100 dark:placeholder-zinc-500"
          />
        </div>

        <button
          type="button"
          onClick={handleSend}
          disabled={isLoading || !prompt.trim()}
          className="w-full rounded-md bg-blue-600 px-4 py-2 font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:bg-zinc-400 disabled:cursor-not-allowed dark:disabled:bg-zinc-600"
          aria-label="Send prompt"
        >
          {isLoading ? "Sending…" : "Send"}
        </button>

        <div>
          <label className="mb-2 block text-sm font-medium text-zinc-700 dark:text-zinc-300">
            Response
          </label>
          <div
            className="min-h-[120px] rounded-md border border-zinc-200 bg-zinc-50 px-3 py-4 dark:border-zinc-600 dark:bg-zinc-700/50"
            aria-live="polite"
          >
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
              <div className="space-y-3">
                <p className="whitespace-pre-wrap text-zinc-700 dark:text-zinc-300">
                  {responseData.response}
                </p>
                <div className="flex flex-wrap gap-2">
                  <StatusItem label="Signed" ok={signed} />
                  <StatusItem label="Timestamped" ok={timestamped} />
                  <StatusItem label="Verifiable" ok={verifiable} />
                </div>
                <p className="text-xs text-zinc-500 dark:text-zinc-400">
                  Model: {responseData.model} · ID: {responseData.id}
                </p>
                <div className="flex flex-wrap items-center gap-3">
                  <Link
                    href={`/verify?id=${responseData.id}`}
                    className="inline-flex items-center text-sm font-medium text-blue-600 hover:underline dark:text-blue-400"
                  >
                    Verify this response →
                  </Link>
                  <button
                    type="button"
                    onClick={handleDownloadEvidence}
                    disabled={downloading}
                    className="inline-flex items-center text-sm font-medium text-blue-600 hover:underline disabled:opacity-60 dark:text-blue-400"
                    aria-label="Download Evidence Package (.aep)"
                  >
                    {downloading ? "Downloading…" : "Download evidence"}
                  </button>
                </div>
                {downloadError && (
                  <p className="text-xs text-red-600 dark:text-red-400">
                    {downloadError}
                  </p>
                )}
              </div>
            )}
            {!responseData && !error && !isLoading && (
              <span className="italic text-zinc-500 dark:text-zinc-400">
                Response will appear here after you send a prompt.
              </span>
            )}
          </div>
        </div>

        <footer className="mt-8 text-center text-xs text-zinc-500 dark:text-zinc-400">
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
