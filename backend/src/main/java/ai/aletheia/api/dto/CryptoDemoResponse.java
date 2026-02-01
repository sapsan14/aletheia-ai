package ai.aletheia.api.dto;

/**
 * Response body for POST /api/crypto/demo.
 * Exposes canonical form, hash, and optionally signature.
 */
public record CryptoDemoResponse(
        String text,
        String canonicalBase64,
        String hash,
        String signature,
        String signatureStatus
) {
}
