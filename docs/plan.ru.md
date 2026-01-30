# Aletheia AI — План реализации (русский)

Пошаговый план сборки PoC: верифицируемые ответы ИИ с криптографической подписью и RFC 3161 временными метками. У каждой задачи есть **инструкции для кодинга** (prompt для LLM или разработчика).

**Стек (из PoC):** Next.js, Java Spring Boot, PostgreSQL, OpenSSL/BouncyCastle, один LLM (OpenAI/Gemini/Mistral), локальный RFC 3161 TSA.

---

## Этап 1 — Настройка проекта и каркас

**Цель:** Структура репозитория, backend и frontend проекты, заготовка схемы БД.  
**Оценка:** 6–8 ч

### Задача 1.1 — Репозиторий и документация

| Поле | Значение |
|------|----------|
| **Оценка** | 1 ч |
| **Описание** | Инициализировать репозиторий (при необходимости), добавить README с целью проекта и инструкциями по запуску backend/frontend/БД. Добавить диаграмму архитектуры Mermaid по PoC (frontend → backend API → PostgreSQL). |

**Инструкция для кодинга (LLM-readable):**
- Создать или обновить README.md: название проекта "Aletheia AI", одна строка цели "Верифицируемые ответы ИИ с подписью и временными метками". Разделы: Требования, Запуск backend, Запуск frontend, Запуск PostgreSQL (или Docker). Ссылка на docs/PoC.
- Добавить docs/architecture.md или раздел в README с одной Mermaid-диаграммой: поток от Frontend (Next.js) к Backend API (шаги: отправка prompt → LLM → canonicalize → hash → sign → timestamp → store) к PostgreSQL. Прямоугольники и стрелки; подписать каждый шаг. Код не писать; только документация.

---

### Задача 1.2 — Каркас backend (Java Spring Boot)

| Поле | Значение |
|------|----------|
| **Оценка** | 2 ч |
| **Описание** | Создать приложение Spring Boot с пакетами: llm, crypto, audit, api, db. Без бизнес-логики; один health/readiness endpoint. |

**Инструкция для кодинга (LLM-readable):**
- Создать новый проект Spring Boot 3.x (Maven или Gradle). Java 21+. Базовый пакет например ai.aletheia.
- Создать пустую структуру пакетов: ai.aletheia.llm, ai.aletheia.crypto, ai.aletheia.audit, ai.aletheia.api, ai.aletheia.db. Классы пока не обязательны, кроме одного REST-контроллера в api с GET /actuator/health или GET /health, возвращающим 200 и JSON вида {"status":"UP"}.
- Добавить зависимости: spring-boot-starter-web, spring-boot-starter-data-jpa (на потом), BouncyCastle (bcpkix, bcprov). В этой задаче не реализовывать логику LLM, crypto или БД.

---

### Задача 1.3 — Каркас frontend (Next.js)

| Поле | Значение |
|------|----------|
| **Оценка** | 1.5 ч |
| **Описание** | Приложение Next.js с одной страницей: заглушка для ввода prompt и области ответа. Без вызовов API. |

**Инструкция для кодинга (LLM-readable):**
- Создать Next.js приложение (App Router). Одна страница "/" с: текстовое поле "Prompt", кнопка "Send", блок для "Response". К backend не подключать; кнопка может быть disabled или показывать "Скоро". Минимальные стили (Tailwind или обычный CSS). Запуск через npm run dev, отображается layout. Переменные окружения для API URL пока не нужны.

---

### Задача 1.4 — PostgreSQL и схема

| Поле | Значение |
|------|----------|
| **Оценка** | 2 ч |
| **Описание** | Определить и применить схему для таблицы ai_response. Опционально: Docker Compose для локального Postgres. |

**Инструкция для кодинга (LLM-readable):**
- Определить SQL-схему таблицы ai_response с колонками: id (UUID или bigserial PK), prompt (text), response (text), response_hash (varchar 64 или bytea), signature (bytea или text/base64), tsa_token (bytea или text), llm_model (varchar), created_at (timestamptz). Опционально: request_id, temperature, system_prompt, version по PoC.
- Предоставить: (a) миграцию Flyway/Liquibase, создающую таблицу, или (b) отдельный SQL-файл для ручного запуска. При использовании Docker: docker-compose.yml с сервисом postgres, образ postgres:15-alpine, порт 5432, env POSTGRES_DB=aletheia. В README описать запуск БД и миграций.

---

## Этап 2 — Крипто-слой

**Цель:** Канонизация, хеш, подпись, временная метка. Вся логика в сервисах; REST пока не трогать.  
**Оценка:** 10–14 ч

### Задача 2.1 — Канонизация текста

| Поле | Значение |
|------|----------|
| **Оценка** | 2 ч |
| **Описание** | Реализовать детерминированную каноническую форму текста ответа LLM, чтобы один и тот же смысл давал одни и те же байты перед хешированием. |

**Инструкция для кодинга (LLM-readable):**
- Реализовать функцию канонизации (утилита или CanonicalizationService). Вход: строка (ответ LLM). Выход: byte[] (UTF-8), детерминированный. Правила: (1) Нормализация Unicode в NFC. (2) Переносы строк в \n. (3) Убрать trailing whitespace в каждой строке, схлопнуть пустые строки в одну. (4) В конце файла — без завершающего \n или ровно один; зафиксировать и задокументировать. Кодировка результата UTF-8. Юнит-тесты: один и тот же вход → один и тот же byte[]; строки, отличающиеся только \r\n vs \n, дают один результат. Язык: Java (или тот же, что backend). REST и БД в этой задаче не подключать.

---

### Задача 2.2 — HashService (SHA-256)

| Поле | Значение |
|------|----------|
| **Оценка** | 1 ч |
| **Описание** | Сервис: хеш канонических байтов SHA-256, возврат строки в hex (или фиксированный формат). |

**Инструкция для кодинга (LLM-readable):**
- Создать HashService (интерфейс + impl). Метод: hash(canonicalBytes: byte[]) → String (64 символа hex). Использовать стандартный MessageDigest (SHA-256); BouncyCastle для хеширования не требуется. Без внешнего I/O. Юнит-тест: хеш известной строки (например "hello\n" после канонизации) и сравнение с известным SHA-256 hex. Интеграция с канонизацией: вход строка → канонические байты → hash; в документации указать, что вызывающий передаёт уже канонические байты или один метод принимает строку и делает canonicalize+hash.

---

### Задача 2.3 — SignatureService (BouncyCastle, RSA или ECDSA)

| Поле | Значение |
|------|----------|
| **Оценка** | 3 ч |
| **Описание** | Подписать хеш (или канонические байты) приватным ключом; проверка: по хешу и подписи вернуть valid/invalid. |

**Инструкция для кодинга (LLM-readable):**
- Создать SignatureService. Загрузка приватного ключа из файла или env (путь к PEM или материал ключа). BouncyCastle, RSA или ECDSA. Методы: (1) sign(hashHex: String или hashBytes: byte[]) → byte[] или Base64. (2) verify(hashHex, signature) → boolean. Формат ключа: PEM, сгенерированный OpenSSL или кодом. Документировать генерацию (например openssl genpkey -algorithm RSA -out ai.key). Юнит-тесты: подписать известный хеш, verify true; подделанная подпись → false. TSA в этой задаче не вызывать. PoC: один ключ; ротация ключей вне скоупа.

---

### Задача 2.4 — TimestampService (локальный RFC 3161 TSA)

| Поле | Значение |
|------|----------|
| **Оценка** | 4–5 ч |
| **Описание** | Запрос RFC 3161 временной метки у TSA (локальный сервер или заглушка). Вход: хеш ответа (или подписи). Выход: TSA token (байты или Base64). |

**Инструкция для кодинга (LLM-readable):**
- Создать TimestampService: запрос RFC 3161 к настраиваемому URL TSA (например http://localhost:3180). Вход: digest (SHA-256 байты данных для метки — например хеш ответа или подпись; выбор задокументировать). Выход: timestamp token byte[] или Base64 String. BouncyCastle TSP: сформировать запрос с digest, HTTP POST к TSA, разобрать ответ и извлечь token. Ошибки соединения и невалидный ответ: Optional или исключение; задокументировать. Если TSA нет: (a) заглушка для тестов, возвращающая фиксированную последовательность байт, или (b) в README описать запуск простого RFC 3161 сервера. Юнит-тест: с mock или реальным локальным TSA запросить метку для известного хеша, проверить что token не пустой и парсится. Сам TSA-сервер в этой задаче не реализовывать (кроме минимальной заглушки при необходимости).

---

## Этап 3 — Интеграция LLM

**Цель:** Один клиент к одному провайдеру LLM; возврат текста и метаданных модели.  
**Оценка:** 4–6 ч

### Задача 3.1 — LLM-клиент (один провайдер)

| Поле | Значение |
|------|----------|
| **Оценка** | 3 ч |
| **Описание** | Интеграция с одним LLM API: OpenAI, Gemini или Mistral. Принять prompt; вернуть текст ответа и идентификатор модели. |

**Инструкция для кодинга (LLM-readable):**
- Создать LLMClient (интерфейс + impl). Интерфейс: метод complete(prompt: String) → LLMResult, где LLMResult: responseText: String, modelId: String (например "gpt-4", "gemini-pro"). Реализация: один провайдер (OpenAI, Gemini или Mistral). Официальный SDK или HTTP-клиент. API-ключ из переменной окружения (OPENAI_API_KEY или GEMINI_API_KEY). В этой задаче не канонизировать и не хешировать; только вызов LLM и возврат text + model. Ошибки (rate limit, timeout, неверный ключ) — явные исключения или Result. Юнит-тест: mock или реальный API с коротким prompt; ответ не пустой, modelId задан. В README перечислить нужные env.

---

### Задача 3.2 — Логирование model name, version, parameters

| Поле | Значение |
|------|----------|
| **Оценка** | 1 ч |
| **Описание** | Каждый вызов LLM должен сохранять идентификатор модели и при возможности параметры для аудита. |

**Инструкция для кодинга (LLM-readable):**
- Расширить LLMResult или вызывающий код: сохранять model name/id и при наличии из API — version или parameters (temperature и т.п.). Эти поля пойдут в ai_response (llm_model и опционально version/parameters в metadata). Одно место (например AuditRecord или контекст запроса), где эти значения задаются из LLMResult. Новый API не добавлять; только обеспечить доступность данных для слоя аудита. Опционально: логировать в slf4j по каждому запросу (model, длина prompt, длина ответа).

---

## Этап 4 — Аудит и персистентность

**Цель:** Сохранение prompt, response, hash, signature, tsa_token, llm_model, created_at в PostgreSQL.  
**Оценка:** 4–6 ч

### Задача 4.1 — Сущность ai_response и репозиторий

| Поле | Значение |
|------|----------|
| **Оценка** | 2 ч |
| **Описание** | JPA-сущность и репозиторий для таблицы ai_response. |

**Инструкция для кодинга (LLM-readable):**
- Создать JPA-сущность AiResponse с полями: id (UUID или Long), prompt, response, responseHash, signature (byte[] или Base64), tsaToken (byte[] или string), llmModel, createdAt. Маппинг на таблицу ai_response. Spring Data JPA: AiResponseRepository с save и findById. Миграция Flyway/Liquibase, если не сделана в этапе 1. Юнит-тест: сохранить одну сущность, findById, проверить все поля. Для тестов — H2 или Testcontainers.

---

### Задача 4.2 — AuditRecordService (оркестрация сохранения)

| Поле | Значение |
|------|----------|
| **Оценка** | 2–3 ч |
| **Описание** | Сервис принимает prompt, response, hash, signature, tsa_token, llm_model (и опционально metadata) и сохраняет одну запись. |

**Инструкция для кодинга (LLM-readable):**
- Создать AuditRecordService с методом save(request: AuditRecordRequest): AuditRecordId. AuditRecordRequest: prompt, response, responseHash, signature, tsaToken, llmModel, опционально requestId, temperature, systemPrompt, version. Сервис генерирует id и created_at, маппит в AiResponse, вызывает repository.save. Возвращает сохранённый id. LLM и крипто в этом сервисе не вызывать; только персистентность. Юнит-тест: сохранить одну запись, загрузить по id, проверить поля. Этот сервис будет вызываться из API после вычисления hash/sign/timestamp.

---

## Этап 5 — Backend API

**Цель:** REST endpoint: принять prompt, вызвать LLM, канонизировать, хеш, подпись, метка времени, сохранение, вернуть ответ и данные для верификации.  
**Оценка:** 6–8 ч

### Задача 5.1 — POST /api/ai/ask (или /chat)

| Поле | Значение |
|------|----------|
| **Оценка** | 4–5 ч |
| **Описание** | Один endpoint: body { "prompt": "..." }; цепочка: LLM → canonicalize → hash → sign → timestamp → store; ответ: { "response", "responseHash", "signature", "tsaToken", "id", "model" }. |

**Инструкция для кодинга (LLM-readable):**
- Создать REST-контроллер (AiController). POST /api/ai/ask (или /api/chat). Тело: JSON с полем "prompt" (string). Цепочка: (1) LLMClient.complete(prompt) → responseText, modelId. (2) Канонизация responseText → canonical bytes. (3) HashService.hash(canonicalBytes) → responseHash. (4) SignatureService.sign(responseHash) → signature. (5) TimestampService.getTimestamp(байты responseHash или signature) → tsaToken. (6) AuditRecordService.save(prompt, response, responseHash, signature, tsaToken, modelId, ...) → id. (7) Ответ JSON: response (оригинал или каноническая строка), responseHash (hex), signature (Base64), tsaToken (Base64), id, model (modelId). DTO для request/response. При ошибке LLM или TSA — 502 или 503 с понятным сообщением. Интеграционный тест: вызов endpoint с коротким prompt, 200, в теле response, responseHash, signature, tsaToken, id; в БД одна строка с тем же id и hash.

---

### Задача 5.2 — GET /api/ai/verify/:id (рекомендуется)

| Поле | Значение |
|------|----------|
| **Оценка** | 2 ч |
| **Описание** | Вернуть сохранённую запись по id для страницы верификации: prompt, response, hash, signature, tsa_token, model, created_at. |

**Инструкция для кодинга (LLM-readable):**
- Добавить GET /api/ai/verify/{id}. Загрузить AiResponse по id из репозитория. Ответ JSON: prompt, response, responseHash, signature, tsaToken, llmModel, createdAt. Если не найден — 404. Логику верификации в endpoint не закладывать; только отдача данных для страницы верификации во frontend.

---

## Этап 6 — Frontend

**Цель:** UI: ввод prompt, Send, отображение ответа и статуса (signed, timestamped, verifiable); ссылка на страницу верификации.  
**Оценка:** 6–8 ч

### Задача 6.1 — Поле prompt и кнопка Send

| Поле | Значение |
|------|----------|
| **Оценка** | 2 ч |
| **Описание** | Подключить поле prompt и кнопку Send к POST /api/ai/ask; показывать загрузку и ошибки. |

**Инструкция для кодинга (LLM-readable):**
- В Next.js: по нажатию "Send" читать prompt из input/textarea. POST на backend (/api/ai/ask), body { prompt }. API base URL из env (NEXT_PUBLIC_API_URL). Показывать состояние загрузки (disabled кнопка или спиннер). При успехе сохранять ответ в state. При 4xx/5xx показывать сообщение об ошибке. Ответ и верификацию пока не рендерить; только получение данных и обработка ошибок.

---

### Задача 6.2 — Отображение ответа и статуса (signed, timestamped, verifiable)

| Поле | Значение |
|------|----------|
| **Оценка** | 2 ч |
| **Описание** | Показать текст ответа ИИ и блок статуса: ✔ signed, ✔ timestamped, ✔ verifiable (по наличию signature и tsaToken). Ссылка "Verify this response" на /verify?id=... |

**Инструкция для кодинга (LLM-readable):**
- После успешного POST: вывести response.response в отдельном блоке. Блок статуса с тремя галочками: "Signed", "Timestamped", "Verifiable" — показывать как да, если response.signature и response.tsaToken не пусты. Ссылка/кнопка "Verify this response" ведёт на /verify?id={response.id}. Id брать из ответа API. Логику верификации (проверка хеша, подписи) в этой задаче не делать; только отображение и переход.

---

### Задача 6.3 — Страница Verify: хеш, подпись, TSA token

| Поле | Значение |
|------|----------|
| **Оценка** | 2–3 ч |
| **Описание** | Страница /verify?id=... запрашивает GET /api/ai/verify/:id и показывает prompt, response, hash, signature (сниппет Base64), TSA token (сниппет), model, дату. Опционально: проверка хеша на клиенте (пересчёт после канонизации). |

**Инструкция для кодинга (LLM-readable):**
- Создать маршрут /verify. Id из query (?id=...). Запрос GET /api/ai/verify/{id}. Отобразить: prompt, текст ответа, responseHash (полностью или обрезанный), signature (Base64, первые/последние 20 символов + "..."), tsaToken (аналогично), llmModel, createdAt. Опционально: кнопка "Verify hash" — (1) канонизировать отображаемый response в браузере (те же правила, что backend), (2) SHA-256 (Web Crypto API или небольшая библиотека), (3) сравнить с сохранённым responseHash и показать "Match" / "Mismatch". Полную проверку подписи в браузере для PoC можно не делать; достаточно отображения данных.

---

## Этап 7 — Верификация и документация

**Цель:** Опциональный endpoint верификации на backend; README и инструкции по запуску.  
**Оценка:** 2–4 ч

### Задача 7.1 — Backend: проверка хеша и подписи (опционально)

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | GET /api/ai/verify/:id может дополнительно возвращать hashMatch (пересчёт хеша от сохранённого response) и signatureValid (SignatureService.verify). |

**Инструкция для кодинга (LLM-readable):**
- Опционально расширить ответ GET /api/ai/verify/:id (или добавить GET /api/ai/verify/:id/check): hashMatch (boolean — пересчитать хеш от сохранённого response с той же канонизацией, сравнить с responseHash), signatureValid (boolean — SignatureService.verify(storedHash, storedSignature)). Ответ JSON: { ...record, hashMatch, signatureValid }. Frontend может показать "Hash match: yes/no", "Signature valid: yes/no". Реализовывать только при наличии времени; иначе оставить на потом.

---

### Задача 7.2 — README и инструкции по запуску

| Поле | Значение |
|------|----------|
| **Оценка** | 1–2 ч |
| **Описание** | README: как сгенерировать ключ, задать env, запустить PostgreSQL, backend, frontend, локальный TSA (если есть). Список env: API-ключи, URL БД, URL TSA, путь к ключу. |

**Инструкция для кодинга (LLM-readable):**
- Обновить README: (1) Требования: Java 21+, Node 18+, PostgreSQL 15+, опционально Docker. (2) Переменные окружения: OPENAI_API_KEY или GEMINI_API_KEY (или Mistral), URL БД, URL TSA, путь к приватному ключу для подписи. (3) Генерация ключа: openssl genpkey -algorithm RSA -out ai.key. (4) Запуск БД: docker-compose up -d или локальная установка, миграции. (5) Запуск backend: ./mvnw spring-boot:run с выставленным env. (6) Запуск frontend: npm run dev, NEXT_PUBLIC_API_URL. (7) Опционально: запуск локального RFC 3161 TSA для разработки. Разделы кратко; ссылка на docs/PoC для архитектуры.

---

## Сводка — Оценка по времени

| Этап | Часы |
|------|------|
| 1 — Настройка проекта и каркас | 6–8 |
| 2 — Крипто-слой | 10–14 |
| 3 — Интеграция LLM | 4–6 |
| 4 — Аудит и персистентность | 4–6 |
| 5 — Backend API | 6–8 |
| 6 — Frontend | 6–8 |
| 7 — Верификация и документация | 2–4 |
| **Итого** | **38–54** |

---

## Тестирование (по шагам)

Описание тестов и критериев приёмки по каждому шагу. Backend: `mvn test` из каталога `backend/`; frontend: `npm test` при наличии.

| Шаг | Тип теста | Что тестировать | Критерий приёмки |
|-----|------------|-----------------|------------------|
| **1.1** | Ручная проверка / док | README и диаграмма | README: Prerequisites, Run backend/frontend/DB; Mermaid рендерится; ссылка на docs/PoC. |
| **1.2** | Unit | Health endpoint | GET /health → 200 и JSON {"status":"UP"}. @WebMvcTest(HealthController.class) + MockMvc. |
| **1.2** | Интеграция | Контекст | @SpringBootTest — контекст поднимается без ошибок. |
| **1.3** | Ручная | Frontend | npm run dev; страница: Prompt, Send, Response. |
| **1.4** | Ручная / миграция | Схема БД | Liquibase/Flyway выполняется; таблица ai_response с нужными колонками. |
| **2.1** | Unit | Канонизация | Один и тот же текст → одни и те же байты; \r\n vs \n → один результат. |
| **2.2** | Unit | HashService | Известный вход → известный SHA-256 hex (64 символа). |
| **2.3** | Unit | SignatureService | sign(hash), затем verify(hash, signature) → true; подделанная подпись → false. |
| **2.4** | Unit | TimestampService | Mock TSA: запрос возвращает непустой token; невалидный ответ обрабатывается. |
| **3.1** | Unit | LLMClient | Mock: complete(prompt) возвращает непустой текст и modelId. |
| **3.2** | — | Поток данных | LLMResult / audit содержит model id и опционально параметры. |
| **4.1** | Unit | Repository | save(entity); findById(id); проверить поля. H2 или Testcontainers. |
| **4.2** | Unit | AuditRecordService | save(request) → id; загрузка по id, проверка полей. |
| **5.1** | Интеграция | POST /api/ai/ask | 200; в теле response, responseHash, signature, tsaToken, id, model; в БД одна запись. |
| **5.2** | Unit / интеграция | GET /api/ai/verify/:id | 200 с записью; 404 для неизвестного id. |
| **6.1** | Ручная / E2E | Frontend → backend | Отправка prompt → ответ и статус; при ошибке показывается сообщение. |
| **6.2** | Ручная | UI | Блоки signed, timestamped, verifiable; ссылка "Verify" с id. |
| **6.3** | Ручная | Страница Verify | Загрузка по id; отображение hash, signature, TSA token; опционально проверка hash на клиенте. |
| **7.1** | Unit | Верификация | hashMatch и signatureValid в ответе при реализации. |
| **7.2** | Ручная | README | Описаны запуск и все env. |

**Команда тестов backend:** из каталога `backend/`: `./mvnw test` или `mvn test`. Для тестов используется H2 (профиль по умолчанию).

---

**Условное обозначение:** Блок «Инструкция для кодинга (LLM-readable)» у каждой задачи можно копировать и передавать LLM или разработчику для реализации. В нём указано: что делать, входы/выходы, стек, тесты и чего не делать. Оценки и объём можно корректировать под команду.
