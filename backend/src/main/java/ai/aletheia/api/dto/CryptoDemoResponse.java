package ai.aletheia.api.dto;

/**
 * Response body for POST /api/crypto/demo.
 * Exposes canonical form, hash, signature, and optionally TSA timestamp token.
 */
public record CryptoDemoResponse(
        String text,
        String canonicalBase64,
        String hash,
        String signature,
        String signatureStatus,
        String tsaToken,
        String tsaStatus
) {
}
