package ai.aletheia.api;

import ai.aletheia.db.AiResponseRepository;
import ai.aletheia.db.entity.AiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AiVerifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiResponseRepository repository;

    @Test
    void verify_existingId_returnsRecord() throws Exception {
        AiResponse entity = new AiResponse("Q", "A", "hash123");
        entity.setLlmModel("gpt-4");
        entity.setPolicyCoverage(0.5);
        entity.setPolicyRulesEvaluated("[{\"ruleId\":\"R1\",\"status\":\"pass\"}]");
        AiResponse saved = repository.save(entity);

        mockMvc.perform(get("/api/ai/verify/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.prompt").value("Q"))
                .andExpect(jsonPath("$.response").value("A"))
                .andExpect(jsonPath("$.responseHash").value("hash123"))
                .andExpect(jsonPath("$.llmModel").value("gpt-4"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.hashMatch").exists())
                .andExpect(jsonPath("$.signatureValid").exists())
                .andExpect(jsonPath("$.policyCoverage").value(0.5))
                .andExpect(jsonPath("$.policyRulesEvaluated[0].ruleId").value("R1"))
                .andExpect(jsonPath("$.policyRulesEvaluated[0].status").value("pass"));
    }

    @Test
    void verify_recordWithClaim_returnsClaimConfidencePolicyVersion() throws Exception {
        AiResponse entity = new AiResponse("Does this comply with GDPR?", "Yes, it does.", "abc123");
        entity.setLlmModel("gpt-4");
        entity.setClaim("Yes, it does.");
        entity.setConfidence(0.85);
        entity.setPolicyVersion("gdpr-2024");
        AiResponse saved = repository.save(entity);

        mockMvc.perform(get("/api/ai/verify/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claim").value("Yes, it does."))
                .andExpect(jsonPath("$.confidence").value(0.85))
                .andExpect(jsonPath("$.policyVersion").value("gdpr-2024"));
    }

    @Test
    void verify_unknownId_returns404WithJsonBody() throws Exception {
        mockMvc.perform(get("/api/ai/verify/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Record not found"))
                .andExpect(jsonPath("$.details").value(999999));
    }
}
