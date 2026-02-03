# Phase 4 killer scenario — Legal/compliance

## Context

A legal team uses an AI assistant to assess whether a contract clause is compliant.
When a dispute arises, the company must prove **what was said**, **under which policy**,
and **when** it was issued.

## Prerequisites

- Backend running with signing key and TSA configured.
- Frontend running and connected to the backend.
- Demo policy available (see `docs/en/policy/aletheia-demo-2026-01.json`).
- Verifier JAR available (download from the Verify page or build locally).

## Steps (1–5)

1. **Ask a compliance question.**  
   Open the app and submit a prompt such as:  
   `Does this clause comply with GDPR? "We store user data for up to 24 months for audit purposes."`
2. **Review the response.**  
   The backend returns a signed response. The operator sees Trust Summary and AI Claim (if present).
3. **Download the Evidence Package.**  
   Open the Verify page and click **Download evidence** to save the `.aep` file.
4. **Send to an auditor.**  
   Provide the `.aep` file via email or secure transfer.
5. **Verify offline.**  
   The auditor runs:  
   `java -jar aletheia-verifier.jar /path/to/aletheia-evidence-<id>.aep`  
   The verifier prints **VALID** and the timestamp.

## Outcome

You can prove **exact wording**, **policy context**, and **timestamp** without relying on the Aletheia backend.

## Video

Video: **TBD** — record a 3–5 minute screencast and replace this with the final link.
