package ai.aletheia.llm;

/**
 * Result of an LLM completion call.
 *
 * @param responseText the model's response (plain text)
 * @param modelId      model identifier (e.g. "gpt-4", "gemini-pro")
 */
public record LLMResult(String responseText, String modelId) {
    public LLMResult {
        if (responseText == null) {
            responseText = "";
        }
        if (modelId == null) {
            modelId = "";
        }
    }
}
