package ai.aletheia.crypto;

/**
 * Produces a deterministic byte representation of LLM response text
 * so that the same logical content always yields the same bytes before hashing.
 */
public interface CanonicalizationService {

    /**
     * Canonicalize input text to UTF-8 bytes.
     * Rules: (1) Unicode NFC, (2) line endings \n, (3) trim per line, collapse blank lines to one,
     * (4) non-empty result ends with exactly one newline (see impl).
     *
     * @param input LLM response text (may be null; null → empty byte array)
     * @return UTF-8 bytes, deterministic for same logical content
     */
    byte[] canonicalize(String input);
}
