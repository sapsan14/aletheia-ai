package ai.aletheia.api;

import ai.aletheia.llm.LLMClient;
import ai.aletheia.llm.LLMException;
import ai.aletheia.llm.LLMResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demo endpoint to test LLM completion. Only available when LLMClient bean exists (OPENAI_API_KEY set).
 *
 * <p>POST /api/llm/demo with {"prompt": "..."} → {"responseText", "modelId"}.
 */
@RestController
@RequestMapping("/api/llm")
@ConditionalOnBean(LLMClient.class)
public class LlmDemoController {

    private final LLMClient llmClient;

    public LlmDemoController(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @Operation(summary = "LLM demo", description = "Test LLM completion only (no persistence). Requires OPENAI_API_KEY.")
    @ApiResponse(responseCode = "200", description = "responseText, modelId")
    @ApiResponse(responseCode = "400", description = "Missing prompt")
    @ApiResponse(responseCode = "502", description = "LLM failed")
    @PostMapping(value = "/demo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> demo(@RequestBody Map<String, String> body) {
        String prompt = body != null ? body.get("prompt") : null;
        if (prompt == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'prompt' field"));
        }
        try {
            LLMResult result = llmClient.complete(prompt);
            var response = new java.util.HashMap<String, Object>();
            response.put("responseText", result.responseText());
            response.put("modelId", result.modelId());
            if (result.temperature() != null) {
                response.put("temperature", result.temperature());
            }
            return ResponseEntity.ok(response);
        } catch (LLMException e) {
            return ResponseEntity.status(502).body(Map.of(
                    "error", "LLM failed",
                    "message", e.getMessage()
            ));
        }
    }
}
