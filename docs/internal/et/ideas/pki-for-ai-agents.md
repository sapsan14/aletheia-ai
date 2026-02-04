# Idee: PKI AI jaoks — kasutamine ja testimine (tulevik)

Perspektiivne idee: kasutada Aletheia usaldusinfrastruktuuri (allkiri, ajatemplid, võrguühenduseta kontroll) **autonoomsete AI-agentide** jaoks, mitte ainult «ühe mudeli vastuse» jaoks. Dokument kirjeldab PoC stsenaariumi OpenClaw agendi põhjal ja laiendab seda teiste agentide ja MCP ökosüsteemi näidetega.

**Staatus:** idee, ei kuulu praegusesse roadmapi.  
**Seotud:** [Visioon ja teekond](../VISION_AND_ROADMAP.md) · [Usaldusmudel](../TRUST_MODEL.md) · [PoC](../PoC.md).

---

## Sisukord

- [Mis on OpenClaw](#mis-on-openclaw)
- [Miks OpenClaw sobib Aletheia PKI PoC jaoks](#miks-openclaw-sobib-aletheia-pki-poc-jaoks)
- [Arhitektuur: OpenClaw ja Aletheia](#arhitektuur-openclaw-ja-aletheia)
- [Konkreetsed PoC stsenaariumid](#konkreetsed-poc-stsenaariumid)
- [Riskid ja nende käsitlemine](#riskid-ja-nende-käsitlemine)
- [Teised agentid ja MCP: kontroll ja sertifitseerimine meie PKI-ga](#teised-agentid-ja-mcp-kontroll-ja-sertifitseerimine-meie-pki-ga)
- [Lühike pitch](#lühike-pitch)

---

## Mis on OpenClaw

**OpenClaw** on avatud lähtekoodiga AI-assistent, mida käivitad oma arvutil või serveris ja mida juhid tavaliste vestlusrakenduste kaudu (WhatsApp, Telegram, Discord, Slack jne). Ta mitte ainult «vastab küsimustele», vaid teostab tegelikke toiminguid: automatiseerib ülesandeid, käivitab käske, hallab faile, brauserit, e-kirju ja integreerub erinevate teenustega.

### Põhiomadused

| Aspekt | Kirjeldus |
|--------|-----------|
| **Lokaalne käivitamine** | Töötab sinu masinal või VPS-il; andmed jäävad sinu kontrolli alla. |
| **Mitu kanalit** | Suhtlus messengerite kaudu nagu kolleegi-assistendiga. |
| **Automatiseerimine** | Shell-käsud, brauser, failid, kasutaja skriptid. |
| **Laiendatavus** | Pluginaid/skille: valmis ja oma loodud. |
| **Mälu ja kontekst** | Mäletab seadeid ja konteksti sessioonide vahel. |

OpenClaw on tuntud kui varajane näide **autonoomse isikliku agenti** kohta, kes tegelikult täidab ülesandeid (mitte ainult genereerib teksti). Projekt kogub GitHubis tuhandeid tähti; seda saab käivitada kodus või serveris, ilma täielikult pilvepakuja sõltuvuseta.

### Plussid ja miinused (tagasiside põhjal)

**Plussid:** suudab luua oskusi/integratsioone üldise juhendi järgi; aitab automatiseerida igapäevaseid ülesandeid.  
**Miinused:** nõuab tehnilisi oskusi seadistamiseks; võib olla kallis, kui kasutad aktiivselt pilve LLM API-d; turvariskid laia süsteemi juurdepääsu korral; osade stsenaariumite puhul kiirus ja usaldusväärsus on vastuolulised.

**Lühidalt:** OpenClaw ei ole lihtsalt chat-bot, vaid **AI-agent sinu infrastruktuuril**, aktiveeritav vestluse kaudu, võimega täita käske, automatiseerida ülesandeid ja laieneda pluginatega.

---

## Miks OpenClaw sobib Aletheia PKI PoC jaoks

Aletheia idee on usaldus, kontrollitavus, allkirjad, ajatemplid, võrguühenduseta kontroll. OpenClaw võib mängida **«AI-agendi passiga»** rolli.

1. **OpenClaw on täideviiv agent.** Ta ei ole lihtsalt LLM, vaid agent, kes teeb otsuseid, teostab toiminguid, kutsub skripte ja API-sid ja elab sinu infrastruktuuril. Mugav objekt PKI ümberpakkumiseks.

2. **Lokaalsus ja kontroll.** Võtmed ja allkirjad võivad olla agendi lähedal; pilvepakujale ei pea krüptograafiat usaldama.

3. **Usaldusahel.** Aletheia annab identiteedi, aja, allkirja ja proof bundle; OpenClaw on kontrollitavate toimingute tootja.

---

## Arhitektuur: OpenClaw ja Aletheia

Rollid PoC-s:

| Komponent | Roll |
|-----------|------|
| **OpenClaw** | AI Agent; toimingute allkirjastaja; verifiable claims allikas. |
| **Aletheia** | Identity; Time (RFC 3161); Signature; Proof bundle. |

Stsenaarium: agent teostab toimingu → tulemus kanoniseeritakse ja allkirjastatakse Aletheia võtmega (või meie PKI kaudu väljastatud) → lisatakse RFC 3161 timestamp → moodustatakse tõendipakett.

---

## Konkreetsed PoC stsenaariumid

### 1. «Allkirjastatud AI toiming» (Signed AI Action)

OpenClaw teostab toimingu (teksti analüüs, vastuse genereerimine jne) ja:

1. Genereerib tulemuse.
2. Kanoniseerib väljundi (canonicalize).
3. Allkirjastab tulemuse Aletheia võtmega.
4. Lisab RFC 3161 timestamp.
5. Tagastab näiteks:

```json
{
  "output": "...",
  "signature": "...",
  "timestamp": "...",
  "agent_id": "openclaw-001"
}
```

**Tulemus:** võrguühenduseta kontrollitav AI väljund. Tugev demo stsenaarium.

### 2. Agendi identiteet (AI Passport)

Iga OpenClaw instantsi jaoks:

- Väljastatakse X.509 sertifikaat.
- Issuer = Aletheia Root (või meie CA).
- Purpose = AI Agent.

Saab visuaalselt näidata: tühistatud agent ❌, aegunud ❌, kehtiv ✅. See on PKI, arusaadav ärile ja regulaatoritele.

### 3. Chain of Thought ≠ Proof of Action

OpenClaw võib sisemiselt «mõelda» kuidas tahes; välja antakse ainult allkirjastatud **Action Proof**. Nii näitame **AI accountability-d ilma sisemise mõttekäigu avalikustamiseta**.

### 4. Võrguühenduseta kontrollija demo

Stsenaarium: OpenClaw töötab online; tulemuse kontroll — **täielikult offline** (sertifikaat, CRL/OCSP snapshot, timestamp, allkiri). Selline võimalus on AI-projektides haruldane ja võib olla tugev eristav tunnus.

---

## Riskid ja nende käsitlemine

| Risk | Vastus PoC raames |
|------|-------------------|
| OpenClaw ebastabiilne / toor | PoC jaoks pluss: näitame, et «isegi ebastabiilse agentiga tagame usalduse ja kontrollitavuse». |
| Turvalisus | Rõhutame: sandboxed agent, piiratud õigused, usaldus sertifikaatide põhjal. |

---

## Teised agentid ja MCP: kontroll ja sertifitseerimine meie PKI-ga

PKI idee AI jaoks ei piirdu OpenClawiga. Allpool **konkreetsed näited** agentidest ja protokollidest, mis võivad osaleda kontrollis või sertifitseerimises Aletheia infrastruktuuriga, sealhulgas **Model Context Protocol (MCP)** ökosüsteemis.

### MCP kui usalduse keskkond

**Model Context Protocol (MCP)** on avatud standard AI-rakenduste ühendamiseks väliste andmete ja tööriistadega («USB-C AI jaoks»). MCP-t toetavad agentid võivad kutsuda ühiseid tööriistu ja servereid; loomulik koht **allkirjade kontrollimise** ja **Aletheia sertifikaatide kontrollimise** sissetoomiseks.

### Agentide ja komponentide näited

#### 1. OpenClaw (täideviiv agent)

- **Roll PKI-s:** Toimingute «allkirjastaja»; AI Agent sertifikaadi omanik.
- **Stsenaarium:** Toiming → canonicalize → sign (Aletheia võti) → timestamp → proof bundle. Kontrollija (ükskõik milline allpool) saab kontrollida allkirja ja aega offline.

#### 2. Cursor (IDE MCP-ga)

- **Roll:** Kontrollija-agent või allkirjastatud artefaktide tarbija.
- **Konkreetne näide:** Cursoris on ühendatud MCP-server «Aletheia Verify»: tööriist `verify_evidence_bundle(path)` võtab vastu Evidence Package (.aep) või response.json, kontrollib lokaalselt allkirja, sertifikaatide ahelat ja RFC 3161 timestampi ning tagastab tulemuse (valid/invalid + üksikasjad). Arendaja näeb vestluses: «See AI vastus on Aletheia poolt allkirjastatud ja kontrollitud».

#### 3. Claude Desktop / Claude Code (Anthropic, MCP)

- **Roll:** Agent, kes kas allkirjastab oma järeldused Aletheia kaudu või kontrollib teiste oma.
- **Konkreetne näide:** MCP-tööriist `aletheia_sign_response(canonical_response)` — Claude kutsub meie backendi (või lokaalse signeri), saab allkirja ja timestampi, lisab vastuse juurde. Või `aletheia_verify(bundle)` — kontroll ilma meie serverita avalike võtmete ja CRL/OCSP põhjal.

#### 4. MCP-I (MCP-Identity) ja Verification Protocol

- **Mis see on:** MCP ökosüsteemi laiendus identiteedi ja kontrolli jaoks: decentraliseeritud identifikaatorid (DIDs), verifiable credentials, challenge-response võtme omamise kontrollimiseks.
- **Seos Aletheia PKI-ga:** Meie PKI võib olla **identiteedi allikas** MCP-agentidele: väljastame sertifikaadid (X.509 või VC-ga ühilduvad), MCP-I Verification Protocol kasutab neid krüptograafiliseks kontrolliks «kes see agent on» ja «kas tal on õigus allkirjastada». Konkreetselt: agent esitab Aletheia sertifikaadi; kontrollija kontrollib ahela kuni Aletheia Rootini ja staatuse (CRL/OCSP).

#### 5. OpenAI Codex / OpenAI agentid MCP-ga

- **Roll:** Täideviivad agentid MCP-ga (Hosted MCP tools, Streamable HTTP, Stdio). Võivad olla «allkirjastajad» või kutsuda kontrolli.
- **Konkreetne näide:** Agent pärast koodi või aruande genereerimist kutsub Aletheia MCP-tööriista (nt `sign_and_timestamp(payload)`), saab proof bundle ja lisab selle tulemuse juurde auditi jaoks.

#### 6. MCP-serverid kui identiteedi hoidjad

- **Näited:** Stripe MCP, teised avalikud MCP-serverid (andmebaasid, API).
- **Roll PKI-s:** Serveril võib olla Aletheia **agendi sertifikaat**: päringud «Stripe MCP»-lt kriitilistele süsteemidele on allkirjastatud, kontrollitav meie PKI kaudu. Nii sertifitseerime mitte ainult «inimagent», vaid ka tööriista/teenuse MCP-võrgustikus.

#### 7. Kontrollijad ja benchmarkid MCP ökosüsteemis

- **MCPMark:** benchmark agentide hindamiseks realistlike MCP-ülesannete järgi programmeeritava kontrolliga. Võib lisada **täiendava kriteeriumi:** «agendi tulemus peab olema Aletheia poolt allkirjastatud ja läbima võrguühenduseta kontrolli» — siis on usaldus tulemuse suhtes mõõdetav ja taastatav.
- **CoSAI (Coalition for Secure AI):** MCP turvalisuse soovitused (identiteet, juurdepääs, süstid jne). Aletheia PKI mahub «identity and access controls» alla: agentide sertifikaadid ja allkirjastatud toimingud annavad selge mudeli «kes mida tegi».

### Kokkuvõtav tabel: kes mida teeb meie PKI-ga

| Agent / komponent | Roll | Konkreetne näide Aletheia PKI-ga |
|-------------------|------|-----------------------------------|
| **OpenClaw** | Täideviiv agent, allkirjastaja | Toiming → allkiri + timestamp → proof bundle; AI Agent sertifikaat Aletheialt. |
| **Cursor** | Kontrollija (MCP kaudu) | MCP-tööriist `verify_evidence_bundle` — allkirja ja aja offline kontroll. |
| **Claude Desktop / Claude Code** | Allkirjastaja või kontrollija | MCP `aletheia_sign_response` / `aletheia_verify`; vastused proof bundle-iga. |
| **OpenAI Codex / MCP-agentid** | Allkirjastaja | MCP-kutse `sign_and_timestamp` pärast artefakti genereerimist. |
| **MCP-I Verification Protocol** | Identiteedi kontrolli standard | Meie sertifikaadid — identiteedi allikas; ahela ja staatuse kontroll MCP-I-s. |
| **MCP-serverid (nt Stripe MCP)** | Identiteedi hoidjad | Aletheia agendi sertifikaat päringute allkirjastamiseks kriitilistele süsteemidele. |
| **MCPMark / CoSAI** | Benchmarkid ja turvalisus | Kriteerium «Aletheia allkirjastatud tulemus»; joondamine identity/access controls-iga. |

Need näited näitavad, et **sama Aletheia PKI** võib olla kasutusel nii «allkirjastaja» (OpenClaw, Claude, Codex) kui «kontrollija» (Cursor, iga MCP-klient verify tööriistaga), identiteedi standardite (MCP-I) ja mõõdetava usalduse (MCPMark, CoSAI) jaoks.

---

## Lühike pitch

> Kasutame autonoomset AI-agent (nt OpenClaw). Aletheia annab krüptograafilise identiteedi, allkirja ja ajatemplid agendi toimingutele. Tulemuseks on **kontrollitav, võrguühenduseta kontrollitav tõend** sellest, et konkreetne AI-agent andis konkreetse väljundi konkreetsel ajal.

Selline stsenaarium kõlab tugevalt PKI kogukonnale, regulaatoritele (sh EL), turvalisuse spetsialistidele ja investoritele.

**Võrdlus «lihtsalt LLM API»-ga:**

| Kriteerium | OpenClaw + Aletheia | Pilve LLM |
|------------|---------------------|-----------|
| Kontroll | ✅ täielik | ❌ |
| Lokaalsed võtmed | ✅ | ❌ |
| Agendi identiteet | ✅ | ❌ |
| PKI-integratsioon | ✅ loomulikult | ❌ |
| Lugu EL / compliance jaoks | ✅ | ❌ |

---

## Järgmised sammud (idee elluviimisel)

- Kavandada integratsioonivoo OpenClaw ↔ Aletheia (või teine agent ↔ Aletheia).
- Lisada arhitektuuri Mermaid-diagramm: agent → canonicalize → sign → timestamp → proof bundle → kontrollija.
- Määratleda üks killer demo stsenaarium 5-minutiliseks presentatsiooniks (nt «allkirjastatud toiming messengeris → võrguühenduseta kontroll audiitori sülearvutil»).
- Rakendada MCP-tööriistad `aletheia_verify` ja vajadusel `aletheia_sign` Cursor / Claude / teiste MCP-klientide jaoks.

---

*Osa [Tuleviku ideed](README.md). Aletheia AI dokumentatsioon: [indeks](../../README.md), [Visioon ja teekond](../VISION_AND_ROADMAP.md). Tõlge: [RU](../ru/ideas/PKI_FOR_AI_AGENTS.md).*
