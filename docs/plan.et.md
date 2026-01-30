# Aletheia AI — Rakendusplaan (eesti keeles)

Samm-sammuline plaan PoC koostamiseks: kontrollitavad AI vastused krüptograafilise allkirjastamise ja RFC 3161 ajatemplitega. Iga ülesande juures on **koodi juhendid** (prompt LLM-le või arendajale).

**Stekk (PoC põhjal):** Next.js, Java Spring Boot, PostgreSQL, OpenSSL/BouncyCastle, üks LLM (OpenAI/Gemini/Mistral), lokaalne RFC 3161 TSA.

---

## Samm 1 — Projekti seadistus ja raam

**Eesmärk:** Repo struktuur, backend- ja frontend-projektid, andmebaasi skeemi mustand.  
**Hinnang:** 6–8 t

### Ülesanne 1.1 — Repo ja dokumentatsioon

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1 t |
| **Kirjeldus** | Initsialiseeri repo (vajadusel), lisa README projekti eesmärgiga ja juhistega backend/frontend/DB käivitamiseks. Lisa Mermaid arhitektuuri diagramm vastavalt PoC-le (frontend → backend API → PostgreSQL). |

**Koodi juhend (LLM-readable):**
- Loo või uuenda README.md: projekti nimi "Aletheia AI", üherealune eesmärk "Kontrollitavad AI vastused allkirjastamise ja ajatemplitega". Sektsioonid: Eeldused, Backend käivitamine, Frontend käivitamine, PostgreSQL käivitamine (või Docker). Viide docs/PoC-le.
- Lisa docs/architecture.md või sektsioon README-s ühe Mermaid diagrammiga: voog Frontendist (Next.js) Backend API-le (samud: prompt saatmine → LLM → canonicalize → hash → sign → timestamp → store) PostgreSQLi. Kasuta ristkülikuid ja nooli; märgi iga samm. Koodi ei kirjuta; ainult dokumentatsioon.

---

### Ülesanne 1.2 — Backend raam (Java Spring Boot)

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 2 t |
| **Kirjeldus** | Loo Spring Boot rakendus pakettidega: llm, crypto, audit, api, db. Äriloogikat veel mitte; üks health/readiness endpoint. |

**Koodi juhend (LLM-readable):**
- Loo uus Spring Boot 3.x projekt (Maven või Gradle). Java 21+. Baaspakett nt ai.aletheia.
- Loo tühi pakettide struktuur: ai.aletheia.llm, ai.aletheia.crypto, ai.aletheia.audit, ai.aletheia.api, ai.aletheia.db. Klasse pole veel vaja, välja arvatud üks REST kontroller api all GET /actuator/health või GET /health, tagastab 200 ja lihtsa JSONi nt {"status":"UP"}.
- Lisa sõltuvused: spring-boot-starter-web, spring-boot-starter-data-jpa (hiljem), BouncyCastle (bcpkix, bcprov). Selles ülesandes ära rakenda LLM-, crypto- ega DB-loogikat.

---

### Ülesanne 1.3 — Frontend raam (Next.js)

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1.5 t |
| **Kirjeldus** | Next.js rakendus ühe lehega: placeholder prompti sisestuseks ja vastuse ala. API päringuid veel mitte. |

**Koodi juhend (LLM-readable):**
- Loo Next.js rakendus (App Router). Üks leht "/" koos: tekstiväli "Prompt", nupp "Send", plokk "Response". Backendiga ära ühenda; nupp võib olla disabled või näidata "Tulekul". Minimaalsed stiilid (Tailwind või tavaline CSS). Käivitus npm run dev, kuvatakse layout. API URLi env muutujaid veel pole vaja.

---

### Ülesanne 1.4 — PostgreSQL ja skeem

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 2 t |
| **Kirjeldus** | Defineeri ja rakenda ai_response tabeli skeem. Valikuliselt: Docker Compose lokaalse Postgresi jaoks. |

**Koodi juhend (LLM-readable):**
- Defineeri SQL skeem tabelile ai_response veergudega: id (UUID või bigserial PK), prompt (text), response (text), response_hash (varchar 64 või bytea), signature (bytea või text/base64), tsa_token (bytea või text), llm_model (varchar), created_at (timestamptz). Valikuliselt: request_id, temperature, system_prompt, version nagu PoC-s.
- Paku: (a) Flyway/Liquibase migratsioon, mis loob tabeli, või (b) eraldi SQL fail käsitsi käivitamiseks. Dockeriga: docker-compose.yml teenusega postgres, image postgres:15-alpine, port 5432, env POSTGRES_DB=aletheia. README-s kirjelda DB käivitamine ja migratsioonide käivitamine.

---

## Samm 2 — Krüpto kiht

**Eesmärk:** Kanoniseerimine, räsi, allkiri, ajatempel. Kogu loogika teenustes; REST veel mitte.  
**Hinnang:** 10–14 t

### Ülesanne 2.1 — Teksti kanoniseerimine

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 2 t |
| **Kirjeldus** | Rakenda deterministlik kanooniline vorm LLM vastuse teksti jaoks, et sama loogiline sisu annaks alati samad baitid enne räsimist. |

**Koodi juhend (LLM-readable):**
- Rakenda kanoniseerimise funktsioon (utility või CanonicalizationService). Sisend: string (LLM vastus). Väljund: byte[] (UTF-8), deterministlik. Reeglid: (1) Unicode normaliseerimine NFC. (2) Rea lõpud \n. (3) Eemalda iga rea lõpust tühikud, ahenda järjestikused tühjad read üheks. (4) Faili lõpus — ilma lõpus reata või täpselt üks; vali üks reegel ja dokumenteeri. UTF-8 lõpptulemuseks. Üksiktestid: sama sisend → sama byte[]; kaks stringi erinevusega ainult \r\n vs \n annavad sama tulemuse. Keel: Java (või sama mis backend). RESTi ja DB-d selles ülesandes ära ühenda.

---

### Ülesanne 2.2 — HashService (SHA-256)

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1 t |
| **Kirjeldus** | Teenus: kanooniliste baitide SHA-256 räsi, tagasta hex string (või fikseeritud vorming). |

**Koodi juhend (LLM-readable):**
- Loo HashService (liides + impl). Meetod: hash(canonicalBytes: byte[]) → String (64 tähemärki hex). Kasuta standardset MessageDigest (SHA-256); BouncyCastle räsimiseks pole vaja. Välise I/O-d pole. Üksiktest: teadaoleva stringi räsi (nt "hello\n" pärast kanoniseerimist) ja võrdle teadaoleva SHA-256 hex väärtusega. Integreeri kanoniseerimisega: sisend string → kanoonilised baitid → hash; dokumenteeri, et kutsuja edastab juba kanoonilised baitid või üks meetod võtab stringi ja teeb canonicalize+hash.

---

### Ülesanne 2.3 — SignatureService (BouncyCastle, RSA või ECDSA)

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 3 t |
| **Kirjeldus** | Allkirjasta räsi (või kanoonilised baitid) privaatvõtmega; kontroll: räsi + allkiri → valid/invalid. |

**Koodi juhend (LLM-readable):**
- Loo SignatureService. Laadi privaatvõti failist või env (PEM tee või võtme materjal). BouncyCastle RSA või ECDSA. Meetodid: (1) sign(hashHex: String või hashBytes: byte[]) → byte[] või Base64 string. (2) verify(hashHex, signature) → boolean. Võtme vorming: PEM, OpenSSL või koodiga genereeritud. Dokumenteeri genereerimine (nt openssl genpkey -algorithm RSA -out ai.key). Üksiktestid: allkirjasta teadaolev räsi, verify true; võltsitud allkiri → false. TSA-d selles ülesandes ära kutsu. PoC: üks võti; võtme rotatsioon väljaspool ulatust.

---

### Ülesanne 2.4 — TimestampService (lokaalne RFC 3161 TSA)

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 4–5 t |
| **Kirjeldus** | Päring RFC 3161 ajatemplile TSA-st (lokaalne server või stub). Sisend: vastuse räsi (või allkiri). Väljund: TSA vastuse token (baitid või Base64). |

**Koodi juhend (LLM-readable):**
- Loo TimestampService, mis küsib RFC 3161 ajatemplit konfigureeritava TSA URLi järgi (nt http://localhost:3180). Sisend: digest (SHA-256 räsi baitidega andmetest, mida ajatempliga märgitud — nt vastuse räsi või allkiri; vali üks ja dokumenteeri). Väljund: ajatempli token byte[] või Base64 String. BouncyCastle TSP: genereeri päring digestiga, HTTP POST TSA-le, parseeri vastus ja võta token. Ühenduse vead ja kehtetu vastus: Optional või erand; dokumenteeri. Kui TSA serverit pole: (a) stub/mock, mis tagastab testide jaoks fikseeritud baitide jada, või (b) README-s kirjelda lihtsa RFC 3161 serveri käivitamine. Üksiktest: mock või reaalse lokaalse TSA-ga päri ajatempel teadaoleva räsi jaoks, kontrolli et token pole tühi ja parseeritav. TSA serverit enda selles ülesandes ära rakenda (v.a. minimaalne stub vajadusel).

---

## Samm 3 — LLM integratsioon

**Eesmärk:** Üks klient ühe LLM pakkuja poole; tagasta plain text ja mudeli metaandmed.  
**Hinnang:** 4–6 t

### Ülesanne 3.1 — LLM klient (üks pakkuja)

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 3 t |
| **Kirjeldus** | Integreeri üks LLM API: OpenAI, Gemini või Mistral. Võta vastu prompt string; tagasta vastuse tekst ja mudeli identifikaator. |

**Koodi juhend (LLM-readable):**
- Loo LLMClient (liides + impl). Liides: meetod complete(prompt: String) → LLMResult, kus LLMResult sisaldab: responseText: String, modelId: String (nt "gpt-4", "gemini-pro"). Implementatsioon: üks pakkuja (OpenAI, Gemini või Mistral). Ametlik SDK või HTTP klient. API võti keskkonnamuutujast (OPENAI_API_KEY või GEMINI_API_KEY). Selles ülesandes ära kanoniseeri ega räsi; ainult kutsu LLM ja tagasta text + model. Vead (rate limit, timeout, vale võti) — selged erandid või Result tüüp. Üksiktest: mock või reaalse API-ga lühikese promptiga; kontrolli et vastus pole tühi ja modelId seatud. README-s loetle vajalikud env.

---

### Ülesanne 3.2 — Logi mudeli nimi, versioon, parameetrid

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1 t |
| **Kirjeldus** | Iga LLM päring peab salvestama mudeli identifikaatori ja võimalusel parameetrid auditi jaoks. |

**Koodi juhend (LLM-readable):**
- Laienda LLMResult või kutsuja: salvesta mudeli nimi/id ja API-st võimalusel versioon või parameetrid (nt temperature). Need väljad lähevad ai_response-sse (llm_model ja valikuliselt version/parameters metadata-s). Üks koht (nt AuditRecord või päringu kontekst), kus need väärtused seatakse LLMResult-ist. Uut API-d ära lisa; ainult tagada andmete kättesaadavus auditi kihile. Valikuliselt: logi slf4j-sse iga päringu kohta (mudel, prompti pikkus, vastuse pikkus).

---

## Samm 4 — Audit ja persisteerimine

**Eesmärk:** Salvesta prompt, response, hash, signature, tsa_token, llm_model, created_at PostgreSQLi.  
**Hinnang:** 4–6 t

### Ülesanne 4.1 — ai_response üksus ja repository

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 2 t |
| **Kirjeldus** | JPA üksus (või ekvivalent) ja repository tabelile ai_response. |

**Koodi juhend (LLM-readable):**
- Loo JPA üksus AiResponse väljadega: id (UUID või Long), prompt, response, responseHash, signature (byte[] või Base64), tsaToken (byte[] või string), llmModel, createdAt. Vastendus tabeliga ai_response. Spring Data JPA repository: AiResponseRepository save ja findById-ga. Lisa Flyway/Liquibase migratsioon kui ei tehtud Sammus 1. Üksiktest: salvesta üks üksus, findById, kontrolli kõik väljad. Testideks H2 või Testcontainers.

---

### Ülesanne 4.2 — AuditRecordService (salvestamise orkestreerimine)

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 2–3 t |
| **Kirjeldus** | Teenus võtab vastu prompt, response, hash, signature, tsa_token, llm_model (ja valikuliselt metadata) ja persisteerib ühe rea. |

**Koodi juhend (LLM-readable):**
- Loo AuditRecordService meetodiga save(request: AuditRecordRequest): AuditRecordId. AuditRecordRequest sisaldab: prompt, response, responseHash, signature, tsaToken, llmModel, valikuliselt requestId, temperature, systemPrompt, version. Teenus genereerib id ja created_at, vastendab AiResponse üksusega, kutsub repository.save. Tagasta salvestatud id. LLM-i ja krüptot selles teenuses ära kutsu; ainult persisteerimine. Üksiktest: salvesta üks kirje, laadi id järgi, kontrolli väljad. Seda teenust kutsutakse API kihilt pärast hash/sign/timestamp arvutamist.

---

## Samm 5 — Backend API

**Eesmärk:** REST endpoint: võta vastu prompt, kutsu LLM, kanoniseeri, räsi, allkirjasta, ajatempel, salvesta, tagasta vastus ja kontrolli andmed.  
**Hinnang:** 6–8 t

### Ülesanne 5.1 — POST /api/ai/ask (või /chat)

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 4–5 t |
| **Kirjeldus** | Üks endpoint: body { "prompt": "..." }; voog: LLM → canonicalize → hash → sign → timestamp → store; vastus: { "response", "responseHash", "signature", "tsaToken", "id", "model" }. |

**Koodi juhend (LLM-readable):**
- Loo REST kontroller (nt AiController). POST /api/ai/ask (või /api/chat). Päringu keha: JSON väljaga "prompt" (string). Voog: (1) LLMClient.complete(prompt) → responseText, modelId. (2) Kanoniseeri responseText → kanoonilised baitid. (3) HashService.hash(canonicalBytes) → responseHash. (4) SignatureService.sign(responseHash) → signature. (5) TimestampService.getTimestamp(responseHash baitid või signature) → tsaToken. (6) AuditRecordService.save(prompt, response, responseHash, signature, tsaToken, modelId, ...) → id. (7) Tagasta JSON: response (originaal või kanooniline string), responseHash (hex), signature (Base64), tsaToken (Base64), id, model (modelId). DTO-d päringu/vastuse jaoks. LLM või TSA vea korral tagasta 502 või 503 selge sõnumiga. Integratsioonitest: kutsu endpoint lühikese promptiga, 200, kehas response, responseHash, signature, tsaToken, id; DB-s üks rida sama id ja hash-iga.

---

### Ülesanne 5.2 — GET /api/ai/verify/:id (soovitatav)

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 2 t |
| **Kirjeldus** | Tagasta salvestatud kirje id järgi kontrolli lehe jaoks: prompt, response, hash, signature, tsa_token, model, created_at. |

**Koodi juhend (LLM-readable):**
- Lisa GET /api/ai/verify/{id}. Laadi AiResponse id järgi repository-st. Tagasta JSON: prompt, response, responseHash, signature, tsaToken, llmModel, createdAt. Kui ei leitud — 404. Kontrolli loogikat selles endpointis ära lisa; ainult andmete tagastamine frontendi kontrolli lehele.

---

## Samm 6 — Frontend

**Eesmärk:** UI: prompti sisestus, Send, vastuse ja staatuse kuvamine (allkirjastatud, ajatempliga, kontrollitav); link kontrolli lehele.  
**Hinnang:** 6–8 t

### Ülesanne 6.1 — Prompti väli ja Send nupp

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 2 t |
| **Kirjeldus** | Ühenda prompti väli ja Send nupp POST /api/ai/ask-ga; näita laadimise olekut ja vigu. |

**Koodi juhend (LLM-readable):**
- Next.js rakenduses: "Send" klõpsul loe prompt input/textarea-st. POST backendile (nt /api/ai/ask), keha { prompt }. API baas URL env-st (NEXT_PUBLIC_API_URL). Näita laadimise olekut (disabled nupp või spinner). Edu korral salvesta vastuse JSON state-i. 4xx/5xx korral näita kasutajale veateade. Vastust ja kontrolli veel ära renderda; ainult andmete hankimine ja vigade käsitlemine.

---

### Ülesanne 6.2 — Vastuse ja staatuse kuvamine (signed, timestamped, verifiable)

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 2 t |
| **Kirjeldus** | Pärast edukat POST-i näita AI vastuse teksti ja staatuse plokki: ✔ signed, ✔ timestamped, ✔ verifiable (vastavalt signature ja tsaToken olemasolule). Link "Verify this response" → /verify?id=... |

**Koodi juhend (LLM-readable):**
- Pärast edukat POST-i: kuva response.response eraldi alal. Staatuse plokk kolme märgiga: "Signed", "Timestamped", "Verifiable" — näita positiivsena kui response.signature ja response.tsaToken pole tühjad. Lisa link või nupp "Verify this response", mis navigeerib /verify?id={response.id}. Kasuta API-st tagastatud id. Kontrolli loogikat (räsi kontroll, allkirja kontroll) selles ülesandes ära tee; ainult kuvamine ja navigeerimine.

---

### Ülesanne 6.3 — Kontrolli leht: räsi, allkiri, TSA token

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 2–3 t |
| **Kirjeldus** | Leht /verify?id=... küsib GET /api/ai/verify/:id ja kuvab prompt, response, hash, signature (nt Base64 snippet), TSA token (snippet), model, kuupäev. Valikuliselt: kliendi poolel räsi kontroll (kanoniseeri uuesti ja arvuta SHA-256, võrdle salvestatud hash-iga). |

**Koodi juhend (LLM-readable):**
- Loo lehe marsruut /verify. Id query-st (?id=...). Päring GET /api/ai/verify/{id}. Kuva: prompt, vastuse tekst, responseHash (täielik või kärbitud), signature (Base64, esimesed/viimased 20 tähemärki + "..."), tsaToken (sama), llmModel, createdAt. Valikuliselt: nupp "Verify hash" — (1) kanoniseeri brauseris kuvatav response (samad reeglid mis backend), (2) arvuta SHA-256 (Web Crypto API või väike teek), (3) võrdle salvestatud responseHash-iga ja näita "Match" / "Mismatch". Täielikku allkirja kontrolli brauseris PoC jaoks vaja pole; andmete kuvamine piisab.

---

## Samm 7 — Kontroll ja dokumentatsioon

**Eesmärk:** Valikuline backend kontrolli endpoint; README ja käivitamise juhised.  
**Hinnang:** 2–4 t

### Ülesanne 7.1 — Backend: räsi ja allkirja kontroll (valikuline)

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1–2 t |
| **Kirjeldus** | GET /api/ai/verify/:id võib valikuliselt tagastada hashMatch (räsi ümberarvestus salvestatud response-ist) ja signatureValid (SignatureService.verify). |

**Koodi juhend (LLM-readable):**
- Valikuliselt laienda GET /api/ai/verify/:id vastust (või lisa GET /api/ai/verify/:id/check): hashMatch (boolean — arvuta räsi uuesti salvestatud response-ist sama kanoniseerimisega, võrdle responseHash-iga), signatureValid (boolean — SignatureService.verify(storedHash, storedSignature)). Tagasta JSON: { ...record, hashMatch, signatureValid }. Frontend võib näidata "Hash match: yes/no", "Signature valid: yes/no". Rakenda ainult kui aega jätkub; muul juhul jäta tuleviku tööks.

---

### Ülesanne 7.2 — README ja käivitamise juhised

| Väli | Väärtus |
|------|---------|
| **Hinnang** | 1–2 t |
| **Kirjeldus** | README: kuidas genereerida võti, seada env, käivitada PostgreSQL, backend, frontend, lokaalne TSA (kui on). Loetle env: API võtmed, DB URL, TSA URL, võtme tee. |

**Koodi juhend (LLM-readable):**
- Uuenda README: (1) Eeldused: Java 21+, Node 18+, PostgreSQL 15+, valikuliselt Docker. (2) Keskkonnamuutujad: OPENAI_API_KEY või GEMINI_API_KEY (või Mistral), DB URL, TSA URL, privaatvõtme tee allkirjastamiseks. (3) Võtme genereerimine: openssl genpkey -algorithm RSA -out ai.key. (4) DB käivitamine: docker-compose up -d või lokaalne install, migratsioonid. (5) Backend käivitamine: ./mvnw spring-boot:run env-iga. (6) Frontend käivitamine: npm run dev, NEXT_PUBLIC_API_URL. (7) Valikuliselt: lokaalse RFC 3161 TSA käivitamine arenduseks. Sektsioonid lühikesed; viide docs/PoC-le arhitektuuri jaoks.

---

## Kokkuvõte — Hinnang tundide järgi

| Samm | Tunnid |
|------|--------|
| 1 — Projekti seadistus ja raam | 6–8 |
| 2 — Krüpto kiht | 10–14 |
| 3 — LLM integratsioon | 4–6 |
| 4 — Audit ja persisteerimine | 4–6 |
| 5 — Backend API | 6–8 |
| 6 — Frontend | 6–8 |
| 7 — Kontroll ja dokumentatsioon | 2–4 |
| **Kokku** | **38–54** |

---

## Testimine (sammude kaupa)

Iga sammu testide ulatus ja vastuvõtukriteeriumid. Backend: `mvn test` kaustast `backend/`; frontend: `npm test` kui olemas.

| Samm | Testi tüüp | Mida testida | Vastuvõtukriteerium |
|------|------------|--------------|---------------------|
| **1.1** | Käsitsi / dok | README ja diagramm | README: Prerequisites, Run backend/frontend/DB; Mermaid renderdub; link docs/PoC-le. |
| **1.2** | Üksiktest | Health endpoint | GET /health → 200 ja JSON {"status":"UP"}. @WebMvcTest(HealthController.class) + MockMvc. |
| **1.2** | Integratsioon | Kontekst | @SpringBootTest kontekst käivitub. |
| **1.3** | Käsitsi | Frontend | npm run dev; leht: Prompt, Send, Response. |
| **1.4** | Käsitsi / migratsioon | DB skeem | Liquibase/Flyway käivub; tabel ai_response olemas. |
| **2.1** | Üksiktest | Kanoniseerimine | Sama tekst → samad baitid; \r\n vs \n → sama tulemus. |
| **2.2** | Üksiktest | HashService | Teadaolev sisend → teadaolev SHA-256 hex (64 tähemärki). |
| **2.3** | Üksiktest | SignatureService | sign(hash), verify(hash, signature) → true; võltsitud allkiri → false. |
| **2.4** | Üksiktest | TimestampService | Mock TSA: päring tagastab mitte-tühja tokeni; kehtetu vastus käsitletud. |
| **3.1** | Üksiktest | LLMClient | Mock: complete(prompt) tagastab mitte-tühja teksti ja modelId. |
| **3.2** | — | Andmevoog | LLMResult / audit sisaldab model id ja valikuliselt parameetreid. |
| **4.1** | Üksiktest | Repository | save(entity); findById(id); kontrolli välju. H2 või Testcontainers. |
| **4.2** | Üksiktest | AuditRecordService | save(request) → id; laadi id järgi, kontrolli välju. |
| **5.1** | Integratsioon | POST /api/ai/ask | 200; kehas response, responseHash, signature, tsaToken, id, model; DB-s üks kirje. |
| **5.2** | Üksiktest / integratsioon | GET /api/ai/verify/:id | 200 koos kirjega; 404 tundmatu id korral. |
| **6.1** | Käsitsi / E2E | Frontend → backend | Saada prompt → vastus ja staatus; vea korral teade. |
| **6.2** | Käsitsi | UI | Signed, timestamped, verifiable märgid; link "Verify" id-ga. |
| **6.3** | Käsitsi | Verify leht | Laadi id järgi; kuva hash, allkiri, TSA token; valikuliselt hash kontroll kliendis. |
| **7.1** | Üksiktest | Kontroll | hashMatch ja signatureValid vastuses kui rakendatud. |
| **7.2** | Käsitsi | README | Kõik käivitamise ja env juhised dokumenteeritud. |

**Backend testide käsk:** kaustast `backend/`: `./mvnw test` või `mvn test`. Testide jaoks kasutatakse H2 (vaikeprofiil).

---

**Koodi juhendi konventsioon:** Iga ülesande "Koodi juhend (LLM-readable)" plokki võib kopeerida või edastada LLM-le/arendajale ülesande teostamiseks. Seal on määratud: mida ehitada, sisendid/väljundid, tehnoloogiad, testid ja mida mitte teha. Hinnangud ja ulatust võib kohandada vastavalt meeskonnale.
