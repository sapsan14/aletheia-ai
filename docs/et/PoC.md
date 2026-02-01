# 🏗️ Proof of Concept: Aletheia AI arhitektuur

Dokument kirjeldab ettepanekul olevat PoC-arhitektuuri süsteemile, mis tagab tehisintellekti vastuste kinnitamise krüptograafilise allkirja ja ajatemplite abil.

**Seotud:** [Visioon ja teekond](VISION_AND_ROADMAP.md) · [Rakendusplaan](plan.md) · [Usaldusmudel](TRUST_MODEL.md) · [Arhitektuuri diagrammid](../../diagrams/architecture.md)

---

## Arhitektuur

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

**Kokkuvõte:** see on juba väga tugev PoC.

---

## 🔐 Krüptograafia: mida tegelikult kasutada

### Allkiri (Signing)

#### 🔹 OpenSSL — parim algus

**Miks:**

- minimaalne
- standardne
- juristidele arusaadav
- lihtne kontrollida

**Näide:**

```bash
openssl dgst -sha256 -sign ai.key response.txt > signature.bin
```

- ✔ ideaalne PoC jaoks  
- ✔ lihtne selgitada  
- ✔ ilma infrastruktuuri valuta  
- ➡️ **parim valik nr 1**

#### 🔹 Smallstep — teine etapp

Kasuta, kui tahad näidata «PKI mõtlemist»:

- `step-ca`
- X.509 sert
- automatiseerimine

**Kuid:** PoC jaoks on see juba «tase 2». Alusta OpenSSLiga, hiljem saab taustsüsteemi moodulit vahetada.

---

### ⏱️ Ajatemplid (TSA)

Variantid keerukuse kasvamise järjekorras:

| Tase | Variant | Kirjeldus |
|------|---------|-----------|
| 🟢 | **Lokaalne RFC 3161 TSA** | Ideaalne PoC jaoks |

#### 🟢 Variant 1 — lokaalne RFC 3161 TSA (ideaalne PoC jaoks)

Võid käivitada lokaalse TSA.

**Tööriistad:**

- OpenSSL TSA
- OpenTSA
- lihtne RFC3161 server

**Plussid:**

- täielikult võrguühenduseta
- ideaalselt demonstreerib ideed
- standard RFC 3161

**Miinused:**

- usaldus = sinu server (kuid see on PoC!)

➡️ **Ideaalne variant alguseks.**

#### 🟡 Variant 2 — avalikud TSA-d

Näiteks:

- DigiCert TSA
- GlobalSign TSA
- FreeTSA (piiratud)

**Probleemid:** piirangud, rate limitid, mõnikord tasuline. Võib jätta «tuleviku tööks».

#### 🔴 eIDAS Qualified TSA

- ❌ mitte praegu  
- ❌ mitte PoC jaoks  
- ❌ kallis  

Arhitektuuris võib ausalt kirjutada: *«Architecture compatible with eIDAS Qualified TSA»* — ja see on korrektne.

---

## 🗄️ Andmebaas

**PostgreSQL** — jah, 100%.

**Tabeli struktuur:**

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

**Valikuliselt lisada:**

- `request_id`
- `temperature`
- `system_prompt`
- `version`

See on juba täisväärtuslik auditijälg.

---

## 🤖 LLM — maksimaalselt lihtne

- **Üks LLM** alguses.

**Valik:**

- OpenAI (tasuta krediidid)
- Gemini (sageli laialdane tasuta tasand)
- Mistral (avatud/tasuta)

Alusta ühega.

**Oluline:** logi `model name`, `version`, `parameters` — hiljem on see auditi jaoks väga kasulik.

---

## 🧑‍💻 Backend — mis sobib sulle parimini

Sinu profiili (PKI, enterprise) arvestades:

### ✅ Java + Spring Boot

**Plussid:**

- oled «kodus»
- krüpto — loomulik (BouncyCastle)
- ajatempli tugi

**Miinus:** veidi raskem start.

### ✅ Node.js

**Plussid:**

- kiire
- OpenSSL läbi shelli
- lihtne üles seada

**Miinus:** krüpto vähem «kanooniline» enterprise’i jaoks.

### ⚠️ Python

Hea prototüübi jaoks, kuid vähem «enterprise».

---

**Soovitus:** Java. PKI sügavuse arvestades on loogiline kasutada BouncyCastle’i.

---

## 📦 Backend-moodulite struktuur

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

See on juba arhitektuur, mitte demokeskus.

---

## 🌐 Frontend (minimaalne)

- väli **prompt**
- nupp **«Saada»** («Send»)
- AI vastus
- staatuse plokk:
  - ✔ allkirjastatud  
  - ✔ ajatempliga  
  - ✔ kontrollitav  

Ja link **«Kontrolli seda vastust»** («Verify this response»), kus saab:

- hash uuesti arvutada
- allkirja kontrollida
- TSA tokenit näidata

➡️ see annab vau-efekti.

---

## 🧪 Dagster?

➡️ **Praegu pole vaja.**

Dagster on töövoogude orkestreerimine. Mõtet on lisada hiljem, kui tekib:

- pakett-analüüs
- plaaniline AI kontrollimine
- öine audit

PoC jaoks — üleliigne.

---

## 🧭 Kokkuvõte — soovitatav PoC-stekk

Minimaalne, kuid võimas:

| Kiht | Tehnoloogia |
|------|-------------|
| Frontend | Next.js |
| Backend | Java Spring Boot |
| Crypto | OpenSSL + BouncyCastle |
| Allkiri | lokaalne RSA/ECDSA võti |
| Ajatempel | RFC 3161 TSA (vaikimisi: DigiCert) |
| DB | PostgreSQL |
| LLM | üks (Gemini / OpenAI / Mistral) |

---

## 🚀 Deploy

**Valitud lähenemine:** Full stack (Docker + Ansible + GitHub Actions) automatiseeritud deploy'iks siht-VM-le (nt `ssh ubuntu@193.40.157.132`).

- **Docker:** Backend ja frontend konteinerites; docker-compose koos PostgreSQLiga.
- **Ansible:** VM seadistus (Docker install), .env mall, `docker-compose up`.
- **GitHub Actions:** Push main'ile: testid → build → deploy üle SSH/Ansible.

**Alternatiivid:** Ainult Ansible, ainult skript (bash üle SSH), ainult Docker Compose. Üksikasjad [plan.md](plan.md) Samm 8.

---

## 💡 Miks see PoC on tugev

Sa ei tee «AI that tells truth», vaid **AI whose answers can be proven**.

See on põhimõtteline erinevus — ja see on suund, kuhu ELi regulatiiv tegelikult liigub.

---

## 📋 Järgmised sammud

1. [Arhitektuuri diagrammid](../../diagrams/architecture.md) (Mermaid: pipeline, usaldusahel)
2. PoC README kui GitHubi projekt
3. Näide RFC 3161 ajatempli voost
4. Näide BouncyCastle ajatempli kontrollimisest
5. [Visioon ja teekond](VISION_AND_ROADMAP.md) — PoC → demo → ELi-stiilis toode
