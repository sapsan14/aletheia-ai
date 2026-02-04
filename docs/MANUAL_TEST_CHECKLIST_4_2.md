# Manual / exploratory testing — Phase 4.2

Checklist for **§4.2** of [PHASE4_5_TRANSITION](en/PHASE4_5_TRANSITION.md). Run with backend + frontend up; tick when done.

**Prerequisites:** Backend (`mvn spring-boot:run`), frontend (`npm run dev`), signing key + TSA in backend `.env`. See [DEMO_SCRIPT](DEMO_SCRIPT.md#prerequisites).

---

## 1. End-to-end demo flow

| # | Step | Expected | ☐ |
|---|------|----------|---|
| 1.1 | Open main page (e.g. http://localhost:3000) | Page loads, prompt field visible | |
| 1.2 | Run demo: enter a prompt (e.g. GDPR clause question), click **Send & Verify** | Response appears; Trust summary visible | |
| 1.3 | On main page after response: check **Trust summary** | Signed, Timestamped, Policy %, Claim, Offline badges or labels present | |
| 1.4 | On main page: check **Policy coverage** block | "Coverage-policy (demo)" and percentage (e.g. 50%) shown | |
| 1.5 | Click **"Why is confidence not 100%?"** | Explanation expands (what is / isn’t checked) | |
| 1.6 | Follow **"Full verification page"** (or verify link) | Verify page opens with same record | |
| 1.7 | On verify page: check Trust summary, Policy coverage, "Why not 100%?" | Same content as on main, consistent | |

---

## 2. Evidence Package

| # | Step | Expected | ☐ |
|---|------|----------|---|
| 2.1 | From verify page (or main with id): click **Download evidence** | `.aep` file downloads | |
| 2.2 | Run verifier locally: `java -jar aletheia-verifier.jar /path/to/<file>.aep` | Verifier runs and reports **VALID** (and timestamp if TSA used) | |
| 2.3 | (Optional) Unzip `.aep`, open `metadata.json` | Contains `policy_coverage`, `policy_rules_evaluated`, `policy_version` | |

---

## 3. Use cases page

| # | Step | Expected | ☐ |
|---|------|----------|---|
| 3.1 | From **hero**: click "Explore use cases →" (or equivalent) | Navigates to `/use-cases` | |
| 3.2 | From **footer**: click use-cases link | Navigates to `/use-cases` | |
| 3.3 | On use-cases page: "← Back to home" / "Try demo" | Return to home (state preserved if from demo) | |

---

## Automated API run (optional)

The following were verified via API (backend + frontend running):

- **POST /api/ai/ask** — returns `id`, `response`, `responseHash`, `signature`; LLM response received.
- **GET /api/ai/verify/:id** — returns `policyCoverage` (e.g. 0.5), `policyRulesEvaluated` (4 rules), `policyVersion` (e.g. gdpr-2024).
- **GET /api/ai/evidence/:id?format=json** — `metadata.json` (base64) contains `policy_coverage`, `policy_rules_evaluated`.

UI steps (1.1–1.7, 2.1–2.2, 3.1–3.3) require manual run in browser (MCP snapshot does not expose element refs for click/fill).

---

## Sign-off

- **Date:** _______________
- **Tester:** _______________
- **Notes:** _______________________________________________
