# Aletheia Trust Lab — Haridusplatvormi plaan (PLAN_EDU)

**Eksperimentaalne usalduslabor: PQC, kontrollitav tehisintellekti vastutus, õppe- ja partnerstsenaariumid.**

See dokument kirjeldab Aletheia AI projekti muutmist **õppe- ja demoplatvormiks** (lab / training platform) üliõpilastele ja partneritele: postkvantkrüptograafia (PQC), kontrollitav tehisintellekti vastutus, klassikaline PKI ja ajatemplid — ühes praktilises keskkonnas.

**Staatus:** Plaan · **Seotud dokumendid:** [PLAN_PQC](PLAN_PQC.md), [Visioon ja teekond](VISION_AND_ROADMAP.md), [Usaldusmudel](TRUST_MODEL.md), [legal/README](../../legal/README.md) (PQC, eIDAS, AI Act).

---

## Sisukord

- [Positsioneerimine ja eesmärgid](#positsioneerimine-ja-eesmärgid)
- [Miks see on tugev idee](#miks-see-on-tugev-idee)
- [Nimetus ja narratiiv](#nimetus-ja-narratiiv)
- [Teekond: Lab → Demo → EL / Pilot](#teekond-lab--demo--el--pilot)
- [Arendusülesanded (üksikasjalikud sammud)](#arendusülesanded-üksikasjalikud-sammud)
- [LLM-i prompid](#llm-i-prompid)
- [Laboristsenaariumid](#laboristsenaariumid)
- [Sihtrühm ja partnerid](#sihtrühm-ja-partnerid)
- [Valmiduskriteeriumid ja riskid](#valmiduskriteeriumid-ja-riskid)

---

## Positsioneerimine ja eesmärgid

| Aspekt | Kirjeldus |
|--------|------------|
| **Mitte toode** | Eksperimentaalne labor ja koolitusplatvorm. Mitte sertifitseeritud CA, mitte „igavene turvalisus“, vaid „tunne järgi, kontrolli, murra“. |
| **Fookus** | Post-quantum cryptography (PQC), kontrollitav tehisintellekti vastutus (AI accountability), andmete usaldus ja pikaajaline kontrollitavus. |
| **Vorming** | Lab / PoC / training: reprodutseeritavad katsed, diagrammid (Mermaid), dokumentatsioon ja stsenaariumid õppejõududele ja partneritele. |

**Üherealaine (Hero):**

> **Aletheia Trust Lab** — praktiline labor postkvantkrüptograafia ja kontrollitava tehisintellekti vastutuse alal.

**Alapealkirja võimalused:**

- *Experiment. Verify. Trust.*
- *Hands-on lab for post-quantum cryptography and verifiable AI responsibility.*

---

## Miks see on tugev idee

1. **PQC kui „õppevalu“**  
   Kõik räägivad postkvantkrüptograafiast, kuid vähesel kohal on elus laborid ja seos tõelise PKI, ajatemplite ja nõuete täitmisega. Platvorm pakub „turvalist liivakasti“ tuleviku krüptograafia mõistmiseks.

2. **Tehisintellekti vastutuse kontroll — õiguslik trend**  
   Tehisintellekt genereerib sisu; tuleb tõestatavalt fikseerida: kes, millal, millise mudeliga, milliste reeglite alusel. Ahel: AI väljund → kanoniseerimine → räsi → allkiri (RSA + vajadusel PQC) → RFC 3161 ajatempel = kontrollitav vastutus. See ristub haridusega, nõuete täitmisega, legal-techiga, valitsemisega.

3. **Lab-vorming vähendab riske**  
   Positsioneerimine laborina, mitte tootena, vähendab õiguslikke ootusi ja sobib ülikoolidele, ettevõtetele ja EL-projektidele.

4. **Unikaalne kombinatsioon**  
   Tõeline taristu (PKI, DevOps), matemaatiline alus ja nõuete täitmise mõistmine annavad haridus- ja pilotstsenaariumiteks harva esineva kombinatsiooni.

---

## Nimetus ja narratiiv

**Soovituslik nimetus:** **Aletheia Trust Lab**  
(Aletheia — „tõde, avastamine“, kreeka k.; ei ole seotud konkreetse tehnoloogiaga ja sobib hästi trust / verification / proof konteksti.)

**Alternatiivid:**

- **VeriQ Lab** — Verifiable & Quantum-ready (lühike, tehniline).
- **Post-Quantum Trust Lab (PQTL)** — otsekõnelev, mugav taotluste ja grantide jaoks.

**Landing-narratiiv (probleem → lahendus):**

- **Probleem:** Tehisintellekt genereerib otsuseid ja tekste; krüptograafia liigub postkvantajastusse. Samas ei suuda me usaldusväärselt tõestada: kes mida genereeris, millal, milliste reeglite järgi ja kas see jääb usaldusväärseks aastate pärast.
- **Lahendus:** Aletheia Trust Lab on praktiline keskkond, kus AI väljundeid saab räsida, allkirjastada ja kontrollida; klassikalist ja postkvantkrüptograafiat saab võrrelda; usalduse eeldused on selged ja neid saab õppeeesmärgil „murda“. Ilma võluteta ja mustade kastideta.

**Sihtrühm:** üliõpilased (krüptograafia, küberturvalisus, AI valitsemine), tööstuspartnerid (PKI, nõuete täitmine, AI tarnijad), avalik sektor ja uurimispilotid.

---

## Teekond: Lab → Demo → EL / Pilot

| Faas | Eesmärk | Tähtaeg | Põhitulemused |
|------|---------|---------|----------------|
| **Faas 1 — Lab** | Näidata, et kõik töötab „käega“ | Praegu | Repo, dokumentatsioon, diagrammid, CLI või minimaalne UI, stsenaarium „kuidas kontrollida 5 minutiga“. |
| **Faas 2 — Demo** | Teha arusaadavaks mitte-krüptograafidele | 3–6 kuud | Veebi-UI, voog „Genereeri → Allkirjasta → Kontrolli“, lüliti hybrid/PQC, valmis laboristsenaariumid, salvestatud walkthrough, pitch deck partneritele. |
| **Faas 3 — EL / partneri pilot** | Legitiimsus ja rahastus | 12+ kuud | AI Act, pikaajaline usaldus, postkvantvalmidus, digitaalne suveräänsus; pilot 1–2 partneriga, aruanne, mõõdetavad tulemused, taaskasutatavad õppemoodulid. |

Projekti praegune olek katab juba olulise osa Faasist 1 (allkiri, TSA, Evidence Package, valikuline PQC, veebiliides, offline kontrollija). PLAN_EDU keskendub haridusliini vormistamisele ja järgmistele sammudele.

---

## Arendusülesanded (üksikasjalikud sammud)

### EDU.1 — Dokumentatsioon ja navigatsioon hariduse jaoks

| Samm | Tegevus | Üksikasjad |
|------|---------|------------|
| EDU.1.1 | Lisada peamisse README-sse plokk „Education & Research“ | Viited PLAN_EDU-le (ET), PLAN_PQC-le, legal (PQC, eIDAS, AI Act), laboristsenaariumidele. |
| EDU.1.2 | docs/README-sse rida „Plan EDU“ | Dokumentide teemade tabelis: Plan EDU (ET) — haridusplatvorm, lab-stsenaariumid, PQC. |
| EDU.1.3 | Luua docs/internal/et/EDU_README.md (valikuline) | Lühike indeks: mida lugeda üliõpilasel/õppejõul (PoC, PLAN_PQC, Usaldusmudel, Legal), kuidas lab käivitada. |

### EDU.2 — Laboristsenaariumid (markdown + diagrammid)

| Samm | Tegevus | Üksikasjad |
|------|---------|------------|
| EDU.2.1 | Stsenaarium „Klassikaline usaldusahel“ | Kirjeldus: päring → AI vastus → kanoniseerimine → räsi → RSA-allkiri → RFC 3161. Mermaid-ahela diagramm. Ülesanne: kontrollida üht vastust UI ja offline JARiga. |
| EDU.2.2 | Stsenaarium „Hübriidallkiri (RSA + PQC)“ | Kui PQC on sisse lülitatud: teine allkirjastaja (ML-DSA) sama räsi üle. Selgitada, miks kaks allkirjastajat, kuidas mõlemat Evidence Package’is vaadata. Ülesanne: laadi .aep alla ja veendu signature_pqc.sig ja pqc_public_key.pem olemasolus. |
| EDU.2.3 | Stsenaarium „Murra usaldusahel“ | Õppeülesanne: muuta lahtipakitud .aep-s response.txt — kontrollija peab tagastama INVALID. Arutada: terviklikkus, räsi, allkiri. |
| EDU.2.4 | Stsenaarium „Kontroll 10 aasta pärast“ | Kontseptuaalne stsenaarium: mis jääb kontrollitavaks PQC-allkirja ja TSA-tokeni olemasolul; mida tuleb säilitada (avalik võti, algoritm, token). Viited NIST FIPS 204, RFC 3161, legal/README. |

### EDU.3 — Õppematerjalid PQC ja kvantohuvis kohta

| Samm | Tegevus | Üksikasjad |
|------|---------|------------|
| EDU.3.1 | Lühike glossaar (PLAN_EDU-s või eraldi fail) | ML-DSA, Dilithium, FIPS 204, postkvantohu, hübriidallkiri, Evidence Package, TSA. |
| EDU.3.2 | Viited alla laaditud dokumentidele legal-is | legal/README sektsioon „Hariduse ja uurimistöö jaoks“: PDF-de nimekiri (NIST FIPS 203/204/205, vajadusel NIST IR 8547, ETSI TR) ja lühidalt, milleks iga dokument. |
| EDU.3.3 | Üks lehekülg „Miks Aletheia kasutab PQC“ | Akadeemilisele sihtrühmale: üks lehekülg (ET või EN) motivatsiooniga, hübriidallkirja skeemiga ja viidetega PLAN_PQC-le ja legalile. |

### EDU.4 — UI/UX täiendused demo ja õppe jaoks

| Samm | Tegevus | Üksikasjad |
|------|---------|------------|
| EDU.4.1 | Vihjed „Õppejõule“ / „Üliõpilasele“ | Valikulised plokid verify-lehel või dokumentatsioonis: „mida tunnis näidata“, „milliseid küsimusi esitada“. |
| EDU.4.2 | Lehekülg „Projektist“ / „Trust Lab“ | Lühidalt: mis on Trust Lab, kellele, viited PLAN_EDU-le, PLAN_PQC-le, legalile, repole. |
| EDU.4.3 | „Lühikokkuvõtte“ eksport aruande jaoks | Juba olemas „Copy summary“; vajadusel lisada struktureeritud eksport (nt JSON või tekst üliõpilase aruande sisestamiseks). |

### EDU.5 — Partneri- ja grantipakett (ettevalmistus)

| Samm | Tegevus | Üksikasjad |
|------|---------|------------|
| EDU.5.1 | One-pager (1 lehekülg) | Aletheia Trust Lab: probleem, lahendus, kellele, faasid, kontaktid. PDF või Markdown. |
| EDU.5.2 | Grant-stiilis kokkuvõte (pool lehekülge) | Taotluse stiilis kokkuvõte: eesmärgid, meetodid, oodatavad tulemused, seos AI Act / eIDAS / PQC-ga. |
| EDU.5.3 | Arhitektuuridiagramm (Mermaid) | Ahel: AI → kanoniseerimine → räsi → RSA → PQC (valikuline) → TSA → Evidence Package; kasutada README-s ja esitlustes. |

---

## LLM-i prompid

Allpool valmis prompid LLM-ide (Cursor, ChatGPT, Claude jne) kasutamiseks PLAN_EDU ülesannete elluviimisel.

---

### Prompt EDU-A: Lisa README-sse sektsioon Education

```
Lisa Aletheia AI projekti juurkausta README.md faili sektsioon "Education & Research" (või "Haridus ja uurimistöö"). 
Selles: 1) Üks-kaks lauset, et projekti saab kasutada õppelaborina postkvantkrüptograafia ja kontrollitava tehisintellekti vastutuse alal. 
2) Linkide nimekiri: docs/internal/et/PLAN_EDU.md (haridusplatvormi plaan), docs/internal/en/plan-pqc.md (postkvantkrüptograafia), docs/legal/README.md (normatiivsed ja PQC dokumendid) ning vajadusel viide laboristsenaariumidele docs-is. 
3) Lühike lause: üliõpilastele ja partneritele, reprodutseeritavad katsed, Evidence Package ja offline kontrollija. 
Säilita neutraalne ja faktipõhine toon, ilma turunduslubadusteta.
```

---

### Prompt EDU-B: Loo laboristsenaarium „Klassikaline ahel“

```
Loo uus fail docs/internal/et/LAB_CLASSICAL_CHAIN.md. 
Sisu eesti keeles. 
Struktuur: 1) Stsenaariumi eesmärk (mõista klassikalist ahelat: AI vastus → kanoniseerimine → räsi → RSA-allkiri → RFC 3161). 
2) Eeltingimused (käivitatud backend ja frontend või ainult JAR kontrollija ja valmis .aep). 
3) Samm-sammulised sammud: saada päring, saa vastus, laadi Evidence Package alla, kontrolli UI ja java -jar aletheia-verifier.jar kaudu. 
4) Mermaid-diagramm (sequenceDiagram või flowchart) ahelast „AI vastus“ kuni „TSA token“. 
5) Enesekontrolli küsimused (2–3 küsimust). 
6) Viited SIGNING.md, TIMESTAMPING.md, TRUST_MODEL.md. 
Ära leiuta API-d ja teid — kasuta projekti olemasolevaid (nt GET /api/ai/verify/:id, GET /api/ai/evidence/:id).
```

---

### Prompt EDU-C: Loo laboristsenaarium „Hübriidallkiri (PQC)“

```
Loo fail docs/internal/et/LAB_HYBRID_PQC.md eesti keeles. 
Stsenaarium juhul, kui Aletheias on PQC (ML-DSA) sisse lülitatud. 
Sisalda: 1) Eesmärk (mõista, miks Evidence Package’is on kaks allkirjastajat — RSA ja ML-DSA). 
2) Tingimused (PQC sisse lülitatud backendis, kuidas kontrollida API vastuse või signature_pqc.sig olemasolu järgi .aep-s). 
3) Sammud: genereeri vastus PQC-ga, laadi .aep alla, paki lahti, veendu signature_pqc.sig, pqc_public_key.pem, pqc_algorithm.json olemasolus; käivita offline kontrollija ja loe raportist rida PQC signature kohta. 
4) Mermaid-skeem: üks räsi, kaks allkirja (RSA + ML-DSA), seejärel TSA. 
5) Viited docs/internal/en/plan-pqc.md ja docs/legal/README.md (PQC sektsioon). 
Kasuta ainult repo olemasolevaid teid ja vorminguid.
```

---

### Prompt EDU-D: Stsenaarium „Murra ahel“

```
Loo docs/internal/et/LAB_BREAK_CHAIN.md eesti keeles. 
Õppestsenaarium: riku tahtlikult Evidence Package’i terviklikkust ja veendu, et kontrollija tagastab INVALID. 
Sammud: 1) Laadi .aep alla, paki lahti. 2) Muuda response.txt sisu (nt lisa tühik või sümbol). 3) Paki uuesti ZIP-iks (või kasuta muudetud failiga kausta, kui kontrollija toetab). 4) Käivita java -jar aletheia-verifier.jar muudetud pakil. 5) Selgita tekstis: miks räsi ei klapi, miks allkirja ei kontrollita. 
Lisa 1–2 kinnistusküsimust (mida räsi kaitseb, mida allkiri kaitseb). 
Viited CRYPTO_REFERENCE ja TRUST_MODEL.
```

---

### Prompt EDU-E: Glossaar ja lehekülg „Miks PQC“

```
Loo docs/internal/et/ kaks faili. 
1) EDU_GLOSSARY.md — lühike glossaar terminite kohta eesti keeles: ML-DSA, Dilithium, FIPS 204, postkvantohu, hübriidallkiri, Evidence Package, TSA, kanoniseerimine, RFC 3161. Iga termin: 1–2 lauset. 
2) EDU_WHY_PQC.md — üks lehekülg „Miks Aletheia kasutab PQC“: motivatsioon (pikaajaline kontrollitavus, kvantohu), skeem „üks räsi — kaks allkirja“ (RSA + ML-DSA), viited PLAN_PQC-le ja legal/README-le (NIST FIPS 204). Keel — eesti, toon — hariv, ilma reklaamita.
```

---

### Prompt EDU-F: Uuenda legal/README hariduse jaoks

```
Lisa docs/legal/README.md faili sektsioon "Hariduse ja akadeemilise uurimistöö jaoks" (pärast Post-Quantum Cryptography sektsiooni või lõpus enne Aletheia Mappingut). 
Sektsioonis: 1) Lühike lõik, et selle kausta ja downloads/ dokumendid on kasulikud krüptograafia, turvalisuse ja tehisintellekti regulatsiooni kursuste jaoks. 
2) Tabel või nimekiri: millist dokumenti (NIST FIPS 203/204/205, ETSI TR vajadusel, NIST IR 8547 vajadusel) milleks kasutada (nt FIPS 204 — ML-DSA allkirjade standard PQC laboritundide jaoks). 
3) Viide docs/internal/et/PLAN_EDU.md ja laboristsenaariumidele docs/internal/et/LAB_*.md kui need on olemas. 
Ära eemalda olemasolevat sisu, lisa ainult uus sektsioon.
```

---

## Laboristsenaariumid

(Lühike indeks — täida pärast failide loomist.)

| Stsenaarium | Fail | Eesmärk |
|-------------|------|---------|
| Klassikaline usaldusahel | [LAB_CLASSICAL_CHAIN.md](LAB_CLASSICAL_CHAIN.md) | AI vastus → kanoniseerimine → räsi → RSA → TSA; kontroll UI ja JARiga. |
| Hübriidallkiri (PQC) | [LAB_HYBRID_PQC.md](LAB_HYBRID_PQC.md) | ML-DSA olemasolu Evidence Package’is; kontrollija raport PQC kohta. |
| Murra ahel | [LAB_BREAK_CHAIN.md](LAB_BREAK_CHAIN.md) | Terviklikkuse rikkumine; oodatav tulemus INVALID. |
| Kontroll „10 aasta pärast“ | (kontsept PLAN_EDU-s) | Mida säilitada pikaajalise kontrolli jaoks; viited standarditele. |

---

## Sihtrühm ja partnerid

| Sihtrühm | Huvid | Suhtlusvorm |
|----------|-------|-------------|
| **Üliõpilased** | Krüptograafia, küberturvalisus, AI valitsemine | Laboritööd, kursused, demo tundides. |
| **Õppejõud** | Valmis stsenaariumid, diagrammid, viited standarditele | PLAN_EDU, LAB_*.md, legal/README, Mermaid repos. |
| **Partnerid (pangad, avalik sektor, nõuete täitmine)** | PKI, ajatemplid, PQC, tehisintellekti auditeeritavus | Demo, one-pager, pitch deck, pilotstsenaariumid. |
| **AI tarnijad** | Genereerimise tõestatav vastutus | Dokumentatsioon ahela kohta: AI väljund → allkiri → ajatempel. |
| **EL / grantid** | AI Act, eIDAS, digitaalne suveräänsus, PQC | Taotluse stiilis kokkuvõte, Faas 3 teekond. |

---

## Valmiduskriteeriumid ja riskid

**Valmiduskriteeriumid (haridusplatvormi MVP):**

- [ ] README-s on sektsioon Education & Research viidetega PLAN_EDU-le, PLAN_PQC-le, legalile.
- [ ] docs/README-s on lisatud rida Plan EDU (ET) kohta.
- [ ] Vähemalt üks laboristsenaarium (LAB_CLASSICAL_CHAIN või LAB_HYBRID_PQC) docs/internal/et/ Mermaidiga ja samm-sammuliste juhenditega.
- [ ] legal/README-s on alasektsioon dokumentide kasutamisest hariduse/uuringute jaoks; vajadusel uuendatud download.sh PQC PDF-ide jaoks.
- [ ] Glossaar või lehekülg „Miks PQC“ (EDU_GLOSSARY.md / EDU_WHY_PQC.md) on loodud.

**Riskid ja piirangud:**

- Labor ei ole sertifitseeritud toode; materjalides on selgelt märgitud „eksperimentaalne / hariduslik“.
- Õiguslikud ja normatiivsed järeldused peavad toetuma ametlikele allikatele (legal/README, EUR-Lex, NIST, ETSI); projekt ei anna juriidilist nõustamist.
- Dokumente legal/downloads/ võib vaja olla käsitsi alla laadida, kui automaatsed URL-id pole kättesaadavad.

---

## Viited

- [Plaan PQC](PLAN_PQC.md) — postkvantkrüptograafia Aletheias.
- [Visioon ja teekond](VISION_AND_ROADMAP.md) — toote faasid.
- [Usaldusmudel](TRUST_MODEL.md) — kes mille eest kinnitab, eIDAS.
- [Legal & Regulatory](../../legal/README.md) — PQC (NIST FIPS 203/204/205), eIDAS, AI Act, RFC 3161.
- [PoC](PoC.md) — arhitektuur ja stack.

**Tõlked:** Plaan EDU on olemas eesti (see fail), vene ([docs/internal/ru/plan-edu.md](../ru/PLAN_EDU.md)) ja vajadusel inglise keeles.
