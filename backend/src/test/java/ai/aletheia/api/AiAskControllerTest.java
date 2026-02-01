package ai.aletheia.api;

import ai.aletheia.db.AiResponseRepository;
import ai.aletheia.db.entity.AiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiAskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiResponseRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void ask_returns200_withResponseHashSignatureTsaTokenIdModel() throws Exception {
        String json = mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.response").exists())
                .andExpect(jsonPath("$.responseHash").isString())
                .andExpect(jsonPath("$.signature").exists())
                .andExpect(jsonPath("$.tsaToken").exists())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.model").value("mock-model"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(json).get("id").asLong();
        String responseHash = objectMapper.readTree(json).get("responseHash").asText();
        Optional<AiResponse> saved = repository.findById(id);
        assertThat(saved).isPresent();
        assertThat(saved.get().getResponseHash()).isEqualTo(responseHash);
        assertThat(saved.get().getLlmModel()).isEqualTo("mock-model");
    }

    @Test
    void ask_nullPrompt_returns400() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ask_emptyPrompt_returns400() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
