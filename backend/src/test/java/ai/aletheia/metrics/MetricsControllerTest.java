package ai.aletheia.metrics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MetricEventRepository repository;

    @Test
    void recordEvent_persistsEvent() throws Exception {
        long before = repository.count();

        mockMvc.perform(post("/api/metrics/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"event\":\"landing_view\"}"))
                .andExpect(status().isNoContent());

        assertThat(repository.count()).isEqualTo(before + 1);
    }
}
