# Krüptograafiline testoraakel

**Deterministlikud etalonväljundid krüptograafiliste implementatsioonide korrektsuse kontrollimiseks.**

---

## Sisukord

- [Mis on krüptograafiline testoraakel](#mis-on-krüptograafiline-testoraakel)
- [Miks on vaja krüptograafilisi oraaklit](#miks-on-vaja-krüptograafilisi-oraaklit)
- [Mis oraakel ON (ja mis ta EI OLE)](#mis-oraakel-on-ja-mis-ta-ei-ole)
- [Näide: ajatempli oraakel](#näide-ajatempli-oraakel)
- [Seos MOCK_TSA-ga](#seos-mock_tsa-ga)
- [Oraakli muster praktikas](#oraakli-muster-praktikas)
- [Analoogia: krüptograafia videoregistraator](#analoogia-krüptograafia-videoregistraator)
- [Miks see on oluline AI / LLM-agentide jaoks](#miks-see-on-oluline-ai--llm-agentide-jaoks)
- [Oraakli tüübid Aletheias](#oraakli-tüübid-aletheias)
- [Implementeerimise strateegia](#implementeerimise-strateegia)
- [Kokkuvõte](#kokkuvõte)
- [Seotud dokumendid](#seotud-dokumendid)

---

## Mis on krüptograafiline testoraakel

**Krüptograafiline testoraakel** on deterministlik ja reprodutseeritav etalonväljund, mida kasutatakse krüptograafiliste implementatsioonide korrektsuse kontrollimiseks.

Krüptograafias on paljud operatsioonid **disaini järgi mittedeterministlikud**:

- Digitaalallkirjad kasutavad juhuslikke nonce'e (ECDSA, RSA-PSS)
- Ajatemplid sõltuvad reaalajast
- Entroopia allikad erinevad süsteemiti
- Välised teenused (TSA, HSM, LLM-id) toodavad muutuvaid tulemusi
- Padding skeemid toovad sisse juhuslikust

Seetõttu on traditsioonilised kontrollid nagu:

```java
assertEquals(expected, actual);
```

sageli **võimatud** ilma oraakliga.

**Oraakel määratleb, mida peetakse „õigeks".**

---

## Miks on vaja krüptograafilisi oraaklit

Ilma krüptograafilise oraakliga degradeeruvad testid tavaliselt pinnapealseks kontrolliks:

```java
// ❌ Nõrgad testid ilma oraakliga
assertNotNull(response);              // "vastus ei ole null"
assertTrue(token.canBeParsed());      // "token on parsitav"
assertTrue(verifySignature(token));   // "allkirja kontrollimine tagastab true"
```

Need kontrollid valideerivad **struktuuri**, kuid mitte **korrektsust**.

Krüptograafiline oraakel võimaldab testimist süvamal tasemel:

| Testi tase | Ilma oraakliga | Oraakliga |
|------------|----------------|-----------|
| **Struktuur** | ✅ Saab parsida ASN.1 | ✅ Saab parsida ASN.1 |
| **Korrektsus** | ❌ Teadmata, kas väljund on õige | ✅ Baidipõhine vastavus etaloniga |
| **Regressioon** | ❌ Muudatused jäävad märkamatuks | ✅ Iga muudatus lõhub testi |
| **Ühilduvus** | ❌ Puudub versioonideheline kontroll | ✅ Sama oraakel töötab kõigil versioonidel |

**Eelised:**

- **Baidipõhine reprodutseeritavus** — täpne väljund antud sisendile
- **Deterministlik verifitseerimine** — sama oraakel töötab igavesti
- **Pikaajaline regressioonide kaitse** — muudatused avastatakse koheselt
- **Krüptograafiline ühilduvus** — tagab implementatsioonide ühilduvuse versioonide vahel

**See on eriti oluline:**

- PKI süsteemide jaoks (X.509, sertifikaadi ahelad)
- RFC 3161 ajatempelduse jaoks
- CMS / ASN.1 struktuuride jaoks
- Allkirjade valideerimise jaoks
- Agentide allkirjastamise ja audit trail'i jaoks
- Mitteeitavate väljundite jaoks

---

## Mis oraakel ON (ja mis ta EI OLE)

### ✅ Oraakel ON:

- **Deterministlik** — sama sisend annab alati sama väljundi
- **Reprodutseeritav** — saab vajaduse korral uuesti genereerida
- **Stabiilne aja jooksul** — töötab täna, homme, 10 aasta pärast
- **Sõltumatu elavatest teenustest** — pole võrku, pole väliseid sõltuvusi
- **Sobiv CI/CD jaoks** — kiire, usaldusväärne, pole flaky teste

### ❌ Oraakel EI OLE:

- **Päris TSA** — ei tõesta reaalset aega
- **Tootmis-CA** — ei väljasta usaldusväärseid sertifikaate
- **Elusorganisatsioon** — ei paku juriidilisi garantiisid
- **Turvalisuse piir** — mitte tootmises kasutamiseks
- **Asendus päris teenustele** — ainult testimiseks

**Võtmeprintsiip:**

> Oraakel on **testide tõeallikas**, mitte **usaldustugi**.

---

## Näide: ajatempli oraakel

### Sisend

```
digest = SHA-256("hello world")
       = b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9
```

### Oraakel määratleb

Oraakel hoiab **fikseeritud RFC 3161 ajatempli tokenit** selle räsi jaoks:

```
timestamp_token = BASE64(
  308203a1020101300d06092a864886f70d01010105000482012f...
)
```

See token on:

- **Genereeritud üks kord** — deterministliku MOCK_TSA poolt
- **Salvestatud test fixture'ina** — nt `src/test/resources/fixtures/hello-world.tsr`
- **Taaskasutatud igavesti** — kõigis tuleviku testides

### Mida oraakel valideerib

Iga tuleviku test kontrollib:

| Aspekt | Mida oraakel testib |
|--------|---------------------|
| **Parseerimine** | Token saab parseerida ASN.1 / RFC 3161 vormingus |
| **Struktuur** | Sisaldab validset `TSTInfo`, `SignedData`, `ContentInfo` |
| **Allkiri** | Allkirja verifitseerimise loogika töötab |
| **Ajatempel** | `genTime` väli vastab oodatud fikseeritud ajale |
| **Message imprint** | Räsi vastab sisendile |
| **RFC 3161 vastavus** | Kõik nõutud väljad on olemas |

**Kui baidid erinevad — implementatsioon on muutunud.**

See on regressiooni signaal: kas lisati viga või krüptograafiateek muutis käitumist.

---

## Seos MOCK_TSA-ga

**Kontseptuaalne erinevus:**

| Kontseptsioon | Roll |
|---------------|------|
| **MOCK_TSA** | Implementeerimise mehhanism — genereerib deterministlikke tokeneid |
| **Krüptograafiline oraakel** | Kontseptuaalne roll — määratleb, mis on „õige" |

**Aletheias:**

1. **MOCK_TSA** genereerib deterministlikke ajatempli tokeneid (RFC 3161).
2. Need tokenid **tegutsevad krüptograafiliste oraakliena** — nad määratlevad oodatud väljundid.
3. Testid valideerivad käitumist **oraakli suhtes** (baidipõhine võrdlus või struktuuri validatsioon).

**Näidis voog:**

```
Sisend (digest) → MOCK_TSA → TimeStampToken (baidid)
                                    ↓
                           Salvesta oraakliga
                                    ↓
             Tuleviku testid võrdlevad oraakliga
```

See võimaldab testida ajatempli loogikat **ilma vajaduseta**:

- Päris TSA endpoint
- Võrguühendus
- Süsteemikella sõltuvus
- Välised krüptograafilised teenused

---

## Oraakli muster praktikas

### 1. Genereerimise faas (üks kord)

```java
// Genereeri oraakel üks kord
MockTsaService mockTsa = new MockTsaService(fixedKey, fixedCert);
byte[] digest = sha256("hello world");
byte[] oracleToken = mockTsa.respond(createTSQ(digest));

// Salvesta test ressurssidesse
Files.write(Paths.get("fixtures/hello-world.tsr"), oracleToken);
```

### 2. Testimise faas (kordub igavesti)

```java
@Test
void timestampServiceMatchesOracle() throws Exception {
    // Laadi oraakel
    byte[] expectedToken = loadFixture("hello-world.tsr");
    
    // Genereeri token sama sisendiga
    byte[] actualToken = timestampService.timestamp("hello world");
    
    // Võrdle oraakliga
    assertArrayEquals(expectedToken, actualToken);
}
```

**Kui test kukub:**

- Kas implementatsioon muutus (regressioon)
- Või oraaklit tuleb uuendada (tahtlik muudatus)

### 3. Mitmetasandiline validatsioon

Oraaklit saab kasutada erinevatel tasanditel:

| Tase | Validatsioon |
|------|--------------|
| **Baidi tase** | `assertArrayEquals(expected, actual)` — kõige rangem |
| **Struktuuri tase** | Parseeri mõlemad, võrdle välju (`genTime`, `serial`, `policy`) |
| **Semantiline tase** | Kontrolli allkirja, räsi, valideeri ASN.1 — kõige leebem |

**Maksimaalse regressioonide kaitse jaoks: kasuta baidi taset.**

---

## Analoogia: krüptograafia videoregistraator

Krüptograafiline oraakel on nagu **videoregistraator** krüptograafia jaoks.

**Ta EI otsusta:**

- Kes on süüdi
- Mis oleks pidanud juhtuma
- Mis on abstraktses mõttes „õige"

**Ta salvestab:**

> „See on **täpselt** see, mis juhtus selles krüptograafilises hetkes."

Hiljem saab implementatsioone kontrollida selle vastu:

- Kas väljund muutus?
- Kas uus käitumine on ühilduv?
- Kas me saame sama tulemuse reprodutseerida?

**Oraakel on tunnistaja, mitte kohtunik.**

---

## Miks see on oluline AI / LLM-agentide jaoks

Kuna LLM-agendid muutuvad autonoomseks, hakkavad nad:

- Tegema otsuseid
- Genereerima väljundeid
- Käivitama toiminguid
- Suhtlema väliste süsteemidega (API-d, andmebaasid, kasutajad)

**Auditeeritavuse** ja **vastutuse eraldamise** jaoks muutub kriitiliseks teada:

| Küsimus | Miks see on oluline |
|---------|---------------------|
| **Mida agent ütles?** | Vastuse sisu |
| **Millal ta seda ütles?** | Ajatempel (mitteeitav) |
| **Millise mudeli/konfiigiga?** | Kontekst: mudeli versioon, temperatuur, prompt |
| **Kas ajalugu saab ümber kirjutada?** | Ei — krüptograafiline tõend |

### Krüptograafilised oraaklit võimaldavad:

1. **Deterministlik replay** — reprodutseerida agendi täpset väljundit testides
2. **Verifitseeritavad audit logid** — tõestada, mida agent ütles ja millal
3. **Mitteeitavad agendi väljundid** — agent ei saa varasemaid avaldusi eitada
4. **Pikaajaline vastutus** — audit trail elab üle aastaid

### Näide: agendi väljundi oraakel

```
Sisend:
  prompt = "Võta kokku leping X"
  model  = "gpt-4"
  
Oraakel:
  response     = "Leping X väidab..."
  signature    = 0x4a3f2e...
  timestamp    = 2026-01-01T00:00:00Z (TSA token)
  
Test:
  replay(prompt, model) → peab vastama oraakli response'ile
```

See tagab:

- Agendi käitumine on reprodutseeritav
- Testid püüavad kinni tahtmatud mudeli muudatused
- Audit trail on võltsimiskindel

**Aletheia jaoks:** Iga allkirjastatud ja ajatemplitatud LLM vastus võib teenida oraakliga regressioonitestimiseks ja auditi verifitseerimiseks.

---

## Oraakli tüübid Aletheias

| Oraakli tüüp | Eesmärk | Näidis fixture |
|--------------|---------|----------------|
| **Kanoniseerimise oraakel** | Kontrolli teksti normaliseerimist | `canonical-text.txt` |
| **Räsi oraakel** | Kontrolli SHA-256 väljundit | `hash-golden.bin` (32 baiti) |
| **Allkirja oraakel** | Kontrolli RSA PKCS#1 v1.5 allkirja | `signature-golden.sig` |
| **Ajatempli oraakel** | Kontrolli RFC 3161 tokenit | `timestamp-golden.tsr` |
| **RFC 3161 testvektor** | Täielik TSQ/TSR paar protokolli testimiseks | `test-vector.json` + `.tsq`/`.tsr` failid |
| **LLM vastuse oraakel** | Kontrolli agendi väljundit (deterministlike mudelite jaoks) | `llm-response-golden.json` |

Iga oraakel elab `src/test/resources/fixtures/` all ja on versioonis Git'is.

**Märkus RFC 3161 testvektorite kohta:** Erinevalt lihtsatest golden fixture'itest (üks väljundfail), sisaldavad testvektorid nii sisendit (`TimeStampRequest` / `.tsq`) kui oodatavat väljundit (`TimeStampResponse` / `.tsr`), pluss metaandmed (oodatav aeg, seerianumber, policy OID). Neid kasutatakse täieliku RFC 3161 protokolli vastavuse testimiseks, mitte ainult väljundi reprodutseeritavuse testimiseks. Vt [MOCK_TSA](MOCK_TSA.md) testvektorite genereerimise kohta deterministliku MOCK_TSA abil.

---

## Implementeerimise strateegia

### Samm 1: Genereeri oraaklit

Kasuta deterministlikke implementatsioone (MOCK_TSA, fikseeritud võtmed, fikseeritud aeg) etalonväljundite genereerimiseks:

```java
MockTsaService mockTsa = new MockTsaService(fixedKey, fixedCert);
byte[] oracle = mockTsa.respond(tsq);
saveFixture("oracle.tsr", oracle);
```

### Samm 2: Salvesta hoidlasse

```
backend/src/test/resources/fixtures/
  ├── canonical-text-1.txt
  ├── hash-golden-1.bin
  ├── signature-golden-1.sig
  └── timestamp-golden-1.tsr
```

Commit Git'i — oraaklit muutuvad koodibaasi osaks.

### Samm 3: Kirjuta testid

```java
@Test
void matchesOracle() {
    byte[] expected = loadFixture("oracle.tsr");
    byte[] actual = service.process(input);
    assertArrayEquals(expected, actual);
}
```

### Samm 4: Halda oraaklit

- **Tahtliku muudatuse korral** (algoritmi uuendus, teegi upgrade): regenereeri oraakel ja commit.
- **Juhuslike muudatuse korral** (viga, regressioon): test kukub → paranda kood, mitte oraakel.

**Kuldne reegel:** Oraakel peaks muutuma ainult siis, kui sa **tahtlikult** muudad krüptograafilist käitumist.

---

## Kokkuvõte

Krüptograafilised testoraaklit pakuvad:

| Eelis | Kirjeldus |
|-------|-----------|
| **Deterministlik verifitseerimine** | Sama sisend annab alati sama väljundi |
| **Reprodutseeritav testimine** | Testid töötavad igavesti, pole flakiness'i |
| **Krüptograafiline kindlustunne** | Baidipõhine korrektsus, mitte ainult struktuur |
| **Tulevikukindel regressioonikontroll** | Iga muudatus avastatakse koheselt |

**Need on fundamentaalsed usaldusväärsete süsteemide ehitamiseks, mis hõlmavad:**

- **PKI** (sertifikaadid, allkirjad, ahelad)
- **Ajatempeldus** (RFC 3161, TSA)
- **Allkirjastamine** (RSA, ECDSA, sisu allkirjastamine)
- **Autonoomsed AI agendid** (audit trail, mitteeitavus, vastutus)

**Aletheia jaoks:** Krüptograafilised oraaklit tagavad, et iga komponenti (kanoniseerimine, räsimine, allkirjastamine, ajatemplid, LLM vastused) testitakse deterministlike etalonväljundite vastu, võimaldades pikaajalise auditeeritavuse ja verifitseeritavuse.

---

## Seotud dokumendid

- [MOCK_TSA](MOCK_TSA.md) — Deterministlik TSA implementatsioon ajatempli oraakli genereerimiseks
- [Ajatemplid (TIMESTAMPING)](TIMESTAMPING.md) — RFC 3161, TSA endpoint, mida me ajatempliga märgime
- [Allkirjastamine (SIGNING)](SIGNING.md) — RSA PKCS#1 v1.5, deterministlikud allkirjad
- [Usaldusmudel (TRUST_MODEL)](TRUST_MODEL.md) — Kes mida kinnitab, eIDAS vastendus
- [Implementeerimise plaan (PLAN)](PLAN.md) — Ülesanne 2.4: TimestampService (kus oraaklit kasutatakse)
- [Arhitektuuri diagrammid](../../diagrams/architecture.md) — Pipeline krüptokihi ja usaldusahelaga
- [README](../../README.md) — Projekti ülevaade, disain, käivitamise juhised

---

**Staatus:** Kontseptuaalne dokument. Oraakli-põhine testimine on planeeritud Task 2.4 (TimestampService) ja hiljem.

**Litsents:** MIT (Aletheia AI projekti järgi).
