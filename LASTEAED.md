# Aletheia AI — Selgitus lastele ja täiskasvanutele

**Keeruliste asjade lihtne selgitus neile, kes tahavad mõista krüptograafiat ja testimist "kapoti all".**

---

## Kõige väiksematele: Mis see projekt on?

### Kujuta ette võluabilist

Kas sa tead, mis on ChatGPT või teised "targad abilised"? See on nagu võlusõber arvutis, kes oskab vastata küsimustele, kirjutada lugusid ja aidata kodutöödega.

Aga on üks probleem...

### Probleem: "Kas ta tõesti nii ütles?"

Kujuta ette sellist olukorda:

```
Sina: "Ema, robot ütles, et võib jäätist õhtusöögiks süüa!"
Ema: "Tõesti? Näita!"
Sina: *näitad ekraani*
Ema: "Aga äkki sa ise kirjutasid selle ja ütled, et see on robot?"
```

Kuidas **tõestada**, et robot tõesti nii ütles?

### Lahendus: Võlupitser

Meie projekt on nagu **võlupitser** roboti vastustele.

Kui robot vastab, siis me:
1. **Pildistame** tema vastuse (aga eriliselt — seda nimetatakse "räsi")
2. **Paneme pitseri** — "Jah, see on tõeline vastus!" (seda nimetatakse "allkiri")
3. **Kirjutame aja üles** — "See oli kell 3 päeval!" (seda nimetatakse "ajatempel")

Nüüd **keegi ei saa**:
- Muuta vastust (pitser läheb katki)
- Öelda "see polnud mina" (allkiri on olemas)
- Valetada aja kohta (ajatempel on olemas)

### Lihtne pilt

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│   🤖 Robot ütleb: "2 + 2 = 4"                              │
│                                                             │
│         ↓                                                   │
│                                                             │
│   📸 Pildistame (räsi): "abc123..."                        │
│                                                             │
│         ↓                                                   │
│                                                             │
│   🔏 Paneme pitseri (allkiri): "See on tõsi!"              │
│                                                             │
│         ↓                                                   │
│                                                             │
│   🕐 Kirjutame aja: "30. jaanuar 2026, 15:00"              │
│                                                             │
│         ↓                                                   │
│                                                             │
│   ✅ Nüüd on see TÕESTATAV!                                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Milleks seda päriselus vaja on?

1. **Arst küsib robotilt** ravimi kohta → peab tõestama, et robot just seda soovitas
2. **Jurist kasutab robotit** lepingu jaoks → peab kohtus tõestama
3. **Õpetaja kontrollib** — kas õpilane kirjutas ise või robot aitas?
4. **Firma teeb otsuse** roboti nõuande põhjal → vaja "tšekki" auditi jaoks

### Miks seda nimetatakse "Aletheia"?

**Aletheia** (Ἀλήθεια) — see on vanakreeka sõna.

See tähendab **"tõde"** või **"seda, mis pole varjatud"**.

Vanad kreeklased uskusid, et tõde on siis, kui eemaldad kõik katted ja näed asju sellistena, nagu nad on.

Meie projekt teeb sama: **näitab tõde** selle kohta, mida robot ütles.

---

## Tulevik: Kuhu me läheme?

### Maailm, kus tehisintellekt on kõikjal

Mõne aasta pärast robotabilised võivad PKI-d kasutada selleks, et:
🚗 Juhivad autosid
🩺 Panevad diagnoose
🏭 Juhivad tehaseid
💰 Teevad finantsotsuseid
⚖️ Aitavad kohtutes

**Küsimus:** Kuidas usaldada nende otsuseid?

### PKI tehisintellektile — mida see tähendab

**PKI** (Public Key Infrastructure) — see on "digitaalsete passide" ja "pitserite" süsteem.

Praegu kasutatakse PKI järgmistel eesmärkidel:
🔒 Turvalised veebisaidid (brauseris)
✍️ Dokumentide elektrooniline allkirjastamine
💸 Pangalised ülekanded
📧 Turvaline e-post (S/MIME)
🧑‍💻 Kasutajate autentimine ettevõtte süsteemides (VPN, portaalid)
🤖 Kaitstud IoT-kommunikatsioon (tark kodu, tööstusautomaatika)
🖥️ Tarkvara allkirjastamine ja kontroll (code signing)
💬 Turvalised sõnumid ja vestlused
🆔 Digipassid ja ID-kaardid
🗳️ Elektrooniline hääletamine ja küsitlused
☁️ Pilvandmete krüpteerimine
🏢 Juurdepääsu kontroll kriitilistes süsteemides (serverid, andmekeskused)
📜 Dokumentide kaitse ja kontroll riigiteenustes (sertifikaadid, litsentsid)

**Tulevik:** PKI tehisintellektile tähendab:
- Igal AI-agendil on oma "pass" (sertifikaat)
- Iga AI otsus on allkirjastatud ja tõestatav
- Saab jälgida ahelat: kes lõi AI → kes õpetas → mida otsustas

### Arengu teekaart

```
Praegu (PoC)
├── ✅ LLM vastuste allkirjastamine
├── ✅ Ajatemplid (RFC 3161)
└── ✅ Tegevuste audit-logi

Järgmine etapp
├── 🔜 AI-agentide sertifikaadid (X.509)
├── 🔜 Usaldusahel: Arendaja → Mudel → Agent
├── 🔜 Treeninguandmete allkirjastamine
└── 🔜 Mudelite versioonihaldus allkirjaga

Edasijõudnute tase
├── 📋 Kvalifitseeritud allkirjad (eIDAS tase 3)
├── 📋 Juriidiliselt siduvad AI otsused
├── 📋 Rahvusvaheline tunnustamine (EL, USA, Aasia)
└── 📋 ISO standard AI Audit Traili jaoks

Kaugem tulevik
├── 🚀 AI detsentraliseeritud identifitseerimine (DID)
├── 🚀 Plokiahel muutmatu auditi jaoks
├── 🚀 Ise-allkirjastavad autonoomsed agendid
└── 🚀 AI "elektroonilise isiku" õiguslik staatus
```

### Konkreetsed PKI-funktsioonid arenduseks

| Funktsioon | Kirjeldus | Kasu |
|------------|-----------|------|
| **AI Agent Certificates** | X.509 sertifikaat igale agendile | Identifitseerimine "kes see AI on" |
| **Model Signing** | Närvivõrgu kaalude allkirjastamine | Tõestus "seda mudelit pole asendatud" |
| **Training Data Attestation** | Andmestiku allkirjastamine | Tõestus "millel õpetati" |
| **Decision Chain Signatures** | Iga arutlussammu allkirjastamine | Selgitatavus "miks nii otsustas" |
| **Delegation Certificates** | Sertifikaat "AI tegutseb inimese nimel" | Juriidiline vastutus |
| **Revocation for AI** | "Halva" AI sertifikaadi tühistamine | Kaitse kompromiteeritud agentide eest |
| **Cross-Border Recognition** | Vastastikune tunnustamine riikide vahel | Globaalsed AI-teenused |

### Miks see on ühiskonnale oluline

```
Ilma PKI-ta AI jaoks:
├── ❌ Võimatu tõestada, mida AI ütles
├── ❌ Võimatu vastutusele võtta
├── ❌ Võltsitud AI vastused
├── ❌ Pole usaldust automaatsete otsuste vastu
└── ❌ Regulaatorid keelavad AI kriitilistes valdkondades

PKI-ga AI jaoks:
├── ✅ Iga otsus on tõestatav
├── ✅ Selge vastutusahel
├── ✅ Kaitse võltsimise eest
├── ✅ Äri ja kodanike usaldus
└── ✅ AI saab töötada meditsiinis, õiguses, rahanduses
```

### Aletheia kui vundament

Meie projekt on **esimene telliskivi** selles hoones.

Me näitame, et juba täna saab:
- Allkirjastada AI vastuseid
- Panna ajatempleid
- Luua audit traili
- Olla eIDAS-iga ühilduv

See on **proof of concept** (kontseptsiooni tõestus), mida saab arendada täielikuks süsteemiks.

---

## Sisukord (põhidokument)

1. [Projekti põhiidee](#1-projekti-põhiidee)
2. [Mis on krüptograafia lihtsate sõnadega](#2-mis-on-krüptograafia-lihtsate-sõnadega)
3. [Räsimine (SHA-256)](#3-räsimine-sha-256)
4. [Digitaalallkiri (RSA)](#4-digitaalallkiri-rsa)
5. [Ajatemplid (RFC 3161)](#5-ajatemplid-rfc-3161)
6. [PKI — Avaliku võtme infrastruktuur](#6-pki--avaliku-võtme-infrastruktuur)
7. [eIDAS — Euroopa usaldusreeglid](#7-eidas--euroopa-usaldusreeglid)
8. [MOCK_TSA — Simulaator testide jaoks](#8-mock_tsa--simulaator-testide-jaoks)
9. [Cryptographic Oracle — Etalonvastused](#9-cryptographic-oracle--etalonvastused)
10. [Golden Fixtures — Kuldsed näidised](#10-golden-fixtures--kuldsed-näidised)
11. [Testimine — Miks seda vaja on](#11-testimine--miks-seda-vaja-on)
12. [Agent Audit Model — AI tegevuste audit](#12-agent-audit-model--ai-tegevuste-audit)
13. [Mida me täna lisasime](#13-mida-me-täna-lisasime)
14. [Terminite sõnastik](#14-terminite-sõnastik)

---

## 1. Projekti põhiidee

### Probleem

Kujuta ette: sa küsisid ChatGPT-lt nõu, ta vastas. Nädala pärast tahad sõbrale tõestada, et AI tõesti nii ütles. Kuidas seda teha?

**Kuidagi mitte.** Sa ei saa tõestada:
- Et AI tõesti selle kirjutas (mitte sina ise)
- Millal täpselt ta selle kirjutas
- Et teksti pole muudetud

### Aletheia AI lahendus

Me teeme nii, et **iga AI vastust saab tõestada**:

```
AI kirjutas teksti → me "pitseerisime" selle krüptograafiliselt → saab kontrollida
```

**Aletheia** (Ἀλήθεια) — kreeka sõna, mis tähendab "tõde" või "paljastamine". Filosoofid kasutasid seda tähistamaks "seda, mis pole varjatud".

Meie projekt on **tõestatav tõde AI-lt**.

---

## 2. Mis on krüptograafia lihtsate sõnadega

### Analoogia ümbrikutega

Kujuta ette kolme tüüpi ümbrikuid:

1. **Tavaline ümbrik** — igaüks saab avada ja lugeda
2. **Pitseeritud ümbrik** — näha, kui keegi avas
3. **Seif koodiga** — ainult sina saad avada

Krüptograafia on teadus "digitaalsetest ümbrikutest":

| Ülesanne | Analoogia | Krüptograafiline meetod |
|----------|-----------|------------------------|
| Kontrollida, et teksti pole muudetud | Pitser ümbrikul | **Räsimine** |
| Tõestada, kes on autor | Allkiri dokumendil | **Digitaalallkiri** |
| Fikseerida aeg | Postmark | **Ajatempel** |
| Varjata sisu | Seif koodiga | Krüpteerimine (pole selles projektis) |

### Oluline mõista

Aletheia AI-s me **EI KRÜPTEERI** AI vastuseid. Me:
- **Allkirjastame** — et tõestada autorlust
- **Templime ajaga** — et tõestada, millal see oli
- **Räsime** — et avastada igasugused muudatused

Tekst jääb loetavaks, aga nüüd saab seda **kontrollida**.

---

## 3. Räsimine (SHA-256)

### Mis see on

**Räsi** — see on "sõrmejälg" andmete jaoks.

Nagu igal inimesel on unikaalne sõrmejälg, on igal tekstil unikaalne räsi.

### Kuidas töötab

```
Tekst: "Tere, maailm!"
         ↓
    [ SHA-256 ]
         ↓
Räsi: "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e"
```

### Räsi omadused

1. **Sama sisend → sama väljund**
   - "Tere" annab alati sama räsi
   - Seda nimetatakse **determinism**

2. **Väike muudatus → täiesti erinev räsi**
   - "Tere" ja "tere" (väike täht) annavad ERINEVAD räsid
   - Seda nimetatakse **laviiniefekt**

3. **Räsist ei saa teksti taastada**
   - Teades räsi, on võimatu teada saada algset teksti
   - See on **ühesuunaline funktsioon**

4. **Fikseeritud suurus**
   - SHA-256 annab alati 64 sümbolit (256 bitti)
   - Pole vahet, kas see on üks sõna või terve raamat

### Näide elust

Kujuta ette, et sa annad eksamit. Õpetaja ütleb:
> "Võta oma vastus, lase läbi räsi-masina, ütle mulle number."

Sa ütled: **"a591a6d40bf4..."**

Nüüd:
- Õpetaja **ei tea** sinu vastust
- Aga kui sa hiljem muudad vastust, räsi **muutub**
- Õpetaja saab kohe aru, et sa sohki tegid

### SHA-256 Aletheia's

```java
// Meie kood teeb seda:
String text = "AI vastus: Prantsusmaa pealinn on Pariis.";
byte[] hash = sha256(text.getBytes());
// hash = "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"
```

Nüüd see räsi on AI vastuse "sõrmejälg".

---

## 4. Digitaalallkiri (RSA)

### Miks allkirja vaja on

Räsi tõestab, et teksti pole muudetud. Aga **kes** selle lõi?

Digitaalallkiri on viis öelda:
> "Mina, selle võtme omanik, kinnitan seda dokumenti."

### Kuidas töötab (lihtsustatud)

Sul on kaks võtit — nagu kaks pusletükki:

```
┌─────────────────┐      ┌─────────────────┐
│  PRIVAATNE      │      │  AVALIK         │
│  (salajane)     │      │  (avalik)       │
│                 │      │                 │
│  Ainult sinul   │      │  Kõigil         │
│  Allkirjastamiseks │   │  Kontrollimiseks │
└─────────────────┘      └─────────────────┘
```

**Allkirjastamise protsess:**

```
Dokumendi räsi + Privaatne võti → Allkiri
```

**Kontrollimise protsess:**

```
Allkiri + Avalik võti → Räsi (peab kattuma originaaliga)
```

### Analoogia vahapitsatiga

Keskajal kuningad pitseerisid kirju vahaga isikliku pitseriga:
- Ainult kuningal oli **pitser** (privaatne võti)
- Kõik teadsid, kuidas **jäljend** välja näeb (avalik võti)
- Pitserit võltsida oli väga raske

Digitaalallkiri on "matemaatiline vahapitser".

### RSA PKCS#1 v1.5

**RSA** — see on algoritm, mille leiutasid 1977. aastal kolm teadlast (Rivest, Shamir, Adleman).

**PKCS#1 v1.5** — see on standard, kuidas täpselt RSA-d allkirjade jaoks kasutada.

Miks me just seda kasutame:
- **Deterministlik** — sama sisend → sama allkiri
- **Läbiproovitud** — kasutatakse 40+ aastat
- **Ühilduv** — töötab kõikjal

### Aletheia koodis

```java
// Allkirjastame AI vastuse räsi
byte[] hash = sha256(aiResponse);
byte[] signature = signWithRSA(hash, privateKey);

// Nüüd igaüks saab kontrollida:
boolean valid = verifyWithRSA(hash, signature, publicKey);
// valid = true → allkiri on ehtne
// valid = false → midagi on valesti (võltsing või muudatus)
```

---

## 5. Ajatemplid (RFC 3161)

### Aja probleem

Sa allkirjastasid dokumendi. Aga **millal** sa seda tegid?

Sinu arvuti võib näidata suvalist aega. Kuidas tõestada, et allkiri tehti just 30. jaanuaril 2026, mitte eile?

### Lahendus: Usaldusväärne tunnistaja

Kujuta ette notarit, kes:
1. Vaatab sinu dokumenti
2. Kirjutab üles praeguse aja
3. Paneb oma pitseri
4. Annab sulle tõendi

Nüüd sul on **sõltumatu aja kinnitus**.

### RFC 3161 — ajatemplite standard

**RFC** = Request for Comments — nii nimetatakse internetistandardeid.

**RFC 3161** — see on 2001. aasta standard, mis kirjeldab:
- Kuidas ajatemplit küsida
- Kuidas seda saada
- Kuidas kontrollida

### TSA — Time-Stamp Authority

**TSA** = Ajatemplite server (nagu digitaalne notar).

```
┌──────────┐                    ┌──────────┐
│  Klient  │ ── "Pane tempel    │   TSA    │
│          │    sellele räsile"→│          │
│          │                    │ (Aja     │
│          │ ←── Siin on token  │  notar)  │
│          │     ajaga ────     │          │
└──────────┘                    └──────────┘
```

### Mida token sisaldab

```
TimeStampToken:
├── genTime: 2026-01-30T15:30:00Z  ← Millal väljastati
├── serialNumber: 123456789        ← Unikaalne number
├── messageImprint: abc123...      ← Räsi, mida templiti
├── policy: 1.2.3.4.5.6            ← TSA reeglid
└── signature: xyz789...           ← TSA allkiri
```

### Oluline: mida me templime

Aletheia's me templime **allkirja**, mitte teksti:

```
AI tekst → räsi → allkiri → ajatempel(allkiri)
                              ↑
                        Seda templime!
```

Miks nii:
- **Meie** kinnitame **mida** AI ütles (allkiri)
- **TSA** kinnitab **millal** me selle allkirjastasime (tempel)

See on PKI-s standardne lähenemine.

---

## 6. PKI — Avaliku võtme infrastruktuur

### Usalduse probleem

Sa said avaliku võtme. Aga kust sa tead, et see on **õige** inimese **õige** võti?

Võib-olla ründaja asendas võtme omaga?

### Lahendus: Usaldusahel

Kujuta ette käendussüsteemi:

```
Valitsus
    ↓ usaldab
Ministeerium
    ↓ usaldab
Ülikool
    ↓ usaldab
Tudeng Jüri
```

Kui sa usaldad valitsust, saad usaldada Jürit läbi ahela.

### X.509 sertifikaadid

**Sertifikaat** — see on "pass" avaliku võtme jaoks:

```
┌────────────────────────────────────┐
│           SERTIFIKAAT              │
├────────────────────────────────────┤
│ Omanik: Aletheia AI                │
│ Avalik võti: MIIBIjANBg...         │
│ Kehtiv: 2026-01-01 — 2027-01       │
│ Väljaandja: DigiCert CA            │
│ Väljaandja allkiri: MEUCIQD...     │
└────────────────────────────────────┘
```

### CA — Certificate Authority

**CA** = Sertifitseerimiskeskus (nagu passiamet).

Tuntud CA-d:
- DigiCert
- Let's Encrypt
- GlobalSign

Brauserid ja operatsioonisüsteemid **usaldavad** neid CA-sid vaikimisi.

### Ahel Aletheia's

```
Root CA (juur)
    ↓
TSA CA (ajatemplite jaoks)
    ↓
TSA Server (väljastab templeid)
    ↓
Meie token (ajatempliga)
```

---

## 7. eIDAS — Euroopa usaldusreeglid

### Mis see on

**eIDAS** = electronic IDentification, Authentication and trust Services

See on **EL seadus** (Määrus 910/2014), mis ütleb:
> "Elektrooniline allkiri on juriidiliselt kehtiv, kui vastab nõuetele."

### Allkirjade tasemed

| Tase | Nimetus | Juriidiline jõud |
|------|---------|------------------|
| Põhi | Elektrooniline allkiri | Aktsepteeritakse, aga saab vaidlustada |
| Keskmine | Täiustatud allkiri | Raskem vaidlustada |
| Kõrgeim | Kvalifitseeritud allkiri | = omakäeline allkiri |

### Kvalifitseeritud TSA

**Kvalifitseeritud TSA** — see on TSA, mis:
- On läbinud auditi
- On kantud EL usaldusnimekirja
- Tema templid on kohtus juriidiliselt kehtivad

### Aletheia ja eIDAS

Meie PoC on **eIDAS-iga ühilduv**:
- Arhitektuur võimaldab ühendada kvalifitseeritud TSA
- Formaadid on standardsed (RFC 3161)
- Saab "uuendada" juriidilise jõuni

Aga PoC jaoks kasutame lokaalset TSA-d (lihtsam ja tasuta).

---

## 8. MOCK_TSA — Simulaator testide jaoks

### Testimise probleem

Päris TSA:
- Nõuab internetti
- Tagastab **erineva** aja iga kord
- Võib olla kättesaamatu
- Maksab raha

Kuidas testida koodi, kui tulemus on alati erinev?

### Lahendus: Deterministlik simulaator

**MOCK_TSA** — see on "mängu" TSA, mis:
- Töötab lokaalselt (ilma internetita)
- Tagastab alati **sama** tulemuse
- Kohene (pole vaja oodata)
- Tasuta

### Mida tähendab "deterministlik"

```
Deterministlik = ennustatav

Sisend A → alati väljund B
Sisend A → alati väljund B
Sisend A → alati väljund B
...igavesti...
```

Pole juhust. Pole sõltuvust ajast.

### Kuidas MOCK_TSA saavutab determinismi

| Element | Päris TSA | MOCK_TSA |
|---------|-----------|----------|
| Aeg | Päris kell | Fikseeritud: `2026-01-01T00:00:00Z` |
| Serial | Loendur baasis | Arvutatakse räsist |
| Allkiri | Võib olla juhuslik | RSA PKCS#1 v1.5 (deterministlik) |

### Näide

```java
// Esimene kutse
byte[] token1 = mockTsa.respond(request);

// Teine kutse (päev hiljem, teises arvutis)
byte[] token2 = mockTsa.respond(request);

// Tulemus
assertArrayEquals(token1, token2); // ✅ Identsed!
```

### Miks seda vaja on

1. **Testid ei läbi juhuslikult** (pole "flaky tests")
2. **Saab võrrelda näidistega** (golden fixtures)
3. **CI/CD töötab ilma internetita**
4. **Arendus on kiirem** (pole vaja päris TSA-d)

---

## 9. Cryptographic Oracle — Etalonvastused

### Mis on Oracle

Testimises **Oracle** = õige vastuse allikas.

Kujuta ette matemaatikaõpetajat:
- Sa lahendad ülesannet
- Õpetaja teab õiget vastust
- Sa võrdled — kas kattub või mitte

Oracle on "õpetaja" koodi jaoks.

### Cryptographic Oracle

Krüptograafia jaoks Oracle on **teadaolev õige tulemus**:

```
Sisend: "hello world"
Oracle: räsi = "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"
```

Kui sinu kood annab teise räsi — see on katki.

### Oracle'i tüübid Aletheia's

| Tüüp | Mida kontrollib | Näide |
|------|-----------------|-------|
| Hash Oracle | SHA-256 töötab õigesti | "hello" → "2cf24..." |
| Signature Oracle | RSA allkiri on korrektne | hash → signature bytes |
| Timestamp Oracle | MOCK_TSA on deterministlik | request → token bytes |
| LLM Oracle | LLM stub on ennustatav | prompt → fixed response |

### Miks see on oluline

Krüptograafia on valdkond, kus **"peaaegu õige" = täiesti katki**.

Kui räsi erineb kasvõi 1 biti võrra — verifitseerimine ei läbi.

Oracle garanteerib: kood töötab **täpselt** nagu peab.

---

## 10. Golden Fixtures — Kuldsed näidised

### Mis see on

**Golden Fixture** = fail "etalon" tulemusega, salvestatud Gitis.

```
fixtures/
├── hashes/
│   └── hello-world-sha256.bin    ← Õige räsi
├── signatures/
│   └── test-message-rsa.sig      ← Õige allkiri
└── timestamps/
    └── hello-world.tsr           ← Õige token
```

### Kuidas kasutatakse

```java
@Test
void testHashMatchesGoldenFixture() {
    // 1. Arvutame räsi
    byte[] actual = sha256("hello world");
    
    // 2. Laadime etaloni
    byte[] expected = loadFixture("hashes/hello-world-sha256.bin");
    
    // 3. Võrdleme
    assertArrayEquals(expected, actual);
    // Kui ei kattu — test läbib!
}
```

### Miks fixture'id on vajalikud

1. **Regressioonitestimine**
   - Uuendasime teeki → kontrollisime, et tulemus ei muutunud

2. **Dokumentatsioon**
   - Fixture on konkreetne näide "kuidas peab olema"

3. **Silumine**
   - Saab võrrelda "mis tuli" vs "mida oodati" bait-baidi haaval

4. **CI/CD**
   - Automaatne kontroll igal commit'il

### Manifest — fixture'ide metaandmed

Iga fixture'ide kaust sisaldab `manifest.json`:

```json
{
  "version": "1.0",
  "generated_at": "2026-01-30T17:30:00Z",
  "fixtures": [
    {
      "name": "hello-world",
      "input": "hello world",
      "hash_hex": "b94d27b9..."
    }
  ]
}
```

See aitab mõista:
- Millal fixture loodi
- Millest see saadi
- Mis algoritmi versioon

---

## 11. Testimine — Miks seda vaja on

### Testimise tasemed

```
┌─────────────────────────────────────────┐
│           E2E (End-to-End)              │  ← Kõik koos
├─────────────────────────────────────────┤
│         Integration Tests               │  ← Komponendid koos
├─────────────────────────────────────────┤
│            Unit Tests                   │  ← Üksikud funktsioonid
└─────────────────────────────────────────┘
```

### Unit Tests — Aatomilised kontrollid

Kontrollime **ühte** funktsiooni isoleeritult:

```java
@Test
void sha256_peab_olema_deterministlik() {
    byte[] hash1 = sha256("test");
    byte[] hash2 = sha256("test");
    
    assertArrayEquals(hash1, hash2);
}
```

### Integration Tests — Komponentide sidemed

Kontrollime, et komponendid töötavad **koos**:

```java
@Test
void täielik_allkirjastamise_ja_verifitseerimise_voog() {
    // Hash → Sign → Verify
    byte[] hash = hashService.sha256(text);
    byte[] sig = signatureService.sign(hash);
    boolean valid = signatureService.verify(hash, sig);
    
    assertTrue(valid);
}
```

### Mocking — Sõltuvuste asendamine

**Mock** = päris komponendi "maketi".

Miks:
- Mitte sõltuda välistest teenustest
- Kontrollida tulemust
- Kiirendada teste

```java
// Päris LLM asemel — mock
LLMStub llm = new LLMStub();
llm.addResponse("Mis on Prantsusmaa pealinn?", "Pariis");

String answer = llm.complete("Mis on Prantsusmaa pealinn?");
assertEquals("Pariis", answer); // Alati sama!
```

### Testimise Mermaid diagramm

Me lisasime visuaalse diagrammi dokumentatsiooni:

```
LLM Output → Hash → Test Type?
                     ├── Unit → MOCK_TSA → Golden Fixture
                     ├── Integration → Local TSA → Verify
                     └── Audit → LLM Stub + Logging → Compliance
```

See aitab arendajatel mõista, millist testi tüüpi kasutada.

---

## 12. Agent Audit Model — AI tegevuste audit

### Probleem: AI teeb otsuseid

Kujuta ette: AI-agent tellib firmal kaupa.

Kuu hiljem küsimus:
> "Miks AI tellis 1000 tooli 100 asemel?"

Kuidas vastata, kui logisid pole?

### Lahendus: Audit Trail

**Audit Trail** = "jälg" kõigist AI tegevustest.

Iga tegevuse kohta kirjutame:

```json
{
  "agent_id": "logistics-agent-v1",
  "timestamp": "2026-01-30T15:00:00Z",
  "prompt": "Mitu tooli tellida?",
  "response": "Soovitan tellida 1000 tooli...",
  "model": "gpt-4",
  "parameters": {
    "temperature": 0.2
  }
}
```

### Krüptograafiline kaitse

Lihtsaid logisid saab võltsida. Seepärast:

```
Kirje → Räsi → Allkiri → Ajatempel
```

Nüüd:
- **Ei saa muuta** kirjet (räsi ei kattu)
- **Ei saa eitada** autorlust (allkiri)
- **Ei saa võltsida** aega (TSA token)

### Non-repudiation — Eitamatus

**Non-repudiation** = võimatus eitada tegevust.

Kui AI allkirjastas otsuse — tema (täpsemalt süsteem) ei saa öelda "mina seda ei teinud".

See on oluline:
- Juriidiliste vaidluste jaoks
- Turvaauditi jaoks
- Regulatsioonidele vastavuse jaoks (compliance)

---

## 13. Mida me täna lisasime

### Hommikul: Dokumentatsioon ja diagrammid

1. **Mermaid diagramm** TESTING_STRATEGY.md-s
   - Visuaalne testimise töövoog
   - Unit / Integration / Audit vood
   - Värviline kodeerimine

2. **Golden Fixtures Branch Strategy**
   - Kuidas hoida fixture'id Gitis
   - CI integratsioon
   - Uuendamise protsess

3. **LLM Stub Audit** laiendus
   - Audit logging compliance testide jaoks

### Õhtul: Infrastruktuur

4. **docs/en/MOCK_TSA.md** (553 rida)
   - Ingliskeelne dokumentatsiooni versioon
   - Java/Bouncy Castle näited
   - RFC 3161 testvektorid

5. **.env.example** (88 rida)
   - Kõigi keskkonnamuutujate mall
   - Kommentaarid ja näited
   - Turvanõuanded

6. **docker-compose.yml** (85 rida)
   - PostgreSQL teenus
   - Valikuline TSA
   - Kasutusjuhised

7. **README badges** (5 tükki)
   - Java, Spring Boot, PostgreSQL
   - License, RFC 3161
   - Professionaalne välimus

8. **Quick Start** sektsioon
   - 4 sammu käivitamiseks
   - Kopeeri ja tööta

9. **Golden Fixtures struktuur**
   - `backend/src/test/resources/fixtures/`
   - README (335 rida)
   - Manifest'id iga kategooria jaoks

### Commit'id

```
aa1caaa — docs: add comprehensive testing strategy and audit model documentation
37b5570 — chore: add development infrastructure and golden fixtures structure
```

**Kokku:** +6,570 koodi- ja dokumentatsioonirida.

---

## 14. Terminite sõnastik

### A-C

| Termin | Tähendus |
|--------|----------|
| **ASN.1** | Andmestruktuuride kirjelduskeel (nagu JSON, aga binaarne) |
| **Audit Trail** | Kõigi tegevuste logi kontrollimiseks |
| **Bouncy Castle** | Java krüptograafiateek |
| **CA (Certificate Authority)** | Sertifikaate väljaandev organisatsioon |
| **CI/CD** | Continuous Integration/Delivery — automaatne ehitamine ja testimine |
| **Compliance** | Nõuetele vastavus (seadused, standardid) |
| **Cryptographic Oracle** | Krüptograafia kontrollimise etalontulemus |

### D-G

| Termin | Tähendus |
|--------|----------|
| **DER** | ASN.1 binaarne kodeerimisformaat |
| **Deterministic** | Ennustatav: sama sisend → sama väljund |
| **Digest** | Räsi teine nimetus |
| **eIDAS** | EL määrus elektrooniliste allkirjade kohta |
| **Fixture** | Testandmed (näidisfailid) |
| **Flaky Test** | Test, mis mõnikord juhuslikult läbib |
| **Golden Fixture** | Võrdlemise etalonfail |

### H-M

| Termin | Tähendus |
|--------|----------|
| **Hash** | Andmete fikseeritud pikkusega "sõrmejälg" |
| **Integration Test** | Mitme komponendi sideme test |
| **JUnit** | Java testimisraamistik |
| **LLM** | Large Language Model (ChatGPT jt) |
| **Manifest** | Metaandmete fail (sisu kirjeldus) |
| **Mermaid** | Diagrammide joonistamise keel Markdownis |
| **Mock** | Maketi/tühikasi testide jaoks |
| **MOCK_TSA** | Deterministlik TSA simulaator |

### N-R

| Termin | Tähendus |
|--------|----------|
| **Non-repudiation** | Võimatus eitada tegevust |
| **Nonce** | Ühekordne juhuslik arv |
| **PEM** | Võtmete ja sertifikaatide tekstiformaat |
| **PKI** | Public Key Infrastructure — võtmeinfrastruktuur |
| **PKCS#1** | RSA krüptograafia standard |
| **Policy OID** | Poliitika (reeglite) identifikaator |
| **Regression Test** | Halvenemiste puudumise test |
| **RFC** | Request for Comments — internetistandard |
| **RSA** | Asümmeetrilise krüptograafia algoritm |

### S-Z

| Termin | Tähendus |
|--------|----------|
| **Serial Number** | Unikaalne number (token, sertifikaat) |
| **SHA-256** | Räsimisalgoritm (256 bitti) |
| **Signature** | Digitaalallkiri |
| **Spring Boot** | Java backendi raamistik |
| **Stub** | Lihtne tühikasi fikseeritud käitumisega |
| **TSA** | Time-Stamp Authority — ajatemplite server |
| **TSQ** | TimeStampRequest — templi päring |
| **TSR** | TimeStampResponse — templiga vastus |
| **Unit Test** | Ühe funktsiooni isoleeritud test |
| **X.509** | Sertifikaatide formaadi standard |

---

## Kokkuvõte

Täna me:

1. **Laiendasime dokumentatsiooni** — Mermaid diagrammid, LLM Stub audit, Golden Fixtures strategy
2. **Lõime infrastruktuuri** — .env.example, docker-compose.yml, fixtures struktuur
3. **Parandasime README** — badges, Quick Start, viited fixture'idele
4. **Tõlkisime MOCK_TSA** inglise keelde

Kõik see teeb projekti:
- **Arusaadavamaks** — visuaalsed diagrammid ja sammhaaval juhised
- **Professionaalsemaks** — badges, struktureeritud fixtures
- **Lihtsamaks arenduses** — Quick Start 5 minutiga

Aletheia AI — see pole lihtsalt "allkirjastame AI vastuseid". See on **krüptograafiliselt verifitseeritav süsteem** täieliku audit trail'iga, deterministliku testimisega ja Euroopa standarditega (eIDAS) ühilduvusega.

**Tulevik:** PKI tehisintellektile on tee usaldusväärsete autonoomsete süsteemide poole, kus iga otsus on tõestatav, iga agent on identifitseeritud ja iga tegevust saab jälgida.

---

**Autor:** Anton Sokolov  
**Kuupäev:** 30. jaanuar 2026  
**Projekt:** Aletheia AI — Verifiable AI Responses

---

*"Aletheia" (Ἀλήθεια) — tõde, mida ei saa varjata.*
