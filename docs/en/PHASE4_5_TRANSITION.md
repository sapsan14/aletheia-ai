# Phase 4 → Phase 5 Transition Plan

**Goal:** Turn the Phase 4 demo (policy coverage + narrative + market validation) into a solid foundation for Phase 5 as a real **Evidence-as-a-Service** product.

This document defines:

- What **must be truly complete** from Phase 4 before we start Phase 5.
- The **development tasks** to harden policy, UI, API, and documentation.
- **LLM-readable coding prompts** for each task.
- How we present the product to **users, developers, and investors**.
- A clear understanding of **what “policy” is in Aletheia**, and how it can be **used, switched, edited, and updated**.

---

## 0. Branch & Scope

- **Branch name (suggested):** `phase4-5-transition`
- **Base:** latest `main` with Phase 4 merged.
- **Out of scope:**
  - New major Phase 5 features.
  - New blockchain / SIEM integrations (covered in later phases).
- **In scope:** hardening and polishing Phase 4 so that Phase 5 is not “API without a story”.

---

## 1. Phase 4 Completion Checklist (Dependencies)

Phase 5 assumes the following Phase 4 items are **real and stable**, not drafts.

### 1.1 Policy engine & coverage

- [x] **Demo policy is finalized**
  - `docs/en/policy/aletheia-demo-2026-01.json` and `.md` exist and are considered canonical.
  - Rule IDs `R1`–`R4` are **stable** and referenced in:
    - backend policy evaluation,
    - verify API,
    - Evidence Package metadata,
    - UI (verify page).

- [x] **Backend policy evaluation is on by default**
  - Each new AI response gets:
    - `policy_coverage` stored in DB.
    - `policy_rules_evaluated` (array of ruleId + status) stored in DB.
  - No hidden flags that silently disable policy evaluation in production.

- [x] **Verify API & Evidence Packages expose policy coverage**
  - `GET /api/ai/verify/:id` includes:
    - `policyCoverage` (0.0–1.0),
    - `policyRulesEvaluated` (array).
  - `metadata.json` in Evidence Package includes:
    - `policy_coverage`,
    - `policy_rules_evaluated`.

- [x] **Verify page renders coverage correctly**
  - Shows percentage, “N of 4 rules checked”, and rules R1–R4 with status.

### 1.2 Compliance narrative & disclaimers

- [x] The message **“We don’t certify truth. We certify responsibility.”** is consistent:
  - On the hero section of the main page.
  - In the Trust / Verify UI (copy around Policy coverage).
  - In documentation (Trust Model, Phase 4 plan, policy docs).

- [x] **“Why is confidence not 100%?”**:
  - Explains in **plain language**:
    - which checks ran and passed,
    - which checks are not evaluated in this demo policy.
  - Includes a short line like: “We do not certify truth; we show what was checked.”

- [x] *(Recommended)* **Ambiguity warning exists**
  - At least one scenario where the UI explicitly hints that:
    - policy coverage or claim does **not** apply to the whole conversation or context,
    - or the request is outside the scope of the demo policy.

- [x] *(Recommended)* **AI Act-friendly wording**
  - At least one paragraph in docs explaining:
    - what is logged / signed,
    - how this helps with audit / accountability duties (without overclaiming compliance).

### 1.3 Market validation

- [x] **Demo scenario is written down**
  - A single “hero” use case (e.g. HR or Legal/compliance) is clearly described in Phase 4 docs (e.g. `DEMO_SCENARIO_PHASE4.md`).

- [x] **Outreach log is non-empty**
  - `docs/outreach/PHASE4_OUTREACH.md` contains:
    - at least a few **real contacts / outcomes** (even “no interest” is data),
    - 3–5 key insights (what resonated / what confused people).

- [x] **Default integration target selected**
  - One main direction is explicitly chosen for Phase 5, e.g.:
    > “Aletheia signs external AI outputs for regulated workflows (HR / legal / governance).”
  - Other directions (MCP, SIEM, blockchain) are marked as **future tracks**.

---

## 2. What is “Policy” in Aletheia?

We use “policy” to mean:

> A small, versioned set of rules that define **what is checked** about an AI output (signature, timestamp, model identity, content checks, human review, etc.) and how this is reported to users.

### 2.1 Coverage-policy and Claim-policy (UI and docs naming)

In the product and documentation we use two distinct terms:

- **Coverage-policy** — The versioned set of rules that define **what Aletheia checks** about an AI output (signature, timestamp, model identity, etc.) and how coverage is reported. Stored per response; in Phase 4.5 the single demo policy is `aletheia-demo (2026-01)`. Shown in the "Coverage-policy (demo)" block on the verify page and in Trust Panel summaries.

- **Claim-policy** — The policy or regulatory framework **the AI used when forming the claim** (e.g. AI-ACT-2024, GDPR). This is the `policyVersion` field in the AI claim; it identifies which rules or standards the model claims to have applied. Shown in the "AI Claim" block as "Claim-policy:".

Use these names consistently in the UI, API labels, and docs.

Key properties (for Coverage-policy):

- **Versioned:** `policy_id` + `policy_version` (e.g. `aletheia-demo`, `2026-01`).
- **Rule-based:** rules `R1`–`R4` (and later more), with:
  - `ruleId` (e.g. `"R1"`),
  - `description`,
  - `type` (technical / content / human),
  - `status` (pass / not_evaluated / fail),
  - `required` (for compliance interpretation).
- **Evaluated per-response:** `policy_coverage` + list of rule results stored alongside each AI response.
- **Surfaced everywhere:**
  - Verify API,
  - Evidence Package metadata,
  - Verify UI (Policy coverage & “Why not 100%?”),
  - Docs (policy files + narrative).

In this transition phase we want to make **policy lifecycle explicit**:

- How policies are **defined** (files + docs),
- How they are **selected/switched** (per environment, per tenant, per use case),
- How they can be **edited / rolled out / deprecated**.

---

## 3. Workstreams for Phase 4 → 5 Transition

### 3.1 Policy lifecycle & selection

#### 3.1.1 Task: Define policy selection model (per environment / per key)

**Goal:** Make it clear **which policy applies** to a given response, and how it can be changed.

**Design (high-level):**

- **Phase 4.5:** keep it simple:
  - Single **demo policy** for all responses: `aletheia-demo-2026-01`.
  - Policy id/version is stored and returned in verify API & metadata.
- **Phase 5-ready design:**  
  - Policy selection could be based on:
    - environment (dev/test/prod),
    - API key (per tenant),
    - or explicit field in the request (with server-side allowlist).

**LLM coding prompt (backend):**

> Implement a minimal policy selection mechanism for Phase 4.5:
> 
> - Add fields to the `AiResponse` entity for `policyId` and `policyVersion`, if not already present.
> - In the service that evaluates policy (Phase 4 policy evaluation logic), always use the `aletheia-demo-2026-01` policy and set `policyId="aletheia-demo"` and `policyVersion="2026-01"` on the saved `AiResponse`.
> - Ensure `GET /api/ai/verify/:id` returns these values, and that they are included in the Evidence Package metadata under `policy_id` and `policy_version`.
> - Do **not** yet implement per-tenant or per-request policy switching; document this as a Phase 5 extension.

**Tests:**

- Unit tests:
  - saving a response sets `policyId` / `policyVersion` correctly,
  - verify API and metadata include them.

**Docs:**

- Update `docs/en/policy/aletheia-demo-2026-01.md`:
  - Add a short section “Where this policy is used in Phase 4.5”.
- Add a short section here:
  - “Current policy selection model: single demo-policy for all responses”.

#### 3.1.2 Task: Document future policy switching strategy (design-only)

**Goal:** Prepare Phase 5 to support **multiple policies** (per tenant / per product tier) without writing all code now.

**LLM documentation prompt (no code):**

> Draft a short design section (2–3 pages) outlining how Aletheia could support multiple policies in Phase 5:
> - Define three levels of policy selection: (1) per-environment default, (2) per-API-key override, (3) per-request explicit policyId/policyVersion (only if allowed for that key).
> - Describe how policy files are stored and versioned (e.g. `docs/en/policy/` plus an internal registry).
> - Explain how policy changes are rolled out and how policyVersion is pinned to historical evidence (never rewritten).
> - Include a “risk & misuse” note: client cannot silently “downgrade” policy without leaving an audit trace.

Place this design in a new doc, e.g. `docs/en/policy/POLICY_LIFECYCLE_DESIGN.md`.

---

### 3.2 Ambiguity & disclaimers

#### 3.2.1 Task: Minimal ambiguity warning in UI

**Goal:** When the system cannot guarantee that the whole context is covered by policy (e.g. multi-turn chat, external tools), show an **explicit ambiguity warning**.

**LLM UI prompt:**

> Identify one or two concrete scenarios in the existing UI where policy coverage may be misunderstood (e.g. only the last AI response is signed and covered by policy).
> - For each scenario, propose a short, clear warning text (1–2 sentences) that:
>   - clarifies what is **actually** signed / covered,
>   - warns that other parts of the context may not be.
> - Suggest where to place this warning in the UI (e.g. under Policy coverage, or as a small info line near the claim/confidence block).
> - Ensure the wording is compliant with the narrative “We don’t certify truth, we certify responsibility.”

Once wording is agreed, implement it in the verify UI.

**Tests:**

- Manual: check that the warning appears in the relevant scenario and is not overly intrusive.

#### 3.2.2 Task: AI Act-friendly snippet in docs

**Goal:** Provide a short explanation of how Aletheia helps with **audit and accountability**, without claiming full compliance.

**LLM docs prompt:**

> Write a 1–2 paragraph section “Relation to AI Act / audit duties” for `docs/en/TRUST_MODEL.md` (or a similar document):
> - Explain that Aletheia logs and signs AI outputs and policy evaluation results.
> - Clarify that this supports traceability and audit of AI-assisted decisions.
> - Explicitly say that Aletheia does not, by itself, guarantee legal compliance or correctness of decisions.
> - Use neutral, non-marketing language.

---

### 3.3 Product story & developer / investor view

#### 3.3.1 Task: Product one-liner & default integration target

**Goal:** Have a single, repeatable message for docs, README, and investor deck.

**Suggested one-liner:**

> **“Aletheia signs external AI outputs for regulated workflows.”**

**Where to use it:**

- Top of `README.md`.
- Top of `docs/en/PLAN_PHASE5.md`.
- In hero area (short variant for UI, if desired).

#### 3.3.2 Task: Draft basic packaging / pricing hooks (docs only)

Not full pricing, but at least **product tiers**:

- **Free:** offline verifier, basic Evidence Package.
- **Standard (Paid signer):** signing + storage API, policy coverage, simple analytics.
- **Compliance bundle:** tailored policies + reporting for HR/legal.

**LLM docs prompt:**

> In `docs/en/PLAN_PHASE5.md` or `docs/en/PRODUCT_PACKAGING.md`, draft a short section describing:
> - A Free tier: offline verifier, limited Evidence Package usage, intended for evaluation.
> - A Paid signer tier: API access to sign and store AI outputs with policy coverage.
> - A Compliance bundle: additional consulting/policies for HR/legal teams.
> - Do not include actual prices; focus on positioning and target audience.

---

### 3.4 UI upgrades (Phase 4.5 polish)

#### 3.4.1 Task: “Trust summary” panel for investors & users

**Goal:** Make the Trust Panel and Verify page visually communicate **how nice the product is**:

- Signed?
- Timestamped?
- Policy coverage?
- Claim/confidence present?
- Offline verifiable?

**LLM UI prompt:**

> Review the existing Trust Panel and verify page.
> - Propose a compact “Trust summary” block that:
>   - shows badges for Signed, Timestamped, Policy coverage %, Claim present (yes/no), Offline verification available,
>   - uses consistent colors/icons,
>   - is screenshot-friendly for investor decks.
> - Suggest exact text labels (max 2–3 words per badge) and hover tooltips.
> - Ensure the summary is visible both on the main page and on `/verify`.

Then implement the refined layout; this is largely a polish on top of existing Phase 4 UI.

#### 3.4.2 Task: Developer-friendly hints in UI

**Goal:** Make it obvious for developers where to go next after the demo.

Ideas:

- Small “View API” / “OpenAPI spec” link near the Evidence Package / Verifier utility.
- “Integrate in your stack” link to docs/en/PLAN_PHASE5 or an integrations section.

**LLM UI prompt:**

> Add subtle developer CTAs to the main page and verify page:
> - A link “View API spec” pointing to `/docs/api` or the published OpenAPI, located near the Evidence Package / Verifier section.
> - A link “Integrate with your stack” pointing to a doc page (integrations, Phase 5 plan).
> - Keep them low-contrast but discoverable; they should not compete with the main demo CTA.

---

### 3.5 API surface & OpenAPI

#### 3.5.1 Task: Sync OpenAPI with Phase 4.5

**Goal:** Ensure `docs/api/openapi.yaml` fully reflects:

- Policy fields,
- Evidence Package endpoints,
- Any Phase 4/4.5 changes to verify / metrics APIs.

**LLM coding prompt (OpenAPI only):**

> Update `docs/api/openapi.yaml` so that it matches the current Phase 4.5 backend:
> - Include `policyCoverage` and `policyRulesEvaluated` fields in the schema of `GET /api/ai/verify/{id}`.
> - Document the Evidence Package download endpoint and the offline verifier endpoint, if present.
> - Ensure all new metrics endpoints (if any) are documented.
> - Add concise descriptions that explain how these fields relate to the demo policy and policy coverage.

**Tests:**

- Use an OpenAPI validator to ensure the YAML is syntactically correct.
- Optionally, use a mock client generator to verify that types line up.

---

### 3.6 Analytics & events

#### 3.6.1 Task: Minimal funnel tracking

**Goal:** Be able to tell a simple story:

> “X people saw the landing, Y ran the demo, Z downloaded Evidence/Verifier, K asked for API docs.”

For Phase 4.5, a minimal implementation is enough:

- Event `page_view` with page name.
- Event `cta_click` (demo).
- Event `download_evidence`.
- Event `download_verifier`.
- (Optional) Event `view_use_cases`.

**LLM coding prompt (frontend analytics layer):**

> In `frontend/lib/analytics.ts`, implement a minimal analytics interface:
> - A function `trackEvent(name: string, props?: Record<string, unknown>)`.
> - For Phase 4.5, log to console in dev and provide a hook to plug in a real provider later.
> - Ensure the following events are fired:
>   - `page_view` on main page, verify page, and `/use-cases`.
>   - `cta_click` when “Verify a response — Demo” is clicked.
>   - `download_evidence` and `download_verifier` from the Trust Panel.
>   - `view_use_cases` when the use-cases page is opened.
> - Do not hardcode any external analytics keys yet; keep it provider-agnostic.

**Docs:**

- Add a short section in `docs/PHASE4_ANALYTICS.md` explaining which events are tracked and why.

---

## 4. Testing strategy

For Phase 4.5 we want **high confidence** before Phase 5 work starts.

### 4.1 Automated tests

- **Backend:**
  - Policy evaluation unit tests:
    - R1–R4 evaluation with and without signature / timestamp / model.
    - Correct `policy_coverage` value (e.g. 2/4 = 0.5).
  - Verify API tests:
    - Response includes `policyCoverage`, `policyRulesEvaluated`, `policyId`, `policyVersion`.
  - Evidence Package tests:
    - `metadata.json` includes policy fields.

- **Frontend:**
  - Basic component tests for Policy coverage block (rendered vs hidden).
  - Test for “Why is confidence not 100%?” toggle.

### 4.2 Manual / exploratory

Use the [Manual test checklist (4.2)](../MANUAL_TEST_CHECKLIST_4_2.md) to run and sign off.

- End-to-end demo flow:
  - Open main page → run demo → see Trust summary, Policy coverage, “Why not 100%?”.
  - Follow “Full verification page” → check verify UI.
- Evidence Package:
  - Download evidence.
  - Run verifier locally with `.aep` file.
- Use cases:
  - Check `/use-cases` from both:
    - hero link “Explore use cases →`,
    - footer link.

---

## 5. Deliverables summary

When this transition phase is complete, we should have:

- **Hard, minimal policy engine**:
  - Stable demo policy file,
  - coverage + rule results saved and exposed consistently.
- **Clear policy narrative**:
  - UI and docs both explain what is checked, what is not, and why confidence < 100%.
- **Default integration target**:
  - One main story: “Aletheia signs external AI outputs for regulated workflows.”
- **Investor-friendly UI**:
  - Hero + Trust summary that clearly show the product’s value.
- **Developer-friendly hooks**:
  - Links to API/OpenAPI and integrations docs directly from the demo.
- **Analytics**:
  - Minimal visibility into the funnel from landing to demo and downloads.

This ensures that **Phase 5** builds on a **solid, meaningful core**, rather than turning into an “API without a story”.

