# Phase 4 analytics (minimal)

## What is tracked

Events recorded (Phase 4):

- `landing_view` — landing page view
- `cta_click` — click on the main CTA button
- `demo_view` — demo page view (home or verify)
- `download_evidence_click` — click on **Download evidence**

## How it is collected

Frontend sends `POST /api/metrics/event` with JSON:

```json
{
  "event": "landing_view",
  "responseId": 123
}
```

`responseId` is optional and used when the event relates to a specific response
(e.g. evidence download).

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

No personal data is collected. Events only include the event name, timestamp, and
optional response id.
