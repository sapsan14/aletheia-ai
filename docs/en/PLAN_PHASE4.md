# Aletheia AI — Plan Phase 4: Market Validation & Policy Foundation (2026)

This document describes **Phase 4** of the roadmap: market validation (landing, one scenario, outreach, pilots) plus a minimal policy foundation (demo policy, policy coverage, UI “Why not 100%?”). It is based on the completed Phase 2 and Phase 3 and the chosen direction from [NEXT.md](../tmp/NEXT.md): **B as base, A.1–A.3 as strengthening, C later** when there are requests.

**Status:** Draft for review  
**Related:** [Vision & roadmap](VISION_AND_ROADMAP.md) · [Plan Phase 2](PLAN_PHASE2.md) · [Plan Phase 3 UI](PLAN_PHASE3_UI.md) · [NEXT.md](../tmp/NEXT.md)

---

## Table of contents

- [Phase 4 goal and scope](#phase-4-goal-and-scope)
- [Deliverables and development steps](#deliverables-and-development-steps)
- [Out of scope (Phase 5+)](#out-of-scope-phase-5)
- [Completion criteria](#completion-criteria)
- [Timeline](#timeline)
- [Risks and mitigation](#risks-and-mitigation)
- [References](#references)

---

## Phase 4 goal and scope

**Goal:** Create a minimal **policy-transparency** and **market-packaging** layer so that we can:

- run first pilots (HR, legal/compliance, corporate governance),
- demonstrate product maturity (policy, coverage, confidence reasoning),
- collect feedback from real users,
- prepare the ground for an API platform (Phase 5).

Phase 4 is **market validation + policy foundation**, without heavy engineering.

**In scope:**

- One canonical demo policy (A.1).
- Policy coverage in backend, API, Evidence Package, and UI (A.2).
- UI: policy coverage block and “Why is confidence not 100%?” (A.3).
- Landing: hero + CTA (B.1).
- One killer scenario (text + video) (B.3).
- Use cases page (B.4).
- Outreach and pilots (B.5).
- Minimal analytics (B.6).

**Out of scope for Phase 4:** Full Policy Registry (A.5), Policy Evaluation Pipeline (A.6), time-travel verify (A.7), human/hybrid review (A.8), public API/OpenAPI (C.1), sign-only API (C.2), SDK (C.3), MCP attestation (C.4), SIEM/blockchain integrations (C.5), partner scenarios (C.6).

---

## Deliverables and development steps

Each deliverable is broken down into tasks with **LLM-readable coding prompts** and acceptance criteria.

---

### 2.1 Demo policy (A.1)

**Goal:** One canonical policy file and documentation so that all demos and pilots use the same rule set.

**Deliverables:**

- File `docs/en/policy/aletheia-demo-2026-01.json` (or `.yaml`) defining the demo policy.
- 3–4 rules: R1 (signature + timestamp), R2 (model_id recorded), R3 (no prohibited domains — e.g. medical/legal advice) as `not_evaluated`, R4 (human review) as `not_evaluated`.
- Documentation: `docs/en/policy/aletheia-demo-2026-01.md`.

#### Task P4.1.1 — Define demo policy file format and content

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Create the canonical demo policy file and document its structure. |

**Coding prompt (LLM-readable):**

- Create directory `docs/en/policy/` if it does not exist.
- Create file `docs/en/policy/aletheia-demo-2026-01.json` with the following structure (adapt keys to your preferred style; this is a minimal example):

```json
{
  "policy_id": "aletheia-demo",
  "policy_version": "2026-01",
  "name": "Aletheia Demo Policy",
  "description": "Minimal policy for Phase 4 demos and pilots. Defines what is checked and what is not.",
  "rules": [
    {
      "id": "R1",
      "description": "Response is signed and timestamped",
      "type": "technical",
      "check": "automated",
      "required": true
    },
    {
      "id": "R2",
      "description": "Model identity (model_id) is recorded",
      "type": "technical",
      "check": "automated",
      "required": true
    },
    {
      "id": "R3",
      "description": "No medical or legal advice in response",
      "type": "content",
      "check": "not_evaluated",
      "required": false
    },
    {
      "id": "R4",
      "description": "Human review performed",
      "type": "human",
      "check": "not_evaluated",
      "required": false
    }
  ]
}
```

- Ensure the file is valid JSON. Rule IDs (R1–R4) must be stable; they will be used in backend and UI.
- Create file `docs/en/policy/aletheia-demo-2026-01.md` that describes: (1) purpose of this policy; (2) what each rule means; (3) that R3 and R4 are `not_evaluated` in Phase 4 and why (honest transparency). Use short paragraphs and a table listing rule id, description, check status.
- In the backend, ensure `ComplianceInferenceService` (or equivalent) can later resolve policy_version (e.g. "gdpr-2024", "compliance-2024") to this same policy for coverage calculation. For Phase 4, you may keep existing policy_version values in the DB; the single demo policy file defines the rule set used for coverage.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | Policy file | JSON is valid; contains policy_id, policy_version, rules with ids R1–R4. |
| Doc | Policy doc | Markdown describes each rule and states R3/R4 are not_evaluated in Phase 4. |

---

### 2.2 Policy coverage (A.2)

**Goal:** Compute and store policy coverage per response; expose it in the verify API and in the Evidence Package.

**Deliverables:**

- Backend: compute `policy_coverage = evaluated / total_rules` and per-rule status (pass / not_evaluated) using the demo policy.
- Persist coverage (and optionally per-rule results) in the database.
- Add to GET `/api/ai/verify/:id` response and to Evidence Package metadata.

#### Task P4.2.1 — Backend: policy coverage model and storage

| Field | Value |
|-------|--------|
| **Est.** | 2–3 h |
| **Description** | Add data model and persistence for policy coverage and rule evaluation results. |

**Coding prompt (LLM-readable):**

- Define a small model for policy evaluation result: e.g. `PolicyEvaluationResult` or fields on an existing entity. It must include: `policyId`, `policyVersion`, `coverage` (double, 0.0–1.0), and a list or JSON of rule results, each with `ruleId` (e.g. "R1"), `status` ("pass" or "not_evaluated"). Option A: add columns to `ai_response` (e.g. `policy_coverage` DOUBLE, `policy_rules_evaluated` JSON or TEXT). Option B: separate table `policy_evaluation` with `response_id` FK. Prefer Option A for Phase 4 simplicity.
- Add a service or helper that, given an `AiResponse` (with signature, tsaToken, llmModel set), evaluates rules from the demo policy (aletheia-demo-2026-01): R1 pass if signature non-null and tsaToken non-null; R2 pass if llmModel non-null and non-empty; R3 and R4 not_evaluated. Compute `coverage = (number of rules with status pass) / (total rules)` or `(number of rules evaluated) / (total rules)` — document which formula you use. For Phase 4, "evaluated" means either pass or explicitly not_evaluated; total_rules = 4. Result: coverage in [0, 1] and list of { ruleId, status }.
- When saving a new AI response (in the same transaction or immediately after), compute this evaluation and persist coverage and rule results. If using existing `AiResponse`, add columns and set them in `AuditRecordService` or the controller that saves the response.
- Add a Flyway/Liquibase migration: add column `policy_coverage` (DOUBLE, nullable) and `policy_rules_evaluated` (TEXT/JSON, nullable) to `ai_response` if not present. Document migration in README.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Unit | Coverage calculation | For a response with signature, tsaToken, llmModel set: R1 pass, R2 pass, R3 not_evaluated, R4 not_evaluated; coverage = 0.5 (2/4) or 2/4 evaluated as pass. |
| Integration | Save response | After POST /api/ai/ask, loaded entity has policy_coverage and policy_rules_evaluated set. |

#### Task P4.2.2 — Expose policy coverage in verify API and Evidence Package

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Add policy_coverage and rule results to GET /api/ai/verify/:id and to metadata.json in Evidence Package. |

**Coding prompt (LLM-readable):**

- In the DTO or response map for GET `/api/ai/verify/:id`, add fields: `policyCoverage` (Double, nullable) and `policyRulesEvaluated` (list of objects with ruleId and status, or JSON string). Populate from the stored entity. Ensure the frontend and any existing clients can ignore these if null.
- In `EvidencePackageServiceImpl` (or equivalent), when building metadata for the Evidence Package, add to metadata.json: `policy_coverage` (number) and `policy_rules_evaluated` (array of { "ruleId": "R1", "status": "pass" } etc.). Use the same values as stored on the response entity. If policy_coverage is null, omit or set to null in JSON.
- Update the verifier or Evidence Package documentation to mention that metadata may include policy_coverage and policy_rules_evaluated for Phase 4+.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Integration | GET /api/ai/verify/:id | Response JSON includes policyCoverage and policyRulesEvaluated when present. |
| Unit/Manual | Evidence Package | metadata.json contains policy_coverage and policy_rules_evaluated when present. |

---

### 2.3 UI: Policy coverage and “Why is confidence not 100%?” (A.3)

**Goal:** On the verify page, show policy coverage and an expandable explanation of why confidence is not 100%.

**Deliverables:**

- New block on `/verify`: “Policy coverage: X% — N of M rules checked” and a list of rules with pass / not_evaluated.
- Button or link “Why is confidence not 100%?” that expands to a short explanation (which checks were done, which were not).

#### Task P4.3.1 — Policy coverage block on verify page

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Add a “Policy coverage” section to the verify page. |

**Coding prompt (LLM-readable):**

- On the verify page (`frontend/app/verify/page.tsx` or a dedicated component), add a **Policy coverage** block. It must be visible when the API returns `policyCoverage` and/or `policyRulesEvaluated`.
- Display: (1) Heading “Policy coverage” or “Policy coverage (demo)”. (2) A short line: “X% — N of M rules checked” (e.g. “50% — 2 of 4 rules checked”). Use `record.policyCoverage` (e.g. 0.5 → 50%) and length of `record.policyRulesEvaluated` for N, total rules M = 4 for demo policy. (3) A list of rules: for each item in `record.policyRulesEvaluated`, show rule id (e.g. R1) and status (pass → green/check, not_evaluated → grey or “Not checked”). If you have rule descriptions from the policy file, you can show them; otherwise rule id + status is enough.
- If `policyCoverage` is null/undefined, do not render this block (or show “Policy coverage not available”).
- Use existing styling (e.g. card, spacing) to match Trust Summary and AI Claim blocks. Add a tooltip to the heading: “Share of policy rules that were evaluated for this response.”

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | Verify page with coverage | Block shows percentage and list of rules with status. |
| Manual | Verify page without coverage | Block hidden or “not available” when policyCoverage is null. |

#### Task P4.3.2 — “Why is confidence not 100%?” expandable

| Field | Value |
|-------|--------|
| **Est.** | 1 h |
| **Description** | Add a button or link that expands to explain why confidence is below 100%. |

**Coding prompt (LLM-readable):**

- On the verify page, add a button or link labelled “Why is confidence not 100%?” (or “Why not 100%?”). Place it near the AI Claim block (e.g. under the confidence value) or in the Policy coverage block.
- On click, expand an inline section (or open a small modal) that explains in plain language: (1) Confidence reflects how many of the declared policy checks were performed. (2) List which rules were checked (e.g. R1: signature and timestamp; R2: model recorded) and which were not (e.g. R3: content check not run; R4: human review not performed). Use data from `record.policyRulesEvaluated` if available; otherwise use static text for the demo policy. (3) One sentence: “We do not certify truth; we show what was checked.”
- Use the same TOOLTIPS or copy approach as in Phase 3; avoid algorithm jargon. Ensure the expanded content is accessible (e.g. focus, aria-expanded).

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | Click “Why not 100%?” | Explanation expands and mentions which checks were done and which were not. |

---

### 2.4 Landing: Hero and CTA (B.1)

**Goal:** A clear first screen for the product: one question, one tagline, one CTA.

**Deliverables:**

- Hero section: headline “AI said that. But under which rules?” (or equivalent); subline “We don’t certify truth. We certify responsibility.”; CTA button “Verify a response — Demo” (or “Try demo”) linking to the app (main or verify). Optional line under CTA: “No crypto wallet. Just transparency.”

#### Task P4.4.1 — Implement hero and CTA on main/landing page

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Add or update the hero block and primary CTA on the main page. |

**Coding prompt (LLM-readable):**

- On the main page (e.g. `frontend/app/page.tsx` or the root layout), ensure the first visible section (hero) contains: (1) Main headline: “AI said that. But under which rules?” (or the approved translation). (2) Subheadline: “We don’t certify truth. We certify responsibility.” (3) Primary button or link: “Verify a response — Demo” (or “Try demo”). On click, navigate to the app’s main flow (e.g. `/` if the main page is the demo, or `/verify` with a sample id, or keep current behaviour). (4) Optional line below the button: “No crypto wallet. Just transparency.” or “No wallet required. Pure transparency.”
- Use clear typography and spacing so the hero is readable on mobile and desktop. Prefer a minimal design; avoid extra sections for Phase 4. If the current main page already has a hero, replace or refine it to match the above copy.
- Ensure the CTA is focusable and has a visible focus state for accessibility.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | Main page | Hero shows headline, subline, CTA, and optional transparency line. |
| Manual | CTA click | User reaches the demo (main or verify) as intended. |

---

### 2.5 One killer scenario: text and video (B.3)

**Goal:** One repeatable story for pilots and investors: choose one vertical (HR or Legal/compliance), write a 1–2 page scenario, and record a 3–5 minute screencast.

**Deliverables:**

- Text scenario (markdown): steps from “user asks a question” to “auditor verifies offline”.
- Video (3–5 min): screencast of the same flow (question → response → Verify → Evidence Package → offline verifier).

#### Task P4.5.1 — Write scenario document

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Create a short scenario document for the chosen vertical. |

**Coding prompt (LLM-readable):**

- Create file `docs/DEMO_SCENARIO_PHASE4.md` (or add a section to `docs/DEMO_SCRIPT.md`). Title: “Phase 4 killer scenario” and the chosen vertical (e.g. “HR” or “Legal/compliance”).
- Content (1–2 pages): (1) Context: one paragraph on the pain (e.g. “Candidate was rejected; AI was involved. Company must show which criteria and which policy applied.”). (2) Steps: numbered list. Step 1: Operator opens the app and enters a prompt (provide example prompt text, e.g. a short clause + “Does this comply with GDPR?”). Step 2: Backend returns signed response; operator sees Trust Summary and AI Claim. Step 3: Operator goes to Verify page (or stays on main) and downloads Evidence Package. Step 4: Auditor receives .aep (e.g. by email). Step 5: Auditor runs offline verifier (e.g. `java -jar aletheia-verifier.jar path/to/file.aep`) and sees VALID and timestamp. (3) Outcome: one sentence on what this proves (traceability, policy, time).
- Include prerequisites (backend running, key and TSA configured, demo policy in use). Keep language non-technical where possible so that sales or pilots can follow.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | Scenario file | Document has context, steps 1–5, outcome, and prerequisites. |

#### Task P4.5.2 — Record scenario video

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Record a 3–5 minute screencast of the scenario. |

**Coding prompt (LLM-readable):**

- Record a screencast (e.g. OBS, Loom, or built-in OS tool) that follows the steps in `docs/DEMO_SCENARIO_PHASE4.md`: (1) Open app, enter example prompt, submit. (2) Show response, Trust Summary, AI Claim, and (if implemented) Policy coverage. (3) Navigate to Verify (or use main page), click Download evidence, save .aep. (4) Open terminal, run verifier on the downloaded file, show VALID and timestamp. Total duration 3–5 minutes. Add short voiceover or captions if helpful (e.g. “We’re downloading the evidence package so an auditor can verify without our server.”).
- Save the video in a location that can be linked from the repo or landing (e.g. `docs/static/` or `public/` with a clear name like `aletheia-demo-phase4.mp4`, or upload to YouTube/Vimeo and store the link in docs).
- In `docs/DEMO_SCENARIO_PHASE4.md`, add a line: “Video: [link to video].” so that pilots and investors can watch the flow.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | Video | Video exists, 3–5 min, shows full flow from prompt to offline verification. |
| Doc | Scenario doc | Scenario document includes link to video. |

---

### 2.6 Use cases page (B.4)

**Goal:** A “For whom” or “Use cases” page that lists HR, Legal/compliance, Customer support, Education, Corporate governance with short pain + solution text.

**Deliverables:**

- New page or section: “Use cases” or “For whom”.
- For each segment: 2–3 sentences on the pain and how Aletheia helps (fix wording, policy, confidence, offline proof).

#### Task P4.6.1 — Create use cases content and page

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Add a use cases page or section with five segments. |

**Coding prompt (LLM-readable):**

- Create a new page route, e.g. `frontend/app/use-cases/page.tsx` (or add a section on the main page). Title: “Use cases” or “For whom” (or “Who is this for”).
- Add five blocks (cards or sections), one per segment: (1) **HR** — Pain: e.g. “Candidates complain about AI-driven decisions; you need to show which criteria and policy applied.” Solution: “Aletheia fixes the exact wording, policy version, and confidence so you can prove due diligence.” (2) **Legal / compliance** — Pain: “Contracts or clauses are checked by AI; auditors need proof of what was stated and when.” Solution: “Signed response + Evidence Package verifiable offline.” (3) **Customer support** — Pain: “AI answers in official channels; disputes require proof of what was said.” Solution: “We fix the message, policy, and time so you can show what was communicated.” (4) **Education** — Pain: “Students use AI; institutions need to document how AI was used and what was guaranteed.” Solution: “Policy and confidence show what was checked (e.g. no guarantee of correctness).” (5) **Corporate governance** — Pain: “Decisions are supported by AI; internal audit needs traceability.” Solution: “Evidence Package and policy coverage provide an audit trail.”
- Use short paragraphs (2–3 sentences per block). Add a link back to the main page or CTA “Try demo” at the bottom. Ensure the page is linked from the main navigation or footer (e.g. “Use cases” link).

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | Use cases page | Five segments present with pain + solution. Page linked from main or footer. |

---

### 2.7 Outreach and pilots (B.5)

**Goal:** List 10–20 target companies/contacts; prepare a short email template and link to demo and video; aim for 3–5 calls and at least 1 LOI.

**Deliverables:**

- List of 10–20 companies or contacts (legal tech, HR platforms, compliance teams).
- Email template (subject + body) with link to demo and video.
- Execution: send emails, track responses, and report 3–5 calls and 1 LOI (or document outcome).

#### Task P4.7.1 — Outreach list and email template

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Create outreach list and reusable email template. |

**Coding prompt (LLM-readable):**

- Create file `docs/outreach/PHASE4_OUTREACH.md` (or similar). Section 1: “Target list”. Add 10–20 entries: company or role name, contact type (e.g. “Compliance lead”, “HR tech”), and source (e.g. “LinkedIn”, “website”). Use placeholder names if needed (e.g. “Legal tech company A”) so the structure is clear. Section 2: “Email template”. Subject line: short and value-focused (e.g. “Proof for AI-generated decisions — Aletheia demo”). Body: 3–4 short paragraphs. (1) One sentence on the problem (e.g. “When AI is involved in decisions, proving what was said and under which rules is hard.”). (2) One sentence on Aletheia (e.g. “We provide cryptographically verifiable evidence: signed responses, policy, and offline verification.”). (3) Invitation: “I’d like to show you a 5-minute demo and get your feedback.” (4) CTA: link to the live demo (URL) and link to the scenario video (URL). Sign-off. Keep tone professional and concise.
- Document in the same file or in NEXT.md: “Outreach goal: 3–5 calls, 1 LOI. Update this file with sent date and response status for each contact (optional).”

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Doc | Outreach doc | File has target list (10–20) and email template with demo + video links. |

---

### 2.8 Minimal analytics (B.6)

**Goal:** Simple counters: landing visits, CTA clicks, transitions to demo, Evidence Package downloads. Optional simple dashboard.

**Deliverables:**

- Instrumentation: track landing page views, CTA clicks, demo page views, “Download evidence” clicks.
- Optional: simple dashboard or export (e.g. CSV) to review conversion.

#### Task P4.8.1 — Add minimal tracking for Phase 4 metrics

| Field | Value |
|-------|--------|
| **Est.** | 1–2 h |
| **Description** | Add client-side or server-side tracking for key events. |

**Coding prompt (LLM-readable):**

- Choose a minimal approach: (A) Client-side: add a small script or use an existing analytics SDK (e.g. Google Analytics, Plausible, or a custom endpoint) to send events: “landing_view”, “cta_click”, “demo_view” (e.g. main page or verify page), “download_evidence_click”. (B) Server-side: in the backend, log or store counts for “GET /” (or landing), “GET /verify”, “GET /api/ai/evidence/:id” (download). Prefer (A) if you already have a privacy-friendly analytics tool; otherwise (B) with simple log aggregation or a single “metrics” table.
- Document in README or `docs/PHASE4_ANALYTICS.md`: which events are tracked, where data is stored, and how to view counts (e.g. “See dashboard at …” or “Check logs for …”). Comply with privacy: if using third-party analytics, mention it in a privacy note or footer.
- Optional: create a simple admin or internal page that shows counts (e.g. last 7 days: landing views, CTA clicks, evidence downloads). If time is short, skip the dashboard and rely on raw logs or analytics provider’s UI.

**Acceptance criteria:**

| Test type | What to test | Acceptance criteria |
|-----------|--------------|---------------------|
| Manual | Trigger events | At least CTA click and download evidence are tracked (or logged). |
| Doc | Docs | Document describes what is tracked and where to see it. |

---

## Out of scope (Phase 5+)

The following are explicitly **out of scope** for Phase 4 and deferred to Phase 5 or later:

- Full Policy Registry (A.5)
- Policy Evaluation Pipeline (A.6)
- Time-travel verify (A.7)
- Human / hybrid review (A.8)
- Public API and OpenAPI (C.1)
- Sign-only API (C.2)
- SDK (C.3)
- MCP attestation (C.4)
- SIEM / blockchain integrations (C.5)
- Partner scenarios (C.6)

---

## Completion criteria

Phase 4 is complete when all of the following are true:

| # | Criterion | Status |
|---|-----------|--------|
| 1 | Demo policy is published and used by default | [ ] |
| 2 | Policy coverage is exposed in API, UI, and Evidence Package | [ ] |
| 3 | UI explains why confidence &lt; 100% | [ ] |
| 4 | Landing is updated (hero + CTA) | [ ] |
| 5 | One scenario document + video are ready | [ ] |
| 6 | Use cases page is published | [ ] |
| 7 | Outreach has been run (≥10 companies) | [ ] |
| 8 | ≥3 calls and ≥1 LOI (or documented outcome) | [ ] |
| 9 | Metrics are being collected | [ ] |

---

## Timeline

Suggested 4-week split:

| Week | Focus |
|------|--------|
| 1 | Demo policy (P4.1.1); policy coverage backend (P4.2.1, P4.2.2); update Evidence Package metadata |
| 2 | UI: policy coverage block (P4.3.1) and “Why not 100%?” (P4.3.2); landing hero (P4.4.1) |
| 3 | Scenario document (P4.5.1); scenario video (P4.5.2); use cases page (P4.6.1) |
| 4 | Outreach list and template (P4.7.1); analytics (P4.8.1); run outreach and review results |

---

## Risks and mitigation

| Risk | Mitigation |
|------|-------------|
| Too little feedback from pilots | Active outreach + one clear vertical scenario |
| Policy scope creep | Keep policy to 3–4 rules only |
| UI complexity | Minimal feature set; avoid visual overload |
| No LOI | Revisit scenario and positioning; iterate messaging |

---

## References

- [Vision & roadmap](VISION_AND_ROADMAP.md) — Phases 1–6; Phase 4 = market validation + policy foundation.
- [Plan Phase 2](PLAN_PHASE2.md) — Evidence Package, killer demo, AI Claim.
- [Plan Phase 3 UI](PLAN_PHASE3_UI.md) — Verify page wireframe and tooltips.
- [NEXT.md](../tmp/NEXT.md) — Direction choice: B as base, A.1–A.3 as strengthening, C later.

**Translations:** [RU](../ru/PLAN_PHASE4.md) · [ET](../et/PLAN_PHASE4.md)
