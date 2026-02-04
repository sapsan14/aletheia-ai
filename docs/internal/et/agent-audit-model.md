# Agentide auditi mudel

**Tõestatav, kontrollitav ja selgitatav käitumise jälgimine autonoomsete ja poolautonoomsete LLM-agentide jaoks.**

---

## Sisukord

- [Eesmärk](#eesmärk)
- [Mis on agendi tegevus](#mis-on-agendi-tegevus)
- [Auditi filosoofia](#auditi-filosoofia)
- [Auditi kirje struktuur](#auditi-kirje-struktuur)
- [Mida allkirjastatakse](#mida-allkirjastatakse)
- [Krüptograafilised garantiid](#krüptograafilised-garantiid)
- [Vastutuse piir](#vastutuse-piir)
- [Inimene tsüklis](#inimene-tsüklis)
- [Mitmeagentsed süsteemid](#mitmeagentsed-süsteemid)
- [Miks see on oluline](#miks-see-on-oluline)
- [Näide intsidendi selgitusest](#näide-intsidendi-selgitusest)
- [Tulevased laiendused](#tulevased-laiendused)
- [Kokkuvõte](#kokkuvõte)
- [Seotud dokumendid](#seotud-dokumendid)

---

## Eesmärk

See dokument kirjeldab **auditi mudelit autonoomsete või poolautonoomsete LLM-agentide jaoks**.

Selle eesmärk on muuta agentide käitumine **tõestatavaks, kontrollitavaks ja selgitatavaks tagantjärele**.

**Lühidalt:**

> Mitte "miks AI nii mõtles",  
> vaid **mis täpselt juhtus, millal ja millistel tingimustel**.

See mudel on kavandatud töötama krüptograafilise allkirjastamise ja ajatempeldusega, nagu see on implementeeritud Aletheia AI PoC-s.

---

## Mis on agendi tegevus

**Agendi tegevus** on iga väljund, mis võib mõjutada reaalset maailma.

**Näited:**

- Tekstiline soovitus
- Otsuse ettepanek
- Juhis, mis on genereeritud teisele süsteemile
- API kõne ettepanek
- Tööriista täitmise plaan
- Sõnum, mis on saadetud kasutajale või teisele agendile

**Oluline:**

Isegi kui agent *ei* täida tegevust ise, **võib ettepanek üksi kanda vastutust**.

Näide: Agent soovitab meditsiinilist annust. Isegi kui inimene peab selle heaks kiitma, kannab soovitus ise potentsiaalseid tagajärgi.

---

## Auditi filosoofia

Süsteem **ei** püüa tõestada, et vastus on õige.

Selle asemel tõestab see:

| Aspekt | Mida tõestatakse |
|--------|------------------|
| **Sisu** | Mida agent tootis |
| **Aeg** | Millal see toodi |
| **Konfiguratsioon** | Millise konfiguratsiooni all (temperature, top_p jne) |
| **Mudel** | Millise mudeli ja versiooniga |
| **Terviklikkus** | Et seda ei muudetud hiljem |

**See peegeldab põhimõtteid, mida kasutatakse:**

- Finantssüsteemides (tehingute logid, audit trail)
- PKI-s (sertifikaatide läbipaistvus, allkirjade ajatemplid)
- Intsidentidele reageerimises (kohtumeditsiinilised tõendid)
- Lennuki mustades kastides (muutmatud sündmuste logid)

**Eesmärk:** Luua **muutmatu kirje sellest, mis juhtus**, mitte hinnang selle kohta, kas see oli "õige".

---

## Auditi kirje struktuur

Iga agendi tegevus genereerib **muutmatu auditi kirje**.

### Minimaalsed väljad

```json
{
  "agent_id": "logistics-agent-v1",
  "request_id": "uuid",
  "timestamp": "2026-01-31T16:15:00Z",
  "model": "gpt-4.1",
  "model_version": "gpt-4.1-turbo-2026-01-25",
  "prompt": "Arvuta optimaalne marsruut kohaletoimetamiseks...",
  "context": "Kasutaja asukoht: Berliin, Sihtkoht: München, piirangud: ...",
  "response": "Soovitatud marsruut: A9 läbi Nürnbergi. ETA: 4.5 tundi.",
  "parameters": {
    "temperature": 0.2,
    "top_p": 0.9,
    "max_tokens": 500
  }
}
```

Seda struktuuri nimetatakse **Agent Evidence Payload** (agendi tõendite andmestik).

### Väljade kirjeldused

| Väli | Kirjeldus | Kohustuslik |
|------|-----------|-------------|
| `agent_id` | Agendi eksemplari unikaalne identifikaator | ✅ Jah |
| `request_id` | Selle päringu unikaalne ID (korrelatsiooni jaoks) | ✅ Jah |
| `timestamp` | UTC ajatempel, millal vastus genereeriti | ✅ Jah |
| `model` | Mudeli nimi (nt "gpt-4.1", "gemini-pro") | ✅ Jah |
| `model_version` | Täpne versiooni/snapshot'i identifikaator | ✅ Jah |
| `prompt` | Sisend prompt, mis saadeti mudelile | ✅ Jah |
| `context` | Lisatingimus (vestluse ajalugu, süsteemi prompt jne) | Soovitatav |
| `response` | Agendi genereeritud väljund | ✅ Jah |
| `parameters` | Mudeli parameetrid (temperature, top_p, seed jne) | ✅ Jah |
| `tools_used` | Kasutatud tööriistade/API-de nimekiri (kui kohaldatav) | Valikuline |
| `metadata` | Lisametaandmed (user_id, session_id jne) | Valikuline |

---

## Mida allkirjastatakse

Süsteem loob auditi andmestiku **kanonilise representatsiooni**.

### Protsess

```
1. Kanoonilista payload (deterministlik JSON/teksti representatsioon)
2. hash = SHA-256(canonical_payload)
3. signature = Sign(hash, agent_private_key)
4. timestamp = RFC3161(signature)
```

**Soovitatav lähenemine ([Aletheia pipeline'ist](../../diagrams/architecture.md)):**

```
canonical_payload → hash → sign → timestamp(signature)
```

See tagab, et nii **sisu** kui **vastutus** on ajaliselt külmutatud.

### Miks ajatemplida allkirja?

- **Sisu terviklikkus:** Räsi tõestab, et payload ei ole muutunud.
- **Aja tõend:** Ajatempel tõestab, et allkiri eksisteeris ajal T.
- **Usaldusahel:** TSA kinnitab *millal*, agendi võti kinnitab *mida*.

Vt [Usaldusmudel](TRUST_MODEL.md) detailide kohta, kes mida kinnitab.

---

## Krüptograafilised garantiid

Auditi kirje võimaldab kellelgi kontrollida:

| Garantii | Mida tõestatakse |
|----------|------------------|
| ✅ **Sisu terviklikkus** | Sisu ei ole muutunud alates allkirjastamisest |
| ✅ **Autentimine** | Allkiri kuulub agendile/süsteemile (avaliku võtme kaudu) |
| ✅ **Mitteeitavus** | Agent ei saa eitada selle väljundi genereerimist |
| ✅ **Aja tõend** | Ajatempel tõestab olemasolu ajal T (TSA kaudu) |
| ✅ **Kronoloogiline järjekord** | Kirje eelneb igale intsidendile või vaidlusele |

**See loob mitteeitavuse AI käitumisele.**

Igaüks, kellel on:
- Auditi kirje (payload + allkiri + ajatempel)
- Agendi avalik võti
- TSA avalik võti/sertifikaat

saab sõltumatult kontrollida kõiki garantiisid.

---

## Vastutuse piir

**Oluline selgitus:**

| Mida allkiri tähendab | Mida see EI tähenda |
|-----------------------|---------------------|
| ✅ See täpne väljund genereeriti selle agendi poolt | ❌ Vastus on õige |
| ✅ Sellel konkreetsel ajal | ❌ Agent on juriidiliselt vastutav |
| ✅ Selle konfiguratsiooni all | ❌ Väljund on ohutu või sobiv |
| ✅ Sisu on muutmata | ❌ Otsust tuleks järgida |

**Allkiri tõestab:**

> "Kes genereeris ja esitas väljundi."

**See EI tõesta:**

> "Et väljund on õige või sellele peaks usaldama."

### Analoogia

See on sarnane:

- **Videoregistraatori salvestustele** — tõestavad, mis juhtus, mitte kes on süüdi
- **Süsteemi logidele** — salvestavad sündmusi, mitte korrektsust
- **E-posti DKIM allkirjadele** — tõestavad saatjat, mitte sisu kehtivust
- **Notariaalsele kinnitusele** — kinnitab allkirja, mitte dokumendi tõde

### Näidis avaldus

> "Ajal T sai meie süsteem selle täpse väljundi mudelist M ja andis selle muutmata kasutajale. Me ei kinnita väljundi korrektsust, kuid saame tõestada, et see oli genereeritud."

---

## Inimene tsüklis

Kui inimene kiidab heaks või muudab väljundit, saab sellest **uus auditi sündmus**.

### Näidis voog

```
1. Agent genereerib väljundi → Auditi kirje A (allkirjastatud + ajatemplitatud)
   ↓
2. Inimene kontrollib ja muudab → Auditi kirje B (allkirjastatud + ajatemplitatud)
   ↓
3. Muudetud vastus saadetakse kasutajale → seotud mõlemaga A ja B
```

### Auditi kirje inimtegevuse jaoks

```json
{
  "action_type": "human_review",
  "reviewer_id": "user@example.com",
  "timestamp": "2026-01-31T16:20:00Z",
  "original_response_id": "uuid-from-record-A",
  "modified_response": "Uuendatud soovitus lisakitsenduste põhjal...",
  "changes": "Lisatud kitsendus X, eemaldatud ettepanek Y",
  "approval_status": "approved_with_modifications"
}
```

**Iga samm peab tootma eraldi auditi kirje.**

See säilitab täieliku jälgitavuse:
- Mida agent algselt ütles?
- Mida inimene muutis?
- Millal toimus iga tegevus?

---

## Mitmeagentsed süsteemid

Agentide ahelate jaoks (MCP, tööriista agendid, planeerijad, mitmeagentsed töövood):

**Iga agent peab tootma oma auditi kirje.**

### Näidis ahel

```
Planeerija agent → Auditi kirje 1
   ↓
Tööriista agent → Auditi kirje 2
   ↓
Täitmise agent → Auditi kirje 3
```

### Ahela struktuur

```json
{
  "agent_id": "planner-agent",
  "request_id": "chain-uuid",
  "response": "Plaan: Samm 1...",
  "next_agent": "tool-agent",
  "chain_position": 1
}
```

```json
{
  "agent_id": "tool-agent",
  "request_id": "chain-uuid",
  "previous_agent": "planner-agent",
  "response": "Tööriista täitmise tulemus...",
  "chain_position": 2
}
```

### Eelised

- **Rekonstrueerimine:** Täielik arutluskäigu ajajoon saab taastada
- **Süü isoleerimine:** Tuvastada, milline agent ahelas põhjustas probleemi
- **Privaatsus:** Sisemine mõttekäik ei avaldata (ainult lõplikud väljundid)
- **Granuleeritud audit:** Iga samm on sõltumatult kontrollitav

### Ristviited agentide vahel

Kasuta `chain_id` või `parent_request_id` seotud auditi kirjete linkimiseks agentide vahel.

---

## Miks see on oluline

See mudel muutub **kriitiliseks**, kui:

| Stsenaarium | Risk ilma auditi mudelita |
|-------------|---------------------------|
| **Agendid töötavad pidevalt** | Pole kirjet öösiti tehtud otsustest |
| **Agendid juhivad tööriistu** | Ei saa tõestada, milliseid tegevusi tegelikult võeti |
| **Agendid mõjutavad raha, logistikat, ohutust** | Vastutus on ebaselge vea korral |
| **Agendid annavad nõu haavatvatele kasutajatele** | Ei saa kontrollida, mida tegelikult öeldi |
| **Regulaatorid küsivad "kes seda ütles ja millal?"** | Kohtumeditsiinilised tõendid puuduvad |

### Eriti asjakohased juhtumid

- **Logistika vead** — vale marsruut, hilinenud kohaletoimetamine, kahjustatud kaubad
- **Automatiseeritud otsustamine** — krediidi heakskiit, kindlustuskahjud, töölevõtmine
- **Finantsnõuanded** — investeerimisnõuanded, kaubandusotsused
- **Sisu modereerimine** — valepositiivsed/negatiivsed, tsensuurivaidlused
- **Laste ohutus** — ebasobiv sisu näidatud alaealistele
- **Autonoomsed agentide turud** — agent A kutsub agenti B, kes on vastutav?

### Reaalne stsenaarium

**Küsimus:** "Miks agent soovitas seda riskantsest investeeringut?"

**Ilma auditi mudelita:** "Me ei tea, logid kirjutati üle."

**Auditi mudeliga:** "Siin on täpne prompt, mudeli versioon, temperature ja väljund, krüptograafiliselt pitseeritud ajatemblis T. Soovitus põhines kontekstil X."

---

## Näide intsidendi selgitusest

**Stsenaarium:** Agent soovitab logistika marsruuti, mis põhjustab viivituse.

**Vastus agentide auditi mudeliga:**

> "See soovitus genereeriti **agendi logistics-agent-v1** poolt  
> kasutades mudelit **gpt-4.1-turbo-2026-01-25**  
> **2026-01-31 16:15:00 UTC**  
> ja pitseeriti krüptograafiliselt sel hetkel allkirja ja RFC 3161 ajatempli kaudu.  
>  
> Süsteem ei muutnud sisu hiljem.  
>  
> Agent töötas temperature=0.2-ga, antud järgmise kontekstiga: [konteksti detailid].  
>  
> Siin on allkirjastatud auditi kirje: [link kontrollimise portaalile]."

**See on AI ekvivalent:**

> "Siin on musta kasti salvestus."

### Kontrollimise portaal

Kasutajad, regulaatorid või audiitorid saavad:
1. Alla laadida auditi kirje (JSON)
2. Kontrollida allkirja agendi avaliku võtme suhtes
3. Kontrollida ajatempli TSA sertifikaadi suhtes
4. Inspekteerida täpset sisend/väljund/konfiguratsiooni

Kõik see ilma vajaduseta usaldada Aletheia sõna.

---

## Tulevased laiendused

Planeeritud või ühilduvad laiendused täiustatud auditeeritavuse jaoks:

| Laiendus | Eelis |
|----------|-------|
| **eIDAS-kvalifitseeritud TSA** | Juriidilist taset ajatemplid (EL regulatsioon) |
| **Kvalifitseeritud sertifikaadid** | Agentide võtmed sertifitseeritud usaldusväärse CA poolt |
| **HSM-kaitstud agentide võtmed** | Riistvarakaitse agentide privaatvõtmetele |
| **Agendipõhised identiteedi sertifikaadid** | Igal agendil on kontrollitav identiteet |
| **Avalik kontrollimise portaal** | Igaüks saab kontrollida auditi kirjeid veebis |
| **Agentide reputatsioonisüsteemid** | Jälgida agentide sooritust aja jooksul |
| **Auditi kirjete kokkusurumine** | Tõhus miljonite kirjete salvestamine |
| **Nullteadmise tõendid** | Tõestada omadusi ilma täielikku kirjet paljastamata |

Vt [Usaldusmudel (eIDAS vastendus)](TRUST_MODEL.md#eidas-vastendus-mittekvalifitseeritud--kvalifitseeritud) kvalifitseeritud usaldusteenuste täiendamise tee kohta.

---

## Kokkuvõte

**AGENT_AUDIT_MODEL ei käsitle usaldust.**

See käsitleb:

| Põhimõte | Kirjeldus |
|----------|-----------|
| **Mälu** | Püsiv kirje sellest, mis juhtus |
| **Vastutus** | Selge jälg, kes mida tegi |
| **Kronoloogia** | Vaidlustamatud ajatemplid |
| **Tõendid** | Krüptograafiline tõend, mitte ainult logid |
| **Auditeeritavus** | Sõltumatu kontrollimine kolmandate osapoolte poolt |

### Lõplik avaldus

> AI süsteemid teevad vigu.  
>  
> See mudel tagab, et kui nad seda teevad,  
> **ei kao tõde selle kohta, mis juhtus.**

---

## Seotud dokumendid

- [Allkirjastamine (SIGNING)](SIGNING.md) — Kuidas agentide väljundeid allkirjastatakse (RSA PKCS#1 v1.5)
- [Ajatemplid (TIMESTAMPING)](TIMESTAMPING.md) — RFC 3161 ajatemplid agentide tegevuste jaoks
- [Usaldusmudel (TRUST_MODEL)](TRUST_MODEL.md) — Kes mida kinnitab, eIDAS vastendus
- [Krüptograafiline oraakel (CRYPTO_ORACLE)](CRYPTO_ORACLE.md) — Agentide väljundite testimine reprodutseeritavuse jaoks
- [MOCK_TSA (MOCK_TSA)](MOCK_TSA.md) — Deterministlik TSA agentide auditi kirjete testimiseks
- [Arhitektuuri diagrammid](../../diagrams/architecture.md) — Pipeline: kanoniseerimine → räsi → allkiri → ajatempel
- [Implementeerimise plaan (PLAN)](PLAN.md) — Ülesannete tegevuskava audit trail'i implementeerimiseks
- [README](../../README.md) — Projekti ülevaade, disaini filosoofia

---

**Staatus:** Kontseptuaalne mudel. Implementeerimine on planeeritud PoC ja hiljem.

**Litsents:** MIT (Aletheia AI projekti järgi).
