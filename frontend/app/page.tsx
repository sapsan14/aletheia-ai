/**
 * Task 1.3 — Frontend skeleton (Next.js)
 *
 * This is the main page of the Aletheia AI frontend.
 * It shows a placeholder for the prompt input and response area.
 *
 * For beginners:
 * - This file defines what users see when they visit http://localhost:3000
 * - "use client" means this component can use React hooks (useState, etc.)
 *   and handle user interactions (clicks, typing)
 * - We'll connect this to the backend API in a later task (Step 6)
 */

"use client";

import { useState } from "react";

export default function Home() {
  // State: we store what the user types in the prompt field
  // useState("") = start with empty string; setPrompt updates it when user types
  const [prompt, setPrompt] = useState("");
  // Placeholder for future: we'll show the AI response here
  const [response, setResponse] = useState("");

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 p-8 dark:bg-zinc-900">
      <main className="w-full max-w-2xl space-y-6 rounded-lg border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
        {/* Page title */}
        <h1 className="text-2xl font-bold text-zinc-900 dark:text-zinc-50">
          Aletheia AI
        </h1>
        <p className="text-sm text-zinc-600 dark:text-zinc-400">
          Verifiable AI responses with signing and timestamps
        </p>

        {/* Task 1.3: Prompt input — where users type their question for the AI */}
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
            className="w-full rounded-md border border-zinc-300 px-3 py-2 text-zinc-900 placeholder-zinc-400 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-600 dark:bg-zinc-700 dark:text-zinc-100 dark:placeholder-zinc-500"
          />
        </div>

        {/* Task 1.3: Send button — disabled for now; we'll connect to API in Step 6 */}
        <button
          type="button"
          disabled
          className="w-full rounded-md bg-zinc-300 px-4 py-2 font-medium text-zinc-500 cursor-not-allowed dark:bg-zinc-600 dark:text-zinc-400"
          aria-label="Send prompt (coming soon)"
        >
          Send — Coming soon
        </button>

        {/* Task 1.3: Response area — where we'll display the AI answer and verification status */}
        <div>
          <label className="mb-2 block text-sm font-medium text-zinc-700 dark:text-zinc-300">
            Response
          </label>
          <div
            className="min-h-[120px] rounded-md border border-zinc-200 bg-zinc-50 px-3 py-4 text-zinc-500 dark:border-zinc-600 dark:bg-zinc-700/50 dark:text-zinc-400"
            aria-live="polite"
          >
            {response || (
              <span className="italic">
                Response will appear here after you connect to the backend
                (Step 6).
              </span>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
