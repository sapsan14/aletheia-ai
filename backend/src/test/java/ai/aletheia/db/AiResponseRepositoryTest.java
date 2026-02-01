package ai.aletheia.db;

import ai.aletheia.db.entity.AiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link AiResponseRepository}.
 *
 * <p>Verifies save and findById with all fields. Uses H2 in-memory (test profile).
 */
@SpringBootTest
@Transactional
class AiResponseRepositoryTest {

    @Autowired
    private AiResponseRepository repository;

    @Test
    void saveAndFindById_allFieldsMatch() {
        AiResponse entity = new AiResponse("What is 2+2?", "4", "a1b2c3d4e5f6");
        entity.setSignature("sig-base64-here");
        entity.setTsaToken("tsa-base64-here");
        entity.setLlmModel("gpt-4");
        entity.setRequestId("req-123");
        entity.setTemperature(0.7);
        entity.setSystemPrompt("You are helpful.");
        entity.setVersion(1);

        AiResponse saved = repository.save(entity);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        AiResponse found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getPrompt()).isEqualTo("What is 2+2?");
        assertThat(found.getResponse()).isEqualTo("4");
        assertThat(found.getResponseHash()).isEqualTo("a1b2c3d4e5f6");
        assertThat(found.getSignature()).isEqualTo("sig-base64-here");
        assertThat(found.getTsaToken()).isEqualTo("tsa-base64-here");
        assertThat(found.getLlmModel()).isEqualTo("gpt-4");
        assertThat(found.getRequestId()).isEqualTo("req-123");
        assertThat(found.getTemperature()).isEqualTo(0.7);
        assertThat(found.getSystemPrompt()).isEqualTo("You are helpful.");
        assertThat(found.getVersion()).isEqualTo(1);
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void save_minimalFields_optionalNull() {
        AiResponse entity = new AiResponse("Hi", "Hello!", "abc123");
        AiResponse saved = repository.save(entity);

        AiResponse found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getPrompt()).isEqualTo("Hi");
        assertThat(found.getResponse()).isEqualTo("Hello!");
        assertThat(found.getResponseHash()).isEqualTo("abc123");
        assertThat(found.getSignature()).isNull();
        assertThat(found.getTsaToken()).isNull();
        assertThat(found.getLlmModel()).isNull();
        assertThat(found.getRequestId()).isNull();
        assertThat(found.getTemperature()).isNull();
        assertThat(found.getSystemPrompt()).isNull();
        assertThat(found.getVersion()).isEqualTo(1);
    }
}
