package ai.aletheia.siem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Emits structured SIEM events as JSON lines (stdout or file).
 */
@Service
public class SiemEventService {

    private static final Logger log = LoggerFactory.getLogger(SiemEventService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path logPath;

    public SiemEventService(@Value("${ai.aletheia.siem.log-path:}") String logPath) {
        this.logPath = (logPath != null && !logPath.isBlank()) ? Path.of(logPath) : null;
    }

    public void emitResponseGenerated(Long responseId, String responseHash, String policyVersion, String modelId) {
        emitEvent("response_generated", responseId, responseHash, policyVersion, modelId);
    }

    public void emitResponseSigned(Long responseId, String responseHash, String policyVersion, String modelId) {
        emitEvent("response_signed", responseId, responseHash, policyVersion, modelId);
    }

    public void emitEvidenceCreated(Long responseId, String responseHash, String policyVersion, String modelId) {
        emitEvent("evidence_created", responseId, responseHash, policyVersion, modelId);
    }

    private void emitEvent(String eventType, Long responseId, String responseHash, String policyVersion, String modelId) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("event_type", eventType);
        event.put("timestamp", Instant.now().toString());
        if (responseId != null) {
            event.put("response_id", responseId);
        }
        if (responseHash != null && !responseHash.isBlank()) {
            event.put("hash", responseHash);
        }
        if (policyVersion != null && !policyVersion.isBlank()) {
            event.put("policy_version", policyVersion);
        }
        if (modelId != null && !modelId.isBlank()) {
            event.put("model_id", modelId);
        }

        try {
            String json = objectMapper.writeValueAsString(event);
            writeLine(json);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize SIEM event {}", eventType, e);
        } catch (IOException e) {
            log.warn("Failed to write SIEM event {}", eventType, e);
        } catch (Exception e) {
            log.warn("Unexpected error emitting SIEM event {}", eventType, e);
        }
    }

    private void writeLine(String json) throws IOException {
        if (logPath == null) {
            System.out.println(json);
            return;
        }
        Path parent = logPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        Files.writeString(
                logPath,
                json + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND
        );
    }
}
