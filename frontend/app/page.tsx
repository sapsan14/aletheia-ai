/**
 * Task 1.3 + 6.1 + 6.2 — Frontend (Next.js)
 *
 * Main page: prompt input, Send button, loading indicator,
 * response display with status (signed, timestamped, verifiable),
 * link to verify page.
 */

"use client";

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

  const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

  async function handleSend() {
    const trimmed = prompt.trim();
    if (!trimmed) return;

    setIsLoading(true);
    setError(null);
    setResponseData(null);

    try {
      const res = await fetch(`${apiUrl}/api/ai/ask`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ prompt: trimmed }),
      });

      const data = await res.json();

      if (!res.ok) {
        setError(data?.message || data?.error || `Request failed (${res.status})`);
        return;
      }

      setResponseData(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Network error");
    } finally {
      setIsLoading(false);
    }
  }

  const signed = !!(responseData?.signature?.trim());
  const timestamped = !!(responseData?.tsaToken?.trim());
  const verifiable = signed && timestamped;

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
                <Link
                  href={`/verify?id=${responseData.id}`}
                  className="inline-flex items-center text-sm font-medium text-blue-600 hover:underline dark:text-blue-400"
                >
                  Verify this response →
                </Link>
              </div>
            )}
            {!responseData && !error && !isLoading && (
              <span className="italic text-zinc-500 dark:text-zinc-400">
                Response will appear here after you send a prompt.
              </span>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
