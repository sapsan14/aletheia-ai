package ai.aletheia.api.dto;

/**
 * Response for POST /api/audit/demo.
 * Same as crypto demo plus the saved record id.
 */
public record AuditDemoResponse(
        Long id,
        String text,
        String canonicalBase64,
        String hash,
        String signature,
        String signatureStatus,
        String tsaToken,
        String tsaStatus
) {}
