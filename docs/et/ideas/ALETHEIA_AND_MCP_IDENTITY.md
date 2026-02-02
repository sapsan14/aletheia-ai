# Idee: Aletheia ja MCP Identity — kuidas ma positsioneerin projekti (ausalt ja edasi)

Pärast PoC valmimist sattusin lehele [modelcontextprotocol-identity.io](https://modelcontextprotocol-identity.io/). Esimene reaktsioon: «kõik on juba tehtud või peaaegu». Allpool — kuidas ma seda ümber mõtlesin ja kuhu edasi vaatan.

**Staatus:** isiklik idee ja positsioneerimine, ei kuulu formaalsesse roadmapi.  
**Seotud:** [Visioon ja teekond](../VISION_AND_ROADMAP.md) · [Idee: PKI AI-agentide jaoks](PKI_FOR_AI_AGENTS.md).

---

## Sisukord

- [Mida ma avastasin ja kuidas sellega suhtun](#mida-ma-avastasin-ja-kuidas-sellega-suhtun)
- [Millega MCP Identity tegelikult tegeleb](#millega-mcp-identity-tegelikult-tegeleb)
- [Miks ma olen teises tasandis](#miks-ma-olen-teises-tasandis)
- [Mida neil puudub — ja kus on minu tugevus](#mida-neil-puudub--ja-kus-on-minu-tugevus)
- [Kuidas ma ei toimi](#kuidas-ma-ei-toimi)
- [Kuidas ma toimin](#kuidas-ma-toimin)
- [Järgmised sammud (endale)](#järgmised-sammud-endale)

---

## Mida ma avastasin ja kuidas sellega suhtun

Leidsin MCP Identity juba pärast oma PoC-d. Alguses tundus see nagu «mind joodeti ette». Ümber sõnastasin nii:

- **Probleem, millega tegeleme, on reaalne** — kui selle jaoks tehakse eraldi identity-kiht.
- **Suund on aktuaalne** — mõtlen samas vektoris nagu inimesed, kes kujundavad protokolle.
- **See ei ole kaotus, vaid kinnitamine** — enamik PoC-sid surevad, sest keegi sellega ei tegele; minul on vastupidine olukord.

See tähendab: näen MCP Identity kinnitusena, et sattusin õigesse kohta õigel ajal, mitte põhjusena projekti maha jätta.

---

## Millega MCP Identity tegelikult tegeleb

Kui vaadata ilma turunduseta, siis MCP Identity on:

- **Identity-kiht Model Context Protocoli jaoks**
- Fookus: seostamine agent ↔ tööriist ↔ kontekst, runtime identity, federation, authN/authZ.
- Maailm: cloud-native, API-first, developer-centric.

Põhiolemuselt **identiteet suhtluse jaoks** — kes kellega võib rääkida, kes kellele sessioonis usaldab. Mitte usaldus konkreetse tulemuse suhtes ega võrguühenduseta tõendid.

---

## Miks ma olen teises tasandis

Ma (teadlikult või intuitiivselt) läksin teise telje järgi:

| MCP Identity | Aletheia (see, mida ma teen) |
|--------------|-----------------------------|
| Kes kellega võib rääkida | Kes selle väljundi andis |
| Online-protokollid | Võrguühenduseta kontrollitavad tõendid |
| AuthN / AuthZ | Vastutavus / lahtiütlematus |
| Sessioon ja kontekst | Artefakt ja tõend |
| Usaldus API kaudu | Krüptograafiline usaldus |

**Üks lause:** MCP Identity vastutab «kes võib tegutseda», Aletheia — «kes tegutses, millal ja mida täpselt tehti».

Ma ei pea meid konkurentideks. Need on **ortogonaalsed kihid**: üks — identiteedi ja juurdepääsu kohta runtime'is, teine — allkirja, aja ja tulemuse tõendi kohta.

---

## Mida neil puudub — ja kus on minu tugevus

MCP Identity saidil on identity, protokoll, roadmap, mustandid. Seal puudub (või ei ole nende fookus):

- PKI ja võrguühenduseta kontroll sertifikaatide põhjal
- Ajatemplid ja non-repudiation
- Allkirjastatud AI-artefaktid
- Raam «AI kui subjekt, kelle tegevust saab tõestada»
- EU trust services / eIDAS narratiiv

Just selle peale ma Aletheiat ehitangi. Ma ei püüa neid protokolli poolest «kinni jõuda» — tegeleme teise mõttekihtiga.

---

## Kuidas ma ei toimi

Ma ei hakka:

- projekti MCP Identity tõttu maha jätma;
- kõike nullist ümber mõtlema;
- paanikasse sattuma ja järsku pööret tegema;
- nende protokolli funktsioonide järele taga ajama.

Ma usun, et ei võistle funktsioonidega, vaid mõttekihtiga: usaldus tulemuse suhtes, mitte sessiooni suhtes.

---

## Kuidas ma toimin

### 1. Ümber mõelda positsioneerimine (kõigepealt narratiiv, mitte kood)

Sõnastan Aletheia nii:

> **Aletheia on krüptograafiline usalduse ja tõendite kiht AI väljundite ja toimingute jaoks, mis täiendab agentide identiteedi ja orkestreerimise protokolle.**

Minu jaoks võtmesõna: **täiendav** (complementary). Ma ei asenda MCP Identity, vaid töötan selle kohal, mida agentid ja protokollid juba toodavad.

### 2. Teha MCP-st oma loo osa, mitte ohtu

Ütlen avalikult:

> **Me ei võistle teadlikult MCP Identityga. Eeldame, et MCP või sarnased protokollid on olemas — ja tagame usalduse nende tulemuste suhtes.**

Minu jaoks tõstab see projekti arhitektuuri tasandile, võtab ära kunstliku konflikti ja teeb Aletheia vajalikuks iga agent-raamistikuga kõrval.

### 3. Uuendada PoC-d eelkõige narratiiviga

Minu PoC on juba olemas. Lisan ühe ploki (slaid / README):

**«Töötab MCP / OpenClaw / iga agent-raamistikuga»** — ja näitan voogu:

```
Agent (MCP / OpenClaw)
   ↓
AI toiming
   ↓
Aletheia:
  - canonicalize
  - sign
  - timestamp
  - bundle proof
   ↓
Võrguühenduseta kontrollija
```

Nii positsioneerin end **trust anchorina**, mitte veel ühe identiteediprotokollina.

### 4. Fokusseeruda sellele, mida neil ei tule (või ei ole prioriteet)

Panustan sellele, mis jääb «minu»:

- **EU-first / eIDAS** — usaldusteenused, regulatiivsus.
- **PKI, CRL, OCSP, ajatemplid** — klassikaline usaldusahel.
- **Evidence bundle, mitte sessioon** — artefakt, mida saab hiljem kontrollida ilma pakujata.
- **Legal / audit / compliance** — nurk «tõend kohtule ja auditile».
- **Töötab isegi siis, kui AI pakuja kaob** — võrguühenduseta kontroll avalike võtmete ja CRL/OCSP põhjal.

See ei pruugi olla moodne, kuid see on see, mida regulaatorid, riigiasutused ja enterprise vajavad.

---

## Järgmised sammud (endale)

Aus ja tulevikku suunatud nimekiri:

1. **Ümber kirjutada VISION / README** MCP arvestusega ja sõnastusega «Aletheia täiendab, ei asenda».
2. **Selgelt sõnastada:** «Miks Aletheia on vajalik isegi siis, kui MCP Identity saab standardiks».
3. **Mõelda välja üks killer demo**, mida MCP Identity definitsiooni järgi ei kata (nt agenti allkirjastatud tulemuse võrguühenduseta kontroll ilma nende API juurdepääsuta).
4. **Pakendada positsioon** EU / PKI / trust services alla: regulatiivsus, audit, lahtiütlematus.

---

## Kokkuvõte (ausalt)

Kui ma näeksin MCP Identity ja mul oleks ükskõik — see räägiks nõrgast projektist. See, et olin kurvastatud, tähendab: sattusin tabamuspunkti, tundsin mõõtu ja «mõtete võistlust». Minu jaoks on see elava idee märk, mitte ühekordne hackathon-mäng.

Ma ei usu, et jäime hiljaks. Sisenesin teema juurde siis, kui see muutus tõeliselt huvitavaks — ja minu ülesanne on nüüd selgelt oma kihi hõivata: **usaldus tulemuse suhtes, mitte sessiooni suhtes**.

---

*Osa [Tuleviku ideed](README.md). Aletheia AI dokumentatsioon: [indeks](../../README.md), [Visioon ja teekond](../VISION_AND_ROADMAP.md). Tõlge: [RU](../ru/ideas/ALETHEIA_AND_MCP_IDENTITY.md).*
