# Aletheia AI — Visioon ja teekond (eesti keeles)

Tootevisioon ja järgmised suured sammud: krüptograafiliselt kontrollitavate AI vastuste juurest **AI attestatsioonini** ja **usaldusinfrastruktuurini**. Dokument kirjeldab strateegilist suunda praeguse PoC ja demo taga.

**Seotud:** [PoC (arhitektuur)](PoC.md) · [Rakendusplaan](plan.md) · [Usaldusmudel](TRUST_MODEL.md)

---

## Sisukord

- [Praegune seis: mis meil on](#praegune-seis-mis-meil-on)
- [Positsioneerimine välismaailmale](#positsioneerimine-välismaailmale)
- [Suur samm 1: AI Claim (kinnitatav väide)](#suur-samm-1-ai-claim-kinnitatav-väide)
- [Suur samm 2: Kontroll meie süsteemiväliselt](#suur-samm-2-kontroll-meie-süsteemiväliselt)
- [Suur samm 3: Võtme- ja usaldusmudel](#suur-samm-3-võtme--ja-usaldusmudel)
- [Suur samm 4: Usaldusväärne aeg, mitte lihtsalt ajatempel](#suur-samm-4-usaldusväärne-aeg-mitte-lihtsalt-ajatempel)
- [Suur samm 5: AI Evidence Package](#suur-samm-5-ai-evidence-package)
- [Suur samm 6: AI vs AI kontroll](#suur-samm-6-ai-vs-ai-kontroll)
- [Suur samm 7: Vali üks killer-valdkond](#suur-samm-7-vali-üks-killer-valdkond)
- [Kokkuvõte ja järgmised toimingud](#kokkuvõte-ja-järgmised-toimingud)
- [Pilv ja HSM](#pilv-ja-hsm)
- [Strateegilised prioriteedid](#strateegilised-prioriteedid)
- [Investori pitch](#investori-pitch)

---

## Praegune seis: mis meil on

| Valdkond | Staatus |
|----------|---------|
| **PoC** | ✅ Valmis |
| **Demo** | ✅ Töötab lõpuni |
| **Krüpto-pipeline** | ✅ Canonicalize → hash → sign → timestamp (RFC 3161) |
| **RFC 3161 TSA** | ✅ Rakendatud (haruldane AI-projektides) |
| **Usaldusahel** | ✅ Allkirjastatud vastus + TSA-token; kontroll meie backendis |
| **Deploy** | ✅ Docker, docker-compose, Ansible |

Kõige raskem osa on tehtud: **krüptograafiliselt range usaldusahel AI jaoks**. Paljud stardiettevõtted kukuvad just siin. Järgmine tase on muuta «allkirjastatud AI vastus» **dokumenteeritavaks, audit-tasemel AI attestatsiooniks** ja **kontrolliks ilma meie serverita**.

---

## Positsioneerimine välismaailmale

Võime juba öelda:

> **Aletheia AI pakub krüptograafiliselt kontrollitavaid AI väiteid usaldusväärsete ajatemplitega — auditiks, juriidiliseks ja regulatiivseks kasutuseks.**

Nii liigume «chatboti turvalisusest» **AI attestatsiooni** ja **usaldusinfrastruktuuri** suunas.

---

## Suur samm 1: AI Claim (kinnitatav väide)

**Praegu:** «AI vastas nii ja see on allkirjastatud + ajatempliga».

**Edasi:** «AI tegi **väite X** **tingimustel Y**, ja see on **tõestatav**».

### Claim object (vormistatud vastus)

Allkirjastada mitte toorteksti, vaid struktureeritud **väidet** ja konteksti:

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
| **Tulemus** | Legal / compliance / audit tase |

→ Väljume «chatboti turvalisusest» **AI attestatsiooni** suunas.

---

## Suur samm 2: Kontroll meie süsteemiväliselt

**Praegu:** Kontroll = meie backend.

**Edasi:** Kontroll **ilma** meie süsteemita.

### Võrguühenduseta kontroll

| Tulemus | Eesmärk |
|---------|---------|
| **CLI** | `aletheia verify response.json` — kohalik käivitamine |
| **Puhas JS kontrollija** | Brauser või Node — ilma backend-päringuta |
| **Ekspordipakett** | `response.json`, `signature.pem`, `tsa.tsr`, `public_key.pem` |

Auditorid, kohus või ettevõtted saavad kontrollida **räsi**, **allkirja** ja **ajatemplit** ilma meie serverita. See on hetk, mil projekt muutub **usaldusinfrastruktuuriks**.

---

## Suur samm 3: Võtme- ja usaldusmudel

**Praegu:** Praktikas üks võti allkirjastamiseks.

**Edasi:** Selge **võtme- ja usaldusmudel**.

### Miinimum

| Element | Kirjeldus |
|---------|-----------|
| **key_id** | Igas kirjes; seos avaliku võtmega |
| **Avalik key registry** | JSON või JWKS |
| **Metaandmed** | `purpose`, `algorithm`, `valid_from` / `valid_to` |

### Hiljem

- **Võtmete rotatsioon** ilma vana tõendite rikkumata
- **Key provenance** — kes ja miks võtme väljastas
- **Erinevad võtmed** nt legal, medical, financial AI jaoks

→ See on juba **PKI AI jaoks**, mitte lihtsalt allkiri.

Vaata ka: [Usaldusmudel ja eIDAS](TRUST_MODEL.md).

---

## Suur samm 4: Usaldusväärne aeg, mitte lihtsalt ajatempel

**Praegu:** TSA = tehniline «allkirjastamise aeg».

**Edasi:** **Tõestus hetkest ajaloos** — usaldusväärne aeg.

### Ideed

| Mehhanism | Eesmärk |
|-----------|---------|
| **Multi-TSA** | 2–3 sõltumatut TSA-d üleliigsuse ja usalduse jaoks |
| **Ankur avalikesse allikatesse** | Bitcoin ploki räsi, Ethereum calldata või RFC 9162 Roughtime (valikuline) |

Tulemus: «See AI vastus **eksisteeris enne** sündmust X» — juriidiliselt ja teaduslikult tugev.

Vaata ka: [Ajatemplid](TIMESTAMPING.md).

---

## Suur samm 5: AI Evidence Package

Kogu kõik ühte **tõendipaketti**:

```
Aletheia Evidence Package (.aep)
├── response.txt
├── canonical.bin
├── hash.sha256
├── signature.sig
├── timestamp.tsr
├── metadata.json
└── public_key.pem
```

### Kasutusalad

- Kohtud
- Audit
- Regulaatorid
- Ettevõtlusuuringud
- Teaduspublikatsioonid

→ Defineerime **uue tõendivormingu AI jaoks**.

---

## Suur samm 6: AI vs AI kontroll

**Idee:** Üks AI vastab; teine AI **kontrollib** väidet. Mõlemad vastused on allkirjastatud ja ajatempliga.

| Roll | Näide |
|------|--------|
| **AI-1** | «See kood on turvaline.» |
| **AI-2** | «Kinnitan: väide on õige.» |

→ **Multi-agent usaldus** — lähemal teaduslikule kontrollile.

---

## Suur samm 7: Vali üks killer-valdkond

Oleme valmis tegutsema **arhitektina**, mitte ainult insenerina. Tuleb valida **üks** põhivaldkond.

### Tugevad kandidaadid

| Valdkond | Miks |
|----------|------|
| **Legal / compliance AI** | Lepinguid, klausleid, regulatiivne kontroll |
| **Medical AI opinions** | Teine arvamus, dokumentatsioon |
| **AI-genereeritud lepingud** | AI-põhiste lepingute allkirjastamine ja kinnitamine |
| **Scientific AI results** | Taastatavus, viited |
| **AI žurnalistika / faktiväited** | Kontrollitavad allikad ja väited |

Oluline: mitte «chat», vaid **väited tagajärgedega**.

---

## Kokkuvõte ja järgmised toimingud

### Juba tehtud

- ✅ PoC
- ✅ Demo
- ✅ Täielik krüpto-pipeline (canonicalize, hash, sign, timestamp)
- ✅ RFC 3161 (haruldane AI-projektides)
- ✅ Docker, docker-compose, Ansible deploy

### Järgmised suured sammud (mõju järjekorras)

1. **Evidence Package + võrguühenduseta kontroll** — .aep eksport, CLI kontrollija; *usaldusinfrastruktuuri hetk*
2. **Killer demo (legal/compliance)** — GDPR klausli kontrolli stsenaarium; vali üks valdkond
3. **AI Claim** — struktureeritud väide + kontekst lihtsa teksti asemel
4. **Usaldus- ja võtmemudel** — key_id, registry, rotatsioon, provenance
5. **Tugev ajatempli ankur** — multi-TSA, avalikud ankrud
6. **Killer-valdkond** — soovitus: **legal / compliance AI** (EU AI Act, lepingud, audit)

### Valikuline

- Üheleheküljeline **product vision**
- **Killer demo** investori jaoks
- **EU-stiilis teekond** (PoC → Pilot → Regulation)

---

## Pilv ja HSM

### Pilvepõhine deploy

Stakk (Docker, Spring Boot, Next.js) on pilvevalmis. Soovituslik tee:

| Pilv | Teenused |
|------|----------|
| **AWS** | ECS/EKS, RDS PostgreSQL, valikuliselt CloudHSM |
| **GCP** | Cloud Run, Cloud SQL |
| **Azure** | Container Apps, PostgreSQL |

→ Üks production-deploy pilves skaleeritavuse, SLA ja investorite usalduse jaoks.

### HSM (Hardware Security Module)

**Praegu:** Failipõhine allkirjastamisvõti (`ai.key`).

**Production / enterprise:** Allkirjastamine HSMi kaudu juriidilise kaalu ja compliance'i jaoks.

| Valik | Kasutus |
|-------|---------|
| **AWS KMS** | Managed; allkirjastamine API kaudu |
| **AWS CloudHSM** | Eritatud HSM; täielik kontroll |
| **Azure Key Vault HSM** | Managed HSM backing |
| **Google Cloud HSM** | Cloud HSM module |

**Tee:** Abstraheerida `SignatureService` — üks impl failipõhiseks, teine HSM/KMS jaoks. Dokumenteerida: «Enterprise: vaheta failist HSM-ile ühe konfig muudatusega.»

→ Arhitektuur toetab HSM-i; enterprise-klientidel on võimalik oma HSM sisse lülitada.

---

## Strateegilised prioriteedid

Soovituslik järjekord maksimaalse mõju saavutamiseks:

| # | Samm | Eesmärk |
|---|------|---------|
| 1 | **Evidence Package + võrguühenduseta kontroll** | Usaldusinfrastruktuur; kontroll ilma meie serverita |
| 2 | **Killer demo (legal/compliance)** | Product-market fit; investori lugu |
| 3 | **Pilvepõhine deploy** | Production-grade; SLA |
| 4 | **HSM integratsiooni tee** | Enterprise valmidus |
| 5 | **AI Claim** | Struktureeritud attestatsioon |
| 6 | **Key registry + rotatsioon** | PKI AI jaoks |

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
