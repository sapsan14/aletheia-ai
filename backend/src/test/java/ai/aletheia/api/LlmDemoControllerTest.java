package ai.aletheia.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LlmDemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void demo_returnsResponseAndModelId() throws Exception {
        mockMvc.perform(post("/api/llm/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseText").isNotEmpty())
                .andExpect(jsonPath("$.modelId").value("mock-model"));
    }

    @Test
    void demo_nullPrompt_returns400() throws Exception {
        mockMvc.perform(post("/api/llm/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
