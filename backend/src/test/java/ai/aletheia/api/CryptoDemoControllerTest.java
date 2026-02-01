package ai.aletheia.api;

import ai.aletheia.crypto.HashService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for POST /api/crypto/demo.
 * Verifies: canonicalize → hash → sign pipeline returns correct JSON.
 * Uses real CanonicalizationService, HashService, SignatureService (test key from classpath).
 */
@SpringBootTest
@AutoConfigureMockMvc
class CryptoDemoControllerTest {

    /** SHA-256 of "hello\n" (canonical form of "hello") */
    private static final String EXPECTED_HASH_HELLO = "5891b5b522d5df086d0ff0b110fbd9d21bb4fc7163af34d08286a2e846f6be03";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HashService hashService;

    @Test
    void demo_withHello_returns200AndHash() throws Exception {
        mockMvc.perform(post("/api/crypto/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value("hello"))
                .andExpect(jsonPath("$.canonicalBase64").exists())
                .andExpect(jsonPath("$.hash").value(EXPECTED_HASH_HELLO))
                .andExpect(jsonPath("$.signature").isNotEmpty())
                .andExpect(jsonPath("$.signatureStatus").value("SIGNED"))
                .andExpect(jsonPath("$.tsaToken").isNotEmpty())
                .andExpect(jsonPath("$.tsaStatus").value("MOCK_TSA"));
    }

    @Test
    void demo_withHelloWorld_returnsHashFromPipeline() throws Exception {
        String expectedHash = hashService.hashFromString("hello world");

        mockMvc.perform(post("/api/crypto/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello world\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hash").value(expectedHash))
                .andExpect(jsonPath("$.signatureStatus").value("SIGNED"))
                .andExpect(jsonPath("$.tsaToken").isNotEmpty())
                .andExpect(jsonPath("$.tsaStatus").value("MOCK_TSA"));
    }

    @Test
    void demo_withNullText_returns400() throws Exception {
        mockMvc.perform(post("/api/crypto/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void demo_emptyJson_returns400() throws Exception {
        mockMvc.perform(post("/api/crypto/demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
