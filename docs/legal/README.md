# Legal & Regulatory References — Aletheia AI

This folder indexes EU and international law documents relevant to verifiable AI responses: electronic signatures, timestamps, trust services, AI regulation, and data protection. Use these for compliance, audit, and legal alignment.

**Project relevance:** Aletheia uses digital signatures (RSA) and RFC 3161 timestamps; see [TRUST_MODEL](../en/TRUST_MODEL.md) for eIDAS mapping. When PQC is enabled, a second signature (ML-DSA per FIPS 204) is added over the same hash for long-term, quantum-resistant verification; see [Plan PQC](../en/PLAN_PQC.md).

---

## Post-Quantum Cryptography (NIST and related)

These documents define and explain the post-quantum algorithms used for long-term evidence verification. **Significance for Aletheia:** Our optional PQC layer follows **FIPS 204 (ML-DSA)** so that evidence remains verifiable even if classical cryptography (e.g. RSA) is broken by future quantum computers. The NIST PQC project is the de facto global reference; FIPS 203/204/205 are the first federal standards for quantum-resistant cryptography (August 2024).

### NIST FIPS — Post-Quantum Standards (official)

| Document | Source | Significance and contents |
|----------|--------|---------------------------|
| **FIPS 204** — Module-Lattice-Based Digital Signature Standard (ML-DSA) | [CSRC final](https://csrc.nist.gov/pubs/fips/204/final) · [PDF](https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.204.pdf) | **Primary standard for PQC digital signatures.** Specifies ML-DSA (derived from CRYSTALS-Dilithium): key generation, signing, and verification. Believed secure against large-scale quantum computers. **Aletheia uses ML-DSA (Dilithium3)** for the optional second signature over the evidence hash. |
| **FIPS 203** — Module-Lattice-Based Key-Encapsulation Mechanism Standard (ML-KEM) | [CSRC final](https://csrc.nist.gov/pubs/fips/203/final) | Key encapsulation (key agreement/encryption), derived from CRYSTALS-Kyber. Used for establishing shared secrets; not used by Aletheia’s current signature-only pipeline but relevant for future PQC TLS or encrypted channels. |
| **FIPS 205** — Stateless Hash-Based Digital Signature Standard (SLH-DSA) | [CSRC final](https://csrc.nist.gov/pubs/fips/205/final) | Hash-based digital signatures (SPHINCS+). Alternative to ML-DSA; different security assumptions (hash security only). NIST recommends ML-DSA and SLH-DSA as the two initial PQC signature standards. |
| **NIST PQC Project** — Post-Quantum Cryptography Standardization | [CSRC project](https://csrc.nist.gov/projects/post-quantum-cryptography) · [Short URL](https://www.nist.gov/pqc) | **Central NIST programme** for PQC. Overview of FIPS 203/204/205, migration guidance, FAQs, news, and ongoing work (e.g. Falcon, HQC). Explains why PQC is needed and how the standards were selected. |
| **What Is Post-Quantum Cryptography?** (plain-language intro) | [NIST](https://www.nist.gov/cybersecurity/what-post-quantum-cryptography) | Non-technical explanation of the quantum threat and NIST’s role. Useful for compliance and stakeholder communication. |
| **NIST news (Aug 2024)** — First 3 finalized PQC standards | [NIST](https://www.nist.gov/news-events/news/2024/08/nist-releases-first-3-finalized-post-quantum-encryption-standards) | Announcement of FIPS 203, 204, 205 approval. Context and links to the three standards. |
| **PQC FIPS FAQs** | [CSRC](https://csrc.nist.gov/Projects/post-quantum-cryptography/faqs) | Answers to common questions on the PQC FIPS (definitions, use cases, migration). |
| **NIST IR 8547** — Migration to PQC (draft) | [CSRC](https://csrc.nist.gov/pubs/ir/8547/ipd) | Transition timeline and deprecation of quantum-vulnerable algorithms (e.g. by 2035). Supports planning and compliance narratives. |

### Other PQC-related references

| Document | Source | Significance and contents |
|----------|--------|---------------------------|
| **CRYSTALS-Dilithium** | [pq-crystals.org](https://pq-crystals.org/dilithium/) | Original algorithm and research behind ML-DSA (FIPS 204). Parameter sets, security, and implementation notes. |
| **ETSI TR 103 616** — Quantum-Safe Signatures | [ETSI search](https://www.etsi.org/standards-search?search=103%20616) · [deliver](https://www.etsi.org/deliver/etsi_tr/103600_103699/103616/01.01.01_60/tr_103616v010101p.pdf) | ETSI Technical Report on quantum-safe signature candidates (aligned with NIST process). Describes schemes in consistent notation for comparison. PDF may require acceptance of ETSI terms. |
| **ETSI TR 103 823** — Quantum-Safe PKE and KEM | [ETSI search](https://www.etsi.org/standards-search?search=103%20823) · [deliver](https://www.etsi.org/deliver/etsi_tr/103800_103899/103823/01.01.01_60/tr_103823v010101p.pdf) | ETSI Technical Report on quantum-safe public-key encryption and key encapsulation. Complements FIPS 203 (ML-KEM). PDF may require acceptance of ETSI terms. |
| **Federal Register (FIPS 203/204/205)** | [Federal Register](https://www.federalregister.gov/d/2024-17956) | Official notice of approval of the three FIPS. Legal and administrative record. |

For implementation details and algorithm choice in Aletheia, see [Plan PQC](../en/PLAN_PQC.md) and [Crypto reference](../en/CRYPTO_REFERENCE.md).

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

## For education and academic research

The documents in this folder and in `downloads/` are suitable for courses on cryptography, security, digital trust, and AI regulation.

| Document | Use in teaching / research |
|----------|----------------------------|
| **NIST FIPS 204** (ML-DSA) | Primary standard for post-quantum digital signatures; lab work on PQC, hybrid signing, long-term verification. |
| **NIST FIPS 203** (ML-KEM) | Key encapsulation; optional topic for PQC key agreement and future TLS. |
| **NIST FIPS 205** (SLH-DSA) | Hash-based PQC signatures; comparison with lattice-based ML-DSA. |
| **NIST PQC project, FAQs** | Motivation for PQC, migration timeline, algorithm selection (see links in [Post-Quantum Cryptography](#post-quantum-cryptography-nist-and-related)). |
| **eIDAS, EU AI Act, GDPR** | Legal and policy context for trust services, timestamps, and AI accountability. |
| **RFC 3161** | Timestamp protocol used in Aletheia and in many TSA implementations. |

**Lab and curriculum:** For lab scenarios and a structured education plan, see [Plan EDU (RU)](../internal/ru/plan-edu.md). Laboratory scripts (when added) live under `docs/internal/ru/`. Implementation and PQC design are described in [Plan PQC](../internal/en/plan-pqc.md).

---

## Download Script

Run from project root: `./docs/legal/download.sh` — saves PDFs to `docs/legal/downloads/`.

**Downloaded files:** EU regulations (eIDAS 1/2, AI Act, GDPR), RFC 3161, and **NIST FIPS 203, 204, 205** (post-quantum standards). These PQC PDFs are useful for local reference and teaching.

**Note:** EUR-Lex may require a browser session; if the script yields empty files, download manually via the links in the tables above. ETSI standards: download from www.etsi.org (see README links).

---

## Aletheia Mapping

| Aletheia component | eIDAS / legal reference |
|--------------------|-------------------------|
| RSA signature over hash | Advanced electronic signature (Art. 26); non-qualified |
| RFC 3161 timestamp (DigiCert, etc.) | Electronic timestamp (Art. 41); qualified if TSA is qualified |
| Hash (SHA-256) | Integrity; supports Art. 26(d) — change detection |
| Canonicalization | Determinism for verification and legal consistency |
| ML-DSA (PQC) signature (optional) | FIPS 204 (ML-DSA); post-quantum, long-term evidence verification |

For a path to qualified trust services, see [TRUST_MODEL](../en/TRUST_MODEL.md#eidas-mapping-non-qualified--qualified). For PQC standards and links, see [Post-Quantum Cryptography (NIST and related)](#post-quantum-cryptography-nist-and-related) above.
