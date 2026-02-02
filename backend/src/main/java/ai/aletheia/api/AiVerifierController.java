package ai.aletheia.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Map;

/**
 * Serves the offline verifier JAR for download (P3.10).
 * GET /api/ai/verifier returns aletheia-verifier.jar when the file is available.
 *
 * <p>JAR path: env AI_ALETHEIA_VERIFIER_JAR_PATH, or default
 * "target/aletheia-verifier.jar" relative to user.dir (build with mvn package -Pverifier).
 */
@RestController
@RequestMapping("/api/ai")
public class AiVerifierController {

    private static final Logger log = LoggerFactory.getLogger(AiVerifierController.class);

    private static final String DEFAULT_JAR_NAME = "aletheia-verifier.jar";
    private static final String DEFAULT_RELATIVE_PATH = "target/" + DEFAULT_JAR_NAME;

    @Operation(summary = "Download verifier JAR", description = "Returns the offline verifier JAR (aletheia-verifier.jar) for Evidence Package verification. 503 if JAR not built.")
    @ApiResponse(responseCode = "200", description = "JAR file")
    @ApiResponse(responseCode = "503", description = "Verifier JAR not available (build with mvn package -Pverifier)")
    @GetMapping(value = "/verifier", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Object> verifier() {
        String path = System.getenv("AI_ALETHEIA_VERIFIER_JAR_PATH");
        if (path == null || path.isBlank()) {
            path = System.getProperty("user.dir") + File.separator + DEFAULT_RELATIVE_PATH.replace("/", File.separator);
        }
        File file = new File(path);
        if (!file.isFile() || !file.canRead()) {
            log.debug("Verifier JAR not found or not readable: {}", path);
            return ResponseEntity.status(503)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "error", "Verifier JAR not available",
                            "message", "Build the verifier with: cd backend && mvn package -Pverifier -DskipTests"
                    ));
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/java-archive"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + DEFAULT_JAR_NAME + "\"")
                .body(resource);
    }
}
