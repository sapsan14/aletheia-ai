package ai.aletheia.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health/readiness endpoint for the Aletheia AI backend.
 */
@RestController
public class HealthController {

    @Operation(summary = "Health check", description = "Returns 200 when the backend is up")
    @ApiResponse(responseCode = "200", description = "Backend is running")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
