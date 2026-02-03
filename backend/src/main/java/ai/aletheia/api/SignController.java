package ai.aletheia.api;

import ai.aletheia.api.dto.ApiErrorResponse;
import ai.aletheia.api.dto.SignRequest;
import ai.aletheia.api.dto.SignResponse;
import ai.aletheia.audit.AuditRecordService;
import ai.aletheia.audit.dto.AuditRecordRequest;
import ai.aletheia.claim.ClaimCanonical;
import ai.aletheia.claim.ComplianceClaim;
import ai.aletheia.claim.ComplianceInferenceService;
import ai.aletheia.crypto.CanonicalizationService;
import ai.aletheia.crypto.HashService;
import ai.aletheia.crypto.PqcSignatureService;
import ai.aletheia.crypto.PqcSignatureServiceImpl;
import ai.aletheia.crypto.SignatureService;
import ai.aletheia.crypto.TimestampException;
import ai.aletheia.crypto.TimestampService;
import ai.aletheia.db.entity.AiResponse;
import ai.aletheia.siem.SiemEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Sign-only endpoint: accepts an external LLM response and signs it without calling any LLM.
 */
@RestController
@RequestMapping("/api")
public class SignController {

    private static final Logger log = LoggerFactory.getLogger(SignController.class);
    private static final String DEFAULT_MODEL_ID = "external";

    private final CanonicalizationService canonicalizationService;
    private final HashService hashService;
    private final SignatureService signatureService;
    private final TimestampService timestampService;
    private final AuditRecordService auditRecordService;
    private final ComplianceInferenceService complianceInferenceService;
    private final PqcSignatureService pqcSignatureService;
    private final SiemEventService siemEventService;

    public SignController(
            CanonicalizationService canonicalizationService,
            HashService hashService,
            SignatureService signatureService,
            TimestampService timestampService,
            AuditRecordService auditRecordService,
            ComplianceInferenceService complianceInferenceService,
            SiemEventService siemEventService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) PqcSignatureService pqcSignatureService) {
        this.canonicalizationService = canonicalizationService;
        this.hashService = hashService;
        this.signatureService = signatureService;
        this.timestampService = timestampService;
        this.auditRecordService = auditRecordService;
        this.complianceInferenceService = complianceInferenceService;
        this.siemEventService = siemEventService;
        this.pqcSignatureService = pqcSignatureService;
    }

    @Operation(
            summary = "Sign-only API",
            description = "Sign a pre-generated LLM response without calling any LLM.")
    @ApiResponse(responseCode = "200", description = "Signed response metadata with id, signature, tsaToken")
    @ApiResponse(responseCode = "400", description = "Missing or empty response")
    @ApiResponse(responseCode = "502", description = "Timestamp service unavailable")
    @ApiResponse(responseCode = "503", description = "Signing failed or key not configured")
    @PostMapping(value = "/sign", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sign(@RequestBody SignRequest request) {
        if (request == null || request.response() == null || request.response().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiErrorResponse.of("VALIDATION_ERROR", "Missing or empty 'response'"));
        }

        String responseText = request.response();
        String prompt = request.prompt() != null ? request.prompt() : "";
        String modelId = request.modelId() != null && !request.modelId().isBlank()
                ? request.modelId().trim()
                : DEFAULT_MODEL_ID;

        try {
            byte[] canonical = canonicalizationService.canonicalize(responseText);
            String canonicalResponse = new String(canonical, StandardCharsets.UTF_8);

            ComplianceClaim compliance = null;
            if (!prompt.isBlank()) {
                compliance = complianceInferenceService.infer(prompt, responseText);
            }

            String claim = null;
            Double confidence = null;
            String policyVersion = null;

            if (compliance != null) {
                claim = compliance.claim();
                confidence = compliance.confidence();
                policyVersion = compliance.policyVersion();
            }

            if (request.policyId() != null && !request.policyId().isBlank()) {
                policyVersion = request.policyId().trim();
            }

            boolean hasClaimMetadata = (claim != null && !claim.isBlank())
                    || (policyVersion != null && !policyVersion.isBlank());

            String responseHash;
            if (hasClaimMetadata) {
                byte[] claimBytes = ClaimCanonical.toCanonicalBytes(claim, confidence, modelId, policyVersion);
                byte[] bytesToSign = new byte[canonical.length + 1 + claimBytes.length];
                System.arraycopy(canonical, 0, bytesToSign, 0, canonical.length);
                bytesToSign[canonical.length] = '\n';
                System.arraycopy(claimBytes, 0, bytesToSign, canonical.length + 1, claimBytes.length);
                responseHash = hashService.hash(bytesToSign);
            } else {
                responseHash = hashService.hash(canonical);
            }

            String signature;
            try {
                signature = signatureService.sign(responseHash);
            } catch (IllegalStateException e) {
                return ResponseEntity.status(503)
                        .body(ApiErrorResponse.of("SIGNING_ERROR", e.getMessage()));
            }

            String tsaToken;
            try {
                byte[] signatureBytes = Base64.getDecoder().decode(signature);
                byte[] token = timestampService.timestamp(signatureBytes);
                tsaToken = Base64.getEncoder().encodeToString(token);
            } catch (TimestampException e) {
                return ResponseEntity.status(502)
                        .body(ApiErrorResponse.of("TIMESTAMP_UNAVAILABLE", e.getMessage()));
            }

            String signaturePqcBase64 = null;
            String pqcPublicKeyPem = null;
            if (pqcSignatureService != null && pqcSignatureService.isAvailable()) {
                try {
                    byte[] hashBytes = PqcSignatureServiceImpl.hashHexToBytes(responseHash);
                    byte[] pqcSig = pqcSignatureService.sign(hashBytes);
                    signaturePqcBase64 = Base64.getEncoder().encodeToString(pqcSig);
                    pqcPublicKeyPem = pqcSignatureService.getPublicKeyPem();
                } catch (Exception e) {
                    log.warn("PQC signing failed, continuing without PQC signature: {}", e.getMessage());
                }
            }

            AuditRecordRequest auditRequest = new AuditRecordRequest(
                    prompt,
                    canonicalResponse,
                    responseHash,
                    signature,
                    signaturePqcBase64,
                    pqcPublicKeyPem,
                    tsaToken,
                    modelId,
                    request.requestId(),
                    null,
                    null,
                    1,
                    claim,
                    confidence,
                    policyVersion
            );
            AiResponse saved = auditRecordService.saveAndReturn(auditRequest);

            siemEventService.emitResponseSigned(
                    saved.getId(),
                    responseHash,
                    policyVersion,
                    modelId
            );

            return ResponseEntity.ok(new SignResponse(
                    saved.getId(),
                    responseHash,
                    signature,
                    tsaToken,
                    claim,
                    confidence,
                    policyVersion,
                    modelId,
                    saved.getCreatedAt()
            ));
        } catch (Exception e) {
            log.error("Sign-only failed", e);
            return ResponseEntity.status(500)
                    .body(ApiErrorResponse.of("INTERNAL_ERROR", "Sign-only processing failed"));
        }
    }
}
