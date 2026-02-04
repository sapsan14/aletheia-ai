# Creating and Updating Policies — and Human Verification

This document describes how to create and update policies in Aletheia, how **both** policy types (Coverage-policy and Claim-policy) are used, and how **human verification** can be implemented. It complements [POLICY_LIFECYCLE_DESIGN.md](POLICY_LIFECYCLE_DESIGN.md) and [aletheia-demo-2026-01.md](aletheia-demo-2026-01.md).

**Related:** [PHASE4_5_TRANSITION.md](../PHASE4_5_TRANSITION.md) §2.1 (policy naming) · [TRUST_MODEL.md](../TRUST_MODEL.md)

---

## Table of contents

- [The two policy types](#the-two-policy-types)
- [Coverage-policy: creating and updating](#coverage-policy-creating-and-updating)
- [Claim-policy: how it is used](#claim-policy-how-it-is-used)
- [Human verification: design and implementation](#human-verification-design-and-implementation)
- [Summary](#summary)

---

## The two policy types

Aletheia uses two distinct policy concepts. Use the same names in UI, API, and docs.

| Term | Meaning | Who defines it | Where it appears |
|------|--------|----------------|-------------------|
| **Coverage-policy** | The versioned set of rules that define **what Aletheia checks** about an AI output (signature, timestamp, model identity, content, human review, etc.) and how coverage is reported. | Aletheia (operator / config). Stored in policy files and evaluated by the backend. | Verify API, Evidence Package metadata, Verify UI (“Coverage-policy (demo)” block), Trust Panel. |
| **Claim-policy** | The policy or regulatory framework **the AI used when forming the claim** (e.g. AI-ACT-2024, GDPR). Identifies which rules or standards the model claims to have applied. | The AI / client. Sent in the request as `policyVersion` (or equivalent) and stored as part of the AI claim. | Stored on each response; shown in the “AI Claim” block as “Claim-policy:”. |

- **Coverage-policy** answers: “What did Aletheia actually check for this response?”
- **Claim-policy** answers: “Which standard or policy did the AI say it was following when it made this claim?”

Both can appear on the same response: for example, Coverage-policy = `aletheia-demo (2026-01)` and Claim-policy = `AI-ACT-2024`.

---

## Coverage-policy: creating and updating

### 1. Policy definition (files and format)

Coverage-policies are defined in **versioned files**. The reference format is JSON plus an optional Markdown description.

**Location (current):** `docs/en/policy/` (e.g. `aletheia-demo-2026-01.json`, `aletheia-demo-2026-01.md`). Phase 5 may add an internal registry (DB or config service) that maps `(policy_id, policy_version)` to the rule set.

**JSON structure (example):**

```json
{
  "policy_id": "aletheia-demo",
  "policy_version": "2026-01",
  "name": "Aletheia Demo Policy",
  "description": "Short description of what this policy checks.",
  "rules": [
    {
      "id": "R1",
      "description": "Response is signed and timestamped",
      "type": "technical",
      "check": "automated",
      "required": true
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

- **policy_id** — Stable identifier (e.g. `aletheia-demo`, `hr-compliance`).
- **policy_version** — Version string (e.g. `2026-01`). New versions = new files or new registry entries; **never edit** an existing version so historical evidence stays interpretable.
- **rules** — List of rules. Each rule has:
  - **id** — Stable rule ID (e.g. `R1`, `R2`, `R4`).
  - **description** — Human-readable description (used in UI and docs).
  - **type** — `technical` | `content` | `human` (for grouping and future tooling).
  - **check** — `automated` (evaluated by backend) | `not_evaluated` (not run in current phase) | later possibly `fail` when a check runs and fails.
  - **required** — Whether the rule is required for compliance interpretation (affects coverage semantics if needed).

### 2. Creating a new policy or new version

1. **New policy:** Add a new JSON file (and optional `.md`) with a new `policy_id` (e.g. `hr-policy-2026-01.json`). Document purpose and rules in the `.md` file.
2. **New version of an existing policy:** Add a new file with the same `policy_id` and a **new** `policy_version` (e.g. `aletheia-demo-2026-02.json`). Do **not** change the content of `aletheia-demo-2026-01.json`; old evidence must remain tied to the old version.
3. **Backend:** Ensure the evaluation service (e.g. `PolicyEvaluationService`) can load and evaluate the new policy. In Phase 4.5 only the demo policy is wired; Phase 5 can support multiple policies via [POLICY_LIFECYCLE_DESIGN.md](POLICY_LIFECYCLE_DESIGN.md) (environment default, per-API-key, per-request allowlist).
4. **Rollout:** Configure the environment default (or per-key override) to use the new policy/version for **new** requests only. Existing records keep their stored `policy_id` and `policy_version`.

### 3. Updating rules within a version (do not do)

Do **not** change the rule set or semantics of an existing `(policy_id, policy_version)` after any response has been stored with that version. Otherwise verification and audit would become ambiguous. To change behaviour, introduce a **new** `policy_version` (or new `policy_id`) and point new traffic to it.

### 4. Deprecation

Old versions can be marked as **deprecated** in documentation or in a registry (no new assignments). They remain valid for interpreting historical evidence; verify API and Evidence Package always expose the stored `policy_id` and `policy_version` for each response.

---

## Claim-policy: how it is used

- **Source:** The Claim-policy is provided by the **client** (or the AI pipeline) when submitting the request. It is typically a short identifier such as `AI-ACT-2024`, `GDPR`, or `internal-guidelines-2025`. It is stored in the `policyVersion` field of the AI claim (on the `AiResponse` entity) and is **not** selected or validated by Aletheia beyond storage and display.
- **Meaning:** It denotes which policy or standard the AI **claims** to have applied when producing the claim and confidence. Aletheia does not verify that the AI actually followed that policy; it only records and attests that this claim (including Claim-policy) was made and signed.
- **Use in product:** Shown in the Verify UI under “AI Claim” as “Claim-policy:”. Included in the signed payload when the claim is present, so it cannot be altered without breaking verification. Useful for auditors to see “this output was produced under the claim of following X”.
- **Creating/updating:** There is no Aletheia “Claim-policy file”. Values are chosen by the integration (e.g. model card, prompt, or client config). To support a new Claim-policy label, the client sends the new string in the request; no change is required in Aletheia policy files.

---

## Human verification: design and implementation

The demo Coverage-policy defines **R4: “Human review performed”** as a rule of type `human`, currently marked `not_evaluated` in Phase 4. This section describes how human verification can be implemented so that R4 can eventually be set to `pass` or `fail` when a human has (or has not) reviewed the response.

### 1. Goal

- Record **whether** and **when** a human reviewed a given AI response (and optionally **who**).
- Feed that into Coverage-policy evaluation so that R4 status is `pass` when review is recorded, and `not_evaluated` or `fail` when it is not (depending on policy semantics).
- Expose human-review status in the Verify API and Evidence Package so that auditors and the UI can see it.

### 2. Data model options

**Option A — Fields on the same record (simple):**

- Add to `AiResponse` (or equivalent):
  - `human_reviewed_at` — timestamp (UTC) when the review was recorded (null = not reviewed).
  - Optionally: `human_reviewer_id` or `human_reviewer_name` — identifier of the reviewer (for audit; can be hashed or role-based if needed for privacy).

When `human_reviewed_at` is non-null, the policy evaluator can set R4 to `pass`; otherwise R4 stays `not_evaluated` or `fail` (if the policy requires human review).

**Option B — Separate “human review” table (audit-heavy):**

- New entity, e.g. `HumanReview`, with: `response_id` (FK to `AiResponse`), `reviewed_at`, `reviewer_id`/`reviewer_name`, optional `outcome` (e.g. approved / rejected / amended) and comment.
- One-to-one or one-to-many per response, depending on whether you allow multiple review events. Policy evaluation then checks for the existence of at least one review record (and optionally outcome) for the given response.

**Recommendation for Phase 5:** Start with Option A (minimal schema change, single source of truth per response). Move to Option B if you need full audit history (multiple reviews, outcomes, comments).

### 3. Recording human review

- **API:** Add an endpoint, e.g. `POST /api/ai/review/:id` or `PATCH /api/ai/verify/:id/review`, that accepts the response id and optionally reviewer identifier. The backend sets `human_reviewed_at` (and optionally `human_reviewer_id`) on the record. Optionally enforce idempotency (e.g. only allow one “review recorded” event per response).
- **Auth:** Protect the endpoint so only authorised roles (e.g. reviewers, admins) can record a review. Use the same API key or tenant model as the rest of the API.
- **UI:** On the Verify page (or in an internal dashboard), add a “Mark as human-reviewed” (or “Record review”) button that calls this API. After success, the same page can be refreshed or the Trust summary can show “Human review: ✓ performed at &lt;time&gt;”.
- **Integration:** External workflow systems (HR tools, ticketing, compliance dashboards) can call the same API when a human completes review in their system, so Aletheia stays the single place where “signed + policy coverage + human review” are attested together.

### 4. Policy evaluation (R4)

- In `PolicyEvaluationService` (or equivalent), when evaluating a response:
  - If the policy includes a rule like R4 “Human review performed”:
    - If the response has `human_reviewed_at != null` (or has a linked review record in Option B): set R4 status to `pass`.
    - Else: set R4 to `not_evaluated` (Phase 4 style) or `fail` if the policy marks R4 as required and you want to reflect “required but missing”.
- Store the updated rule list and coverage in the same way as today (e.g. `policy_rules_evaluated` JSON and `policy_coverage`). If human review is recorded **after** the response was first saved, you have two options:
  - **Re-evaluate on read:** When building the verify API response (and Evidence Package), re-run policy evaluation so that R4 and coverage reflect the current `human_reviewed_at` state. Stored `policy_rules_evaluated` might be “at sign time”; the API can return “current” evaluation. Document which semantics you use.
  - **Re-evaluate on review:** When `POST /api/ai/review/:id` is called, re-run policy evaluation and **update** the stored `policy_rules_evaluated` and `policy_coverage` on the record. Then verify API and Evidence Package can remain “as stored” and still show R4 = pass. This keeps a single evaluation state per response and is often simpler for auditing.

### 5. Evidence Package and Verify API

- **Verify API:** Include a field such as `humanReviewedAt` (ISO-8601 or Unix timestamp) and optionally `humanReviewerId`. Include the evaluated R4 status in `policyRulesEvaluated` so the UI can show “Human review: ✓” or “Not performed”.
- **Evidence Package (metadata.json):** Add `human_reviewed_at` (and optionally `human_reviewer_id`) and ensure `policy_rules_evaluated` contains the up-to-date R4 status so offline verifiers and auditors can interpret the evidence.

### 6. Summary: human verification

| Aspect | Recommendation |
|--------|----------------|
| Data model | Add `human_reviewed_at` (and optionally `human_reviewer_id`) on the response record; optional separate `HumanReview` table later if full history is needed. |
| Recording | `POST /api/ai/review/:id` (or PATCH) with auth; UI “Mark as reviewed” and/or integration from external systems. |
| Policy (R4) | Evaluator sets R4 to `pass` when `human_reviewed_at` is set; otherwise `not_evaluated` or `fail` per policy. |
| When to evaluate | Re-evaluate policy when review is recorded and update stored `policy_rules_evaluated` and `policy_coverage` so verify and Evidence Package stay consistent. |
| Exposure | Verify API and Evidence Package expose human-review timestamp (and reviewer if present) and R4 status. |

---

## Summary

| Topic | Coverage-policy | Claim-policy | Human verification |
|-------|-----------------|-------------|---------------------|
| **Who defines** | Aletheia (files / registry) | Client / AI (per request) | Operator + API to record review |
| **Creating/updating** | New JSON (+ .md) with new version; never edit existing version | Client sends new string; no Aletheia file | Add `human_reviewed_at` (and optional reviewer); API to set it |
| **Where used** | Backend evaluation, Verify API, Evidence Package, UI “Coverage-policy” | Stored on response; UI “Claim-policy” in AI Claim block | R4 in Coverage-policy; Verify API and Evidence Package |
| **Human verification** | R4 “Human review performed” in demo policy; implement by storing review time (and reviewer), re-evaluating policy, and exposing in API and Evidence Package. | N/A | See [Human verification: design and implementation](#human-verification-design-and-implementation) above. |

For policy lifecycle (selection, rollout, deprecation), see [POLICY_LIFECYCLE_DESIGN.md](POLICY_LIFECYCLE_DESIGN.md). For the current demo policy rules and usage, see [aletheia-demo-2026-01.md](aletheia-demo-2026-01.md).
