package ai.aletheia.audit.dto;

/**
 * Request to persist a verifiable AI response record.
 *
 * <p>Required: prompt, response, responseHash. Optional: signature, signaturePqc, tsaToken,
 * llmModel, requestId, temperature, systemPrompt, version.
 * DP2.4: Optional claim, confidence, policy_version (Minimal AI Claim).
 * PQC.3: Optional signaturePqc (ML-DSA over same hash), pqcPublicKeyPem (stored for Evidence Package).
 */
public record AuditRecordRequest(
        String prompt,
        String response,
        String responseHash,
        String signature,
        String signaturePqc,
        String pqcPublicKeyPem,
        String tsaToken,
        String llmModel,
        String requestId,
        Double temperature,
        String systemPrompt,
        Integer version,
        String claim,
        Double confidence,
        String policyVersion
) {
    /** Minimal request with required fields only. */
    public static AuditRecordRequest of(String prompt, String response, String responseHash) {
        return new AuditRecordRequest(
                prompt, response, responseHash,
                null, null, null, null, null, null, null, null, null,
                null, null, null
        );
    }
}
