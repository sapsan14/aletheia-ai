# Aletheia AI — Visioon ja teekond (eesti keeles)

Tootevisioon ja järgmised sammud: krüptograafiliselt kontrollitavate AI vastuste juurest **AI attestatsioonini** ja **usaldusinfrastruktuurini**. Strateegiline suund praeguse PoC ja demo taga.

**Seotud:** [PoC (arhitektuur)](PoC.md) · [Rakendusplaan](PLAN.md) · [Usaldusmudel](TRUST_MODEL.md) · [Tuleviku ideed](ideas/README.md)

---

## Sisukord

- [Praegune seis](#praegune-seis)
- [Positsioneerimine](#positsioneerimine)
- [Laiendatud visioon](#laiendatud-visioon)
- [Strateegiline teekond](#strateegiline-teekond)
- [Fookusalad (üksikasjalikult)](#fookusalad-üksikasjalikult)
- [Production: pilv ja HSM](#production-pilv-ja-hsm)
- [Investori pitch](#investori-pitch)

---

## Praegune seis

| Valdkond | Staatus |
|----------|---------|
| **PoC** | ✅ Valmis |
| **Demo** | ✅ Töötab lõpuni |
| **Krüpto-pipeline** | ✅ Canonicalize → hash → sign → timestamp (RFC 3161) |
| **RFC 3161 TSA** | ✅ Rakendatud (haruldane AI-projektides) |
| **Usaldusahel** | ✅ Allkirjastatud vastus + TSA-token; kontroll meie backendis |
| **Deploy** | ✅ Docker, docker-compose, Ansible |

Kõige raskem osa on tehtud: **krüptograafiliselt range usaldusahel AI jaoks**. Järgmine tase on muuta «allkirjastatud AI vastus» **audit-tasemel AI attestatsiooniks** ja **kontrolliks ilma meie serverita**.

---

## Positsioneerimine

> **Aletheia AI pakub krüptograafiliselt kontrollitavaid AI väiteid usaldusväärsete ajatemplitega — auditiks, juriidiliseks ja regulatiivseks kasutuseks.**

Nii liigume «chatboti turvalisusest» **AI attestatsiooni** ja **usaldusinfrastruktuuri** suunas.

---

## Laiendatud visioon

Vastuse-taseme attestatsioonist kaugemale võib usaldusinfrastruktuur ulatuda:

| Valdkond | Mida kinnitame | Miks oluline |
|----------|----------------|--------------|
| **Mudelid** | Mudeli kaalud, arhitektuur, checkpointi terviklikkus | Taastatavus, supply chain; EU AI Act traceability kõrge riskiga süsteemidele |
| **Agentid** | Agendi identiteet, võimalused, tööriistade kasutus | Multi-agent süsteemid; kes mida tegi |
| **Andmekogud ja andmed** | Koolitusandmete provenants, lineage, consent | Compliance, bias audit, autoriõigused |
| **AI-to-AI läbi MCP** | Attestatsioon Model Context Protocoli peal | Üks agent kinnitab teist; ühilduv AI trust kiht |

**Närvivõrgu kaalude allkirjastamine** — checkpointi krüptograafiline seostamine konkreetse versiooni ja provenantsiga. Kontrollija saab kinnitada täpselt mudelit, mis tulemuse andis.

Need on strateegilised perspektiivid: «allkirjastatud vastustest» **full-stack AI trust** — mudelid, andmed, agentid, vastused. Lähituleviku roadmap jääb vastuse attestatsiooni juurde; see on silmapiir.

### Täiendavad usalduskihid

Usaldus **tehtu** suhtes (kes mida tegi, millal) täieneb teiste kihtidega. **Proof of Human** (nt [World ID](https://worldcoin.org) / Orb ja sarnased) vastab küsimusele «kes on inimene» ilma isikuandmeid avaldamata. Aletheia vastab «mida täpselt tehti, kes ja millal». Need on ortogonaalsed teljed: üks kiht on inimese identiteet, teine on tulemuse krüptograafiline tõend. Proof of Human võib olla valikuline sisend agentide sertifikaatide väljastamisel (nt «agent kontrollitava inimese all»). Täpsemalt: [idee: World ID ja Aletheia](../ru/ideas/WORLD_ID_AND_ALETHEIA.md) (RU).

---

## Strateegiline teekond

Üks prioriteeditud plaan (faasid mõju ja sõltuvuse järgi):

| Faas | Fookus | Eesmärk |
|------|--------|---------|
| **1** | Evidence Package + võrguühenduseta kontroll | Usaldusinfrastruktuur; kontroll ilma meie serverita |
| **2** | Killer demo (legal/compliance) | Product-market fit; investori lugu; vali põhivaldkond |
| **3** | AI Claim (struktureeritud attestatsioon) | Audit-tasemel väited kontekstiga ja confidenceiga |
| **4** | Võtme- ja usaldusmudel | PKI AI jaoks; key_id, registry, rotatsioon |
| **5** | Usaldusväärse aja ankur | Multi-TSA, avalikud ankrud (Bitcoin, Ethereum, Roughtime) |
| **6** | Production: pilv + HSM tee | Skaleeritavus, SLA, enterprise valmidus |

**Tuleviku perspektiivid:**

- **AI vs AI kontroll** — üks AI väidab, teine kontrollib; multi-agent trust (seotus MCP ja laiendatud visiooniga)
- **EU-stiilis teekond** — PoC → Pilot → regulatiivse joondamine

---

## Fookusalad (üksikasjalikult)

### 1. Evidence Package ja võrguühenduseta kontroll

**Praegu:** Kontroll ainult meie backendis.

**Eesmärk:** Kontroll **ilma** meie süsteemita.

| Tulemus | Otstarve |
|---------|----------|
| **Evidence Package (.aep)** | response.txt, canonical.bin, hash.sha256, signature.sig, timestamp.tsr, metadata.json, public_key.pem |
| **CLI** | `aletheia verify response.json` — kohalik käivitamine |
| **Puhas JS kontrollija** | Brauser või Node — ilma backend-päringuta |

Auditorid, kohus või ettevõtted saavad kontrollida räsi, allkirja ja ajatemplit võrguühenduseta. See on hetk, mil projekt muutub **usaldusinfrastruktuuriks**.

---

### 2. Killer demo ja valdkonna valik

Tuleb valida **üks** põhivaldkond. Soovitus: **legal / compliance AI** (EU AI Act, lepingud, audit).

| Valdkond | Miks |
|----------|------|
| **Legal / compliance AI** | Lepinguid, klausleid, regulatiivne kontroll |
| **Medical AI opinions** | Teine arvamus, dokumentatsioon |
| **AI-genereeritud lepingud** | AI-põhiste lepingute allkirjastamine ja kinnitamine |
| **Scientific AI results** | Taastatavus, viited |
| **AI žurnalistika / faktiväited** | Kontrollitavad allikad ja väited |

Oluline: mitte «chat», vaid **väited tagajärgedega**.

**Konkreetne plaan:** Samm-sammulised ülesanded (Evidence Package, võrguühenduseta kontrollija, üks killer demo stsenaarium, LLM-readable koodi juhendid) on dokumendis [Plaan Phase 2](PLAN_PHASE2.md). Seal on ka [võimaluste kirjeldus](PLAN_PHASE2.md#võimalused-miks-see-suund) (turg, nišš, eristaja) lihtsas keeles.

---

### 3. AI Claim (kinnitatav väide)

**Praegu:** «AI vastas nii ja see on allkirjastatud + ajatempliga».

**Eesmärk:** «AI tegi **väite X** **tingimustel Y**, ja see on **tõestatav**».

Allkirjastada struktureeritud **väidet** ja konteksti toorteksti asemel:

```json
{
  "claim": "See lepingusõna on GDPR-iga kooskõlas",
  "confidence": 0.82,
  "reasoning_hash": "...",
  "model": "gpt-4.1",
  "policy_version": "gdpr-2024-05"
}
```

| Aspekt | Kasu |
|--------|------|
| **Allkirjastatud sisu** | Väide + kontekst (mudel, poliitika versioon) |
| **Confidence** | Selge ebakindlus legal/compliance jaoks |
| **Poliitika versioon** | Audit: millised reeglid kehtisid |

---

### 4. Võtme- ja usaldusmudel

**Praegu:** Praktikas üks võti allkirjastamiseks.

**Eesmärk:** Selge **võtme- ja usaldusmudel** — PKI AI jaoks.

| Element | Kirjeldus |
|---------|-----------|
| **key_id** | Igas kirjes; seos avaliku võtmega |
| **Avalik key registry** | JSON või JWKS |
| **Metaandmed** | purpose, algorithm, valid_from / valid_to |
| **Hiljem** | Võtmete rotatsioon, key provenance, erinevad võtmed legal, medical, financial AI jaoks |

**PKI / CA valikud** — allkirjastamisvõtmed saab väljastada ja hallata established PKI kaudu:

| Lahendus | Kasutus |
|----------|---------|
| **Smallstep (step-ca)** | Kerge CA; ACME, OIDC; short-lived certs; DevOps-sõbralik |
| **EJBCA** | Enterprise PKI; CA/RA/VA; Common Criteria; reguleeritud valdkonnad |
| **HashiCorp Vault** | Transit engine: räside allkirjastamine API kaudu (võtmed ei lahku Vaultist); PKI engine certide väljastamiseks; secrets API keys, DB, TSA creds jaoks |

**HashiCorp Vault praktikas** — Backend kutsub Vault Transit vastuse räsi allkirjastamiseks, mitte kohaliku võtmefaili kasutamiseks. Võtmed jäävad Vaulti; allkirjastamine on API-päring. Vault saab väljastada X.509 cert (PKI engine) ja hoida salasid (OpenAI key, TSA config). Levinud enterprise-s; sobib HSM-i kõrval või asemel tarkvarapõhise võtmete kaitse jaoks.

→ Aletheia allkirjastamise sertifikaadid — Smallstep/EJBCA; või allkirjastamine Vault Transit / HSM kaudu; täielik usaldusahel, revocation, lifecycle.

Vaata: [Usaldusmudel ja eIDAS](TRUST_MODEL.md).

---

### 5. Usaldusväärne aeg, mitte lihtsalt ajatempel

**Praegu:** TSA = tehniline «allkirjastamise aeg».

**Eesmärk:** **Tõestus hetkest ajaloos** — usaldusväärne aeg.

| Mehhanism | Eesmärk |
|-----------|---------|
| **Multi-TSA** | 2–3 sõltumatut TSA-d üleliigsuse ja usalduse jaoks |
| **Avalikud ankrud** | Bitcoin ploki räsi, Ethereum calldata, RFC 9162 Roughtime (valikuline) |

Tulemus: «See AI vastus **eksisteeris enne** sündmust X» — juriidiliselt ja teaduslikult tugev.

Vaata: [Ajatemplid](TIMESTAMPING.md).

---

### 6. AI vs AI kontroll (tulevik)

Üks AI vastab; teine AI **kontrollib** väidet. Mõlemad vastused on allkirjastatud ja ajatempliga.

| Roll | Näide |
|------|--------|
| **AI-1** | «See kood on turvaline.» |
| **AI-2** | «Kinnitan: väide on õige.» |

→ **Multi-agent usaldus** — lähemal teaduslikule kontrollile.

**Laiendus:** PKI idee autonoomsete AI-agentide jaoks (OpenClaw, agentid MCP ökosüsteemis, kontroll ja sertifitseerimine meie PKI-ga) on kirjeldatud eraldi dokumendis: [Idee: PKI AI-agentide jaoks — kasutamine ja testimine](ideas/PKI_FOR_AI_AGENTS.md). MCP Identity positsioneerimine: [Idee: Aletheia ja MCP Identity](ideas/ALETHEIA_AND_MCP_IDENTITY.md).

---

## Production: pilv ja HSM

**Pilv:** Stakk (Docker, Spring Boot, Next.js) on pilvevalmis. Soovitus: AWS (ECS/EKS, RDS), GCP (Cloud Run, Cloud SQL) või Azure (Container Apps, PostgreSQL).

**HSM:** Praegu — failipõhine võti `ai.key`. Production — allkirjastamine HSMi kaudu (AWS KMS, CloudHSM, Azure Key Vault HSM, Google Cloud HSM). Abstraheerida `SignatureService` — üks impl failipõhiseks, teine HSM/KMS jaoks. «Enterprise: vaheta failist HSM-ile ühe konfig muudatusega.»

---

## Investori pitch

> **Aletheia AI — Trust Infrastructure for AI**
>
> Muudame AI väljundid krüptograafiliselt kontrollitavateks tõenditeks: allkirjastatud, RFC 3161 ajatempliga, kontrollitavad võrguühenduseta.
>
> **Probleem:** Võimatu tõestada, mida AI ütles, millal ja millises kontekstis.
>
> **Lahendus:** Allkirjastatud AI väited usaldusväärsete ajatemplitega; kontroll ilma meie serverita.
>
> **Turg:** Legal tech, compliance, fintech, regulaatorid (EU AI Act).
>
> **Staatus:** PoC valmis; RFC 3161; eIDAS-ühilduv arhitektuur; tee HSM-i ja qualified TSA juurde.
>
> **Edasi:** Evidence Package, võrguühenduseta kontroll, piloot legal/compliance-s.

---

*Dokument kuulub Aletheia AI dokumentatsiooni. Rakendamise üksikasjad ja navigeerimine: [README](../../README.md), [plaan](PLAN.md), [dokumendite indeks](../README.md).*
