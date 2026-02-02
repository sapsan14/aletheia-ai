package ai.aletheia.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.aletheia.llm.LLMClient;

import java.util.Map;

/**
 * Fallback for POST /api/ai/ask when {@link LLMClient} is not configured (no OPENAI_API_KEY).
 * Returns 503 so the frontend shows a clear message instead of 404.
 */
@RestController
@RequestMapping("/api/ai")
@ConditionalOnMissingBean(LLMClient.class)
public class AiAskFallbackController {

    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> ask(@RequestBody(required = false) Map<String, ?> body) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "OPENAI_API_KEY not configured",
                        "message", "Set OPENAI_API_KEY on the server (e.g. in .env or -e openai_api_key=sk-...) to enable the AI endpoint."));
    }
}
