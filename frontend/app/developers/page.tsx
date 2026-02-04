"use client";

import Link from "next/link";
import Image from "next/image";

const baseUrl = process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") || "http://localhost:8080";
const SWAGGER_HREF = `${baseUrl}/swagger-ui.html`;
const PLAN_PHASE5_HREF =
  process.env.NEXT_PUBLIC_DOCS_PLAN_PHASE5_URL ||
  "https://github.com/sapsan14/aletheia-ai/blob/main/docs/en/PLAN_PHASE5.md";

export default function DevelopersPage() {
  return (
    <div className="min-h-screen bg-zinc-50 p-6 text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100">
      <main className="mx-auto max-w-2xl">
        <div className="mb-8 flex items-center gap-4">
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
            <h1 className="text-2xl font-bold">For Developers</h1>
            <p className="text-sm text-zinc-600 dark:text-zinc-400">
              API spec and integration guide
            </p>
          </div>
        </div>

        <p className="mb-6 text-sm text-zinc-700 dark:text-zinc-300">
          Integrate Aletheia into your pipeline: sign and verify AI outputs via the API, or use the
          offline verifier and Evidence Package for audit.
        </p>

        <div className="space-y-4">
          <section className="rounded-xl border border-zinc-200 bg-white p-4 dark:border-zinc-700 dark:bg-zinc-800">
            <h2 className="mb-2 text-lg font-semibold">View API spec</h2>
            <p className="mb-3 text-sm text-zinc-600 dark:text-zinc-400">
              OpenAPI (Swagger) documentation for the Aletheia backend. Endpoints: ask, verify,
              evidence, verifier.
            </p>
            <a
              href={SWAGGER_HREF}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-100 dark:border-zinc-600 dark:bg-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-600"
            >
              Open Swagger UI →
            </a>
          </section>

          <section className="rounded-xl border border-zinc-200 bg-white p-4 dark:border-zinc-700 dark:bg-zinc-800">
            <h2 className="mb-2 text-lg font-semibold">Integrate with your stack</h2>
            <p className="mb-3 text-sm text-zinc-600 dark:text-zinc-400">
              Phase 5 plan: API platform, sign-only API, SDKs, and integrations (SIEM, MCP,
              partners).
            </p>
            <a
              href={PLAN_PHASE5_HREF}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 rounded-xl border border-zinc-200 bg-zinc-50 px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-100 dark:border-zinc-600 dark:bg-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-600"
            >
              Phase 5 plan &amp; integrations →
            </a>
          </section>
        </div>

        <p className="mt-8">
          <Link
            href="/"
            className="text-sm text-blue-600 hover:underline dark:text-blue-400"
          >
            ← Back to home
          </Link>
        </p>

        <footer className="mt-10 text-center text-xs text-zinc-500 dark:text-zinc-400">
          <p>© 2026 Anton Sokolov &amp; Team 3</p>
        </footer>
      </main>
    </div>
  );
}
