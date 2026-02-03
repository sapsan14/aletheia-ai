package ai.aletheia.api.dto;

/**
 * Response for POST /api/audit/demo.
 * Same as crypto demo plus the saved record id.
 * PQC.5: When PQC is enabled, signaturePqc (Base64 ML-DSA) and pqcAlgorithm are set.
 */
public record AuditDemoResponse(
        Long id,
        String text,
        String canonicalBase64,
        String hash,
        String signature,
        String signatureStatus,
        String tsaToken,
        String tsaStatus,
        String signaturePqc,
        String pqcAlgorithm
) {}
