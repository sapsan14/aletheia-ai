package ai.aletheia.api.dto;

import java.time.Instant;

/**
 * Response for GET /api/ai/verify/:id.
 * Full record for the verification page.
 */
public record AiVerifyResponse(
        Long id,
        String prompt,
        String response,
        String responseHash,
        String signature,
        String tsaToken,
        String llmModel,
        Instant createdAt,
        String requestId,
        Double temperature,
        String systemPrompt,
        Integer version
) {}
