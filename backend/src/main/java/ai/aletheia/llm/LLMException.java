package ai.aletheia.llm;

/**
 * Thrown when an LLM API call fails (rate limit, timeout, invalid key, etc.).
 */
public class LLMException extends RuntimeException {

    public LLMException(String message) {
        super(message);
    }

    public LLMException(String message, Throwable cause) {
        super(message, cause);
    }
}
