# Aletheia AI — PQC manifest: postkvantkrüptograafia (väljaspool põhiscope'i / PoC)

**Hübriidne krüptograafiline attestatsioon AI väljundite jaoks: klassikaline + postkvantallkiri ühe ja sama tõendusliku räsi üle.**

See dokument on **põhiscope'ist väljas**, entusiastide poolt käivitatud plaan: teise, lisanduva PQC-allkirjakihi lisamine ilma olemasolevat usaldusahelat katkimata. Aletheia positsioneeritakse **kvantvalmis** pikaajalise tõendusmaterjali ja regulatoorse narratiivi jaoks, säilitades praeguse RSA + RFC 3161 torustiku täielikult kehtivana ja põhilisena.

**Staatus:** Mustand meditatsiooniks ja PoC · **Seotud:** [Allkirjastamine](SIGNING.md) · [Usaldusmudel](TRUST_MODEL.md) · [Visioon ja teekond](VISION_AND_ROADMAP.md)

---

## Sisu

- [Miks PQC ja miks praegu](#miks-pqc-ja-miks-praegu)
- [Standardid ja viited](#standardid-ja-viited)
- [Disainipõhimõtted](#disainipõhimõtted)
- [Tulemused ja ülesanded (LLM-readable)](#tulemused-ja-ülesanded-llm-readable)
- [Frontend: PQC-märgis ja turundus](#frontend-pqc-märgis-ja-turundus)
- [Backendi muudatused](#backendi-muudatused)
- [Verifitseerija utiliidi muudatused](#verifitseerija-utiliidi-muudatused)
- [Juurutamine (Ansible)](#juurutamine-ansible)
- [Lõpetamise kriteeriumid](#lõpetamise-kriteeriumid)
- [Riskid ja märkused](#riskid-ja-märkused)

---

## Miks PQC ja miks praegu

| Draiver | Kirjeldus |
|--------|------------|
| **Pikaajaline verifitseerimine** | Tänapäeval salvestatud tõendusmaterjale võib vaja minna 10–20 aasta pärast. Klassikalised RSA/ECDSA võivad muutuda suuremahuliste kvantarvutite ees haavatavaks; PQC-allkirjad jäävad selles stenaariumis turvaliseks. |
| **Tulevikukindel narratiiv** | "Aletheia — AI usaldusinfrastruktuur, valmis kvantajastuks." Tugev sõnum kaitse-, rahandus- ja regulatooridele (EU AI Act, eIDAS, pikaajaline arhiveerimine). |
| **Tehniline tipptase** | Hübriidallkiri (klassikaline + PQC ühe räsi üle) on soovitatav migratsioonitee; selle rakendamine näitab inseneride ettenägelikkust. |
| **Ilma katkestamiseta** | PQC on **ainult lisanduv**. Olemasolevad auditeerijad, juristid ja verifitseerijad toetuvad endiselt klassikalisele allkirjale ja TSA-le; PQC on täiendav kiht tuleviku jaoks. |

**Positsioneerimine:** *"Hübriidne krüptograafiline attestatsioon: klassikaline + postkvantallkirjad ühe tõendusliku räsi üle."* — Mitte "me kirjutasime kõik PQC-s ümber", vaid "me lisasime kvantkindla kihi, et tõendusmaterjal jääks aastakümneteks verifitseeritavaks".

---

## Standardid ja viited

| Ressurss | Kirjeldus | URL / viide |
|----------|-----------|-------------|
| **NIST PQC standardiseerimine** | NIST valis ML-DSA (Dilithium), ML-KEM (Kyber), SLH-DSA (SPHINCS+) jne | [NIST PQC Project](https://csrc.nist.gov/projects/post-quantum-cryptography) |
| **FIPS 204** | ML-DSA (Module-Lattice-Based Digital Signature Standard) — Dilithium-põhised allkirjad | [FIPS 204 (2024)](https://csrc.nist.gov/pubs/fips/204/final) |
| **FIPS 205** | SLH-DSA (Stateless Hash-Based Digital Signature Standard) | [FIPS 205 (2024)](https://csrc.nist.gov/pubs/fips/205/final) |
| **CRYSTALS-Dilithium** | Algorütmi algne nimi; NIST standardiseeris ML-DSA nime all | [CRYSTALS-Dilithium](https://pq-crystals.org/dilithium/) |
| **Bouncy Castle PQC** | Java implementatsioon PQC-algorütmidest (Dilithium, Kyber jne) | [Bouncy Castle PQC](https://www.bouncycastle.org/pqc_jce.html) · Maven: `org.bouncycastle:bcpkix-jdk18on` + PQC provider |
| **ETSI / pikaajaline arhiveerimine** | ETSI TS 101 733, PAdES; PQC pikaajalise allkirja kehtivuse jaoks | [ETSI](https://www.etsi.org/) |
| **NIST PQC migratsioon** | NIST juhised hübriidide ja migratsiooni kohta | [NIST PQC FAQ](https://csrc.nist.gov/projects/post-quantum-cryptography/post-quantum-cryptography-standardization) |

**PoC algorütmi valik:** **ML-DSA (Dilithium)** — NIST standard, hästi dokumenteeritud, saadaval Bouncy Castle'is; Falcon võib hiljem lisada.

---

## Disainipõhimõtted

1. **Topeltallkiri ühe räsi üle**  
   `canonical.bin → SHA-256 → [RSA allkiri] (klassikaline) + [ML-DSA allkiri] (PQC) → RFC 3161 timestamp` (ajatemper jääb klassikalise voo peale; TSA PQC on scope'ist väljas, kuni TSA-d PQC-d ei kasuta).

2. **Evidence Package jääb tagasiühilduvaks**  
   Uued failid: `signature_pqc.sig`, `pqc_public_key.pem`, `pqc_algorithm.json`. Olemasolevad `signature.sig`, `public_key.pem` jne muutumatud. Verifitseerijad ilma PQC-toeta eiravad uusi faile ja kontrollivad endiselt klassikat.

3. **SignatureService abstraktsioon**  
   Tuua sisse (või laiendada) abstraktsioon nii, et mitu allkirjastajat võivad kooseisuda: klassikaline (praegune RSA) ja PQC (ML-DSA). Sama räsi sisse, kaks allkirja välja.

4. **Valikuline runtime'is**  
   PQC-allkiri on sisse lülitatud ainult siis, kui PQC-võti on konfigureeritud (nt `ai.aletheia.signing.pqc-key-path`). Kui pole seatud — käitumine nagu praegu (Evidence Package'is PQC-faile pole).

---

## Tulemused ja ülesanded (LLM-readable)

*(Ülesanded PQC.1–PQC.9: täielikud Coding prompt'id on inglise keeles [EN versioonis](../en/PLAN_PQC.md); allpool lühikokkuvõtted ja kriteeriumid.)*

---

### PQC.1 — Backend: Bouncy Castle PQC sõltuvus ja PQC-võtme konfiguratsioon

**Coding prompt (LLM-readable):** vt [EN versioon](../en/PLAN_PQC.md#pqc1--backend-add-bouncy-castle-pqc-dependency-and-pqc-key-configuration).

**Lõpetamise kriteeriumid:** Maven build õnnestub; rakendus käivituda `pqc-enabled=false` ilma PQC-võtmeta.

---

### PQC.2 — Backend: ML-DSA (Dilithium) võtmepaari genereerimine ja salvestamine

**Coding prompt (LLM-readable):** vt [EN versioon](../en/PLAN_PQC.md#pqc2--backend-generate-and-store-ml-dsa-dilithium-key-pair).

**Lõpetamise kriteeriumid:** Utiliit toodab privaat- ja avalikuvõtme failid; Bouncy Castle loeb vormingut.

---

### PQC.3 — Backend: PqcSignatureService ja topeltallkirja torustik

**Coding prompt (LLM-readable):** vt [EN versioon](../en/PLAN_PQC.md#pqc3--backend-introduce-pqcsignatureservice-and-dual-signing-pipeline).

**Lõpetamise kriteeriumid:** Unit-test PqcSignatureService; PQC enabled → entity sisaldab signature ja signature_pqc; disabled → signature_pqc null.

---

### PQC.4 — Backend: Evidence Package laiendamine PQC-artefaktidega

**Coding prompt (LLM-readable):** vt [EN versioon](../en/PLAN_PQC.md#pqc4--backend-extend-evidence-package-with-pqc-artifacts).

**Lõpetamise kriteeriumid:** PQC sisse lülitatud → .aep sisaldab uusi faile; välja lülitatud → puuduvad.

---

### PQC.5 — Backend: PQC staatuse ja allkirja avaldamine API-s (GET /api/ai/verify/:id)

**Coding prompt (LLM-readable):** vt [EN versioon](../en/PLAN_PQC.md#pqc5--backend-expose-pqc-status-and-signature-in-api-get-apiaiverifyid).

**Lõpetamise kriteeriumid:** PQC olemas → JSON sisaldab signaturePqc ja pqcAlgorithm; puudub → null/välja jäetud.

---

## Frontend: PQC-märgis ja turundus

### PQC.6 — UI: Quantum-Resistant märgis ja lühitekst

**Coding prompt (LLM-readable):** vt [EN versioon](../en/PLAN_PQC.md#pqc6--ui-quantum-resistant-badge-and-short-copy).

**Lõpetamise kriteeriumid:** signaturePqc olemas → märgis nähtav; puudub → ei kuvata; tooltip ja aria-label olemas.

---

### PQC.7 — Turundustekst ja logo/märgise assett (valikuline)

**Coding prompt (LLM-readable):** vt [EN versioon](../en/PLAN_PQC.md#pqc7--marketing-copy-and-logobadge-asset-optional).

**Lõpetamise kriteeriumid:** README/Vision sisaldab PQC lauset ja linki; valikuliselt assett kasutusel.

---

## Verifitseerija utiliidi muudatused

### PQC.8 — Verifier: PQC-allkirja lugemine ja kontrollimine Evidence Package'ist

**Coding prompt (LLM-readable):** vt [EN versioon](../en/PLAN_PQC.md#pqc8--verifier-read-and-verify-pqc-signature-from-evidence-package).

**Lõpetamise kriteeriumid:** .aep PQC-ga → aruandes PQC staatus; .aep ilma PQC-ta → "PQC signature: not present"; JAR prindib PQC rea vajadusel.

---

## Juurutamine (Ansible)

### PQC.9 — Ansible: valikuline PQC-võti ja keskkonnamuutujad

**Coding prompt (LLM-readable):** vt [EN versioon](../en/PLAN_PQC.md#pqc9--ansible-optional-pqc-key-and-env).

**Lõpetamise kriteeriumid:** Ansible README dokumenteerib PQC muutujad; PQC välja lülitatud → backend ilma PQC-ta; sisse lülitatud ja võti seatud → PQC-allkirjad.

---

## Lõpetamise kriteeriumid

| # | Kriteerium | Staatus |
|---|------------|---------|
| 1 | Bouncy Castle PQC sõltuvus lisatud; PQC konfig (võtmetee, enabled) paigas | [ ] |
| 2 | ML-DSA võtmete genereerimise utiliit ja võtmefailid dokumenteeritud | [ ] |
| 3 | PqcSignatureService implementeeritud; topeltallkiri torustikus; signature_pqc salvestatakse DB-sse | [ ] |
| 4 | Evidence Package sisaldab PQC lubatud korral signature_pqc.sig, pqc_public_key.pem, pqc_algorithm.json | [ ] |
| 5 | GET /api/ai/verify/:id avaldab signaturePqc ja pqcAlgorithm vajadusel | [ ] |
| 6 | Verify-leht kuvab "Quantum-Resistant" / "PQC Verified" märgise PQC-allkirja korral | [ ] |
| 7 | Offline-verifitseerija loeb ja kontrollib PQC-allkirja; aruandes PQC staatus | [ ] |
| 8 | Ansible (või juurutamine) toetab valikulist PQC-võtit ja keskkonda | [ ] |

---

## Riskid ja märkused

| Risk | Leevendus |
|------|-----------|
| Bouncy Castle PQC API muutused | Kinnita sõltuvuse versioon; dokumenteeri PLAN_PQC-s täpne artefakt ja versioon. |
| PQC võtme suurus / jõudlus | Dilithium3 on PoC jaoks vastuvõetav; mõõda allkirjastamise aeg. Vajadusel Dilithium2. |
| Verifier JAR suurus | PQC provider võib JAR-i suurust suurendada; dokumenteeri. |
| Regulatoorne sõnastus | Kasuta "quantum-resistant" / "post-quantum" ja "pikaajalise verifitseerimise jaoks"; ära väida sertifitseerimist. |

---

## Viited

- [Allkirjastamine](SIGNING.md) — praegune RSA-allkiri ja võtmetee.
- [Usaldusmudel](TRUST_MODEL.md) — kes mille üle attesteerib.
- [Visioon ja teekond](VISION_AND_ROADMAP.md) — toote suund.
- [NIST PQC](https://csrc.nist.gov/projects/post-quantum-cryptography) · [FIPS 204 (ML-DSA)](https://csrc.nist.gov/pubs/fips/204/final) · [Bouncy Castle PQC](https://www.bouncycastle.org/pqc_jce.html).

**Tõlked:** [EN](../en/PLAN_PQC.md) · [RU](../ru/PLAN_PQC.md)
