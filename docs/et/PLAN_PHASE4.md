# Aletheia AI — Plaan Phase 4: Turu valideerimine ja poliitika alus (2026)

Selles dokumendis kirjeldatakse **Phase 4** teekonda: turu valideerimine (landing, üks stsenaarium, outreach, piloodid) pluss minimaalne poliitika alus (demo-poliitika, policy coverage, UI „Miks mitte 100%?“). Põhineb lõpetatud Phase 2 ja Phase 3 ning [NEXT.md](../tmp/NEXT.md) valitud suunal: **B alusena, A.1–A.3 tugevdusena, C hiljem**, kui tekib päringuid.

**Staatus:** Mustand ülevaateks  
**Seotud:** [Visioon ja teekond](VISION_AND_ROADMAP.md) · [Plaan Phase 2](PLAN_PHASE2.md) · [Plaan Phase 3 UI](PLAN_PHASE3_UI.md) · [NEXT.md](../tmp/NEXT.md)

---

## Sisukord

- [Phase 4 eesmärk ja ulatus](#phase-4-eesmärk-ja-ulatus)
- [Tulemused ja arendussammud](#tulemused-ja-arendussammud)
- [Väljaspool ulatust (Phase 5+)](#väljaspool-ulatust-phase-5)
- [Lõpetamise kriteeriumid](#lõpetamise-kriteeriumid)
- [Ajajoon](#ajajoon)
- [Riskid ja leevendused](#riskid-ja-leevendused)
- [Viited](#viited)

---

## Phase 4 eesmärk ja ulatus

**Eesmärk:** Luua minimaalne **poliitika läbipaistvuse** ja **turu pakendamise** kiht, et:

- viia läbi esimesed piloodid (HR, legal/compliance, ettevõtte juhtimine),
- näidata toote küpsust (poliitika, coverage, confidence põhjendus),
- koguda tagasisidet tõelistelt kasutajatel,
- valmistada ette API platvormi alus (Phase 5).

Phase 4 on **turu valideerimine + poliitika alus**, ilma raske inseneritööta.

**Ulatuses:**

- Üks kanooniline demo-poliitika (A.1).
- Policy coverage backendis, API-s, Evidence Package'is ja UI-s (A.2).
- UI: policy coverage plokk ja „Miks confidence ei ole 100%?“ (A.3).
- Landing: hero + CTA (B.1).
- Üks killer-stsenaarium (tekst + video) (B.3).
- Use cases leht (B.4).
- Outreach ja piloodid (B.5).
- Minimaalne analüütika (B.6).

**Väljaspool Phase 4 ulatust:** Täielik Policy Registry (A.5), Policy Evaluation Pipeline (A.6), time-travel verify (A.7), human/hybrid review (A.8), avalik API/OpenAPI (C.1), sign-only API (C.2), SDK (C.3), MCP attestatsioon (C.4), SIEM/blockchain integratsioonid (C.5), partneri stsenaariumid (C.6).

---

## Tulemused ja arendussammud

Iga tulemus on jagatud ülesanneteks **LLM-loetavate koodipromptidega** ja vastuvõtu kriteeriumitega.

---

### 2.1 Demo-poliitika (A.1)

**Eesmärk:** Üks kanooniline poliitika fail ja dokumentatsioon, et kõik demod ja piloodid kasutaksid sama reeglikomplekti.

**Tulemused:**

- Fail `docs/en/policy/aletheia-demo-2026-01.json` (või `.yaml`) demo-poliitika definitsiooniga.
- 3–4 reeglit: R1 (allkiri + timestamp), R2 (model_id fikseeritud), R3 (keelatud valdkonnad puuduvad — nt med/juriidiline nõustamine) — `not_evaluated`, R4 (inimlik ülevaatus) — `not_evaluated`.
- Dokumentatsioon: `docs/en/policy/aletheia-demo-2026-01.md`.

#### Ülesanne P4.1.1 — Demo-poliitika faili vorming ja sisu

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1–2 h |
| **Kirjeldus** | Luua kanooniline demo-poliitika fail ja dokumenteerida selle struktuur. |

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

**Vastuvõtu kriteeriumid:**

| Tüüp | Mida testida | Kriteerium |
|------|--------------|------------|
| Doc | Poliitika fail | JSON kehtiv; sisaldab policy_id, policy_version, rules id-dega R1–R4. |
| Doc | Poliitika doc | Markdown kirjeldab iga reeglit ja märgib, et R3/R4 on Phase 4-s not_evaluated. |

---

### 2.2 Policy coverage (A.2)

**Eesmärk:** Arvutada ja salvestada policy coverage vastuse kohta; avaldada verify API-s ja Evidence Package'is.

**Tulemused:**

- Backend: arvutada `policy_coverage = evaluated / total_rules` ja reeglite staatus (pass / not_evaluated) demo-poliitika põhjal.
- Salvestada andmebaasi.
- Lisada GET `/api/ai/verify/:id` vastusesse ja Evidence Package metadata-sse.

#### Ülesanne P4.2.1 — Backend: policy coverage mudel ja salvestamine

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 2–3 h |
| **Kirjeldus** | Lisada andmemudel ja persisteerimine policy coverage ja reeglite hindamise tulemuste jaoks. |

**Coding prompt (LLM-readable):**

- Define a small model for policy evaluation result: e.g. `PolicyEvaluationResult` or fields on an existing entity. It must include: `policyId`, `policyVersion`, `coverage` (double, 0.0–1.0), and a list or JSON of rule results, each with `ruleId` (e.g. "R1"), `status` ("pass" or "not_evaluated"). Option A: add columns to `ai_response` (e.g. `policy_coverage` DOUBLE, `policy_rules_evaluated` JSON or TEXT). Option B: separate table `policy_evaluation` with `response_id` FK. Prefer Option A for Phase 4 simplicity.
- Add a service or helper that, given an `AiResponse` (with signature, tsaToken, llmModel set), evaluates rules from the demo policy (aletheia-demo-2026-01): R1 pass if signature non-null and tsaToken non-null; R2 pass if llmModel non-null and non-empty; R3 and R4 not_evaluated. Compute `coverage = (number of rules with status pass) / (total rules)` or `(number of rules evaluated) / (total rules)` — document which formula you use. For Phase 4, total_rules = 4. Result: coverage in [0, 1] and list of { ruleId, status }.
- When saving a new AI response, compute this evaluation and persist coverage and rule results. Add Flyway/Liquibase migration: add column `policy_coverage` (DOUBLE, nullable) and `policy_rules_evaluated` (TEXT/JSON, nullable) to `ai_response` if not present.

**Vastuvõtu kriteeriumid:**

| Tüüp | Mida testida | Kriteerium |
|------|--------------|------------|
| Unit | Coverage arvutus | Vastuse puhul signature, tsaToken, llmModel: R1 pass, R2 pass, R3/R4 not_evaluated; coverage = 0.5 (2/4). |
| Integratsioon | Vastuse salvestamine | Pärast POST /api/ai/ask on laaditud entiteedil policy_coverage ja policy_rules_evaluated seatud. |

#### Ülesanne P4.2.2 — Policy coverage avaldamine verify API-s ja Evidence Package'is

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1–2 h |
| **Kirjeldus** | Lisada policy_coverage ja reeglite tulemused GET /api/ai/verify/:id vastusesse ja metadata.json Evidence Package'is. |

**Coding prompt (LLM-readable):**

- In the DTO or response map for GET `/api/ai/verify/:id`, add fields: `policyCoverage` (Double, nullable) and `policyRulesEvaluated` (list of objects with ruleId and status, or JSON string). Populate from the stored entity.
- In `EvidencePackageServiceImpl` (or equivalent), when building metadata for the Evidence Package, add to metadata.json: `policy_coverage` (number) and `policy_rules_evaluated` (array of { "ruleId": "R1", "status": "pass" } etc.). Use the same values as stored on the response entity.
- Update the verifier or Evidence Package documentation to mention that metadata may include policy_coverage and policy_rules_evaluated for Phase 4+.

**Vastuvõtu kriteeriumid:**

| Tüüp | Mida testida | Kriteerium |
|------|--------------|------------|
| Integratsioon | GET /api/ai/verify/:id | Vastuse JSON sisaldab policyCoverage ja policyRulesEvaluated kui olemas. |
| Unit/Manual | Evidence Package | metadata.json sisaldab policy_coverage ja policy_rules_evaluated kui olemas. |

---

### 2.3 UI: Policy coverage ja „Miks confidence ei ole 100%?“ (A.3)

**Eesmärk:** Verify lehel näidata policy coverage ja laiendatavat selgitust, miks confidence ei ole 100%.

**Tulemused:**

- Uus plokk `/verify`: „Policy coverage: X% — N of M reeglit kontrollitud“ ja reeglite nimekiri pass / not_evaluated.
- Nupp või link „Miks confidence ei ole 100%?“ laiendab lühikese selgituse (millised kontrollid tehti, millised mitte).

#### Ülesanne P4.3.1 — Policy coverage plokk verify lehel

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1–2 h |
| **Kirjeldus** | Lisada verify lehele sektsioon „Policy coverage“. |

**Coding prompt (LLM-readable):**

- On the verify page (`frontend/app/verify/page.tsx` or a dedicated component), add a **Policy coverage** block. It must be visible when the API returns `policyCoverage` and/or `policyRulesEvaluated`.
- Display: (1) Heading “Policy coverage” or “Policy coverage (demo)”. (2) A short line: “X% — N of M rules checked”. Use `record.policyCoverage` and `record.policyRulesEvaluated`; total rules M = 4. (3) A list of rules: for each item show rule id and status (pass → green/check, not_evaluated → grey or “Not checked”).
- If `policyCoverage` is null/undefined, do not render this block (or show “Policy coverage not available”).
- Use existing styling to match Trust Summary and AI Claim blocks. Add a tooltip to the heading: “Share of policy rules that were evaluated for this response.”

**Vastuvõtu kriteeriumid:**

| Tüüp | Mida testida | Kriteerium |
|------|--------------|------------|
| Manual | Verify coverage-ga | Plokk näitab protsenti ja reeglite nimekirja staatusega. |
| Manual | Verify coverage-ta | Plokk peidetud või „not available“ kui policyCoverage null. |

#### Ülesanne P4.3.2 — „Miks mitte 100%?“ laiendatav

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1 h |
| **Kirjeldus** | Lisada nupp või link, mis laiendab selgituse, miks confidence on alla 100%. |

**Coding prompt (LLM-readable):**

- On the verify page, add a button or link labelled “Why is confidence not 100%?” (or “Why not 100%?”). Place it near the AI Claim block or in the Policy coverage block.
- On click, expand an inline section (or open a small modal) that explains in plain language: (1) Confidence reflects how many of the declared policy checks were performed. (2) List which rules were checked and which were not. Use data from `record.policyRulesEvaluated` if available. (3) One sentence: “We do not certify truth; we show what was checked.”
- Ensure the expanded content is accessible (e.g. focus, aria-expanded).

**Vastuvõtu kriteeriumid:**

| Tüüp | Mida testida | Kriteerium |
|------|--------------|------------|
| Manual | Klõps „Miks mitte 100%?“ | Selgitus laieneb ja mainib, millised kontrollid tehti ja millised mitte. |

---

### 2.4 Landing: Hero ja CTA (B.1)

**Eesmärk:** Selge esimene ekraan: üks küsimus, üks sildi rida, üks CTA.

**Tulemused:**

- Hero: pealkiri „AI ütles seda. Aga milliste reeglite järgi?“; alapealkiri „Me ei sertifitseeri tõde. Me sertifitseerime vastutust.“; CTA nupp „Kontrolli vastust — Demo“. Valikuline rida CTA all: „Ilma rahakotita. Lihtsalt läbipaistvus.“

#### Ülesanne P4.4.1 — Hero ja CTA pealehel / landingil

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1–2 h |
| **Kirjeldus** | Lisada või uuendada hero plokk ja põhi CTA pealehel. |

**Coding prompt (LLM-readable):**

- On the main page (e.g. `frontend/app/page.tsx` or the root layout), ensure the first visible section (hero) contains: (1) Main headline: “AI said that. But under which rules?” (or approved translation). (2) Subheadline: “We don’t certify truth. We certify responsibility.” (3) Primary button or link: “Verify a response — Demo”. On click, navigate to the app’s main flow. (4) Optional line below the button: “No crypto wallet. Just transparency.”
- Use clear typography and spacing. Prefer a minimal design. Ensure the CTA is focusable and has a visible focus state for accessibility.

**Vastuvõtu kriteeriumid:**

| Tüüp | Mida testida | Kriteerium |
|------|--------------|------------|
| Manual | Pealeht | Hero näitab pealkirja, alapealkirja, CTA ja valikulist rida. |
| Manual | CTA klõps | Kasutaja jõuab demosse. |

---

### 2.5 Üks killer-stsenaarium: tekst ja video (B.3)

**Eesmärk:** Üks korduv stsenaarium pilootidele ja investoritele: vali üks vertikaal (HR või Legal/compliance), kirjuta 1–2 lehe stsenaarium, salvesta 3–5 min skreenikas salvestus.

**Tulemused:**

- Tekstiline stsenaarium (markdown): sammud „kasutaja küsib“ kuni „auditor kontrollib offline“.
- Video 3–5 min: skreenikas samast voost (küsimus → vastus → Verify → Evidence Package → offline verifier).

#### Ülesanne P4.5.1 — Stsenaarium dokument

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1–2 h |
| **Kirjeldus** | Luua lühike stsenaarium dokument valitud vertikaali jaoks. |

**Coding prompt (LLM-readable):**

- Create file `docs/DEMO_SCENARIO_PHASE4.md`. Title: “Phase 4 killer scenario” and the chosen vertical (e.g. “HR” or “Legal/compliance”).
- Content (1–2 pages): (1) Context: one paragraph on the pain. (2) Steps: numbered list (operator opens app, enters prompt; backend returns signed response; operator downloads Evidence Package; auditor runs offline verifier). (3) Outcome: one sentence on what this proves. Include prerequisites.

**Vastuvõtu kriteeriumid:**

| Tüüp | Mida testida | Kriteerium |
|------|--------------|------------|
| Doc | Stsenaarium fail | Dokument sisaldab konteksti, samme 1–5, tulemust ja eeltingimusi. |

#### Ülesanne P4.5.2 — Stsenaarium video salvestus

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1–2 h |
| **Kirjeldus** | Salvestada 3–5 min skreenikas stsenaariumist. |

**Coding prompt (LLM-readable):**

- Record a screencast that follows the steps in `docs/DEMO_SCENARIO_PHASE4.md`: open app, enter prompt, submit; show response, Trust Summary, AI Claim, Policy coverage; download Evidence Package; run verifier, show VALID. Total duration 3–5 minutes.
- Save the video in a location that can be linked from the repo or landing (e.g. `docs/static/` or `public/`, or upload to YouTube/Vimeo and store the link in docs).
- In `docs/DEMO_SCENARIO_PHASE4.md`, add a line: “Video: [link to video].”

**Vastuvõtu kriteeriumid:**

| Tüüp | Mida testida | Kriteerium |
|------|--------------|------------|
| Manual | Video | Video olemas, 3–5 min, täielik voog küsimusest kuni offline kontrollini. |
| Doc | Stsenaarium | Dokumendis on link videole. |

---

### 2.6 Use cases leht (B.4)

**Eesmärk:** Leht „Kellele“ või „Use cases“ viie segmendiga: HR, Legal/compliance, Customer support, Education, Corporate governance — lühike valu + lahendus.

#### Ülesanne P4.6.1 — Use cases sisu ja leht

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1–2 h |
| **Kirjeldus** | Lisada use cases leht või sektsioon viie segmendiga. |

**Coding prompt (LLM-readable):**

- Create a new page route, e.g. `frontend/app/use-cases/page.tsx`. Title: “Use cases” or “For whom”.
- Add five blocks: (1) HR — pain + solution. (2) Legal/compliance — pain + solution. (3) Customer support — pain + solution. (4) Education — pain + solution. (5) Corporate governance — pain + solution.
- Use short paragraphs (2–3 sentences per block). Add link to main page or CTA “Try demo”. Link the page from main navigation or footer.

**Vastuvõtu kriteeriumid:**

| Tüüp | Mida testida | Kriteerium |
|------|--------------|------------|
| Manual | Use cases leht | Viis segmenti valu + lahendusega; leht lingitud navigatsioonist või jalutusest. |

---

### 2.7 Outreach ja piloodid (B.5)

**Eesmärk:** Nimekiri 10–20 ettevõttest/kontaktist; e-kirja mall lingiga demole ja videole; eesmärk 3–5 kõnet ja ≥1 LOI.

#### Ülesanne P4.7.1 — Outreach nimekiri ja e-kirja mall

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1–2 h |
| **Kirjeldus** | Luua sihtnimekiri ja taaskasutatav e-kirja mall. |

**Coding prompt (LLM-readable):**

- Create file `docs/outreach/PHASE4_OUTREACH.md`. Section 1: “Target list” — 10–20 entries. Section 2: “Email template” — subject line and body (problem, Aletheia in one sentence, invitation to 5-min demo, links to demo and video). Keep tone professional and concise.
- Document: “Outreach goal: 3–5 calls, 1 LOI.”

**Vastuvõtu kriteeriumid:**

| Tüüp | Mida testida | Kriteerium |
|------|--------------|------------|
| Doc | Outreach fail | Fail sisaldab nimekirja (10–20) ja e-kirja malli linkidega demole ja videole. |

---

### 2.8 Minimaalne analüütika (B.6)

**Eesmärk:** Lihtsad loendurid: landing külastused, CTA klõpsud, üleminekud demosse, Evidence Package allalaadimised.

#### Ülesanne P4.8.1 — Minimaalne jälgimine Phase 4 mõõdikute jaoks

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1–2 h |
| **Kirjeldus** | Lisada kliendi- või serveripoolne jälgimine võtmesündmuste jaoks. |

**Coding prompt (LLM-readable):**

- Choose a minimal approach: (A) Client-side: analytics SDK or custom endpoint — events “landing_view”, “cta_click”, “demo_view”, “download_evidence_click”. (B) Server-side: log or store counts for GET /, GET /verify, GET /api/ai/evidence/:id.
- Document in README or `docs/PHASE4_ANALYTICS.md`: which events are tracked, where data is stored, how to view counts. Comply with privacy.
- Optional: simple dashboard. If time is short, skip and use logs or analytics provider UI.

**Vastuvõtu kriteeriumid:**

| Tüüp | Mida testida | Kriteerium |
|------|--------------|------------|
| Manual | Sündmused | Vähemalt CTA klõps ja evidence allalaadimine jälgitakse (või logitakse). |
| Doc | Dokumentatsioon | Kirjeldatud, mida jälgitakse ja kust vaadata. |

---

## Väljaspool ulatust (Phase 5+)

Järgmine on Phase 4 jaoks **väljaspool ulatust** ja lükatud Phase 5 või hilisemasse:

- Täielik Policy Registry (A.5)
- Policy Evaluation Pipeline (A.6)
- Time-travel verify (A.7)
- Human / hybrid review (A.8)
- Avalik API ja OpenAPI (C.1)
- Sign-only API (C.2)
- SDK (C.3)
- MCP attestatsioon (C.4)
- SIEM / blockchain integratsioonid (C.5)
- Partneri stsenaariumid (C.6)

---

## Lõpetamise kriteeriumid

Phase 4 on lõpetatud, kui kõik järgmised on täidetud:

| # | Kriteerium | Staatus |
|---|------------|--------|
| 1 | Demo-poliitika avaldatud ja vaikimisi kasutusel | [ ] |
| 2 | Policy coverage avaldatud API-s, UI-s ja Evidence Package'is | [ ] |
| 3 | UI selgitab, miks confidence &lt; 100% | [ ] |
| 4 | Landing uuendatud (hero + CTA) | [ ] |
| 5 | Üks stsenaarium (dokument + video) valmis | [ ] |
| 6 | Use cases leht avaldatud | [ ] |
| 7 | Outreach viidud läbi (≥10 ettevõtet) | [ ] |
| 8 | ≥3 kõnet ja ≥1 LOI (või dokumenteeritud tulemus) | [ ] |
| 9 | Mõõdikud kogutakse | [ ] |

---

## Ajajoon

Soovituslik jaotus 4 nädalaks:

| Nädal | Fookus |
|-------|--------|
| 1 | Demo-poliitika (P4.1.1); policy coverage backend (P4.2.1, P4.2.2); Evidence Package metadata uuendus |
| 2 | UI: policy coverage plokk (P4.3.1) ja „Miks mitte 100%?“ (P4.3.2); landing hero (P4.4.1) |
| 3 | Stsenaarium dokument (P4.5.1); stsenaarium video (P4.5.2); use cases leht (P4.6.1) |
| 4 | Outreach nimekiri ja mall (P4.7.1); analüütika (P4.8.1); outreach läbiviimine ja tulemuste ülevaatus |

---

## Riskid ja leevendused

| Risk | Leevendus |
|------|-----------|
| Liiga vähe tagasisidet pilootidelt | Aktiivne outreach + üks selge vertikaalne stsenaarium |
| Poliitika ulatuse laienemine | Hoida poliitikat 3–4 reegliga |
| UI keerukus | Minimaalne funktsionaalsus; vältida visuaalset ülekoormust |
| LOI puudumine | Läbi vaadata stsenaarium ja positsioneerimine; iteratsioon sõnumitega |

---

## Viited

- [Visioon ja teekond](VISION_AND_ROADMAP.md) — Phase 4 = turu valideerimine + poliitika alus.
- [Plaan Phase 2](PLAN_PHASE2.md) — Evidence Package, killer demo, AI Claim.
- [Plaan Phase 3 UI](PLAN_PHASE3_UI.md) — Verify lehe wireframe ja tooltipid.
- [NEXT.md](../tmp/NEXT.md) — Suuna valik: B alusena, A.1–A.3 tugevdusena, C hiljem.

**Tõlked:** [EN](../en/PLAN_PHASE4.md) · [RU](../ru/PLAN_PHASE4.md)
