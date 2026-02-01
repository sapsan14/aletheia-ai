package ai.aletheia.api.dto;

/** Request for POST /api/ai/ask. */
public record AiAskRequest(String prompt) {}
