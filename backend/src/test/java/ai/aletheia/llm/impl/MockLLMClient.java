package ai.aletheia.llm.impl;

import ai.aletheia.llm.LLMClient;
import ai.aletheia.llm.LLMResult;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Mock LLM client for tests: implements {@link LLMClient} without calling any real API.
 *
 * <p>Used when {@code @ActiveProfiles("test")}: returns deterministic responses so tests
 * don't need OPENAI_API_KEY or network. Replaces OpenAiLLMClient in test context.
 */
@Service
@Profile("test")
@Primary
public class MockLLMClient implements LLMClient {

    @Override
    public LLMResult complete(String prompt) {
        if (prompt == null) {
            prompt = "";
        }
        String response = "2+2 equals 4.";
        if (prompt.toLowerCase().contains("hello")) {
            response = "Hello! How can I help you?";
        }
        return new LLMResult(response, "mock-model", 0.7);
    }
}
