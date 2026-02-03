Aletheia as attestation layer for agent (MCP) outputs: document flow and metadata; optionally extend API.

**Acceptance Criteria:**
- Documentation: `docs/en/mcp/attestation.md` — flow (agent → Aletheia sign → verify/evidence), metadata (agent_id, tool_usage, policy_id, timestamp), example request
- Optional: POST /api/sign accepts agentId, toolUsage; stored in DB and in Evidence Package metadata; returned in GET /api/ai/verify/:id

См. `docs/en/PLAN_PHASE5.md` — 5.4, Tasks P5.4.1, P5.4.2.
