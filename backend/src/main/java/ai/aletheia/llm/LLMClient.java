package ai.aletheia.llm;

/**
 * Client for LLM completion. Implementations call OpenAI, Gemini, Mistral, etc.
 *
 * <p>Returns plain text and model metadata only — no canonicalization or hashing.
 */
public interface LLMClient {

    /**
     * Send prompt to the LLM and return response text and model identifier.
     *
     * @param prompt user prompt (non-null, may be empty)
     * @return response text and model id
     * @throws LLMException on API errors (rate limit, timeout, invalid key, etc.)
     */
    LLMResult complete(String prompt);
}
