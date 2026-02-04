"use client";

import { trackEvent } from "@/lib/analytics";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense, useEffect } from "react";

const USE_CASES = [
  {
    title: "HR",
    pain:
      "Candidates dispute AI-driven decisions; you need to show which criteria and policy applied.",
    solution:
      "Aletheia fixes the exact wording, Claim-policy, and confidence so you can prove due diligence.",
  },
  {
    title: "Legal / compliance",
    pain:
      "Contracts or clauses are checked by AI; auditors need proof of what was stated and when.",
    solution:
      "Signed response plus an Evidence Package that can be verified offline.",
  },
  {
    title: "Customer support",
    pain:
      "AI answers in official channels; disputes require proof of what was said.",
    solution:
      "We fix the message, policy, and time so you can show what was communicated.",
  },
  {
    title: "Education",
    pain:
      "Students use AI; institutions need to document how AI was used and what was guaranteed.",
    solution:
      "Policy and confidence show what was checked (and what was not).",
  },
  {
    title: "Corporate governance",
    pain:
      "Decisions are supported by AI; internal audit needs traceability.",
    solution:
      "Evidence Package and policy coverage provide an audit trail.",
  },
];

function UseCasesContent() {
  const searchParams = useSearchParams();
  const fromId = searchParams.get("fromId");
  const homeHref = fromId ? `/?id=${fromId}` : "/";
  const demoHref = fromId ? `/?id=${fromId}#demo` : "/#demo";

  useEffect(() => {
    trackEvent("view_use_cases");
  }, []);

  return (
    <div className="min-h-screen bg-zinc-50 p-6 text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100">
      <main className="mx-auto w-full max-w-4xl">
        <div className="mb-8 space-y-3">
          <p className="text-xs font-semibold uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
            Aletheia AI
          </p>
          <h1 className="text-3xl font-bold">Use cases</h1>
          <p className="text-sm text-zinc-600 dark:text-zinc-400">
            Who is this for? Five common scenarios where verifiable AI output
            helps teams answer audits, disputes, and compliance questions.
          </p>
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          {USE_CASES.map((item) => (
            <section
              key={item.title}
              className="rounded-xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-700 dark:bg-zinc-800"
            >
              <h2 className="mb-2 text-lg font-semibold">{item.title}</h2>
              <p className="text-sm text-zinc-600 dark:text-zinc-400">
                <span className="font-medium text-zinc-700 dark:text-zinc-300">
                  Pain:{" "}
                </span>
                {item.pain}
              </p>
              <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
                <span className="font-medium text-zinc-700 dark:text-zinc-300">
                  Solution:{" "}
                </span>
                {item.solution}
              </p>
            </section>
          ))}
        </div>

        <div className="mt-8 flex flex-wrap items-center gap-3">
          <Link
            href={demoHref}
            className="inline-flex items-center justify-center rounded-xl bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-500"
          >
            Try demo
          </Link>
          <Link
            href={homeHref}
            className="text-sm text-blue-600 hover:underline dark:text-blue-400"
          >
            ← Back to home
          </Link>
        </div>
      </main>

      <footer className="mt-10 text-center text-xs text-zinc-500 dark:text-zinc-400">
        <p>© 2026 Anton Sokolov &amp; Team 3</p>
      </footer>
    </div>
  );
}

export default function UseCasesPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-zinc-50 p-6 flex items-center justify-center dark:bg-zinc-900">
        <p className="text-zinc-500 dark:text-zinc-400">Loading…</p>
      </div>
    }>
      <UseCasesContent />
    </Suspense>
  );
}
