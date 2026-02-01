package ai.aletheia.api.dto;

import java.time.Instant;

/**
 * Response for GET /api/ai/verify/:id.
 * Full record for the verification page.
 *
 * @param hashMatch       true if recomputed hash equals stored responseHash; false otherwise
 * @param signatureValid  "valid" | "invalid" | "n_a" (not applicable: no signature or key not configured)
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
        Integer version,
        Boolean hashMatch,
        String signatureValid
) {}
