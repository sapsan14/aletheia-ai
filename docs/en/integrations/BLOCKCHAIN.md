# Blockchain Anchoring (Phase 5, optional)

## Purpose

Anchoring the hash of an Evidence Package (or response hash) on a public blockchain provides
an independent **proof of existence** and a timestamp that does not rely solely on a TSA.

## Options

- **Ethereum** — store the hash in transaction data or via a minimal smart contract.
- **Bitcoin** — store the hash in an `OP_RETURN` output.

## Proposed flow

1. After signing and timestamping, compute the hash of the Evidence Package (or reuse `responseHash`).
2. Send the hash to a configured anchor service or blockchain API.
3. Store the returned `tx_id` / `block_number` in metadata.
4. Include anchor info in `metadata.json` inside the Evidence Package.

## Phase 5 scope

Implementation is **optional** in Phase 5. This document describes the intended design;
actual anchoring can be added in Phase 6 based on partner needs.
