# Phase 4 analytics (minimal)

## What is tracked

Events recorded (Phase 4 and 4.5 funnel):

| Event | When | Why |
|-------|------|-----|
| `page_view_main` | User lands on the main (demo) page | Count how many people see the landing / demo. |
| `page_view_verify` | User opens the verify page (`/verify?id=...`) | Count verify page views. |
| `page_view_use_cases` | User opens the use-cases page | Optional; measure interest in use-cases content. |
| `cta_click` | User clicks **Send & Verify** (runs the demo) | Count how many people run the demo. |
| `download_evidence` | User clicks **Download evidence** (Evidence Package) | Count evidence downloads; optional `responseId` for per-response breakdown. |
| `download_verifier` | User clicks **Download verifier** (JAR) | Count verifier downloads. |
| `view_use_cases` | User opens the use-cases page | Same as `page_view_use_cases`; kept for naming consistency with plan. |

Legacy events (still accepted): `landing_view`, `demo_view`, `download_evidence_click`.

**Story we can tell:** “X people saw the landing (`page_view_main`), Y ran the demo (`cta_click`), Z downloaded Evidence (`download_evidence`) or Verifier (`download_verifier`), K viewed use-cases (`view_use_cases`).”

## How it is collected

Frontend calls `trackEvent(name, props?)` in `frontend/lib/analytics.ts`:

- **Development:** events are logged to the browser console (`[analytics] { event, ... }`).
- **Backend:** `POST /api/metrics/event` with JSON body:
  - `event` (string): event name sent to the server (e.g. `page_view_main`, `download_evidence`).
  - `responseId` (number or null): optional, used when the event relates to a specific response (e.g. evidence download).

No external analytics provider or keys are used; the implementation is provider-agnostic so a real provider can be plugged in later.

## Storage

Events are stored in the database table `metrics_event`:

- `event_name` (string)
- `response_id` (nullable)
- `created_at` (timestamp)

## Viewing counts

Use the backend summary endpoint:

```
GET /api/metrics/summary
```

It returns a JSON map of totals per event name.

Alternatively, query the database directly:

```sql
SELECT event_name, COUNT(*) FROM metrics_event GROUP BY event_name;
```

## Privacy

No personal data is collected. Events only include the event name, timestamp, and optional response id.
