/**
 * Task 1.3 + 6.1 — Frontend (Next.js)
 *
 * Main page: prompt input, Send button (connected to POST /api/ai/ask),
 * loading state, error handling. Response stored in state for Task 6.2.
 */

"use client";

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

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 p-8 dark:bg-zinc-900">
      <main className="w-full max-w-2xl space-y-6 rounded-lg border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
        <h1 className="text-2xl font-bold text-zinc-900 dark:text-zinc-50">
          Aletheia AI
        </h1>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Verifiable AI responses with signing and timestamps
        </p>

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
            {error && (
              <p className="text-red-600 dark:text-red-400">{error}</p>
            )}
            {responseData && !error && (
              <p className="text-zinc-700 dark:text-zinc-300">
                Response received (id: {responseData.id})
              </p>
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
