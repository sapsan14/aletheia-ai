package ai.aletheia.api;

import ai.aletheia.crypto.HashService;
import ai.aletheia.db.AiResponseRepository;
import ai.aletheia.db.entity.AiResponse;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuditDemoControllerTest {

    private static final String EXPECTED_HASH_HELLO = "5891b5b522d5df086d0ff0b110fbd9d21bb4fc7163af34d08286a2e846f6be03";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HashService hashService;

    @Autowired
    private AiResponseRepository repository;

    @Test
    void demo_savesRecordAndReturnsId() throws Exception {
        mockMvc.perform(post("/api/audit/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.text").value("hello"))
                .andExpect(jsonPath("$.hash").value(EXPECTED_HASH_HELLO))
                .andExpect(jsonPath("$.signatureStatus").exists())
                .andExpect(jsonPath("$.tsaStatus").exists());
    }

    /** PQC.3: When PQC is disabled, saved entity has signature (classical) and signature_pqc is null. */
    @Test
    void demo_whenPqcDisabled_savedEntityHasSignaturePqcNull() throws Exception {
        String json = mockMvc.perform(post("/api/audit/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(json, "$.id")).longValue();
        AiResponse entity = repository.findById(id).orElseThrow();
        assertThat(entity.getSignature()).isNotBlank();
        assertThat(entity.getSignaturePqc()).isNull();
    }

    @Test
    void demo_nullText_returns400() throws Exception {
        mockMvc.perform(post("/api/audit/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":null}"))
                .andExpect(status().isBadRequest());
    }
}
