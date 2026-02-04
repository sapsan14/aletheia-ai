# Ajatemplid Aletheia AI-s

**RFC 3161 ajatemplid teevad AI väljundi ajas tõestatavaks.**

Dokument kirjeldab ajatemplite integreerimist Aletheia backendi: TSA valikud, mock/real lülitumine, vigade käsitlemine, testimine, salvestamine ja ulatus. Selge selgitus, miks tsaToken: [Crypto reference (EN)](../en/CRYPTO_REFERENCE.md#why-tsatoken-not-a-simple-timestamp).

---

## Sisukord

- [TSA endpoint](#tsa-endpoint)
- [Vigade käsitlemine](#vigade-käsitlemine)
- [Testimise strateegia](#testimise-strateegia)
- [Salvestamise mudel](#salvestamise-mudel)
- [Turvalisus ja ulatus](#turvalisus-ja-ulatus)
- [Kontseptuaalne kokkuvõte](#kontseptuaalne-kokkuvõte)
- [Tulevane töö (ulatusest väljas)](#tulevane-töö-ulatusest-väljas)
- [Lõppsõna](#lõppsõna)
- [Seotud dokumendid](#seotud-dokumendid)

---

## TSA endpoint

TSA (Time-Stamp Authority) endpoint on backendi suhtes **väline**. See võib olla:

- **lokaalne RFC 3161 server** (nt OpenTSA, OpenSSL TSA)
- **test stub** (deterministlik mock üksiktestide ja CI jaoks)
- **avalik TSA** (tulevik)
- **eIDAS-kvalifitseeritud TSA** (tulevik)

Backend saadab ajatempli päringud konfigureeritud URLile ja salvestab tagastatud tokeni läbipaistmatute baitidena.

### MOCK_TSA / REAL_TSA vahetamine

- **mode=mock** (vaikimisi) → MockTsaServiceImpl, võrgueta, deterministlik
- **mode=real** → RealTsaServiceImpl, HTTP POST aadressile `AI_ALETHEIA_TSA_URL`

Konfig: `ai.aletheia.tsa.mode`, `ai.aletheia.tsa.url`. Reaalse TSA valikud: avalikud (DigiCert, Sectigo), lokaalsed (OpenTSA), eIDAS. Üksikasjad: [docs/developers/en/timestamping.md](../en/TIMESTAMPING.md#switching-mock_tsa--real_tsa).

---

## Vigade käsitlemine

`TimestampService` peab käsitlema:

- **ühenduse rikkeid** (aegumine, kättesaamatu host)
- **kehtetuid TSA vastuseid** (mitte-2xx, vale content type)
- **vigaseid tokeneid** (parseerimata baitid)

Implementatsioon võib valida kas:

- **kontrollitud või runtime erandid** (fail fast, kutsuja käsitleb), või  
- **Optional-laadsed vastused** (nt `Optional<byte[]>` või `Result` tüüp)

Valitud lähenemine peab olema **selgelt dokumenteeritud koodis** (nt teenuse liideses ja implementatsiooni Javadocis).

---

## Testimise strateegia

Toetatakse kaht testimise režiimi.

### 1. Mock / Stub TSA (vaikimisi üksiktestide jaoks)

Deterministlik stub võib tagastada:

- fikseeritud baitide jada, või  
- reprodutseeritava mock tokeni

Kasutatakse:

- kiirete üksiktestide jaoks  
- CI pipeline-ites (välist TSA-d pole vaja)  
- pipeline loogika isoleeritud kontrollimiseks  

### 2. Lokaalne RFC 3161 TSA (integratsioonitestid)

Valikuline integratsioonitest OpenSSL TSA, OpenTSA või minimaalse lokaalse RFC 3161 serveriga.

Testide ootused:

- ajatempli token tagastatakse  
- token on BouncyCastle-iga **parseeritav**  
- genereerimise aeg (`genTime`) on ekstraheeritav (ja valikuliselt logitav)  

TSA server ise on selle ülesande **ulatusest väljas** — me tarbime endpointi, me ei rakenda seda.

---

## Salvestamise mudel

Ajatempli token salvestatakse koos:

- AI vastusega (tekst)  
- vastuse räsiga (SHA-256)  
- digitaalse allkirjaga  
- metaandmetega (mudel, parameetrid, aeg)  

Näide andmebaasi väljast:

```sql
timestamp_token BYTEA
```

Token on **muutumatu** ja seda ei tohi pärast salvestamist muuta.

---

## Turvalisus ja ulatus

See PoC **tahtlikult**:

- ei väida õiguslikku kehtivust  
- ei rakenda kvalifitseeritud usaldusteenuseid  
- ei tee sertifikaadiahela kontrollimist  

**Arhitektuur** on aga kavandatud täielikult ühilduvaks:

- eIDAS-kvalifitseeritud ajatempli asutustega  
- pikaajalise arhiveerimise kontrollimisega  
- regulatiivsete auditi stsenaariumitega  

Sama pipeline saab hiljem ühendada kvalifitseeritud TSA-ga ilma ümberkavandamiseta.

---

## Kontseptuaalne kokkuvõte

See ajatempli mudel **ei** püüa teha AI-d "tõeseks".

See teeb AI väljundi **tõestatavaks**.

Süsteem vastab ühele täpsele küsimusele:

> *"Kas saame tõestada, et see täpne väljund eksisteeris sel täpsel ajahetkel?"*

See võime muutub oluliseks, kui AI süsteemid arenevad vestlustööriistadest autonoomseks agentideks, kes mõjutavad reaalseid otsuseid.

---

## Tulevane töö (ulatusest väljas)

Järgmine on PoC ulatusest **tahtlikult välja jäetud**:

- ajatempli **kontrollimise** teenus  
- kvalifitseeritud TSA integratsioon  
- pikaajaline kontrollimine (LTV)  
- Evidence Record Syntax (ERS / RFC 4998)  
- regulatiivsed usaldusprofiilid  

Need jäävad loomulikeks laiendusteks tulevaseks faasiks.

---

## Lõppsõna

Ajatemplid ei ole vastutuse nihutamine.

Need on **reaalsuse säilitamine**.

Kui AI vastus on allkirjastatud ja ajatempliga märgitud, ei saa ajalugu enam ümber kirjutada.

---

## Testimine RFC 3161 testvektoritega

Implementatsiooni testimiseks **RFC 3161 testvektorid** ja **golden fixtures** pakuvad deterministlikke etalonväljundeid:

- **Testvektorid** = konkreetsed näited: antud räsi/digest → oodatav TSA vastus
- **Golden fixtures** = salvestatud etalonväljundid regressiooni testimiseks

**Näide testvektorist:**

```json
{
  "digest_sha256": "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9",
  "tst_token_base64": "MIIC...",
  "expected_time": "2026-01-01T00:00:00Z",
  "policy_oid": "1.2.3.4.5.6.7.8"
}
```

**Eesmärk:**
- Kontrollida, et RFC 3161 klient/server käitub õigesti
- Lisada valideerivad juhtumid ja edge case'id (vigased räsid, valed algoritmid)
- Võimaldada baidipõhist regressioonitestimist teadaolevate hea väljundite vastu

**Allikad:**
- BouncyCastle `tsp` test suites
- Avatud lähtekoodiga TSA implementatsioonid
- Aletheia MOCK_TSA (vt [MOCK_TSA](MOCK_TSA.md))

Deterministlikuks testimiseks Aletheias kasuta MOCK_TSA reprodutseeritavate tokenite genereerimiseks. Vt [Krüptograafiline oraakel](CRYPTO_ORACLE.md) testimise filosoofia jaoks.

---

## Seotud dokumendid

- [Visioon ja teekond](VISION_AND_ROADMAP.md) — järgmised sammud: usaldusväärne aeg, multi-TSA, ankurdamine.
- [Crypto reference (EN)](../en/CRYPTO_REFERENCE.md) — algoritmid, võtmed, miks tsaToken (inglise keeles).
- [Allkirjastamine](SIGNING.md) — mida allkirjastame; allkirja baitid on see, mida ajatempliga märgime.
- [Usaldusmudel](TRUST_MODEL.md) — kes mida kinnitab, eIDAS vastendus.
- [MOCK_TSA](MOCK_TSA.md) — deterministlik TSA testimiseks, RFC 3161 testvektorid.
- [Krüptograafiline oraakel](CRYPTO_ORACLE.md) — oraakli muster ajatempli testimiseks.
- [Arhitektuuri diagrammid](../../diagrams/architecture.md) — pipeline ja usaldusahel.
- [README](../../README.md) — disaini ülevaade, käivitamise juhised.
