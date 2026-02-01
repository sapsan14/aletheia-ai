package ai.aletheia.api;

import ai.aletheia.api.dto.AiAskRequest;
import ai.aletheia.api.dto.AiAskResponse;
import ai.aletheia.audit.AuditRecordService;
import ai.aletheia.audit.dto.AuditRecordRequest;
import ai.aletheia.crypto.CanonicalizationService;
import ai.aletheia.crypto.HashService;
import ai.aletheia.crypto.SignatureService;
import ai.aletheia.crypto.TimestampException;
import ai.aletheia.crypto.TimestampService;
import ai.aletheia.llm.LLMClient;
import ai.aletheia.llm.LLMException;
import ai.aletheia.llm.LLMResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Map;

/**
 * Main AI endpoint: prompt → LLM → canonicalize → hash → sign → timestamp → store.
 *
 * <p>Requires {@link LLMClient} bean (e.g. OPENAI_API_KEY set).
 * Returns verifiable response with id for GET /api/ai/verify/:id.
 */
@RestController
@RequestMapping("/api/ai")
@ConditionalOnBean(LLMClient.class)
public class AiAskController {

    private static final Logger log = LoggerFactory.getLogger(AiAskController.class);

    private final LLMClient llmClient;
    private final CanonicalizationService canonicalizationService;
    private final HashService hashService;
    private final SignatureService signatureService;
    private final TimestampService timestampService;
    private final AuditRecordService auditRecordService;

    public AiAskController(
            LLMClient llmClient,
            CanonicalizationService canonicalizationService,
            HashService hashService,
            SignatureService signatureService,
            TimestampService timestampService,
            AuditRecordService auditRecordService) {
        this.llmClient = llmClient;
        this.canonicalizationService = canonicalizationService;
        this.hashService = hashService;
        this.signatureService = signatureService;
        this.timestampService = timestampService;
        this.auditRecordService = auditRecordService;
    }

    @Operation(summary = "Ask AI", description = "Full flow: prompt → LLM → canonicalize → hash → sign → timestamp → save. Requires OPENAI_API_KEY.")
    @ApiResponse(responseCode = "200", description = "Verifiable response with id, hash, signature, tsaToken")
    @ApiResponse(responseCode = "400", description = "Missing or empty prompt")
    @ApiResponse(responseCode = "502", description = "LLM failed")
    @ApiResponse(responseCode = "503", description = "Processing failed")
    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> ask(@RequestBody AiAskRequest request) {
        if (request == null || request.prompt() == null || request.prompt().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing or empty 'prompt'"));
        }

        try {
            LLMResult llmResult = llmClient.complete(request.prompt());
            String responseText = llmResult.responseText();
            String modelId = llmResult.modelId();

            byte[] canonical = canonicalizationService.canonicalize(responseText);
            String responseHash = hashService.hash(canonical);
            String canonicalResponse = new String(canonical, java.nio.charset.StandardCharsets.UTF_8);

            String signature = null;
            try {
                signature = signatureService.sign(responseHash);
            } catch (IllegalStateException e) {
                if (e.getMessage() == null || !e.getMessage().contains("Signing key not configured")) {
                    throw e;
                }
                log.debug("Signing key not configured, proceeding without signature");
            }

            String tsaToken = null;
            if (signature != null) {
                try {
                    byte[] signatureBytes = Base64.getDecoder().decode(signature);
                    byte[] token = timestampService.timestamp(signatureBytes);
                    tsaToken = Base64.getEncoder().encodeToString(token);
                } catch (TimestampException e) {
                    log.warn("TSA failed, saving without tsaToken: {}", e.getMessage());
                }
            }

            AuditRecordRequest auditRequest = new AuditRecordRequest(
                    request.prompt(),
                    canonicalResponse,
                    responseHash,
                    signature,
                    tsaToken,
                    modelId,
                    null,
                    llmResult.temperature(),
                    null,
                    1
            );
            Long id = auditRecordService.save(auditRequest);

            log.info("AI ask: id={}, model={}, promptLen={}, responseLen={}, temperature={}",
                    id, modelId, request.prompt().length(), responseText.length(), llmResult.temperature());

            return ResponseEntity.ok(new AiAskResponse(
                    canonicalResponse,
                    responseHash,
                    signature,
                    tsaToken,
                    id,
                    modelId
            ));
        } catch (LLMException e) {
            log.warn("LLM failed: {}", e.getMessage());
            return ResponseEntity.status(502).body(Map.of(
                    "error", "LLM failed",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("AI ask failed", e);
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Processing failed",
                    "message", e.getMessage()
            ));
        }
    }
}
