package ai.aletheia.api;

import ai.aletheia.db.AiResponseRepository;
import ai.aletheia.db.entity.AiResponse;
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
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PQC.4: When PQC is enabled and entity has signature_pqc, Evidence Package contains PQC files.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AiEvidenceControllerPqcTest {

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
    void evidence_withPqc_formatJson_includesPqcFiles() throws Exception {
        String path = new ClassPathResource("fixtures/pqc/ai_pqc.key").getFile().getAbsolutePath();
        if (!Files.exists(java.nio.file.Path.of(path))) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "PQC key not available");
        }

        AiResponse entity = new AiResponse("Q", "A\n", "c".repeat(64));
        entity.setSignature(Base64.getEncoder().encodeToString(new byte[] { 1, 2, 3 }));
        entity.setTsaToken(Base64.getEncoder().encodeToString(new byte[] { 4, 5, 6 }));
        entity.setLlmModel("test-model");
        entity.setSignaturePqc(Base64.getEncoder().encodeToString(new byte[] { 7, 8, 9, 10 }));
        AiResponse saved = repository.save(entity);

        mockMvc.perform(get("/api/ai/evidence/" + saved.getId()).param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$['signature_pqc.sig']").exists())
                .andExpect(jsonPath("$['pqc_public_key.pem']").exists())
                .andExpect(jsonPath("$['pqc_algorithm.json']").exists());
    }
}
