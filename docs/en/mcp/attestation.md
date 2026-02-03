# MCP Attestation (Phase 5)

## Purpose

Aletheia can attest agent outputs. An agent (for example, an MCP server) generates a response
and sends it to Aletheia for signing. The signed result and Evidence Package provide
cryptographic proof of **what** the agent said and **when** it was signed.

## Flow

1. **Agent produces output.**
2. **Agent calls Aletheia** via `POST /api/sign` (sign-only) or `POST /api/ai/ask` (LLM + signing).
3. **Aletheia returns** `id`, `signature`, `tsaToken`, and optional policy metadata.
4. **Client retrieves verification data** via `GET /api/ai/verify/:id`.
5. **Evidence Package** is downloaded via `GET /api/ai/evidence/:id` and can be verified offline.

## Recommended metadata

To support agent attestation, include the following metadata in your pipeline:

- `agent_id` — identifier of the agent or MCP server.
- `tool_usage` — optional list of tools used (or a JSON string).
- `policy_id` — policy or ruleset applied to the output.
- `timestamp` — obtained from TSA (`tsaToken`) and stored by Aletheia.

> **Note:** `agent_id` and `tool_usage` fields are planned for a future API extension.
> For now, pass them in your own telemetry and store alongside the returned `id`.

## Example (sign-only)

```json
POST /api/sign
{
  "response": "Agent output text...",
  "modelId": "agent-llm",
  "policyId": "compliance-2024",
  "prompt": "Optional prompt or context"
}
```

## References

- OpenAPI spec: `docs/api/openapi.yaml`
- MCP (Model Context Protocol): https://modelcontextprotocol.io/
