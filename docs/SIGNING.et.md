# Allkirjastamine Aletheia AI-s

**Digitaalsed allkirjad teevad AI väljundi omistatavaks ja võltsimiskindlaks.**

See dokument kirjeldab, kuidas allkirjastamine on Aletheia backendi sisse viidud: mida allkirjastame, võtme haldus, vigade käsitlemine, testimine, salvestamine ja ulatus.

---

## Sisukord

- [Mida allkirjastame](#mida-allkirjastame)
- [Võtme haldus](#võtme-haldus)
- [Liides ja algoritm](#liides-ja-algoritm)
- [Vigade käsitlemine](#vigade-käsitlemine)
- [Testimise strateegia](#testimise-strateegia)
- [Salvestamise mudel](#salvestamise-mudel)
- [Turvalisus ja ulatus](#turvalisus-ja-ulatus)
- [Seotud dokumendid](#seotud-dokumendid)

---

## Mida allkirjastame

Allkirjastame **räsi** kanoonilise AI vastuse pealt, mitte toorest teksti.

Ahel:

```
AI vastuse tekst  →  kanoniseerimine  →  räsi (SHA-256)  →  allkiri (RSA)
                                                                  ↓
                                                    allkirja baitid (ajatempli jaoks)
```

- **Kanoniseerimine** tagab, et sama loogiline sisu annab alati samad baitid (Unicode NFC, rea lõpud, tühikud).
- **Räsi** (SHA-256) annab fikseeritud suurusega räsi; allkirjastame selle räsi.
- **Allkiri** seob räsi meie privaatvõtmega; TSA märgib seejärel **allkirja baitide** peale ajatempli (vaata [Ajatemplid](TIMESTAMPING.et.md)).

Me kinnitame *mida* öeldi; TSA kinnitab *millal* see allkirjastati. Vaata [Usaldusmudel](TRUST_MODEL.et.md) ja [diagrammid](../diagrams/architecture.md).

---

## Võtme haldus

- **Vorming:** PEM (RSA privaatvõti). Genereeri OpenSSLiga:
  ```bash
  openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
  ```
- **Asukoht:** Konfigureeritav tee `ai.aletheia.signing.key-path` (fail või `classpath:...`). Võti on käivitamisel valikuline; sign/verify annavad selge veateate, kui võti pole laetud.
- **PoC ulatus:** Üks võti piisab; võtme rotatsioon on ulatusest väljas. HSM-i pole; võti laetakse failist või classpathist.

---

## Liides ja algoritm

- **Allkirjasta:** `sign(hashHex)` või `sign(hashBytes)` → allkiri byte[] või Base64.
- **Kontrolli:** `verify(hashHex, signatureBase64)` või `verify(hashBytes, signatureBytes)` → boolean.

Algoritm: **RSA koos SHA-256** (PKCS#1 v1.5, DigestInfo). BouncyCastle; avalik võti tuletatakse privaatvõtmest kontrollimiseks.

Teenus on **neutraalne**: allkirjastab ja kontrollib baitide räsisid. Pipelainis edastatakse kanoonilise vastuse räsi; AI-spetsiifilisi tüüpe liideses pole.

---

## Vigade käsitlemine

`SignatureService` peab käsitlema:

- **võti pole konfigureeritud** (tühi tee või puuduv fail) — ebaõnnestub esimesel sign/verify selge sõnumiga;
- **kehtetu sisend** (null räsi, vale pikkus, mitte-hex) — `IllegalArgumentException`;
- **võtme laadimise viga** (vigane PEM, toetamata vorming) — ebaõnnestub laadimisel või esimesel kasutamisel; dokumenteeri Javadocis.

Valitud käitumine (valikuline võti käivitamisel või nõutud) on dokumenteeritud koodis ja README-s.

---

## Testimise strateegia

- **Üksiktestid:** Mälus RSA võtme paar (ilma PEM failita) või test PEM `src/test/resources`-is koos `classpath:test-signing-key.pem`. Testid: allkirjasta ja kontrolli õnnestub; võltsitud allkiri → false; kehtetu sisend → erand.
- **CI:** Välist võtit pole vaja; testid kasutavad mälus võtmeid või commititud test PEM-i (ainult testide jaoks).
- **Integratsioon:** Kui rakendus töötab reaalse võtme teega, sign/verify kasutavad seda võtit; eraldi "allkirjastamise serverit" pole.

---

## Salvestamise mudel

Allkiri salvestatakse koos:

- AI vastusega (tekst),
- vastuse räsiga (SHA-256 hex või baitid),
- ajatempli tokeniga (läbipaistmatud baitid — vaata [Ajatemplid](TIMESTAMPING.et.md)),
- metaandmetega (mudel, parameetrid, aeg).

Näide andmebaasi väljast:

```sql
signature BYTEA
```

Allkiri on **muutumatu** ja seda ei tohi pärast salvestamist muuta.

---

## Turvalisus ja ulatus

See PoC **tahtlikult**:

- ei rakenda eIDAS-i kvalifitseeritud elektroonilist allkirja (QES);
- ei kasuta HSM-i ega võtme tseremooniat;
- kasutab **üht** võtit; rotatsioon on ulatusest väljas.

**Arhitektuur** on kavandatud nii, et sama pipeline saab hiljem ühendada kvalifitseeritud usaldusteenuse pakkujate ja eIDAS-ga ühilduva allkirjastamisega (vaata [Usaldusmudel — eIDAS](TRUST_MODEL.et.md#eidas-vastendus-mittekvalifitseeritud--kvalifitseeritud)).

---

## Seotud dokumendid

- [Ajatemplid](TIMESTAMPING.et.md) — mida ajatempliga märgime (allkirja baitid), TSA, salvestamine.
- [Usaldusmudel](TRUST_MODEL.et.md) — usaldusahel, kes mida kinnitab, eIDAS vastendus.
- [Arhitektuuri diagrammid](../diagrams/architecture.md) — pipeline ja krüptokiht.
- [README](../README.md) — disaini ülevaade, käivitamise juhised.
