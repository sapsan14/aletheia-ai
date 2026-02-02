package ai.aletheia.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Response for GET /api/ai/verify/:id.
 * Full record for the verification page.
 *
 * @param hashMatch       true if recomputed hash equals stored responseHash; false otherwise
 * @param signatureValid  "valid" | "invalid" | "n_a" (not applicable: no signature or key not configured)
 * @param claim           DP2.4: minimal AI claim text (null when not compliance)
 * @param confidence      DP2.4: confidence in [0,1] (null when not compliance)
 * @param policyVersion   DP2.4: e.g. "gdpr-2024" (null when not compliance)
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
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
        String claim,
        Double confidence,
        String policyVersion,
        Boolean hashMatch,
        String signatureValid
) {}
