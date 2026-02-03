# Aletheia AI — Манифест PQC: постквантовая криптография (вне scope / PoC)

**Гибридная криптографическая аттестация выводов ИИ: классическая + постквантовая подпись одного и того же хеша доказательства.**

Этот документ — **вне основного scope, инициатива энтузиастов**: добавление второго, дополнительного слоя PQC-подписи без нарушения существующей цепочки доверия. Aletheia позиционируется как **готовная к квантовой эре** инфраструктура доверия для долгосрочных доказательств и регуляторного нарратива, при этом текущий пайплайн RSA + RFC 3161 остаётся полностью действительным и основным.

**Статус:** Черновик для медитации и PoC · **Связано:** [Подпись](SIGNING.md) · [Крипто-справочник (EN)](../en/CRYPTO_REFERENCE.md) · [Модель доверия](TRUST_MODEL.md) · [Видение и дорожная карта](VISION_AND_ROADMAP.md)

---

## Содержание

- [Зачем PQC и зачем сейчас](#зачем-pqc-и-зачем-сейчас)
- [Стандарты и ссылки](#стандарты-и-ссылки)
- [Принципы проектирования](#принципы-проектирования)
- [Результаты и задачи (LLM-readable)](#результаты-и-задачи-llm-readable)
- [Фронтенд: бейдж PQC и маркетинг](#фронтенд-бейдж-pqc-и-маркетинг)
- [Изменения в backend](#изменения-в-backend)
- [Изменения в утилите верификатора](#изменения-в-утилите-верификатора)
- [Развёртывание (Ansible)](#развёртывание-ansible)
- [Критерии завершения](#критерии-завершения)
- [Риски и замечания](#риски-и-замечания)

---

## Зачем PQC и зачем сейчас

| Драйвер | Описание |
|--------|----------|
| **Долгосрочная верификация** | Доказательства, сохранённые сегодня, могут понадобиться через 10–20 лет. Классические RSA/ECDSA могут стать уязвимы к квантовым компьютерам; PQC-подписи остаются стойкими в этом сценарии. |
| **Нарратив «на будущее»** | «Aletheia — инфраструктура доверия для ИИ, готовая к квантовой эре.» Сильный месседж для обороны, финансов и регуляторов (EU AI Act, eIDAS, долгосрочное архивирование). |
| **Техническое превосходство** | Гибридная подпись (классическая + PQC по одному хешу) — рекомендуемый путь миграции; реализация демонстрирует инженерную дальновидность. |
| **Без поломок** | PQC **только добавляется**. Аудиторы, юристы и верификаторы по-прежнему опираются на классическую подпись и TSA; PQC — дополнительный слой на будущее. |

**Позиционирование:** *«Гибридная криптографическая аттестация: классическая + постквантовая подписи по одному хешу доказательства.»* — Не «мы всё переписали на PQC», а «мы добавили квантоустойчивый слой, чтобы доказательства оставались верифицируемыми десятилетиями».

---

## Стандарты и ссылки

| Ресурс | Описание | URL / ссылка |
|--------|----------|---------------|
| **Стандартизация NIST PQC** | NIST выбрал ML-DSA (Dilithium), ML-KEM (Kyber), SLH-DSA (SPHINCS+) и др. | [NIST PQC Project](https://csrc.nist.gov/projects/post-quantum-cryptography) |
| **FIPS 204** | ML-DSA (Module-Lattice-Based Digital Signature Standard) — стандарт подписей на основе Dilithium | [FIPS 204 (2024)](https://csrc.nist.gov/pubs/fips/204/final) |
| **FIPS 205** | SLH-DSA (Stateless Hash-Based Digital Signature Standard) | [FIPS 205 (2024)](https://csrc.nist.gov/pubs/fips/205/final) |
| **CRYSTALS-Dilithium** | Исходное имя алгоритма; в NIST стандартизирован как ML-DSA | [CRYSTALS-Dilithium](https://pq-crystals.org/dilithium/) |
| **Bouncy Castle PQC** | Реализация PQC-алгоритмов на Java (Dilithium, Kyber и др.) | [Bouncy Castle PQC](https://www.bouncycastle.org/pqc_jce.html) · Maven: `org.bouncycastle:bcpkix-jdk18on` + PQC provider |
| **ETSI / долгосрочное архивирование** | ETSI TS 101 733, PAdES; PQC для долгосрочной действительности подписи | [ETSI](https://www.etsi.org/) |
| **Миграция NIST PQC** | Рекомендации NIST по гибридам и миграции | [NIST PQC FAQ](https://csrc.nist.gov/projects/post-quantum-cryptography/post-quantum-cryptography-standardization) |

**Выбор алгоритма для PoC:** **ML-DSA (Dilithium)** — стандарт NIST, хорошая документация, доступен в Bouncy Castle; Falcon можно добавить позже.

---

## Принципы проектирования

1. **Двойная подпись по одному хешу**  
   `canonical.bin → SHA-256 → [подпись RSA] (классика) + [подпись ML-DSA] (PQC) → RFC 3161 timestamp` (время по-прежнему по классическому потоку; PQC для TSA вне scope, пока TSA не перейдут на PQC).

2. **Evidence Package остаётся обратно совместимым**  
   Новые файлы: `signature_pqc.sig`, `pqc_public_key.pem`, `pqc_algorithm.json`. Существующие `signature.sig`, `public_key.pem` и т.д. не меняются. Верификаторы без поддержки PQC игнорируют новые файлы и по-прежнему проверяют классику.

3. **Абстракция SignatureService**  
   Ввести (или расширить) абстракцию, чтобы сосуществовали несколько подписантов: классический (текущий RSA) и PQC (ML-DSA). Один хеш на входе — две подписи на выходе.

4. **Опционально в runtime**  
   PQC-подпись включается только при настроенном PQC-ключе (напр. `ai.aletheia.signing.pqc-key-path`). Если не задан — поведение как сейчас (нет PQC-файлов в Evidence Package).

---

## Результаты и задачи (LLM-readable)

*(Задачи PQC.1–PQC.9: формулировки и Coding prompt — на английском для прямой работы LLM; критерии приёмки приведены в таблицах ниже.)*

---

### PQC.1 — Backend: зависимость Bouncy Castle PQC и конфигурация PQC-ключа

| Поле | Значение |
|------|----------|
| **Оценка** | 0.5 ч |
| **Описание** | Добавить Bouncy Castle PQC и свойства конфигурации для пути к PQC-ключу. |

**Coding prompt (LLM-readable):** — см. [EN версию](../en/PLAN_PQC.md#pqc1--backend-add-bouncy-castle-pqc-dependency-and-pqc-key-configuration).

**Критерии приёмки:** сборка Maven с новой зависимостью успешна; приложение стартует с `pqc-enabled=false` без PQC-ключа.

---

### PQC.2 — Backend: генерация и хранение пары ключей ML-DSA (Dilithium)

| Поле | Значение |
|------|----------|
| **Оценка** | 1 ч |
| **Описание** | Утилита генерации пары ключей ML-DSA и сохранение рядом с `ai.key`. |

**Coding prompt (LLM-readable):** — см. [EN версию](../en/PLAN_PQC.md#pqc2--backend-generate-and-store-ml-dsa-dilithium-key-pair).

**Критерии приёмки:** запуск утилиты даёт файлы приватного и публичного ключа; формат загружается Bouncy Castle.

---

### PQC.3 — Backend: PqcSignatureService и пайплайн двойной подписи

| Поле | Значение |
|------|----------|
| **Оценка** | 2–3 ч |
| **Описание** | Реализовать PQC-сервис подписи по тому же хешу; встроить в пайплайн выработку обеих подписей при включённом PQC. |

**Coding prompt (LLM-readable):** — см. [EN версию](../en/PLAN_PQC.md#pqc3--backend-introduce-pqcsignatureservice-and-dual-signing-pipeline).

**Критерии приёмки:** unit-тест PqcSignatureService (sign/verify); при PQC enabled в сущности есть и signature, и signature_pqc; при disabled — signature_pqc null.

---

### PQC.4 — Backend: расширение Evidence Package PQC-артефактами

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | Добавить в пакет доказательств: signature_pqc.sig, pqc_public_key.pem, pqc_algorithm.json. |

**Coding prompt (LLM-readable):** — см. [EN версию](../en/PLAN_PQC.md#pqc4--backend-extend-evidence-package-with-pqc-artifacts).

**Критерии приёмки:** при PQC включён .aep содержит новые файлы; при выключен — их нет.

---

### PQC.5 — Backend: поле PQC в API (GET /api/ai/verify/:id)

| Поле | Значение |
|------|----------|
| **Оценка** | 0.5 ч |
| **Описание** | В DTO ответа verify добавить опциональные поля signaturePqc, pqcAlgorithm (и при необходимости pqcVerified). |

**Coding prompt (LLM-readable):** — см. [EN версию](../en/PLAN_PQC.md#pqc5--backend-expose-pqc-status-and-signature-in-api-get-apiaiverifyid).

**Критерии приёмки:** при наличии PQC в ответе JSON содержит signaturePqc и pqcAlgorithm; иначе — null/отсутствуют.

---

## Фронтенд: бейдж PQC и маркетинг

### PQC.6 — UI: бейдж «Quantum-Resistant» и краткий текст

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | На странице verify (и при желании на лендинге/developers) показывать индикатор «Quantum-Resistant» / «PQC Verified», когда в ответе есть PQC-подпись. |

**Coding prompt (LLM-readable):** — см. [EN версию](../en/PLAN_PQC.md#pqc6--ui-quantum-resistant-badge-and-short-copy).

**Критерии приёмки:** при наличии signaturePqc бейдж виден; при отсутствии — не показывается; есть tooltip и aria-label.

---

### PQC.7 — Маркетинговый текст и ассет бейджа (опционально)

| Поле | Значение |
|------|----------|
| **Оценка** | 0.5 ч |
| **Описание** | Одна строка в README/Vision и опционально ассет бейджа для лендинга и документации. |

**Coding prompt (LLM-readable):** — см. [EN версию](../en/PLAN_PQC.md#pqc7--marketing-copy-and-logobadge-asset-optional).

**Критерии приёмки:** в README/Vision есть фраза про PQC и ссылка; при наличии — ассет используется.

---

## Изменения в утилите верификатора

### PQC.8 — Verifier: чтение и проверка PQC-подписи из Evidence Package

| Поле | Значение |
|------|----------|
| **Оценка** | 2–3 ч |
| **Описание** | В офлайн-верификаторе (JAR) при наличии в .aep файлов signature_pqc.sig и pqc_public_key.pem проверять PQC-подпись по тому же хешу и выводить результат в отчёт. |

**Coding prompt (LLM-readable):** — см. [EN версию](../en/PLAN_PQC.md#pqc8--verifier-read-and-verify-pqc-signature-from-evidence-package).

**Критерии приёмки:** при .aep с PQC в отчёте есть статус PQC; при .aep без PQC — «PQC signature: not present»; JAR выводит строку PQC при наличии.

---

## Развёртывание (Ansible)

### PQC.9 — Ansible: опциональный PQC-ключ и переменные окружения

| Поле | Значение |
|------|----------|
| **Оценка** | 0.5 ч |
| **Описание** | В развёртывании через Ansible поддержать опциональные путь к PQC-ключу и флаг pqc-enabled. |

**Coding prompt (LLM-readable):** — см. [EN версию](../en/PLAN_PQC.md#pqc9--ansible-optional-pqc-key-and-env).

**Критерии приёмки:** в README Ansible описаны переменные PQC; при отключённом PQC бэкенд стартует без PQC; при включённом и заданном ключе — выдаёт PQC-подписи.

---

## Критерии завершения

| # | Критерий | Статус |
|---|----------|--------|
| 1 | Зависимость Bouncy Castle PQC добавлена; конфиг PQC (путь к ключу, enabled) настроен | [ ] |
| 2 | Утилита генерации ключей ML-DSA и файлы ключей описаны в документации | [ ] |
| 3 | PqcSignatureService реализован; двойная подпись в пайплайне; signature_pqc сохраняется в БД | [ ] |
| 4 | Evidence Package при включённом PQC содержит signature_pqc.sig, pqc_public_key.pem, pqc_algorithm.json | [ ] |
| 5 | GET /api/ai/verify/:id отдаёт signaturePqc и pqcAlgorithm при наличии | [ ] |
| 6 | Страница verify показывает бейдж «Quantum-Resistant» / «PQC Verified» при наличии PQC-подписи | [ ] |
| 7 | Офлайн-верификатор читает и проверяет PQC-подпись; в отчёте есть статус PQC | [ ] |
| 8 | Ansible (или развёртывание) поддерживает опциональный PQC-ключ и переменные окружения | [ ] |

---

## Риски и замечания

| Риск | Смягчение |
|------|-----------|
| Изменения API Bouncy Castle PQC | Зафиксировать версию зависимости; указать артефакт и версию в PLAN_PQC. |
| Размер ключа / производительность PQC | Dilithium3 приемлем для PoC; замерить время подписи. При необходимости — Dilithium2. |
| Размер JAR верификатора | PQC-провайдер может увеличить размер; зафиксировать в документации. |
| Регуляторные формулировки | Использовать «quantum-resistant» / «post-quantum» и «для долгосрочной верификации»; не заявлять сертификацию. |

---

## Ссылки

- [Подпись](SIGNING.md) — текущая RSA-подпись и путь к ключу.
- [Крипто-справочник (EN)](../en/CRYPTO_REFERENCE.md) — алгоритмы и ключи.
- [Модель доверия](TRUST_MODEL.md) — кто что аттестует.
- [Видение и дорожная карта](VISION_AND_ROADMAP.md) — направление продукта.
- [NIST PQC](https://csrc.nist.gov/projects/post-quantum-cryptography) · [FIPS 204 (ML-DSA)](https://csrc.nist.gov/pubs/fips/204/final) · [Bouncy Castle PQC](https://www.bouncycastle.org/pqc_jce.html).

**Переводы:** [EN](../en/PLAN_PQC.md) · [ET](../et/PLAN_PQC.md)
