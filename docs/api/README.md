# Public API (OpenAPI)

- **Spec file:** `docs/api/openapi.yaml`
- **Base URL:** see `servers` in the spec. For local dev: `http://localhost:8080`.

## Error codes

Common error codes documented in the spec:

- `VALIDATION_ERROR` — missing or invalid input
- `NOT_FOUND` — record not found
- `LLM_UNAVAILABLE` — upstream LLM failed
- `SIGNING_ERROR` — signing key missing or signing failed
- `TIMESTAMP_UNAVAILABLE` — TSA/timestamp service unavailable
- `INTERNAL_ERROR` — unexpected server failure

> Note: Some existing endpoints return legacy error bodies (`error`, `message`, `details`).
> Always rely on the HTTP status code first.

## Changelog

See Git tags or release notes for versioned changes.
