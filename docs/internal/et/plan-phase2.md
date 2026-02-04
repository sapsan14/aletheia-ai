# Aletheia AI — Plaan Phase 2 (Killer demo)

See dokument kirjeldab teekonna **Phase 2**: killer demo legal/compliance valdkonnas, Evidence Package, võrguühenduseta kontroll ja üks taastatav stsenaarium. Sisaldab **võimaluste** kirjeldust, arendussamme **LLM-readable koodi juhenditega** ja testimise kriteeriume.

**Seotud:** [Visioon ja teekond](VISION_AND_ROADMAP.md) (Phase 2 = Killer demo) · [Rakendusplaan](PLAN.md) (PoC sammud) · [Usaldusmudel](TRUST_MODEL.md)

---

## Sisukord

- [Võimalused: miks see suund](#võimalused-miks-see-suund)
- [Phase 2 eesmärk ja ulatus](#phase-2-eesmärk-ja-ulatus)
- [Arendussammud (LLM-readable juhenditega)](#arendussammud-llm-readable-juhenditega)
- [Phase 2 lõpetamise kriteeriumid](#phase-2-lõpetamise-kriteeriumid)
- [Viited](#viited)

---

## Võimalused: miks see suund

### Turg ja regulatiivsus

- **AI Trust / TRiSM turg:** ~$2.34B (2024) → ~$7.44B (2030), CAGR ~21.6%. Draiverid: regulatiivsus, seletatavus, audit, governance.
- **EU AI Act:** Jõus aug 2024; faasitud kuni 2026–2027. Kõrge riskiga süsteemid vajavad traceability, vastavust, dokumentatsiooni. Harmoniseeritud standardite ja vastavuse organite viivitused loovad nõudlust tööriistade järele, mis annavad **tõendid** regulaatoritele.
- **Legal tech / lepingud:** TermScout (Certify™ AI), AlsoCheck (klausli-tasemel «Trust Objects» GDPR, AI Act, ISO 27001 jaoks), iCertis. Trend: mitte ainult «AI analüüsis», vaid **tõestatav väljund ajaga ja poliitikaga** — kohtutele ja auditoritele.
- **Fintech/regtech:** MAS, HKMA, FINRA ootavad AI model risk, audit trail, attestatsiooni. Allkirjastatud ja ajatempliga mudeli väljund sobib «attestation of model output» ja audit trailiga.

### Soovitatav nišš: legal / compliance AI

Phase 2 fookus on **legal / compliance AI** (lepingud, klauslid, EU AI Act, audit), sest:

1. **Kohustuslik nõudlus** — EU AI Act ja lepingute auditid nõuavad tõestatavaid tõendeid; «kes mida ütles, millal» sobib otse.
2. **Eristaja** — Vähesed pakuvad **RFC 3161 + võrguühenduseta kontrolli** ilma nende serverit kutsumata. Aletheia Evidence Package on kontrollitav igaühe poolt avaliku võtme ja TSA ahelaga.
3. **Valmidus maksta** — Juristid ja compliance maksavad audit-tasemel tõendite eest.
4. **Üks demo, palju kasutusviise** — Üks stsenaarium (allkirjastatud AI väide klausli/poliitika kohta) teenib investoreid, piloote ja regulaatore.

### Eristaja ühes lauses

**Aletheia muudab AI väljundid krüptograafiliselt kontrollitavateks, võrguühenduseta kontrollitavateks tõenditeks (allkiri + RFC 3161 ajatempel) — et auditorid, kohus ja ettevõtted saaksid tõestada, mida AI väitis ja millal, ilma meie backendist sõltumata.**

---

## Phase 2 eesmärk ja ulatus

**Eesmärk:** Üks taastatav **killer demo** legal/compliance valdkonnas: AI väide (nt klausli vastavus), allkirjastatud ja ajatempliga, pakendatud Evidence Package'ina ja võrguühenduseta kontrollitav. Uuenda narratiiv: «töötab MCP / iga agent framework'iga».

**Väljaspool Phase 2:** Täielik AI Claim skeem toodangus, PKI key registry, multi-TSA, HSM. Minimaalne AI Claim (claim + confidence + policy_version) — ainult demo toetamiseks.

**Tulemused:** (1) Evidence Package (minimaalne); (2) Võrguühenduseta kontrollija (CLI/skript) `aletheia verify <path>`; (3) Üks killer demo stsenaarium (legal/compliance, ≤5 min); (4) Minimaalne AI Claim paketis; (5) Narratiiv ja dokumentatsioon (README, «Works with MCP / OpenClaw / any agent»).

---

## Arendussammud (LLM-readable juhenditega)

### Samm DP2.1 — Evidence Package genereerimine

**Eesmärk:** Iga allkirjastatud vastuse (või eraldi endpoint'i) jaoks toota Evidence Package: failide kogum või arhiiv kõige vajalikuga võrguühenduseta kontrolliks.

**Hinnang:** 4–6 h

#### Ülesanne DP2.1.1 — Evidence Package vorming

Defineerida ja dokumenteerida minimaalne .aep: response.txt, canonical.bin, hash.sha256, signature.sig, timestamp.tsr, metadata.json, public_key.pem. Dokumenteerida README-s või docs/internal/en/plan-phase2.md. Vastuvõtu kriteerium: kõik seitse komponenti loetletud.

#### Ülesanne DP2.1.2 — Backend: paketi genereerimine response id järgi

Lisada EvidencePackageService (või ekvivalent): sisend — response text, canonical bytes, hash, signature, tsa token, model, created_at, public key PEM; väljund — Map/kaust/ZIP. Valikuline: GET /api/ai/evidence/:id, tagastab ZIP või JSON base64-ga. Kriteeriumid: unit-test (kõik 7 komponenti, hash kattub); kui endpoint olemas — integratsioon 200.

---

### Samm DP2.2 — Võrguühenduseta kontrollija (CLI või skript)

**Eesmärk:** Programm või skript tee järgi Evidence Package'ile (kaust või .aep): allkirja ja TSA kontroll ilma backend'i kutsumata.

**Hinnang:** 4–6 h

#### Ülesanne DP2.2.1 — Kontrolli loogika (allkiri + TSA)

Lugeda hash.sha256, canonical.bin või response.txt, signature.sig, timestamp.tsr, public_key.pem. Kontrollida allkiri räsi järgi (BouncyCastle/OpenSSL/crypto). Parsida RFC 3161 token, eraldada genTime. Väljund: VALID/INVALID + lühike aruanne. Unit-testid: kehtiv pakett → VALID; võltsitud allkiri/räsi → INVALID.

#### Ülesanne DP2.2.2 — CLI

Käsk `aletheia verify <path>` (või npm run verify / skript). Exit 0 kehtiva puhul, 1 kehtetu puhul. Dokumenteerida README-s jaotis «Offline verification».

---

### Samm DP2.3 — Killer demo stsenaarium (legal/compliance)

**Eesmärk:** Üks lõpuni stsenaarium: compliance küsimus → vastus allkirjaga ja ajatempliga → Evidence Package eksport → auditor käivitab verifieri. Taastatav ≤5 min.

#### Ülesanne DP2.3.1 — Demo skript

Luua docs/DEMO_SCRIPT.md (või osa PLAN_PHASE2-s): sammud (1) operaator saadab prompti «Does this clause comply with GDPR? [clause]»; (2) backend tagastab response id; (3) .aep eksport; (4) auditor saab .aep; (5) `aletheia verify <path>` → VALID. Näidisprompt, eeltingimused.

#### Ülesanne DP2.3.2 — Valikuline: nupp «Download evidence» frontendis

Nupp/link vastuse lehel: GET /api/ai/evidence/:id, faili salvestamine .aep/.zip. Kriteerium: käsitsi test — fail laeb alla, verifier aktsepteerib.

---

### Samm DP2.4 — Minimaalne AI Claim (valikuline)

Laiendada metadata.json: claim, confidence, policy_version; allkirjastada kanoniline vorm (response+metadata). Verifier VALID korral võib kuvada claim ja policy_version. Kriteeriumid: unit — metadata sisaldab välju, allkiri kontrollitav.

---

### Samm DP2.5 — Narratiiv ja dokumentatsioon

README-s: jaotised «Evidence Package & offline verification», «Killer demo (Phase 2)», «Works with MCP / OpenClaw / any agent»; lingid [Plan Phase 2](docs/internal/en/plan-phase2.md), DEMO_SCRIPT, Vision Phase 2. docs/README: Plan Phase 2 | [EN](en/plan-phase2.md) · [RU](ru/plan-phase2.md) · [ET](et/plan-phase2.md).

---

## Phase 2 lõpetamise kriteeriumid

Phase 2 on lõpetatud, kui: (1) Evidence Package genereeritakse iga allkirjastatud vastuse jaoks (või endpoint'i kaudu); (2) Võrguühenduseta kontrollija töötab ilma backendita; (3) Demo skript on dokumenteeritud ja taastatav ≤5 min; (4) README ja dokumentatsioon uuendatud; (5) Valikuline: üks piloot või LOI.

---

## Viited

- [Visioon ja teekond](VISION_AND_ROADMAP.md) — Phase 2, Evidence Package, AI Claim.
- [Rakendusplaan](PLAN.md) — PoC sammud (1–8).
- [Usaldusmudel](TRUST_MODEL.md) — kes mida kinnitab; eIDAS.
- [Allkirjastamine](SIGNING.md), [Ajatemplid](TIMESTAMPING.md) — backend allkiri ja TSA.
- [Diagrammid](../../diagrams/architecture.md) — pipeline ja usaldusahel.

**Teised keeled:** [EN](../en/PLAN_PHASE2.md) · [RU](../ru/PLAN_PHASE2.md)
