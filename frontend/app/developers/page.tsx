import Link from "next/link";

const OPENAPI_REPO_URL =
  "https://github.com/sapsan14/aletheia-ai/blob/main/docs/api/openapi.yaml";

export default function DevelopersPage() {
  return (
    <div className="min-h-screen bg-zinc-50 px-4 py-10 text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100">
      <div className="mx-auto flex w-full max-w-4xl flex-col gap-8">
        <header className="space-y-2">
          <p className="text-sm font-semibold uppercase tracking-wide text-blue-600 dark:text-blue-400">
            Aletheia AI API
          </p>
          <h1 className="text-3xl font-bold">For Developers</h1>
          <p className="text-base text-zinc-600 dark:text-zinc-300">
            Integrate Aletheia into your pipeline to generate and attest AI responses,
            or to sign responses from your own LLM.
          </p>
        </header>

        <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
          <h2 className="text-xl font-semibold">OpenAPI spec</h2>
          <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-300">
            Use the OpenAPI 3.0 spec as the source of truth for request/response
            schemas and examples.
          </p>
          <div className="mt-4 flex flex-wrap gap-3 text-sm">
            <a
              href={OPENAPI_REPO_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="rounded-lg border border-blue-200 bg-blue-50 px-3 py-2 font-medium text-blue-700 hover:bg-blue-100 dark:border-blue-500/40 dark:bg-blue-500/10 dark:text-blue-200"
            >
              OpenAPI YAML (repo)
            </a>
            <a
              href="/v3/api-docs"
              className="rounded-lg border border-zinc-200 bg-zinc-50 px-3 py-2 font-medium text-zinc-700 hover:bg-zinc-100 dark:border-zinc-600 dark:bg-zinc-700/40 dark:text-zinc-200"
            >
              Live JSON (when backend running)
            </a>
          </div>
        </section>

        <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
          <h2 className="text-xl font-semibold">Quickstart</h2>
          <ol className="mt-3 list-decimal space-y-2 pl-5 text-sm text-zinc-700 dark:text-zinc-300">
            <li>POST <code className="font-mono">/api/ai/ask</code> with your prompt.</li>
            <li>GET <code className="font-mono">/api/ai/verify/:id</code> to fetch verification data.</li>
            <li>GET <code className="font-mono">/api/ai/evidence/:id</code> to download the Evidence Package.</li>
            <li>Use the offline verifier to validate the .aep file.</li>
          </ol>
        </section>

        <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
          <h2 className="text-xl font-semibold">Sign-only API</h2>
          <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-300">
            If you already have an LLM response, send it to Aletheia for signing only.
            You will receive the same verification data (id, signature, TSA token).
          </p>
          <div className="mt-4 text-sm">
            <pre className="overflow-x-auto rounded-xl bg-zinc-900 p-4 text-zinc-100">
{`curl -X POST http://localhost:8080/api/sign \\
  -H "Content-Type: application/json" \\
  -d '{
    "response": "Here is your LLM output.",
    "modelId": "external-llm",
    "policyId": "compliance-2024"
  }'`}
            </pre>
          </div>
        </section>

        <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
          <h2 className="text-xl font-semibold">cURL examples</h2>
          <div className="mt-4 space-y-4 text-sm">
            <pre className="overflow-x-auto rounded-xl bg-zinc-900 p-4 text-zinc-100">
{`curl -X POST http://localhost:8080/api/ai/ask \\
  -H "Content-Type: application/json" \\
  -d '{"prompt":"What is 2+2?"}'`}
            </pre>
            <pre className="overflow-x-auto rounded-xl bg-zinc-900 p-4 text-zinc-100">
{`curl http://localhost:8080/api/ai/verify/123`}
            </pre>
            <pre className="overflow-x-auto rounded-xl bg-zinc-900 p-4 text-zinc-100">
{`curl -O http://localhost:8080/api/ai/evidence/123`}
            </pre>
          </div>
        </section>

        <section className="flex flex-wrap items-center gap-2 text-sm text-zinc-600 dark:text-zinc-300">
          <span>Back to</span>
          <Link href="/" className="text-blue-600 hover:underline dark:text-blue-400">
            Home
          </Link>
        </section>
      </div>
    </div>
  );
}
