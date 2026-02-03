package ai.aletheia.api;

import ai.aletheia.api.dto.AuditDemoRequest;
import ai.aletheia.api.dto.AuditDemoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ai.aletheia.audit.AuditRecordService;
import ai.aletheia.audit.dto.AuditRecordRequest;
import ai.aletheia.crypto.CanonicalizationService;
import ai.aletheia.crypto.HashService;
import ai.aletheia.crypto.PqcSignatureService;
import ai.aletheia.crypto.PqcSignatureServiceImpl;
import ai.aletheia.crypto.SignatureService;
import ai.aletheia.crypto.TimestampException;
import ai.aletheia.crypto.TimestampService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

/**
 * Demo endpoint: crypto pipeline + save to DB.
 *
 * <p>Accepts text, runs canonicalize → hash → sign → timestamp (same as crypto demo),
 * then persists the result. Returns the saved record id. Use for tangible testing
 * without LLM — verify in H2 console or via GET /api/ai/verify/:id.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditDemoController {

    private static final String STATUS_SIGNED = "SIGNED";
    private static final String STATUS_KEY_NOT_CONFIGURED = "KEY_NOT_CONFIGURED";
    private static final String TSA_MOCK = "MOCK_TSA";
    private static final String TSA_REAL = "REAL_TSA";
    private static final String TSA_NO_SIGNATURE = "NO_SIGNATURE";
    private static final String TSA_ERROR = "TSA_ERROR";
    private static final String LLM_MODEL_DEMO = "audit-demo";

    private final CanonicalizationService canonicalizationService;
    private final HashService hashService;
    private final SignatureService signatureService;
    private final TimestampService timestampService;
    private final AuditRecordService auditRecordService;
    private final PqcSignatureService pqcSignatureService;

    public AuditDemoController(
            CanonicalizationService canonicalizationService,
            HashService hashService,
            SignatureService signatureService,
            TimestampService timestampService,
            AuditRecordService auditRecordService,
            @org.springframework.beans.factory.annotation.Autowired(required = false) PqcSignatureService pqcSignatureService) {
        this.canonicalizationService = canonicalizationService;
        this.hashService = hashService;
        this.signatureService = signatureService;
        this.timestampService = timestampService;
        this.auditRecordService = auditRecordService;
        this.pqcSignatureService = pqcSignatureService;
    }

    @Operation(summary = "Audit demo", description = "Crypto pipeline + save to DB. Test flow without LLM.")
    @ApiResponse(responseCode = "200", description = "Saved record with id, hash, signature, tsaToken")
    @ApiResponse(responseCode = "400", description = "Missing or null text")
    @PostMapping(value = "/demo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuditDemoResponse> demo(@RequestBody AuditDemoRequest request) {
        if (request == null || request.text() == null) {
            return ResponseEntity.badRequest().build();
        }

        byte[] canonical = canonicalizationService.canonicalize(request.text());
        String hash = hashService.hash(canonical);

        String signature = null;
        String signatureStatus = STATUS_KEY_NOT_CONFIGURED;
        try {
            signature = signatureService.sign(hash);
            signatureStatus = STATUS_SIGNED;
        } catch (IllegalStateException e) {
            if (e.getMessage() == null || !e.getMessage().contains("Signing key not configured")) {
                throw e;
            }
        }

        String tsaToken = null;
        String tsaStatus = TSA_NO_SIGNATURE;
        if (signature != null) {
            try {
                byte[] signatureBytes = Base64.getDecoder().decode(signature);
                byte[] token = timestampService.timestamp(signatureBytes);
                tsaToken = Base64.getEncoder().encodeToString(token);
                tsaStatus = timestampService.getClass().getSimpleName().contains("Mock") ? TSA_MOCK : TSA_REAL;
        } catch (TimestampException e) {
            tsaStatus = TSA_ERROR;
        }
        }

        String signaturePqcBase64 = null;
        if (signature != null && pqcSignatureService != null && pqcSignatureService.isAvailable()) {
            try {
                byte[] hashBytes = PqcSignatureServiceImpl.hashHexToBytes(hash);
                byte[] pqcSig = pqcSignatureService.sign(hashBytes);
                signaturePqcBase64 = Base64.getEncoder().encodeToString(pqcSig);
            } catch (Exception e) {
                // log and continue without PQC
            }
        }
        AuditRecordRequest auditRequest = new AuditRecordRequest(
                request.text(),
                request.text(),
                hash,
                signature,
                signaturePqcBase64,
                tsaToken,
                LLM_MODEL_DEMO,
                null,
                null,
                null,
                1,
                null,
                null,
                null
        );
        Long id = auditRecordService.save(auditRequest);

        String canonicalBase64 = Base64.getEncoder().encodeToString(canonical);
        return ResponseEntity.ok(new AuditDemoResponse(
                id,
                request.text(),
                canonicalBase64,
                hash,
                signature,
                signatureStatus,
                tsaToken,
                tsaStatus
        ));
    }
}
