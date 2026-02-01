package ai.aletheia.llm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for LLMClient. Uses MockLLMClient (no real API calls).
 */
@SpringBootTest
@ActiveProfiles("test")
class LLMClientTest {

    @Autowired
    private LLMClient llmClient;

    @Test
    void complete_returnsNonEmptyResponseAndModelId() {
        LLMResult result = llmClient.complete("What is 2+2?");
        assertThat(result.responseText()).isNotBlank();
        assertThat(result.modelId()).isNotBlank();
    }

    @Test
    void complete_hello_returnsGreeting() {
        LLMResult result = llmClient.complete("hello");
        assertThat(result.responseText()).contains("Hello");
        assertThat(result.modelId()).isEqualTo("mock-model");
    }
}
