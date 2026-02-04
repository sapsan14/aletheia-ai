# Policy Lifecycle & Selection — Design (Phase 5)

This document outlines how Aletheia could support **multiple policies** and policy switching in Phase 5. It is **design-only**: no implementation is required in Phase 4.5. It prepares the product for per-tenant or per–product-tier policies without committing to a specific implementation date.

**Related:** [PHASE4_5_TRANSITION.md](../PHASE4_5_TRANSITION.md) §3.1 · [aletheia-demo-2026-01.md](aletheia-demo-2026-01.md) (current Coverage-policy) · [POLICY_CREATION_AND_HUMAN_VERIFICATION.md](POLICY_CREATION_AND_HUMAN_VERIFICATION.md) (creating/updating policies and human verification)

---

## 1. Policy selection levels

We assume three levels of selection, from least to most specific. The first matching level wins (e.g. if a request carries an explicit policy, use it; else if the API key has an override, use that; else use the environment default).

| Level | Scope | Use case |
|-------|--------|----------|
| **1. Per-environment default** | All traffic in that environment (e.g. prod, staging). | Single default Coverage-policy for the whole deployment. Phase 4.5 uses this only (e.g. `aletheia-demo`, `2026-01`). |
| **2. Per-API-key override** | All requests authenticated with that API key. | Tenant A gets “HR policy”, Tenant B gets “Legal policy”. Key is bound to a `policy_id` (and optionally `policy_version`) in the key registry. |
| **3. Per-request explicit** | Single sign/verify request. | Client sends `policyId` and optionally `policyVersion` in the request body. Server accepts only if that policy is in the **allowlist** for that API key (or for the environment). Prevents arbitrary policy injection. |

**Phase 5 implementation order (suggestion):** (1) keep environment default; (2) add per-API-key override in the key store; (3) add optional per-request override with allowlist validation.

---

## 2. Storage and versioning of policy files

- **Authoritative definitions:** Policy files (JSON + optional MD) live in a versioned location. Today: `docs/en/policy/` (e.g. `aletheia-demo-2026-01.json`, `aletheia-demo-2026-01.md`). Phase 5 could keep this for “baked-in” policies and add an **internal registry** (DB or config service) that maps `(policy_id, policy_version)` to the rule set (or to a path/URL).
- **Versioning:** Each policy has a stable `policy_id` (e.g. `aletheia-demo`) and a `policy_version` (e.g. `2026-01`). New versions (e.g. `2026-02`) are new files or new registry entries; old versions are never edited so that historical evidence remains interpretable.
- **Loading:** Backend loads the selected policy (by environment default, key override, or allowlisted request) at sign time and evaluates rules. The chosen `policy_id` and `policy_version` are stored on the `AiResponse` (or equivalent) and returned in verify API and Evidence Package metadata. No need to “look up” policy at verify time beyond what is stored on the record.

---

## 3. Rollout and pinning to historical evidence

- **Rollout:** To introduce a new policy or version:
  - Add the new policy file (or registry entry) with a new `policy_version` (or new `policy_id`).
  - Configure environment default or key overrides to use the new policy for **new** requests.
  - Existing records keep their stored `policy_id` / `policy_version`; they are never rewritten.
- **Pinning:** Every signed response stores the Coverage-policy that was used at sign time. Verify API and Evidence Package always expose that stored value. Auditors can interpret old evidence using the corresponding policy version (e.g. from docs or registry).
- **Deprecation:** Old policy versions can be marked “deprecated” in docs or registry (no new assignments), but they remain valid for interpreting historical evidence.

---

## 4. Risk and misuse

- **Silent downgrade:** A client must not be able to silently “downgrade” to a weaker policy without leaving an audit trace. Mitigations:
  - Require that per-request policy selection (if implemented) is only from an allowlist tied to the API key (or environment). The server logs which policy was applied.
  - Store `policy_id` and `policy_version` on every record and expose them in verify API and Evidence Package so that any verification shows which Coverage-policy was used.
- **Confusion between Coverage-policy and Claim-policy:** Always label clearly in UI and API: **Coverage-policy** = rules Aletheia evaluated; **Claim-policy** = policy/standard the AI used when forming the claim. This design applies to Coverage-policy selection; Claim-policy remains part of the AI claim payload and is not selected by Aletheia.

---

## 5. Summary

| Topic | Design choice |
|-------|----------------|
| Selection | Three levels: environment default → per-API-key override → per-request (allowlist). |
| Storage | Versioned files (e.g. `docs/en/policy/`) plus optional internal registry for Phase 5. |
| Versioning | `policy_id` + `policy_version`; new versions are new files/entries; old ones never edited. |
| Rollout | New policy = new version; default/overrides point to it for new requests only. |
| History | Stored on each response; verify and Evidence Package expose it; no rewrite of past records. |
| Misuse | No silent downgrade; allowlist + audit; clear naming (Coverage-policy vs Claim-policy). |

This design can be implemented incrementally in Phase 5; Phase 4.5 continues with a single demo Coverage-policy for all responses.
