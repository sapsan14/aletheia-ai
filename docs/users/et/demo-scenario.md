# Faasi 4 stsenaarium — Juridiline / vastavus

## Kontekst

Juridiline meeskond kasutab AI-assistenti lepingu klausli vastavuse hindamiseks.
Vaaluse korral peab ettevõte tõestama **mida öeldi**, **millise poliitika alusel** ja **millal** see väljastati.

## Eeltingimused

- Backend töötab allkirjastamise võtme ja TSA-ga.
- Frontend töötab ja on ühendatud backendiga.
- Demopoliitika olemas: [policy/aletheia-demo-2026-01.json](../../policy/aletheia-demo-2026-01.json).
- Verifitseerija JAR olemas (laadi alla Verify lehelt või ehita kohalikult).

## Sammud (1–5)

1. **Esita vastavusküsimus.**  
   Ava rakendus ja sisesta küsimus, nt:  
   `Kas see klausel vastab GDPR-ile? „Hoian kasutajaandmeid kuni 24 kuud auditi jaoks.“`
2. **Vaata vastust.**  
   Backend tagastab allkirjastatud vastuse. Operaator näeb Trust Summary ja AI Claim (kui on).
3. **Laadi alla Evidence Package.**  
   Ava Verify leht ja klõpsa **Download evidence**, et salvestada `.aep` fail.
4. **Saada auditeurile.**  
   Anna `.aep` fail edasi e-kirjaga või turvalise ühenduse kaudu.
5. **Offline kontroll.**  
   Auditeur käivitab:

   ```bash
   java -jar aletheia-verifier.jar /path/to/aletheia-evidence-<id>.aep
   ```

   Verifitseerija kuvab **VALID** ja ajatempli.

## Tulemus

Saab tõestada **täpse sõnastuse**, **poliitika konteksti** ja **ajatempli** ilma Aletheia backendit kasutamata.

## Video

Video: **TBD** — salvesta 3–5 minutiline stsenaarium ja asenda link.
