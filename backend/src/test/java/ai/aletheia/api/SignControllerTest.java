package ai.aletheia.api;

import ai.aletheia.crypto.CanonicalizationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CanonicalizationService canonicalizationService;

    @Test
    void sign_validBody_returnsSignedRecordAndVerifyWorks() throws Exception {
        String responseText = "Hello world";
        String prompt = "Please comply with GDPR.";
        String requestBody = """
                {
                  "response": "Hello world",
                  "modelId": "external-model",
                  "policyId": "gdpr-2024",
                  "prompt": "Please comply with GDPR."
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.responseHash").isNotEmpty())
                .andExpect(jsonPath("$.signature").isNotEmpty())
                .andExpect(jsonPath("$.tsaToken").isNotEmpty())
                .andExpect(jsonPath("$.policyVersion").value("gdpr-2024"))
                .andExpect(jsonPath("$.modelId").value("external-model"))
                .andReturn();

        JsonNode signResponse = objectMapper.readTree(result.getResponse().getContentAsString());
        long id = signResponse.get("id").asLong();
        String responseHash = signResponse.get("responseHash").asText();

        String canonicalResponse = new String(
                canonicalizationService.canonicalize(responseText),
                StandardCharsets.UTF_8
        );

        mockMvc.perform(get("/api/ai/verify/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.prompt").value(prompt))
                .andExpect(jsonPath("$.response").value(canonicalResponse))
                .andExpect(jsonPath("$.responseHash").value(responseHash))
                .andExpect(jsonPath("$.llmModel").value("external-model"));
    }

    @Test
    void sign_missingResponse_returns400() throws Exception {
        mockMvc.perform(post("/api/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
