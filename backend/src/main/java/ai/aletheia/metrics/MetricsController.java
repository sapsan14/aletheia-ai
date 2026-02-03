package ai.aletheia.metrics;

import ai.aletheia.api.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Phase 4 minimal analytics endpoints.
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Operation(summary = "Record analytics event", description = "Stores a minimal metrics event for Phase 4.")
    @ApiResponse(responseCode = "204", description = "Event recorded")
    @ApiResponse(responseCode = "400", description = "Invalid event name")
    @PostMapping(value = "/event", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> recordEvent(@RequestBody(required = false) MetricEventRequest request) {
        if (request == null || request.event() == null || request.event().isBlank()) {
            return ResponseEntity.badRequest().body(ErrorResponse.badRequest("Missing event name"));
        }
        try {
            metricsService.recordEvent(request.event(), request.responseId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ErrorResponse.badRequest(ex.getMessage()));
        }
    }

    @Operation(summary = "Metrics summary", description = "Returns total counts per event for Phase 4.")
    @ApiResponse(responseCode = "200", description = "Summary map")
    @GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Long>> summary() {
        return ResponseEntity.ok(metricsService.summary());
    }
}
