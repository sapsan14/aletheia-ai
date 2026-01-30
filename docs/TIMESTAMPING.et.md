# Ajatemplid Aletheia AI-s

**RFC 3161 ajatemplid teevad AI väljundi ajas tõestatavaks.**

See dokument kirjeldab, kuidas ajatemplid on Aletheia backendi sisse viidud: TSA endpointi valikud, vigade käsitlemine, testimise strateegia, salvestamine ja ulatus.

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

## Seotud dokumendid

- [Allkirjastamine](SIGNING.et.md) — mida allkirjastame; allkirja baitid on see, mida ajatempliga märgime.
- [Usaldusmudel](TRUST_MODEL.et.md) — kes mida kinnitab, eIDAS vastendus.
- [Arhitektuuri diagrammid](../diagrams/architecture.md) — pipeline ja usaldusahel.
- [README](../README.md) — disaini ülevaade, käivitamise juhised.
