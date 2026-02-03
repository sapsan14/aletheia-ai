# SIEM Integration (Phase 5)

## Purpose

Aletheia can emit structured events for SIEM ingestion (audit, compliance, security monitoring).
Events are emitted as **JSON Lines** (one JSON object per line).

## Event types

- `response_generated` — when `POST /api/ai/ask` completes.
- `response_signed` — when `POST /api/ai/ask` or `POST /api/sign` completes.
- `evidence_created` — when an Evidence Package is built or downloaded.

## Event schema (JSON Lines)

Each event is a single JSON object:

```json
{
  "event_type": "response_signed",
  "timestamp": "2026-02-03T10:15:30Z",
  "response_id": 123,
  "hash": "7e7ed3f4dfe18f5c7e2c7f50c7efab0c6f2f3a8a2d16f30c6fef27f7d0c9a1a5",
  "policy_version": "gdpr-2024",
  "model_id": "gpt-4"
}
```

> PII is intentionally avoided. If you need richer context, store it in your own system
> keyed by `response_id`.

## Delivery options

**Option A (implemented):** JSON Lines to a log file or stdout.

- Set `ALETHEIA_SIEM_LOG_PATH=/var/log/aletheia/siem.jsonl` to write events to a file.
- Leave it empty to emit JSON lines to stdout (default).

**Option B (planned):** Webhook delivery to a SIEM collector.

## Enablement

In `.env`:

```
ALETHEIA_SIEM_LOG_PATH=/var/log/aletheia/siem.jsonl
```

Deployers can tail the file or forward stdout to their SIEM collector.
