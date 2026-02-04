import Link from "next/link";

const PIPELINE_STEPS = [
  {
    id: "QSB-1",
    title: "Capture prompt and response",
    summary:
      "Record the user prompt and the raw LLM response. Keep raw text off-chain; store hashes only.",
    outputs: ["response.txt", "metadata.json"],
  },
  {
    id: "QSB-2",
    title: "Canonicalize and hash",
    summary:
      "Normalize text, compute SHA-256 response hash. This is the root for both signatures.",
    outputs: ["canonical.bin", "hash.sha256"],
  },
  {
    id: "QSB-3",
    title: "Classical + PQC signing",
    summary:
      "Sign the same hash with classical key and ML-DSA (Dilithium). Attach TSA token.",
    outputs: ["signature.sig", "signature_pqc.sig", "timestamp.tsr"],
  },
  {
    id: "QSB-4",
    title: "Evidence Package",
    summary:
      "Bundle artifacts into a .aep file. Compute evidence_hash for the ledger.",
    outputs: [".aep", "evidence_hash"],
  },
  {
    id: "QSB-5",
    title: "Ledger block",
    summary:
      "Create a tamper-evident block with previous_hash, merkle_root, and block signatures.",
    outputs: ["block_hash", "block_signature_pqc"],
  },
  {
    id: "QSB-6",
    title: "External anchor (optional)",
    summary:
      "Anchor block_hash to a public chain or anchor service and store tx metadata.",
    outputs: ["anchor_tx_id (optional)"],
  },
  {
    id: "QSB-7",
    title: "Final step: use existing verification",
    summary:
      "Use the current Send and Verify flow to download and validate the Evidence Package.",
    outputs: ["verified response", "offline verifier report"],
  },
];

const ASTROS = [
  {
    id: "A",
    label: "Acceptance",
    tests: [
      "User creates a prompt and gets a response id",
      "Ledger entry is created with response_hash and evidence_hash",
    ],
    expected: "End-to-end flow succeeds without manual steps.",
  },
  {
    id: "S",
    label: "Security",
    tests: [
      "Modify block data and re-verify chain",
      "Attempt to replace evidence_hash",
    ],
    expected: "Chain verification fails on tampering.",
  },
  {
    id: "T",
    label: "Traceability",
    tests: [
      "Resolve tx_id to response id",
      "Evidence hash matches .aep file",
    ],
    expected: "All hashes match across ledger and evidence.",
  },
  {
    id: "R",
    label: "Reliability",
    tests: [
      "Anchor service down during block creation",
      "Retry without losing block",
    ],
    expected: "Block stored even if anchor fails.",
  },
  {
    id: "O",
    label: "Observability",
    tests: ["Log block_id, block_hash, tx_id", "Emit metrics for block creation"],
    expected: "Auditors can trace each ledger event.",
  },
  {
    id: "S2",
    label: "Scalability",
    tests: ["10k tx batch insert", "Paginated block list query"],
    expected: "No timeouts; predictable pagination.",
  },
];

const RUNBOOK = `GOAL:
Build a quantum-safe ledger for LLM responses without replacing the existing trust chain.

INPUTS:
- user prompt
- LLM response
- canonicalization rules (current backend)

OUTPUTS:
- response_hash (SHA-256)
- evidence_hash (SHA-256 of .aep)
- ledger block with PQC signature
- link to existing verification flow

STEPS:
1) Canonicalize response text and compute response_hash.
2) Sign response_hash with classical key and ML-DSA key.
3) Timestamp the classical signature (RFC 3161 TSA).
4) Build Evidence Package (.aep) and compute evidence_hash.
5) Add a ledger transaction with response_hash and evidence_hash.
6) Create a block (previous_hash, merkle_root, block_hash).
7) Sign block_hash with PQC key and store block signatures.
8) Optional: anchor block_hash to external chain.
9) Final: use Send and Verify to validate the Evidence Package.

TESTS:
- Hash determinism: same response -> same response_hash.
- PQC signature verify: block_hash validates with pqc_public_key.
- Evidence integrity: evidence_hash matches .aep bytes.
- Anchor failure does not prevent block creation.`;

const BLOCK_SCHEMA = `BlockHeader {
  block_id: int
  previous_hash: hex
  merkle_root: hex
  created_at: ISO-8601
  block_hash: hex
  signatures: {
    classical: base64
    pqc: base64
    tsa: base64
  }
  anchor: { provider, tx_id, block_height } (optional)
}

Tx {
  tx_id: string
  response_hash: hex
  evidence_hash: hex
  llm_model: string
  policy_version: string
  created_at: ISO-8601
}`;

export default function QuantumPlanPage() {
  return (
    <div className="min-h-screen bg-zinc-50 px-4 py-10 text-zinc-900 dark:bg-zinc-900 dark:text-zinc-100">
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-8">
        <header className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-zinc-500 dark:text-zinc-400">
                Quantum-safe ledger plan
              </p>
              <h1 className="mt-2 text-2xl font-bold text-zinc-900 dark:text-zinc-50">
                Quantum-Safe Blockchain for LLM Responses
              </h1>
              <p className="mt-2 max-w-2xl text-sm text-zinc-600 dark:text-zinc-300">
                A step-by-step blueprint for turning user LLM responses into tamper-evident
                ledger blocks with PQC signatures. The final step reuses the existing
                Send and Verify flow.
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              <span className="rounded-full border border-indigo-200 bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700 dark:border-indigo-700 dark:bg-indigo-950/40 dark:text-indigo-200">
                LLM-ready runbook
              </span>
              <span className="rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700 dark:border-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-200">
                ASTROS test plan
              </span>
              <span className="rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-xs font-medium text-amber-700 dark:border-amber-700 dark:bg-amber-950/40 dark:text-amber-200">
                Evidence-first
              </span>
            </div>
          </div>
          <div className="mt-5 flex flex-wrap gap-3">
            <Link
              href="/"
              className="inline-flex items-center justify-center rounded-xl bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700"
            >
              Open Send and Verify
            </Link>
            <span className="inline-flex items-center rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-2 text-sm text-zinc-600 dark:border-zinc-600 dark:bg-zinc-700/40 dark:text-zinc-300">
              Final step uses existing verification pipeline
            </span>
          </div>
        </header>

        <section className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,360px)]">
          <div className="space-y-6">
            <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
              <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-50">
                Process timeline
              </h2>
              <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-300">
                Follow this sequence to implement a quantum-safe ledger without disrupting
                existing Aletheia verification.
              </p>
              <ol className="mt-6 space-y-5">
                {PIPELINE_STEPS.map((step) => (
                  <li
                    key={step.id}
                    className="rounded-xl border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-600 dark:bg-zinc-700/30"
                  >
                    <div className="flex flex-wrap items-center justify-between gap-3">
                      <div>
                        <p className="text-xs font-semibold uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
                          {step.id}
                        </p>
                        <h3 className="mt-1 text-base font-semibold text-zinc-900 dark:text-zinc-100">
                          {step.title}
                        </h3>
                      </div>
                      <span className="rounded-full border border-zinc-300 bg-white px-3 py-1 text-xs text-zinc-600 dark:border-zinc-500 dark:bg-zinc-800 dark:text-zinc-300">
                        Outputs: {step.outputs.join(", ")}
                      </span>
                    </div>
                    <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-300">
                      {step.summary}
                    </p>
                  </li>
                ))}
              </ol>
            </div>

            <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
              <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-50">
                Ledger schema
              </h2>
              <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-300">
                Use a deterministic block header and keep transactions minimal. Store hashes
                instead of raw prompt or response content.
              </p>
              <pre className="mt-4 overflow-x-auto rounded-xl bg-zinc-100 p-4 text-xs text-zinc-700 dark:bg-zinc-900/60 dark:text-zinc-200">
                {BLOCK_SCHEMA}
              </pre>
            </div>
          </div>

          <div className="space-y-6">
            <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
              <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-50">
                LLM runbook
              </h2>
              <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-300">
                Paste this block into your LLM to implement the plan consistently.
              </p>
              <pre className="mt-4 max-h-[420px] overflow-auto rounded-xl bg-zinc-100 p-4 text-xs text-zinc-700 dark:bg-zinc-900/60 dark:text-zinc-200">
                {RUNBOOK}
              </pre>
            </div>

            <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
              <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-50">
                ASTROS test plan
              </h2>
              <div className="mt-4 space-y-3">
                {ASTROS.map((item) => (
                  <div
                    key={item.id}
                    className="rounded-xl border border-zinc-200 bg-zinc-50 p-4 text-sm text-zinc-700 dark:border-zinc-600 dark:bg-zinc-700/30 dark:text-zinc-200"
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-semibold uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
                        {item.id}
                      </span>
                      <span className="font-semibold">{item.label}</span>
                    </div>
                    <ul className="mt-2 list-disc space-y-1 pl-5 text-xs text-zinc-600 dark:text-zinc-300">
                      {item.tests.map((test) => (
                        <li key={test}>{test}</li>
                      ))}
                    </ul>
                    <p className="mt-2 text-xs text-emerald-700 dark:text-emerald-300">
                      Expected: {item.expected}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>

        <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-700 dark:bg-zinc-800">
          <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-50">
            Implementation checklist
          </h2>
          <div className="mt-4 grid gap-4 md:grid-cols-2">
            {PIPELINE_STEPS.map((step) => (
              <div
                key={`${step.id}-check`}
                className="rounded-xl border border-zinc-200 bg-zinc-50 p-4 text-sm text-zinc-600 dark:border-zinc-600 dark:bg-zinc-700/30 dark:text-zinc-300"
              >
                <p className="text-xs font-semibold uppercase tracking-wide text-zinc-500 dark:text-zinc-400">
                  {step.id}
                </p>
                <p className="mt-2 font-semibold text-zinc-900 dark:text-zinc-50">
                  {step.title}
                </p>
                <p className="mt-1 text-xs">{step.summary}</p>
              </div>
            ))}
          </div>
          <div className="mt-6 flex flex-wrap gap-3">
            <Link
              href="/"
              className="inline-flex items-center justify-center rounded-xl border border-indigo-200 bg-indigo-50 px-4 py-2 text-sm font-semibold text-indigo-700 hover:bg-indigo-100 dark:border-indigo-700 dark:bg-indigo-950/40 dark:text-indigo-200"
            >
              Execute final step with Send and Verify
            </Link>
          </div>
        </section>
      </div>
    </div>
  );
}
