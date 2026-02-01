package ai.aletheia.llm;

/**
 * Result of an LLM completion call.
 *
 * @param responseText the model's response (plain text)
 * @param modelId      model identifier (e.g. "gpt-4", "gpt-4-0613", "gemini-pro")
 * @param temperature  LLM temperature used, if available (null when unknown)
 */
public record LLMResult(String responseText, String modelId, Double temperature) {
    public LLMResult {
        if (responseText == null) {
            responseText = "";
        }
        if (modelId == null) {
            modelId = "";
        }
    }

    /** Convenience constructor without temperature (for tests or when unknown). */
    public LLMResult(String responseText, String modelId) {
        this(responseText, modelId, null);
    }
}
