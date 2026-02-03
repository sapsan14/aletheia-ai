# Aletheia Trust Lab – Official Case Study

## Case: Ambiguous Terminology & Confident AI Error (PQC)

### Summary
This case documents a real interaction with an AI system where an **ambiguous acronym (PQC)** resulted in a **confident but contextually incorrect answer**, despite the response being **timestamped and cryptographically signed**.

The case demonstrates a core principle of AI accountability:
> **Cryptographic proof guarantees existence and integrity — not correctness.**

---

## Context

The project operates as an **AI Trust + Post-Quantum Cryptography (PQC) Lab**, where:
- PQC = *Post-Quantum Cryptography*
- Focus areas include:
  - AI accountability
  - Long-term verifiability
  - EU AI Act–aligned auditability

A question was submitted to an AI model via API:

> *“What does PQC mean in terms of the EU AI Act?”*

The expected interpretation was **Post-Quantum Cryptography**.

---

## AI Response (Original)

The AI responded:

> “PQC stands for Prior Quantitative Conformity assessment. In terms of the EU AI Act, it means that AI systems may have to undergo an inspection done by accredited conformity assessment bodies before market release...”

### Key Observation
- The response was **plausible**
- It aligned with **regulatory language**
- It was **confident**
- It was **wrong in context**

---

## Why This Happened

- **Acronym ambiguity**: PQC has multiple meanings across domains
- **Missing explicit context** (“post-quantum cryptography”)
- **No clarification request by the AI**
- **High linguistic confidence** masking uncertainty

This is a known and documented failure mode of large language models.

---

## Cryptographic Evidence

The response was:
- Timestamped
- Digitally signed
- Stored immutably

### What This Proves
- The response existed
- At a specific time
- In a specific form
- From a specific system

### What This Does *Not* Prove
- Factual correctness
- Regulatory accuracy
- Legal validity

---

## Relevance to the EU AI Act

The EU AI Act requires for certain AI systems:
- Traceability
- Record-keeping
- Auditability
- Accountability

This case shows that:
> **Auditability without semantic verification is insufficient.**

PQC (Post-Quantum Cryptography) supports *long-term* verification, but **human or procedural review remains mandatory**.

---

## Lessons Learned

1. AI can be confidently wrong
2. Cryptographic trust ≠ semantic truth
3. Ambiguity must be explicitly handled
4. AI-generated content must be reviewed before use
5. Verification pipelines must include *meaning*, not just integrity

---

## Why This Case Exists

This case is intentionally preserved as part of the **Aletheia Trust Lab** to:
- Educate students
- Demonstrate real AI risk patterns
- Support AI Act–aligned system design
- Show why “verified AI” still needs humans

---

## Key Takeaway

> **Trust is not about believing answers.  
> Trust is about being able to question them — later.**

---
Aletheia Trust Lab  
Experimental · Educational · Verifiable
