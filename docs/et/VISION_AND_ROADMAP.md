# Aletheia AI — Visioon ja teekond (eesti keeles)

Tootevisioon ja järgmised sammud: krüptograafiliselt kontrollitavate AI vastuste juurest **AI attestatsioonini** ja **usaldusinfrastruktuurini**. Strateegiline suund praeguse PoC ja demo taga.

**Seotud:** [PoC (arhitektuur)](PoC.md) · [Rakendusplaan](plan.md) · [Usaldusmudel](TRUST_MODEL.md)

---

## Sisukord

- [Praegune seis](#praegune-seis)
- [Positsioneerimine](#positsioneerimine)
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

- **AI vs AI kontroll** — üks AI väidab, teine kontrollib; multi-agent trust
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

*Dokument kuulub Aletheia AI dokumentatsiooni. Rakendamise üksikasjad: [README](../../README.md), [plaan](plan.md).*
