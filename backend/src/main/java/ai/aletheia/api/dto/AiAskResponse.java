package ai.aletheia.api.dto;

/**
 * Response for POST /api/ai/ask.
 *
 * @param response     AI response text (canonical form)
 * @param responseHash SHA-256 hex of canonical response
 * @param signature    Base64 RSA signature of hash (null if key not configured)
 * @param tsaToken     Base64 RFC 3161 timestamp token (null if no signature or TSA error)
 * @param id           saved record id in ai_response
 * @param model        model identifier (e.g. gpt-4)
 */
public record AiAskResponse(
        String response,
        String responseHash,
        String signature,
        String tsaToken,
        Long id,
        String model
) {}
