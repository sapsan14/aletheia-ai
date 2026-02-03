# Aletheia AI — План Phase 4: Валидация рынка и основа policy (2026)

В этом документе описан **Phase 4** дорожной карты: валидация рынка (лендинг, один сценарий, outreach, пилоты) плюс минимальная основа policy (demo-policy, policy coverage, UI «Почему не 100%?»). Основано на завершённых Phase 2 и Phase 3 и выбранном направлении из [NEXT.md](../tmp/NEXT.md): **B как основа, A.1–A.3 как усиление, C — позже**, когда появятся запросы.

**Статус:** Черновик для ревью  
**Связанные документы:** [Видение и дорожная карта](VISION_AND_ROADMAP.md) · [План Phase 2](PLAN_PHASE2.md) · [План Phase 3 UI](PLAN_PHASE3_UI.md) · [NEXT.md](../tmp/NEXT.md)

---

## Содержание

- [Цель и границы Phase 4](#цель-и-границы-phase-4)
- [Deliverables и шаги разработки](#deliverables-и-шаги-разработки)
- [Вне скоупа (Phase 5+)](#вне-скоупа-phase-5)
- [Критерии завершения](#критерии-завершения)
- [Таймлайн](#таймлайн)
- [Риски и смягчение](#риски-и-смягчение)
- [Ссылки](#ссылки)

---

## Цель и границы Phase 4

**Цель:** Создать минимальный слой **policy-прозрачности** и **рыночной упаковки**, чтобы:

- проводить первые пилоты (HR, legal/compliance, корпоративное управление),
- демонстрировать зрелость продукта (policy, coverage, объяснимость confidence),
- собирать обратную связь от реальных пользователей,
- готовить основу для API-платформы (Phase 5).

Phase 4 — это **market validation + policy foundation**, без тяжёлой инженерии.

**В скоупе:**

- Одна каноническая demo-policy (A.1).
- Policy coverage в backend, API, Evidence Package и UI (A.2).
- UI: блок policy coverage и «Почему confidence не 100%?» (A.3).
- Лендинг: hero + CTA (B.1).
- Один killer-сценарий (текст + видео) (B.3).
- Страница Use cases (B.4).
- Outreach и пилоты (B.5).
- Минимальная аналитика (B.6).

**Вне скоупа Phase 4:** Полный Policy Registry (A.5), Policy Evaluation Pipeline (A.6), time-travel verify (A.7), human/hybrid review (A.8), публичный API/OpenAPI (C.1), sign-only API (C.2), SDK (C.3), MCP-аттестация (C.4), интеграции SIEM/блокчейн (C.5), партнёрские сценарии (C.6).

---

## Deliverables и шаги разработки

Каждый deliverable разбит на задачи с **LLM-readable инструкциями (coding prompts)** и критериями приёмки.

---

### 2.1 Demo-policy (A.1)

**Цель:** Один канонический файл policy и документация, чтобы все демо и пилоты использовали один и тот же набор правил.

**Результаты:**

- Файл `docs/en/policy/aletheia-demo-2026-01.json` (или `.yaml`) с определением demo-policy.
- 3–4 правила: R1 (подпись + timestamp), R2 (model_id зафиксирован), R3 (нет запрещённых доменов — напр. мед/юрид советов) — `not_evaluated`, R4 (human review) — `not_evaluated`.
- Документация: `docs/en/policy/aletheia-demo-2026-01.md`.

#### Задача P4.1.1 — Формат и содержимое файла demo-policy

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | Создать канонический файл demo-policy и описать его структуру. |

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

**Критерии приёмки:**

| Тип | Что проверять | Критерий |
|-----|----------------|----------|
| Документ | Файл policy | JSON корректен; есть policy_id, policy_version, rules с id R1–R4. |
| Документ | Документ policy | Markdown описывает каждое правило и указывает, что R3/R4 в Phase 4 — not_evaluated. |

---

### 2.2 Policy coverage (A.2)

**Цель:** Вычислять и сохранять policy coverage по каждому ответу; отдавать в verify API и в Evidence Package.

**Результаты:**

- Backend: вычисление `policy_coverage = evaluated / total_rules` и статус по правилам (pass / not_evaluated) по demo-policy.
- Сохранение в БД.
- Добавление в ответ GET `/api/ai/verify/:id` и в metadata Evidence Package.

#### Задача P4.2.1 — Backend: модель и хранение policy coverage

| Поле | Значение |
|------|----------|
| **Оценка** | 2–3 ч |
| **Описание** | Добавить модель данных и персистенцию для policy coverage и результатов проверки правил. |

**Coding prompt (LLM-readable):**

- Define a small model for policy evaluation result: e.g. `PolicyEvaluationResult` or fields on an existing entity. It must include: `policyId`, `policyVersion`, `coverage` (double, 0.0–1.0), and a list or JSON of rule results, each with `ruleId` (e.g. "R1"), `status` ("pass" or "not_evaluated"). Option A: add columns to `ai_response` (e.g. `policy_coverage` DOUBLE, `policy_rules_evaluated` JSON or TEXT). Option B: separate table `policy_evaluation` with `response_id` FK. Prefer Option A for Phase 4 simplicity.
- Add a service or helper that, given an `AiResponse` (with signature, tsaToken, llmModel set), evaluates rules from the demo policy (aletheia-demo-2026-01): R1 pass if signature non-null and tsaToken non-null; R2 pass if llmModel non-null and non-empty; R3 and R4 not_evaluated. Compute `coverage = (number of rules with status pass) / (total rules)` or `(number of rules evaluated) / (total rules)` — document which formula you use. For Phase 4, "evaluated" means either pass or explicitly not_evaluated; total_rules = 4. Result: coverage in [0, 1] and list of { ruleId, status }.
- When saving a new AI response (in the same transaction or immediately after), compute this evaluation and persist coverage and rule results. If using existing `AiResponse`, add columns and set them in `AuditRecordService` or the controller that saves the response.
- Add a Flyway/Liquibase migration: add column `policy_coverage` (DOUBLE, nullable) and `policy_rules_evaluated` (TEXT/JSON, nullable) to `ai_response` if not present. Document migration in README.

**Критерии приёмки:**

| Тип | Что проверять | Критерий |
|-----|----------------|----------|
| Unit | Расчёт coverage | Для ответа с signature, tsaToken, llmModel: R1 pass, R2 pass, R3/R4 not_evaluated; coverage = 0.5 (2/4). |
| Интеграция | Сохранение ответа | После POST /api/ai/ask у загруженной сущности заполнены policy_coverage и policy_rules_evaluated. |

#### Задача P4.2.2 — Отдача policy coverage в verify API и Evidence Package

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | Добавить policy_coverage и результаты правил в GET /api/ai/verify/:id и в metadata.json Evidence Package. |

**Coding prompt (LLM-readable):**

- In the DTO or response map for GET `/api/ai/verify/:id`, add fields: `policyCoverage` (Double, nullable) and `policyRulesEvaluated` (list of objects with ruleId and status, or JSON string). Populate from the stored entity. Ensure the frontend and any existing clients can ignore these if null.
- In `EvidencePackageServiceImpl` (or equivalent), when building metadata for the Evidence Package, add to metadata.json: `policy_coverage` (number) and `policy_rules_evaluated` (array of { "ruleId": "R1", "status": "pass" } etc.). Use the same values as stored on the response entity. If policy_coverage is null, omit or set to null in JSON.
- Update the verifier or Evidence Package documentation to mention that metadata may include policy_coverage and policy_rules_evaluated for Phase 4+.

**Критерии приёмки:**

| Тип | Что проверять | Критерий |
|-----|----------------|----------|
| Интеграция | GET /api/ai/verify/:id | В JSON ответа есть policyCoverage и policyRulesEvaluated при наличии. |
| Unit/Manual | Evidence Package | В metadata.json есть policy_coverage и policy_rules_evaluated при наличии. |

---

### 2.3 UI: Policy coverage и «Почему confidence не 100%?» (A.3)

**Цель:** На странице verify показывать policy coverage и раскрываемое объяснение, почему confidence не 100%.

**Результаты:**

- Новый блок на `/verify`: «Policy coverage: X% — проверено N из M правил» и список правил с pass / not_evaluated.
- Кнопка/ссылка «Почему confidence не 100%?» с раскрывающимся кратким объяснением (какие проверки выполнены, какие нет).

#### Задача P4.3.1 — Блок Policy coverage на странице verify

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | Добавить секцию «Policy coverage» на страницу verify. |

**Coding prompt (LLM-readable):**

- On the verify page (`frontend/app/verify/page.tsx` or a dedicated component), add a **Policy coverage** block. It must be visible when the API returns `policyCoverage` and/or `policyRulesEvaluated`.
- Display: (1) Heading “Policy coverage” or “Policy coverage (demo)”. (2) A short line: “X% — N of M rules checked” (e.g. “50% — 2 of 4 rules checked”). Use `record.policyCoverage` (e.g. 0.5 → 50%) and length of `record.policyRulesEvaluated` for N, total rules M = 4 for demo policy. (3) A list of rules: for each item in `record.policyRulesEvaluated`, show rule id (e.g. R1) and status (pass → green/check, not_evaluated → grey or “Not checked”). If you have rule descriptions from the policy file, you can show them; otherwise rule id + status is enough.
- If `policyCoverage` is null/undefined, do not render this block (or show “Policy coverage not available”).
- Use existing styling (e.g. card, spacing) to match Trust Summary and AI Claim blocks. Add a tooltip to the heading: “Share of policy rules that were evaluated for this response.”

**Критерии приёмки:**

| Тип | Что проверять | Критерий |
|-----|----------------|----------|
| Manual | Verify с coverage | Блок показывает процент и список правил со статусом. |
| Manual | Verify без coverage | Блок скрыт или «не доступно», если policyCoverage null. |

#### Задача P4.3.2 — Раскрываемый блок «Почему confidence не 100%?»

| Поле | Значение |
|------|----------|
| **Оценка** | 1 ч |
| **Описание** | Добавить кнопку/ссылку, раскрывающую объяснение, почему confidence ниже 100%. |

**Coding prompt (LLM-readable):**

- On the verify page, add a button or link labelled “Why is confidence not 100%?” (or “Why not 100%?”). Place it near the AI Claim block (e.g. under the confidence value) or in the Policy coverage block.
- On click, expand an inline section (or open a small modal) that explains in plain language: (1) Confidence reflects how many of the declared policy checks were performed. (2) List which rules were checked (e.g. R1: signature and timestamp; R2: model recorded) and which were not (e.g. R3: content check not run; R4: human review not performed). Use data from `record.policyRulesEvaluated` if available; otherwise use static text for the demo policy. (3) One sentence: “We do not certify truth; we show what was checked.”
- Use the same TOOLTIPS or copy approach as in Phase 3; avoid algorithm jargon. Ensure the expanded content is accessible (e.g. focus, aria-expanded).

**Критерии приёмки:**

| Тип | Что проверять | Критерий |
|-----|----------------|----------|
| Manual | Клик «Почему не 100%?» | Появляется объяснение: какие проверки выполнены, какие нет. |

---

### 2.4 Лендинг: Hero и CTA (B.1)

**Цель:** Понятный первый экран: один вопрос, один подзаголовок, один CTA.

**Результаты:**

- Hero: заголовок «ИИ это сказал. Но по каким правилам?»; подзаголовок «Мы не сертифицируем истину. Мы сертифицируем ответственность.»; кнопка CTA «Проверить ответ — Демо» с переходом в приложение. Опционально под CTA: «Без кошелька. Только прозрачность.»

#### Задача P4.4.1 — Hero и CTA на главной/лендинге

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | Добавить или обновить hero-блок и основной CTA на главной странице. |

**Coding prompt (LLM-readable):**

- On the main page (e.g. `frontend/app/page.tsx` or the root layout), ensure the first visible section (hero) contains: (1) Main headline: “AI said that. But under which rules?” (or the approved translation, e.g. “ИИ это сказал. Но по каким правилам?”). (2) Subheadline: “We don’t certify truth. We certify responsibility.” (3) Primary button or link: “Verify a response — Demo” (or “Проверить ответ — Демо”). On click, navigate to the app’s main flow. (4) Optional line below the button: “No crypto wallet. Just transparency.” or “Без кошелька. Только прозрачность.”
- Use clear typography and spacing so the hero is readable on mobile and desktop. Prefer a minimal design; avoid extra sections for Phase 4.
- Ensure the CTA is focusable and has a visible focus state for accessibility.

**Критерии приёмки:**

| Тип | Что проверять | Критерий |
|-----|----------------|----------|
| Manual | Главная | Hero показывает заголовок, подзаголовок, CTA и опциональную строку. |
| Manual | Клик CTA | Пользователь попадает в демо (главная или verify). |

---

### 2.5 Один killer-сценарий: текст и видео (B.3)

**Цель:** Один повторяемый сценарий для пилотов и инвесторов: выбрать вертикаль (HR или Legal/compliance), написать сценарий на 1–2 стр., записать скринкаст 3–5 мин.

**Результаты:**

- Текстовый сценарий (markdown): от «пользователь задаёт вопрос» до «аудитор проверяет офлайн».
- Видео 3–5 мин: скринкаст того же потока (вопрос → ответ → Verify → Evidence Package → офлайн-верификатор).

#### Задача P4.5.1 — Документ сценария

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | Создать короткий документ сценария для выбранной вертикали. |

**Coding prompt (LLM-readable):**

- Create file `docs/DEMO_SCENARIO_PHASE4.md` (or add a section to `docs/DEMO_SCRIPT.md`). Title: “Phase 4 killer scenario” and the chosen vertical (e.g. “HR” or “Legal/compliance”).
- Content (1–2 pages): (1) Context: one paragraph on the pain. (2) Steps: numbered list (operator opens app, enters prompt; backend returns signed response; operator downloads Evidence Package; auditor runs offline verifier). (3) Outcome: one sentence on what this proves.
- Include prerequisites. Keep language non-technical where possible.

**Критерии приёмки:**

| Тип | Что проверять | Критерий |
|-----|----------------|----------|
| Документ | Файл сценария | Есть контекст, шаги 1–5, исход, пререквизиты. |

#### Задача P4.5.2 — Запись видео сценария

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | Записать скринкаст 3–5 мин по сценарию. |

**Coding prompt (LLM-readable):**

- Record a screencast that follows the steps in `docs/DEMO_SCENARIO_PHASE4.md`: open app, enter prompt, submit; show response and Trust Summary, AI Claim, Policy coverage; download Evidence Package; run verifier, show VALID. Total duration 3–5 minutes.
- Save the video in a location that can be linked from the repo or landing (e.g. `docs/static/` or `public/`, or upload to YouTube/Vimeo and store the link in docs).
- In `docs/DEMO_SCENARIO_PHASE4.md`, add a line: “Video: [link to video].”

**Критерии приёмки:**

| Тип | Что проверять | Критерий |
|-----|----------------|----------|
| Manual | Видео | Видео есть, 3–5 мин, полный поток от промпта до офлайн-верификации. |
| Документ | Сценарий | В документе есть ссылка на видео. |

---

### 2.6 Страница Use cases (B.4)

**Цель:** Страница «Для кого» / «Use cases» с блоками: HR, Legal/compliance, Customer support, Education, Corporate governance — кратко боль и решение.

#### Задача P4.6.1 — Контент и страница Use cases

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | Добавить страницу или секцию Use cases с пятью сегментами. |

**Coding prompt (LLM-readable):**

- Create a new page route, e.g. `frontend/app/use-cases/page.tsx`. Title: “Use cases” or “For whom”.
- Add five blocks: (1) HR — pain + solution (Aletheia fixes wording, policy, confidence). (2) Legal/compliance — pain + solution (signed response, Evidence Package). (3) Customer support — pain + solution. (4) Education — pain + solution. (5) Corporate governance — pain + solution.
- Use short paragraphs (2–3 sentences per block). Add link to main page or CTA “Try demo”. Link the page from main navigation or footer.

**Критерии приёмки:**

| Тип | Что проверять | Критерий |
|-----|----------------|----------|
| Manual | Страница Use cases | Пять сегментов с болью и решением; страница доступна из навигации/футера. |

---

### 2.7 Outreach и пилоты (B.5)

**Цель:** Список 10–20 компаний/контактов; шаблон письма со ссылкой на демо и видео; цель — 3–5 созвонов и ≥1 LOI.

#### Задача P4.7.1 — Список outreach и шаблон письма

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | Создать список целей и переиспользуемый шаблон письма. |

**Coding prompt (LLM-readable):**

- Create file `docs/outreach/PHASE4_OUTREACH.md`. Section 1: “Target list” — 10–20 entries (company/role, contact type, source). Section 2: “Email template” — subject line and body (problem, Aletheia in one sentence, invitation to 5-min demo, links to demo and video). Keep tone professional and concise.
- Document: “Outreach goal: 3–5 calls, 1 LOI.”

**Критерии приёмки:**

| Тип | Что проверять | Критерий |
|-----|----------------|----------|
| Документ | Файл outreach | Есть список (10–20) и шаблон письма со ссылками на демо и видео. |

---

### 2.8 Минимальная аналитика (B.6)

**Цель:** Простые счётчики: посещения лендинга, клики CTA, переходы в демо, скачивания Evidence Package.

#### Задача P4.8.1 — Минимальный трекинг метрик Phase 4

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | Добавить клиентский или серверный трекинг ключевых событий. |

**Coding prompt (LLM-readable):**

- Choose a minimal approach: (A) Client-side: analytics SDK or custom endpoint — events “landing_view”, “cta_click”, “demo_view”, “download_evidence_click”. (B) Server-side: log or store counts for GET /, GET /verify, GET /api/ai/evidence/:id.
- Document in README or `docs/PHASE4_ANALYTICS.md`: which events are tracked, where data is stored, how to view counts. Comply with privacy.
- Optional: simple dashboard for counts. If time is short, skip and use logs or analytics provider UI.

**Критерии приёмки:**

| Тип | Что проверять | Критерий |
|-----|----------------|----------|
| Manual | События | Как минимум клик CTA и скачивание evidence трекаются (или логируются). |
| Документ | Документация | Описано, что трекается и где смотреть. |

---

## Вне скоупа (Phase 5+)

Явно **вне скоупа** Phase 4, перенесено на Phase 5 или позже:

- Full Policy Registry (A.5)
- Policy Evaluation Pipeline (A.6)
- Time-travel verify (A.7)
- Human / hybrid review (A.8)
- Public API и OpenAPI (C.1)
- Sign-only API (C.2)
- SDK (C.3)
- MCP attestation (C.4)
- Интеграции SIEM / блокчейн (C.5)
- Партнёрские сценарии (C.6)

---

## Критерии завершения

Phase 4 считается завершённым, когда выполнено следующее:

| # | Критерий | Статус |
|---|-----------|--------|
| 1 | Demo-policy опубликована и используется по умолчанию | [ ] |
| 2 | Policy coverage отображается в API, UI и Evidence Package | [ ] |
| 3 | UI объясняет, почему confidence &lt; 100% | [ ] |
| 4 | Лендинг обновлён (hero + CTA) | [ ] |
| 5 | Один сценарий (документ + видео) готов | [ ] |
| 6 | Страница Use cases опубликована | [ ] |
| 7 | Outreach проведён (≥10 компаний) | [ ] |
| 8 | ≥3 созвона и ≥1 LOI (или задокументирован исход) | [ ] |
| 9 | Метрики собираются | [ ] |

---

## Таймлайн

Предлагаемое распределение на 4 недели:

| Неделя | Фокус |
|--------|--------|
| 1 | Demo-policy (P4.1.1); policy coverage в backend (P4.2.1, P4.2.2); обновление metadata Evidence Package |
| 2 | UI: блок policy coverage (P4.3.1) и «Почему не 100%?» (P4.3.2); hero лендинга (P4.4.1) |
| 3 | Документ сценария (P4.5.1); видео сценария (P4.5.2); страница Use cases (P4.6.1) |
| 4 | Список и шаблон outreach (P4.7.1); аналитика (P4.8.1); проведение outreach и разбор результатов |

---

## Риски и смягчение

| Риск | Смягчение |
|------|-----------|
| Мало обратной связи от пилотов | Активный outreach + один чёткий вертикальный сценарий |
| Раздувание policy | Ограничить policy 3–4 правилами |
| Перегруженность UI | Минимальный функционал, без визуальной перегрузки |
| Нет LOI | Пересмотр сценария и позиционирования; итерация по сообщениям |

---

## Ссылки

- [Видение и дорожная карта](VISION_AND_ROADMAP.md) — Phase 4 = валидация рынка + основа policy.
- [План Phase 2](PLAN_PHASE2.md) — Evidence Package, killer demo, AI Claim.
- [План Phase 3 UI](PLAN_PHASE3_UI.md) — Wireframe и тултипы страницы verify.
- [NEXT.md](../tmp/NEXT.md) — Выбор направления: B как основа, A.1–A.3 как усиление, C позже.

**Переводы:** [EN](../en/PLAN_PHASE4.md) · [ET](../et/PLAN_PHASE4.md)
