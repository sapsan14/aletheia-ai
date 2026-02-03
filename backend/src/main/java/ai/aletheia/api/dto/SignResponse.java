package ai.aletheia.api.dto;

import java.time.Instant;

/**
 * Response for POST /api/sign (sign-only API).
 */
public record SignResponse(
        Long id,
        String responseHash,
        String signature,
        String tsaToken,
        String claim,
        Double confidence,
        String policyVersion,
        String modelId,
        Instant createdAt
) {}
