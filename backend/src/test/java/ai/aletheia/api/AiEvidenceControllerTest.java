package ai.aletheia.api;

import ai.aletheia.db.AiResponseRepository;
import ai.aletheia.db.entity.AiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for GET /api/ai/evidence/:id.
 * Verifies: 200 with ZIP or JSON (format=json); 404 for unknown id; 503 when signing key not configured is not tested here (key is set in test profile).
 */
@SpringBootTest
@AutoConfigureMockMvc
class AiEvidenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiResponseRepository repository;

    @Test
    void evidence_existingId_returnsZip() throws Exception {
        AiResponse entity = new AiResponse("Q", "A\n", "a".repeat(64));
        entity.setSignature(Base64.getEncoder().encodeToString(new byte[] { 1, 2, 3 }));
        entity.setTsaToken(Base64.getEncoder().encodeToString(new byte[] { 4, 5, 6 }));
        entity.setLlmModel("test-model");
        AiResponse saved = repository.save(entity);

        mockMvc.perform(get("/api/ai/evidence/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/zip")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("aletheia-evidence-" + saved.getId() + ".aep")));
    }

    @Test
    void evidence_existingId_formatJson_returnsJsonWithBase64Files() throws Exception {
        AiResponse entity = new AiResponse("Q", "A\n", "b".repeat(64));
        entity.setSignature(Base64.getEncoder().encodeToString(new byte[] { 1 }));
        entity.setTsaToken(Base64.getEncoder().encodeToString(new byte[] { 2 }));
        entity.setLlmModel("m");
        AiResponse saved = repository.save(entity);

        mockMvc.perform(get("/api/ai/evidence/" + saved.getId()).param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$['response.txt']").exists())
                .andExpect(jsonPath("$['canonical.bin']").exists())
                .andExpect(jsonPath("$['hash.sha256']").exists())
                .andExpect(jsonPath("$['signature.sig']").exists())
                .andExpect(jsonPath("$['timestamp.tsr']").exists())
                .andExpect(jsonPath("$['metadata.json']").exists())
                .andExpect(jsonPath("$['public_key.pem']").exists())
                .andExpect(jsonPath("$['signature_pqc.sig']").doesNotExist())
                .andExpect(jsonPath("$['pqc_public_key.pem']").doesNotExist())
                .andExpect(jsonPath("$['pqc_algorithm.json']").doesNotExist());
    }

    @Test
    void evidence_returnsMetadataWithPolicyFields() throws Exception {
        AiResponse entity = new AiResponse("Q", "A\n", "c".repeat(64));
        entity.setSignature(Base64.getEncoder().encodeToString(new byte[] { 1 }));
        entity.setTsaToken(Base64.getEncoder().encodeToString(new byte[] { 2 }));
        entity.setLlmModel("m");
        entity.setPolicyCoverage(0.5);
        entity.setPolicyRulesEvaluated(
                "[{\"ruleId\":\"R1\",\"status\":\"pass\"},{\"ruleId\":\"R2\",\"status\":\"pass\"}]");
        AiResponse saved = repository.save(entity);

        ResultActions result = mockMvc.perform(get("/api/ai/evidence/" + saved.getId()).param("format", "json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$['metadata.json']").exists());

        String responseBody = result.andReturn().getResponse().getContentAsString();
        JsonNode root = new ObjectMapper().readTree(responseBody);
        String metadataBase64 = root.get("metadata.json").asText();
        String metadataJson = new String(Base64.getDecoder().decode(metadataBase64));
        JsonNode metadata = new ObjectMapper().readTree(metadataJson);

        assertThat(metadata.has("policy_coverage")).isTrue();
        assertThat(metadata.get("policy_coverage").asDouble()).isEqualTo(0.5);
        assertThat(metadata.has("policy_rules_evaluated")).isTrue();
        assertThat(metadata.get("policy_rules_evaluated").isArray()).isTrue();
        assertThat(metadata.get("policy_rules_evaluated").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void evidence_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/ai/evidence/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Record not found"))
                .andExpect(jsonPath("$.details").value(999999));
    }
}
