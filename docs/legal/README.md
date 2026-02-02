# Legal & Regulatory References — Aletheia AI

This folder indexes EU and international law documents relevant to verifiable AI responses: electronic signatures, timestamps, trust services, AI regulation, and data protection. Use these for compliance, audit, and legal alignment.

**Project relevance:** Aletheia uses digital signatures (RSA) and RFC 3161 timestamps; see [TRUST_MODEL](../en/TRUST_MODEL.md) for eIDAS mapping.

---

## EU Regulations (English, official PDFs)

| Document | EUR-Lex / Source | Relevance |
|----------|------------------|-----------|
| **eIDAS 1** — Regulation (EU) 910/2014 | [PDF](https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=CELEX:32014R0910) · [Consolidated](https://eur-lex.europa.eu/eli/reg/2014/910) | Electronic identification, **trust services**, **electronic signatures** (Art. 25–31), **electronic timestamps** (Art. 41–42), legal effect and admissibility |
| **eIDAS 2** — Regulation (EU) 2024/1183 | [PDF](https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=OJ:L_202401183) · [OJ](https://eur-lex.europa.eu/eli/reg/2024/1183/oj) | Amends eIDAS 1; European Digital Identity (EUDI) Wallet; applies from May 2024 |
| **EU AI Act** — Regulation (EU) 2024/1689 | [PDF](https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=OJ:L_202401689) · [OJ](https://eur-lex.europa.eu/eli/reg/2024/1689/oj) | AI development, marketing, use; risk-based rules; transparency and traceability for AI outputs |
| **GDPR** — Regulation (EU) 2016/679 | [PDF](https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=CELEX:32016R0679) · [OJ](https://eur-lex.europa.eu/eli/reg/2016/679/oj) | Data protection when processing prompts/responses; legal basis, retention, rights |

---

## Technical Standards

| Standard | Source | Relevance |
|----------|--------|-----------|
| **RFC 3161** — Internet X.509 PKI Time-Stamp Protocol (TSP) | [PDF](https://www.rfc-editor.org/rfc/pdfrfc/rfc3161.txt.pdf) · [HTML](https://www.rfc-editor.org/rfc/rfc3161) | Core timestamp protocol; TSA request/response format; used by Aletheia (DigiCert, mock TSA) |
| **ETSI EN 319421** — Policy and Security Requirements for TSPs issuing Time-Stamps | [PDF](https://www.etsi.org/deliver/etsi_en/319400_319499/319421/01.03.01_30/en_319421v010301v.pdf) | Qualified timestamp requirements (Commission Implementing Reg.) |
| **ETSI EN 319422** — Time-stamping protocol and time-stamp token profiles | [PDF](https://www.etsi.org/deliver/etsi_en/319400_319499/319422/01.01.01_60/en_319422v010101p.pdf) | Technical profiles for RFC 3161 / qualified timestamps |

---

## Other Relevant Documents

| Document | Source | Relevance |
|----------|--------|-----------|
| **eSignature Directive** (repealed) — 1999/93/EC | [EUR-Lex](https://eur-lex.europa.eu/eli/dir/1999/93/oj) | Precursor to eIDAS; legal admissibility of electronic documents |
| **EU Digital Strategy — eSignatures** | [EC](https://digital-strategy.ec.europa.eu/en/policies/esignatures) | Policy overview and links |
| **EU AI Act — Full text & PDF** | [aiact-info.eu](https://www.aiact-info.eu/full-text-and-pdf-download/) | Unofficial consolidated text |

---

## Download Script

Run from project root: `./docs/legal/download.sh` — saves PDFs to `docs/legal/downloads/`.

**Note:** EUR-Lex may require a browser session; if the script yields empty files, download manually via the links in the tables above. ETSI standards: download from www.etsi.org (see README links).

---

## Aletheia Mapping

| Aletheia component | eIDAS / legal reference |
|--------------------|-------------------------|
| RSA signature over hash | Advanced electronic signature (Art. 26); non-qualified |
| RFC 3161 timestamp (DigiCert, etc.) | Electronic timestamp (Art. 41); qualified if TSA is qualified |
| Hash (SHA-256) | Integrity; supports Art. 26(d) — change detection |
| Canonicalization | Determinism for verification and legal consistency |

For a path to qualified trust services, see [TRUST_MODEL](../en/TRUST_MODEL.md#eidas-mapping-non-qualified--qualified).
