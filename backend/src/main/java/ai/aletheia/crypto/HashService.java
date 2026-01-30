package ai.aletheia.crypto;

/**
 * SHA-256 hash of canonical bytes. Callers should pass bytes produced by
 * {@link CanonicalizationService#canonicalize(String)}, or use
 * {@link HashService#hashFromString(String)} to canonicalize and hash in one step.
 */
public interface HashService {

    /**
     * Hash canonical bytes with SHA-256. Returns 64-character lowercase hex string.
     *
     * @param canonicalBytes bytes in canonical form (e.g. from CanonicalizationService); may be null (→ empty digest)
     * @return 64-char hex string, or digest of empty input if null
     */
    String hash(byte[] canonicalBytes);

    /**
     * Canonicalize input then hash. Use this when you have raw LLM response text.
     * Callers who already have canonical bytes should use {@link #hash(byte[])}.
     *
     * @param input raw text (e.g. LLM response); null → same as hash(empty)
     * @return 64-char hex string
     */
    String hashFromString(String input);
}
