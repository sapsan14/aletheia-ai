# 🏗️ Proof of Concept: Архитектура Aletheia AI

Документ описывает предлагаемую архитектуру PoC для системы верифицируемых ответов ИИ с криптографической подписью и временными метками.

**Связанные документы:** [Видение и дорожная карта](VISION_AND_ROADMAP.md) (шаги после PoC) · [План реализации](plan.md) · [Модель доверия](TRUST_MODEL.md)

---

## Архитектура

```
┌────────────┐
│  Frontend  │  (Next.js / React)
│            │
│ prompt →   │
└─────┬──────┘
      │
      ▼
┌──────────────────────┐
│ Backend API           │
│ (Node / Java / Python)│
│                       │
│ 1. send prompt to LLM │
│ 2. receive response   │
│ 3. canonicalize text  │
│ 4. hash (SHA-256)     │
│ 5. sign hash          │
│ 6. timestamp          │
│ 7. store in DB        │
└─────┬────────────────┘
      │
      ▼
┌──────────────┐
│ PostgreSQL   │
│              │
│ prompt       │
│ response     │
│ hash         │
│ signature    │
│ timestamp    │
│ metadata     │
└──────────────┘
```

**Итог:** это уже очень сильный PoC.

---

## 🔐 Криптография: что реально использовать

### Подпись (Signing)

#### 🔹 OpenSSL — лучший старт

**Почему:**

- минимальный
- стандартный
- понятен юристам
- легко проверить

**Пример:**

```bash
openssl dgst -sha256 -sign ai.key response.txt > signature.bin
```

- ✔ идеально для PoC  
- ✔ легко объяснить  
- ✔ без инфраструктурной боли  
- ➡️ **лучший выбор №1**

#### 🔹 Smallstep — второй этап

Использовать, если хочешь показать «PKI thinking»:

- `step-ca`
- X.509 cert
- автоматизация

**Но:** для PoC это уже «level 2». Начни с OpenSSL, потом можно заменить backend-модуль.

---

### ⏱️ Timestamping (TSA)

Варианты по возрастанию сложности:

| Уровень | Вариант | Описание |
|--------|---------|----------|
| 🟢 | **Локальный RFC 3161 TSA** | Идеально для PoC |

#### 🟢 Вариант 1 — локальный RFC 3161 TSA (идеально для PoC)

Можно поднять локальный TSA.

**Инструменты:**

- OpenSSL TSA
- OpenTSA
- simple RFC3161 server

**Плюсы:**

- полностью офлайн
- идеально демонстрирует идею
- стандарт RFC 3161

**Минусы:**

- доверие = твой сервер (но это PoC!)

➡️ **Идеальный вариант для старта.**

#### 🟡 Вариант 2 — публичные TSA

Например:

- DigiCert TSA
- GlobalSign TSA
- FreeTSA (ограниченно)

**Проблемы:** ограничения, rate limits, иногда платно. Можно оставить на «future work».

#### 🔴 eIDAS Qualified TSA

- ❌ не сейчас  
- ❌ не для PoC  
- ❌ дорого  

Можно честно написать в архитектуре: *«Architecture compatible with eIDAS Qualified TSA»* — и это будет корректно.

---

## 🗄️ База данных

**PostgreSQL** — да, 100%.

**Структура таблицы:**

```sql
ai_response (
  id,
  prompt,
  response,
  response_hash,
  signature,
  tsa_token,
  llm_model,
  created_at
)
```

**Опционально добавить:**

- `request_id`
- `temperature`
- `system_prompt`
- `version`

Это уже полноценный audit trail.

---

## 🤖 LLM — максимально просто

- **Один LLM** на старте.

**Выбор:**

- OpenAI (free credits)
- Gemini (часто generous free tier)
- Mistral (open/free)

Начни с одного.

**Важно:** логировать `model name`, `version`, `parameters` — потом это станет очень полезно для аудита.

---

## 🧑‍💻 Backend — что лучше именно тебе

С учётом профиля (PKI, enterprise):

### ✅ Java + Spring Boot

**Плюсы:**

- ты «дома»
- крипта — родная (BouncyCastle)
- Timestamp support

**Минус:** чуть тяжелее старт.

### ✅ Node.js

**Плюсы:**

- быстро
- OpenSSL через shell
- легко поднять

**Минус:** крипта менее «канонична» для enterprise.

### ⚠️ Python

Хорош для прототипа, но менее «enterprise».

---

**Рекомендация:** Java. С учётом глубины в PKI логично использовать BouncyCastle.

---

## 📦 Структура backend-модулей

```
backend/
├── llm/
│   └── LLMClient.java
├── crypto/
│   ├── HashService
│   ├── SignatureService
│   └── TimestampService
├── audit/
│   └── AuditRecordService
├── api/
│   └── AiController
└── db/
```

Это уже архитектура, а не демка.

---

## 🌐 Frontend (минимум)

- поле **prompt**
- кнопка **«Send»**
- ответ AI
- блок статуса:
  - ✔ signed  
  - ✔ timestamped  
  - ✔ verifiable  

И ссылка **«Verify this response»**, где можно:

- пересчитать hash
- проверить подпись
- показать TSA token

➡️ это даёт вау-эффект.

---

## 🧪 Dagster?

➡️ **Не нужен сейчас.**

Dagster — оркестрация пайплайнов. Имеет смысл добавить позже, если появятся:

- batch analysis
- scheduled AI verification
- nightly audit

Для PoC — лишнее.

---

## 🧭 Итог — рекомендуемый стек PoC

Минимальный, но мощный:

| Слой | Технология |
|------|------------|
| Frontend | Next.js |
| Backend | Java Spring Boot |
| Crypto | OpenSSL + BouncyCastle |
| Signing | локальный ключ RSA/ECDSA |
| Timestamp | RFC 3161 TSA (по умолчанию: DigiCert) |
| DB | PostgreSQL |
| LLM | один (Gemini / OpenAI / Mistral) |

---

## 💡 Почему этот PoC сильный

Ты делаешь не «AI that tells truth», а **AI whose answers can be proven**.

Это фундаментальное отличие — и это направление, куда реально движется регуляторика ЕС.

---

## 📋 Следующие шаги

1. 📐 диаграмма архитектуры (Mermaid)
2. 📄 README PoC как GitHub project
3. 🧪 пример RFC3161 timestamp flow
4. 🔐 пример BouncyCastle timestamp verification
5. 🗺️ roadmap: PoC → demo → EU-style product
roven**.

Это фундаментальное отличие — и это направление, куда реально движется регуляторика ЕС.

---

## 📋 Следующие шаги

1. 📐 диаграмма архитектуры (Mermaid)
2. 📄 README PoC как GitHub project
3. 🧪 пример RFC3161 timestamp flow
4. 🔐 пример BouncyCastle timestamp verification
5. 🗺️ roadmap: PoC → demo → EU-style product
