# Plan Phase 3 — UI: AI Response + Verification

Detailed, machine-readable specification for the **verify page** (and related response UI): wireframe, tooltips, and UX rationale. Use this document to implement the UI without guessing copy or structure.

**Related:** [Plan Phase 2](PLAN_PHASE2.md) (Evidence Package, killer demo) · [Demo script](../DEMO_SCRIPT.md) · [Vision Phase 2](VISION_AND_ROADMAP.md#2-killer-demo--domain-choice)

---

## Table of contents

- [Scope and target page](#scope-and-target-page)
- [Wireframe (section by section)](#wireframe-section-by-section)
- [Tooltip texts (copy-paste ready)](#tooltip-texts-copy-paste-ready)
- [Tooltip writing principles](#tooltip-writing-principles)
- [Top-3 priorities](#top-3-priorities)
- [Implementation steps (numbered, LLM-readable)](#implementation-steps-numbered-llm-readable)
- [Data requirements](#data-requirements)
- [Acceptance criteria](#acceptance-criteria)

---

## Scope and target page

| Item | Value |
|------|--------|
| **Target** | Verify page (`/verify?id=...`) and, where applicable, the main page response block |
| **Goal** | Present “AI Response + Verification” so that trust, claim, and offline verification are obvious without crypto jargon |
| **Principle** | Tooltips explain *why it matters to the user* (prove, verify, trust, independent, offline), not algorithms |

---

## Wireframe (section by section)

Implement sections in this order. Each block is a logical UI region; layout should be single-column on small screens, with optional grouping in a card.

---

### HEADER (unchanged)

Keep the existing header.

```
[Aletheia AI logo]   Aletheia AI
                     Verifiable AI responses with signing and timestamps
```

**UX:** No change. Establishes product and value proposition.

---

### SECTION 1 — Trust Summary (NEW; above the fold)

**Implementation step:** [P3.2 — Trust Summary Card](#step-p32--trust-summary-card-section-1)

**Purpose:** User sees at a glance that the response is verified and what that means. No scroll required.

**Wireframe (text):**

```
┌─────────────────────────────────────────────┐
│ ✅ Verified AI Response                      │
│                                             │
│ 🕒 Created: 2026-02-02 15:35 UTC             │
│ 🤖 Model: gpt-4-0613                         │
│ 🛡️ Integrity: Not altered                   │
│ ⏱️ Timestamp: Trusted (RFC 3161)             │
│                                             │
│ [🔎 What is verified?]   [📋 Copy summary]   │
└─────────────────────────────────────────────┘
```

**Behavior:**

- **Verified AI Response** — Heading. Show only when the record has a valid signature and TSA token; otherwise show a neutral state (e.g. “AI Response” without “Verified”).
- **Created** — Use `record.createdAt` (ISO 8601); display in UTC and optionally in local time.
- **Model** — `record.llmModel` (e.g. `gpt-4-0613`).
- **Integrity** — If `record.hashMatch === true` → “Not altered”; if `false` → “Altered or unknown”; if N/A (no hash check yet) → “Not checked” or “—”.
- **Timestamp** — If TSA token present and (optionally) valid → “Trusted (RFC 3161)”; otherwise “Not available” or “—”.
- **What is verified?** — Button or link that expands or links to a short explanation (e.g. modal or inline): which parts of the response are covered by the signature (response text, and if applicable claim metadata).
- **Copy summary** — Copies a short plain-text summary (e.g. “Verified AI Response — Created: … — Model: … — Integrity: … — Timestamp: …”) to clipboard.

**Component suggestion:** `TrustSummaryCard`. Use `title` + list of rows + action buttons. Each row and each button must have a tooltip (see [Tooltip texts](#tooltip-texts-copy-paste-ready)).

---

### SECTION 2 — Prompt & Response

**Implementation step:** [P3.3 — Prompt & Response block](#step-p33--prompt--response-block-with-copy-response-section-2)

**Purpose:** Show the actual prompt and response. Slight improvement over current: clear labels, copy response only.

**Wireframe (text):**

```
Prompt
────────────────────────────────
Does the following clause comply with GDPR?
...

Response
────────────────────────────────
[ AI response text … ]

[ 📋 Copy response ]
```

**Behavior:**

- **Prompt** — Read-only text, `record.prompt`. Preserve line breaks.
- **Response** — Read-only text, `record.response`. Preserve line breaks.
- **Copy response** — Copies only the response text (no signatures or metadata). Tooltip: see [Tooltip texts](#tooltip-texts-copy-paste-ready).

**UX:** Clear separation of user input vs AI output; “Copy response” avoids confusion with “Copy summary” or “Copy hash”.

---

### SECTION 3 — AI Claim (NEW; show only when claim exists)

**Implementation step:** [P3.4 — AI Claim block](#step-p34--ai-claim-block-section-3)

**Purpose:** Killer differentiator. Show the structured claim (claim text, confidence, policy version) and that it is part of the signed payload.

**Visibility:** Render this section only when `record.claim != null` or `record.policyVersion != null` (or equivalent from API).

**Wireframe (text):**

```
┌─────────────────────────────────────────────┐
│ 🧠 AI Claim                                  │
│                                             │
│ Claim:                                      │
│ “The clause is not relevant to GDPR.”        │
│                                             │
│ Confidence: 0.85                             │
│ Policy version: GDPR-2024                    │
│                                             │
│ 🔐 Included in signed payload                │
└─────────────────────────────────────────────┘
```

**Behavior:**

- **Claim** — `record.claim`. Display in quotes or blockquote. If long, truncate with “…” and “Show more” or full text on expand.
- **Confidence** — `record.confidence` (e.g. `0.85`). Display as number or percentage (e.g. 85%). Tooltip clarifies it is not a legal guarantee.
- **Policy version** — `record.policyVersion` (e.g. `gdpr-2024`). Display as-is or formatted (e.g. “GDPR-2024”).
- **Included in signed payload** — Short label or badge. Tooltip: this claim is cryptographically protected and cannot be changed without breaking verification.

**Component suggestion:** `AIClaimCard`. All fields that have tooltips in the table must get `title` or `aria-describedby` from the tooltip text.

---

### SECTION 4 — Verification Details (advanced, collapsible)

**Implementation step:** [P3.5 — Verification Details collapsible](#step-p35--verification-details-collapsible-section-4)

**Purpose:** For auditors or technical users: hash, signature, timestamp token. Collapsed by default.

**Wireframe (text):**

```
▼ Cryptographic verification details
────────────────────────────────────
Model: gpt-4-0613
Created: 2026-02-02 15:35:31

Response hash (SHA-256):
c2abdbe...

Signature:
iZEFyH...

Timestamp token:
MIIEAY...

[ Verify hash ]   [ 📋 Copy hash ]
```

**Behavior:**

- **Heading** — Collapsible (e.g. “▼ Cryptographic verification details” / “▶ …”). Default: collapsed.
- **Model, Created** — Same as Trust Summary; can repeat or reference.
- **Response hash** — Truncated (e.g. first 12–20 chars + “…”). Full value in tooltip or on “Copy hash”. Tooltip: see table.
- **Signature** — Truncated. Tooltip: see table.
- **Timestamp token** — Truncated (Base64). Tooltip: see table.
- **Verify hash** — Recomputes hash from response (canonical form) in the browser and compares to `record.responseHash`. Same as current “Verify hash” on verify page.
- **Copy hash** — Copies full `record.responseHash` (64-char hex) to clipboard.

**Component suggestion:** `VerificationDetailsCard` or `CollapsibleVerificationDetails`. Each label (Response hash, Signature, Timestamp token, Verify hash) uses the tooltip text from the table.

---

### SECTION 5 — Evidence Package

**Implementation step:** [P3.6 — Evidence Package block](#step-p36--evidence-package-block-section-5)

**Purpose:** Make offline verification obvious and actionable. Critical for compliance/audit narrative.

**Wireframe (text):**

```
📦 Evidence Package
────────────────────────────────
This response can be verified offline.

[ ⬇ Download evidence ]   [ 👀 Preview package ]
```

**Behavior:**

- **This response can be verified offline** — Short sentence. Tooltip: see table.
- **Download evidence** — Same as current “Download evidence”: GET `/api/ai/evidence/:id`, save as `.aep`. Label can be “Download evidence” or “Download .aep”. Tooltip: see table.
- **Preview package** — Optional: open a modal or expandable that lists the contents of the Evidence Package (response.txt, canonical.bin, hash.sha256, signature.sig, timestamp.tsr, metadata.json, public_key.pem) without downloading. Can be implemented by calling GET with `?format=json` and displaying keys or a short description. Tooltip: see table.

**Component suggestion:** `EvidencePackageCard`. Both buttons must have tooltips from the table.

---

### SECTION 6 — Footer

**Implementation step:** [P3.7 — Footer line](#step-p37--footer-line-section-6)

**Purpose:** Reinforce trust and use case.

**Addition:**

- Add one line below the existing footer content:

```
Designed for audit, compliance, and long-term verification
```

**UX:** Positions the product for legal/compliance and long-term evidence.

---

## Tooltip texts (copy-paste ready)

Use these verbatim for `title`, `aria-label`, or your tooltip component. Key: **ID** = use as stable key in code; **Text** = exact copy for UI.

| ID | Text |
|----|------|
| **verified_ai_response** | This response was cryptographically signed and timestamped. Its integrity and creation time can be independently verified. |
| **integrity_not_altered** | The response content has not changed since it was signed. |
| **timestamp_trusted** | The creation time is certified by an independent Time Stamping Authority. |
| **what_is_verified** | See exactly which parts of the response are covered by the signature. |
| **copy_summary** | Copies a short verification summary to the clipboard. |
| **copy_response** | Copies the response text only (without signatures or metadata). |
| **ai_claim_heading** | A structured statement derived from the AI response and included in the signed data. |
| **confidence** | A subjective confidence score provided by the AI for this claim. This is not a legal guarantee. |
| **policy_version** | The policy or regulatory framework used when forming the claim. |
| **included_in_signed_payload** | This claim is cryptographically protected and cannot be changed without breaking verification. |
| **response_hash** | A cryptographic fingerprint of the response content. |
| **signature** | The digital signature created using the private key of the signing service. |
| **timestamp_token** | Proof that the hash existed at a specific moment in time, issued by a trusted authority. |
| **verify_hash** | Recalculates the hash locally and checks it against the signed value. |
| **download_evidence** | Downloads all cryptographic materials required for independent verification. |
| **preview_package** | Explore the contents of the evidence package before downloading. |
| **verified_offline** | No connection to Aletheia AI is required to verify authenticity and integrity. |

**Implementation note:** Prefer a single source of truth (e.g. a `TOOLTIPS` map or i18n file) keyed by ID so copy can be updated in one place.

---

## Tooltip writing principles

- **Do not** explain algorithms (e.g. “Uses SHA-256 hashing algorithm”).
- **Do** explain what the user gets: prove, verify, trust, independent, offline.
- **Do** use short, human sentences.

| ❌ Bad example | ✅ Good example |
|---------------|-----------------|
| “Uses SHA-256 hashing algorithm” | “Creates a fingerprint that changes if even one character is modified” (or use the table: “A cryptographic fingerprint of the response content.”) |

When adding new tooltips, check that they answer “Why should I care?” rather than “How does it work technically?”.

---

## Top-3 priorities

If implementing in stages, prioritize in this order:

1. **Trust Summary Card** (Section 1) — Visible immediately; sets “verified” and “integrity / timestamp” in plain language.
2. **AI Claim block** (Section 3) — Differentiator; shows claim, confidence, policy version, and “included in signed payload”.
3. **Evidence Package block** (Section 5) — Download + optional preview; reinforces “offline verification” and compliance.

Then add Prompt & Response refinements (Section 2), collapsible Verification Details (Section 4), and footer line (Section 6).

---

## Implementation steps (numbered, LLM-readable)

Every step that requires code changes has a number and a **Coding prompt (LLM-readable)** block. Implement in order unless otherwise noted. Steps P3.0, P3.8, P3.9 are optional.

| Step | Title | Est. | Required |
|------|--------|------|----------|
| P3.0 | Backend: verify API returns claim, confidence, policyVersion | 15–30 min | Optional |
| P3.1 | Create TOOLTIPS map (single source of truth) | 15 min | Yes |
| P3.2 | Trust Summary Card (Section 1) | 45–60 min | Yes |
| P3.3 | Prompt & Response block with Copy response (Section 2) | 20–30 min | Yes |
| P3.4 | AI Claim block (Section 3) | 30–45 min | Yes |
| P3.5 | Verification Details collapsible (Section 4) | 40–50 min | Yes |
| P3.6 | Evidence Package block (Section 5) | 25–35 min | Yes |
| P3.7 | Footer line (Section 6) | 5 min | Yes |
| P3.8 | “What is verified?” content | 15–20 min | Optional |
| P3.9 | Preview package modal/expandable | 20–30 min | Optional |

---

### Step P3.0 — Backend: verify API returns claim, confidence, policyVersion (optional)

| Field | Value |
|-------|--------|
| **Est.** | 15–30 min |
| **When** | Only if GET `/api/ai/verify/:id` does not yet return `claim`, `confidence`, `policyVersion`. |

**Coding prompt (LLM-readable):**

- In the backend, ensure the endpoint that serves the verify page (e.g. GET `/api/ai/verify/:id` or the controller that returns the record for the verify UI) includes in the JSON response the fields: `claim` (string or null), `confidence` (number or null), `policyVersion` (string or null). These are already stored on `AiResponse` for compliance responses (see Plan Phase 2, DP2.4). Add them to the DTO or response map used by the verify endpoint. If the verify endpoint uses a different path (e.g. `/api/ai/verify/:id`), ensure the same entity or service is used and the three fields are serialized. No change to frontend in this step.

**Acceptance:** GET `/api/ai/verify/:id` response JSON contains `claim`, `confidence`, `policyVersion` when the record has them; null otherwise.

---

### Step P3.1 — Create TOOLTIPS map (single source of truth)

| Field | Value |
|-------|--------|
| **Est.** | 15 min |

**Coding prompt (LLM-readable):**

- Create a single source of truth for all tooltip texts used on the verify page (and optionally the main page response block). In `frontend/`, add a file (e.g. `frontend/lib/tooltips.ts` or `frontend/app/verify/tooltips.ts`) that exports a constant object mapping **tooltip ID** to **text**. Use the exact IDs and text from the table in [Tooltip texts (copy-paste ready)](#tooltip-texts-copy-paste-ready). Example shape: `export const TOOLTIPS: Record<string, string> = { verified_ai_response: "This response was cryptographically signed...", integrity_not_altered: "The response content has not changed...", ... };`. Every UI element that shows a tooltip must read from this map by ID (e.g. `title={TOOLTIPS.verified_ai_response}` or `aria-label={TOOLTIPS.copy_response}`). Do not hard-code tooltip strings in components.

**Acceptance:** One file exports TOOLTIPS with all 17 IDs and texts from the spec; no tooltip copy duplicated in JSX.

---

### Step P3.2 — Trust Summary Card (Section 1)

| Field | Value |
|-------|--------|
| **Est.** | 45–60 min |
| **Section** | [SECTION 1 — Trust Summary](#section-1--trust-summary-new-above-the-fold) |

**Coding prompt (LLM-readable):**

- On the verify page (`frontend/app/verify/page.tsx` or a child component), add a **Trust Summary** card (or block) that appears above the prompt/response. It must show: (1) Heading: “Verified AI Response” when the record has both `signature` and `tsaToken` non-null/non-empty; otherwise “AI Response”. (2) Row “Created:” with `record.createdAt` formatted in UTC (e.g. ISO or “YYYY-MM-DD HH:mm UTC”). (3) Row “Model:” with `record.llmModel`. (4) Row “Integrity:” with “Not altered” if `record.hashMatch === true`, “Altered or unknown” if `record.hashMatch === false`, “Not checked” or “—” if undefined. (5) Row “Timestamp:” with “Trusted (RFC 3161)” if `record.tsaToken` is present, “Not available” or “—” otherwise. (6) Button or link “What is verified?” that expands or opens a short explanation (inline or modal) of what is covered by the signature (response text and, if applicable, claim metadata). (7) Button “Copy summary” that copies to clipboard a plain-text summary line containing: Verified AI Response, Created, Model, Integrity, Timestamp. Attach tooltips to the heading (use TOOLTIPS.verified_ai_response), to the Integrity row (TOOLTIPS.integrity_not_altered), to the Timestamp row (TOOLTIPS.timestamp_trusted), to “What is verified?” (TOOLTIPS.what_is_verified), and to “Copy summary” (TOOLTIPS.copy_summary). Use the TOOLTIPS map from Step P3.1. Place this card at the top of the page content, so it is visible without scrolling.

**Acceptance:** Trust Summary card is visible first; all four data rows and both buttons present; tooltips attached per spec; Copy summary copies correct text to clipboard.

---

### Step P3.3 — Prompt & Response block with Copy response (Section 2)

| Field | Value |
|-------|--------|
| **Est.** | 20–30 min |
| **Section** | [SECTION 2 — Prompt & Response](#section-2--prompt--response) |

**Coding prompt (LLM-readable):**

- On the verify page, ensure the **Prompt** and **Response** blocks are clearly labeled (“Prompt” and “Response”) and display `record.prompt` and `record.response` with line breaks preserved (e.g. `whitespace-pre-wrap` or equivalent). Add a **Copy response** button that copies only `record.response` (no signatures, hash, or metadata) to the clipboard. Attach the tooltip TOOLTIPS.copy_response to this button. If the verify page already has prompt/response layout, only add the Copy response button and tooltip; ensure labels are explicit.

**Acceptance:** Prompt and Response sections are labeled and preserve line breaks; Copy response copies only response text; button has tooltip from TOOLTIPS.copy_response.

---

### Step P3.4 — AI Claim block (Section 3)

| Field | Value |
|-------|--------|
| **Est.** | 30–45 min |
| **Section** | [SECTION 3 — AI Claim](#section-3--ai-claim-new-show-only-when-claim-exists) |

**Coding prompt (LLM-readable):**

- On the verify page, add an **AI Claim** card (or block) that is **rendered only when** `record.claim != null` or `record.policyVersion != null` (or both). The block must show: (1) Heading “AI Claim” with tooltip TOOLTIPS.ai_claim_heading. (2) Label “Claim:” and the value `record.claim` in quotes or blockquote; if length > ~200 chars, truncate with “…” and optionally “Show more” to expand. (3) Label “Confidence:” and `record.confidence` (e.g. 0.85 or 85%) with tooltip TOOLTIPS.confidence. (4) Label “Policy version:” and `record.policyVersion` (e.g. format “GDPR-2024” if value is “gdpr-2024”) with tooltip TOOLTIPS.policy_version. (5) A short label or badge “Included in signed payload” with tooltip TOOLTIPS.included_in_signed_payload. Use the TOOLTIPS map from Step P3.1. Do not render this block at all when `claim` and `policyVersion` are both null/undefined.

**Acceptance:** AI Claim block appears only when record has claim or policyVersion; all fields and tooltips present; “Included in signed payload” visible with tooltip.

---

### Step P3.5 — Verification Details collapsible (Section 4)

| Field | Value |
|-------|--------|
| **Est.** | 40–50 min |
| **Section** | [SECTION 4 — Verification Details](#section-4--verification-details-advanced-collapsible) |

**Coding prompt (LLM-readable):**

- On the verify page, add a **Verification Details** section with a **collapsible** heading (e.g. “Cryptographic verification details”). Default state: **collapsed**. When expanded, show: (1) Model and Created (same as Trust Summary). (2) “Response hash (SHA-256):” with `record.responseHash` truncated (e.g. first 16 chars + “…”; full value available via Copy hash). Attach tooltip TOOLTIPS.response_hash to the label or value. (3) “Signature:” with `record.signature` truncated (e.g. first 12 chars + “…”). Tooltip TOOLTIPS.signature. (4) “Timestamp token:” with `record.tsaToken` truncated. Tooltip TOOLTIPS.timestamp_token. (5) Button “Verify hash” that recomputes the hash from `record.response` using the same canonicalization as the backend (see existing `canonicalize` and `sha256Hex` on the verify page) and compares to `record.responseHash`; show match/mismatch. Tooltip TOOLTIPS.verify_hash. (6) Button “Copy hash” that copies full `record.responseHash` to clipboard. Use the TOOLTIPS map from Step P3.1. Ensure the section is collapsed on initial load.

**Acceptance:** Section is collapsible and collapsed by default; hash/signature/timestamp truncated; Verify hash and Copy hash work; all labels/buttons have correct tooltips.

---

### Step P3.6 — Evidence Package block (Section 5)

| Field | Value |
|-------|--------|
| **Est.** | 25–35 min |
| **Section** | [SECTION 5 — Evidence Package](#section-5--evidence-package) |

**Coding prompt (LLM-readable):**

- On the verify page, add an **Evidence Package** block that shows: (1) Heading “Evidence Package” (or with icon). (2) The sentence “This response can be verified offline.” with tooltip TOOLTIPS.verified_offline. (3) Button “Download evidence” that calls GET `${apiUrl}/api/ai/evidence/${record.id}`, receives the response as blob, and triggers a file download (e.g. `aletheia-evidence-${record.id}.aep`). Use the same logic as the main page “Download evidence” if present. Attach tooltip TOOLTIPS.download_evidence to this button. (4) Button “Preview package” (optional in this step): when clicked, call GET with `?format=json`, parse the response, and show in a modal or expandable the list of keys (response.txt, canonical.bin, hash.sha256, signature.sig, timestamp.tsr, metadata.json, public_key.pem) or a short description. Tooltip TOOLTIPS.preview_package. Use the TOOLTIPS map from Step P3.1.

**Acceptance:** Block shows “verified offline” sentence with tooltip; Download evidence downloads .aep; both buttons have correct tooltips; Preview package (if implemented) shows package contents.

---

### Step P3.7 — Footer line (Section 6)

| Field | Value |
|-------|--------|
| **Est.** | 5 min |
| **Section** | [SECTION 6 — Footer](#section-6--footer) |

**Coding prompt (LLM-readable):**

- On the verify page, add a single line to the footer (below any existing footer content): “Designed for audit, compliance, and long-term verification”. Use a paragraph or small text element. No tooltip required. If the verify page shares a layout with other pages, add this line only to the verify page footer or to the shared footer so it appears on the verify page.

**Acceptance:** Footer on the verify page includes the exact line “Designed for audit, compliance, and long-term verification”.

---

### Step P3.8 — “What is verified?” content (optional)

| Field | Value |
|-------|--------|
| **Est.** | 15–20 min |
| **Depends on** | P3.2 (Trust Summary) |

**Coding prompt (LLM-readable):**

- Implement the content for the “What is verified?” button from Step P3.2. When the user clicks it, show a short explanation (inline expandable or modal) that states: the signature covers the response text in canonical form; if the record has a claim (claim, confidence, policy version), those are also part of the signed payload; verification can be done offline with the Evidence Package. Use plain language (no algorithm names unless necessary). No new tooltips required; the button already has TOOLTIPS.what_is_verified.

**Acceptance:** Clicking “What is verified?” reveals explanation that mentions response text and (when applicable) claim as part of signed payload and offline verification.

---

### Step P3.9 — Preview package modal/expandable (optional)

| Field | Value |
|-------|--------|
| **Est.** | 20–30 min |
| **Depends on** | P3.6 (Evidence Package block) |

**Coding prompt (LLM-readable):**

- If not done in P3.6, implement the “Preview package” action: on click, request GET `/api/ai/evidence/:id?format=json`, parse the JSON (keys are file names: response.txt, canonical.bin, hash.sha256, signature.sig, timestamp.tsr, metadata.json, public_key.pem; values are base64). Display in a modal or expandable panel the list of file names with optional short descriptions (e.g. “Response text”, “Canonical bytes”, “SHA-256 hash”, etc.). Do not display full base64 content; only the keys or a summary. The button must have tooltip TOOLTIPS.preview_package.

**Acceptance:** Preview package opens a modal or panel listing the seven Evidence Package components; tooltip on button.

---

## Data requirements

The verify page (and any component that shows claim or trust summary) needs the following from the API. Ensure GET `/api/ai/verify/:id` (or equivalent) returns:

| Field | Type | Required for |
|-------|------|----------------|
| `id` | number | All |
| `prompt` | string | Section 2 |
| `response` | string | Sections 2, 4 (hash verification) |
| `responseHash` | string | Sections 1 (integrity), 4 |
| `signature` | string \| null | Sections 1 (verified), 4 |
| `tsaToken` | string \| null | Sections 1 (timestamp), 4 |
| `llmModel` | string | Sections 1, 4 |
| `createdAt` | string (ISO 8601) | Sections 1, 4 |
| `hashMatch` | boolean (optional) | Section 1 (Integrity row) |
| `signatureValid` | string (optional) | Section 1 (if you show signature status) |
| `claim` | string \| null | Section 3 |
| `confidence` | number \| null | Section 3 |
| `policyVersion` | string \| null | Section 3 |

If `claim`, `confidence`, or `policyVersion` are not yet in the verify API response, add them (backend already stores them for compliance responses; see [Plan Phase 2](PLAN_PHASE2.md) DP2.4).

---

## Acceptance criteria

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| **Doc** | This spec | Wireframe and tooltips are implementable without ambiguity. |
| **Manual** | Trust Summary | Section 1 visible without scroll; all four rows (Created, Model, Integrity, Timestamp) and both buttons have correct tooltips. |
| **Manual** | AI Claim | Section 3 appears when record has claim/policyVersion; claim text, confidence, policy version, and “Included in signed payload” shown; tooltips on heading and each field. |
| **Manual** | Evidence Package | Section 5 has “verified offline” sentence and Download (and optionally Preview) with correct tooltips. |
| **Manual** | Verification Details | Section 4 is collapsible; hash/signature/timestamp truncated; Verify hash and Copy hash work; tooltips on each label/button. |
| **Manual** | Footer | Footer includes the line “Designed for audit, compliance, and long-term verification”. |
| **Manual** | Tooltips | All tooltips use the exact text from the table (or approved variant); no algorithm-focused or jargon-heavy copy. |

---

## References

- [Plan Phase 2](PLAN_PHASE2.md) — Evidence Package, killer demo, DP2.4 (claim).
- [Demo script](../DEMO_SCRIPT.md) — Step-by-step demo flow.
- [Vision Phase 2](VISION_AND_ROADMAP.md#2-killer-demo--domain-choice) — Product context.
- Current verify page: `frontend/app/verify/page.tsx`.
- Current main page response block: `frontend/app/page.tsx`.
