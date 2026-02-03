package ai.aletheia.api.dto;

/**
 * Request for POST /api/sign (sign-only API).
 *
 * @param response  LLM response text to sign (required)
 * @param modelId   optional model identifier (default: "external")
 * @param policyId  optional policy/version id for compliance metadata
 * @param prompt    optional prompt for audit trail or claim inference
 * @param requestId optional request correlation id
 */
public record SignRequest(
        String response,
        String modelId,
        String policyId,
        String prompt,
        String requestId
) {}
