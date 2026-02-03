package ai.aletheia.api;

import ai.aletheia.db.AiResponseRepository;
import ai.aletheia.db.entity.AiResponse;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PQC.3 integration: when PQC is enabled, saved entity has both signature (classical) and signature_pqc.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PqcPipelineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiResponseRepository repository;

    @DynamicPropertySource
    static void pqcProperties(DynamicPropertyRegistry registry) {
        try {
            String path = new ClassPathResource("fixtures/pqc/ai_pqc.key").getFile().getAbsolutePath();
            if (Files.exists(java.nio.file.Path.of(path))) {
                registry.add("ai.aletheia.signing.pqc-enabled", () -> "true");
                registry.add("ai.aletheia.signing.pqc-key-path", () -> path);
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    void demo_whenPqcEnabled_savedEntityHasSignatureAndSignaturePqc() throws Exception {
        String path = new ClassPathResource("fixtures/pqc/ai_pqc.key").getFile().getAbsolutePath();
        if (!Files.exists(java.nio.file.Path.of(path))) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "PQC key not available");
        }
        String json = mockMvc.perform(post("/api/audit/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(json, "$.id")).longValue();
        AiResponse entity = repository.findById(id).orElseThrow();
        assertThat(entity.getSignature()).isNotBlank();
        assertThat(entity.getSignaturePqc()).isNotBlank();
    }
}
