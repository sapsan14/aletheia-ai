package ai.aletheia.audit;

import ai.aletheia.audit.dto.AuditRecordRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AuditRecordServiceTest {

    @Autowired
    private AuditRecordService service;

    @Test
    void save_returnsIdAndPersistsAllFields() {
        AuditRecordRequest request = new AuditRecordRequest(
                "What is 2+2?",
                "4",
                "a1b2c3d4e5f6",
                "sig-base64",
                null,
                null,
                "tsa-base64",
                "gpt-4",
                "req-123",
                0.7,
                "You are helpful.",
                1,
                null,
                null,
                null
        );

        Long id = service.save(request);
        assertThat(id).isNotNull();
    }

    @Test
    void save_minimalRequest_persistsRequiredFieldsOnly() {
        AuditRecordRequest request = AuditRecordRequest.of("Hi", "Hello!", "abc123");
        Long id = service.save(request);
        assertThat(id).isNotNull();
    }
}
