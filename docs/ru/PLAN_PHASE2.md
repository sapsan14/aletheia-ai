# Aletheia AI — План Phase 2 (Killer demo)

Документ описывает **Phase 2** дорожной карты: killer demo в legal/compliance, Evidence Package, офлайн-верификация и один воспроизводимый сценарий. Включает описание **возможностей**, шаги разработки с **LLM-readable промптами для кодинга** и критерии тестирования.

**Связанные документы:** [Видение и дорожная карта](VISION_AND_ROADMAP.md) (Phase 2 = Killer demo) · [План реализации](PLAN.md) (шаги PoC) · [Модель доверия](TRUST_MODEL.md)

---

## Содержание

- [Возможности: почему это направление](#возможности-почему-это-направление)
- [Цель и границы Phase 2](#цель-и-границы-phase-2)
- [Шаги разработки (с LLM-readable промптами)](#шаги-разработки-с-llm-readable-промптами)
- [Критерии завершения Phase 2](#критерии-завершения-phase-2)
- [Ссылки](#ссылки)

---

## Возможности: почему это направление

### Рынок и регуляторика

- **Рынок AI Trust / TRiSM:** ~$2.34B (2024) → ~$7.44B (2030), CAGR ~21.6%. Драйверы: регуляторика, объяснимость, аудит, governance.
- **EU AI Act:** В силу с авг. 2024; поэтапно до 2026–2027. High-risk системы требуют traceability, соответствия, документации. Задержки по стандартам и органам оценки создают спрос на инструменты с **доказательствами** для регуляторов.
- **Legal tech / контракты:** TermScout (Certify™ AI), AlsoCheck (поклаузульные «Trust Objects» для GDPR, AI Act, ISO 27001), iCertis. Тренд: не только «AI проанализировал», а **доказуемый вывод с привязкой ко времени и политике** — для судов и аудиторов.
- **Fintech/regtech:** MAS, HKMA, FINRA ожидают AI model risk, audit trail, attestation. Подписанный и таймстемпленный вывод модели подходит под «attestation of model output».

### Рекомендуемая ниша: legal / compliance AI

Фокус Phase 2 — **legal / compliance AI** (контракты, оговорки, EU AI Act, аудит), потому что:

1. **Обязательный спрос** — EU AI Act и аудит контрактов требуют доказуемых доказательств; «кто что сказал, когда» — прямое совпадение.
2. **Дифференциатор** — Немногие дают **RFC 3161 + офлайн-верификацию** без вызова своего сервера. Evidence Package Aletheia проверяется кем угодно по публичному ключу и цепочке TSA.
3. **Готовность платить** — Юриды и compliance платят за audit-grade доказательства.
4. **Один демо — много применений** — Один сценарий (подписанное утверждение AI по оговорке/политике) служит инвесторам, пилотам и регуляторам.

### Дифференциатор в одном предложении

**Aletheia превращает выводы ИИ в криптографически верифицируемые, офлайн-проверяемые доказательства (подпись + RFC 3161 timestamp) — чтобы аудиторы, суды и компании могли доказать, что сказал ИИ и когда, без зависимости от нашего backend.**

---

## Цель и границы Phase 2

**Цель:** Один воспроизводимый **killer demo** в legal/compliance: утверждение ИИ (например, соответствие оговорки), подписанное и с таймстемпом, упакованное в Evidence Package и верифицируемое офлайн. Обновить нарратив: «работает с MCP / любым agent framework».

**Вне Phase 2:** Полная схема AI Claim в production, PKI key registry, multi-TSA, HSM. Минимальный AI Claim (claim + confidence + policy_version) — только для поддержки демо.

**Результаты:** (1) Evidence Package (минимальный); (2) Офлайн-верификатор (CLI/скрипт) `aletheia verify <path>`; (3) Один killer demo сценарий (legal/compliance, ≤5 мин); (4) Минимальный AI Claim в пакете; (5) Нарратив и документация (README, «Works with MCP / OpenClaw / any agent»).

---

## Шаги разработки (с LLM-readable промптами)

### Шаг DP2.1 — Генерация Evidence Package

**Цель:** Для каждого подписанного ответа (или отдельного endpoint) формировать Evidence Package: набор файлов или архив со всем необходимым для офлайн-верификации.

**Оценка:** 4–6 ч

#### Задача DP2.1.1 — Формат Evidence Package

Определить и задокументировать минимальный .aep: response.txt, canonical.bin, hash.sha256, signature.sig, timestamp.tsr, metadata.json, public_key.pem. Документировать в README или docs/en/PLAN_PHASE2.md. Критерий приёмки: все семь компонентов перечислены.

#### Задача DP2.1.2 — Backend: генерация пакета по response id

Добавить EvidencePackageService (или аналог): на входе — response text, canonical bytes, hash, signature, tsa token, model, created_at, public key PEM; на выходе — Map/директория/ZIP. Опционально: GET /api/ai/evidence/:id, возвращающий ZIP или JSON с base64. Критерии: unit-тест (все 7 компонентов, hash совпадает); при наличии endpoint — integration 200.

---

### Шаг DP2.2 — Офлайн-верификатор (CLI или скрипт)

**Цель:** Программа или скрипт по пути к Evidence Package (директория или .aep): проверка подписи и TSA без вызова backend.

**Оценка:** 4–6 ч

#### Задача DP2.2.1 — Логика верификации (подпись + TSA)

Читать hash.sha256, canonical.bin или response.txt, signature.sig, timestamp.tsr, public_key.pem. Проверять подпись по хешу (BouncyCastle/OpenSSL/crypto). Парсить RFC 3161 token, извлекать genTime. Вывод: VALID/INVALID + краткий отчёт. Unit-тесты: валидный пакет → VALID; подделанная подпись/хеш → INVALID.

#### Задача DP2.2.2 — CLI

Команда `aletheia verify <path>` (или npm run verify / скрипт). Exit 0 при valid, 1 при invalid. Документировать в README раздел «Offline verification».

---

### Шаг DP2.3 — Killer demo сценарий (legal/compliance)

**Цель:** Один сквозной сценарий: вопрос по compliance → ответ с подписью и timestamp → экспорт Evidence Package → аудитор запускает verifier. Воспроизводимо за ≤5 мин.

#### Задача DP2.3.1 — Демо-скрипт

Создать docs/DEMO_SCRIPT.md (или раздел в PLAN_PHASE2): шаги (1) оператор отправляет prompt «Does this clause comply with GDPR? [clause]»; (2) backend возвращает response id; (3) экспорт .aep; (4) аудитор получает .aep; (5) `aletheia verify <path>` → VALID. Пример промпта, предварительные условия.

#### Задача DP2.3.2 — Опционально: кнопка «Download evidence» во фронте

Кнопка/ссылка на странице ответа: GET /api/ai/evidence/:id, сохранение файла .aep/.zip. Критерий: ручная проверка — файл скачивается, verifier принимает.

---

### Шаг DP2.4 — Минимальный AI Claim (опционально)

Расширить metadata.json: claim, confidence, policy_version; подписывать каноническую форму (response+metadata). Verifier при VALID может выводить claim и policy_version. Критерии: unit — metadata содержит поля, подпись верифицируется.

---

### Шаг DP2.5 — Нарратив и документация

В README: разделы «Evidence Package & offline verification», «Killer demo (Phase 2)», «Works with MCP / OpenClaw / any agent»; ссылки на [Plan Phase 2](docs/en/PLAN_PHASE2.md), DEMO_SCRIPT, Vision Phase 2. В docs/README таблица: Plan Phase 2 | [EN](en/PLAN_PHASE2.md) · [RU](ru/PLAN_PHASE2.md) · [ET](et/PLAN_PHASE2.md).

---

## Критерии завершения Phase 2

Phase 2 считается выполненной, когда: (1) Evidence Package генерируется для каждого подписанного ответа (или через endpoint); (2) Офлайн-верификатор работает без backend; (3) Демо-скрипт задокументирован и воспроизводим за ≤5 мин; (4) README и документация обновлены; (5) Опционально: один пилот или LOI.

---

## Ссылки

- [Видение и дорожная карта](VISION_AND_ROADMAP.md) — Phase 2, Evidence Package, AI Claim.
- [План реализации](PLAN.md) — шаги PoC (1–8).
- [Модель доверия](TRUST_MODEL.md) — кто что аттестует; eIDAS.
- [Подпись](SIGNING.md), [Временные метки](TIMESTAMPING.md) — подпись и TSA в backend.
- [Диаграммы](../../diagrams/architecture.md) — пайплайн и цепочка доверия.

**Другие языки:** [EN](../en/PLAN_PHASE2.md) · [ET](../et/PLAN_PHASE2.md)
