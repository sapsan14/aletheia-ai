package ai.aletheia.api;

import ai.aletheia.api.dto.ErrorResponse;
import ai.aletheia.claim.ClaimCanonical;
import ai.aletheia.crypto.CanonicalizationService;
import ai.aletheia.crypto.SignatureService;
import ai.aletheia.db.AiResponseRepository;
import ai.aletheia.db.entity.AiResponse;
import ai.aletheia.evidence.EvidencePackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Evidence Package endpoint: GET /api/ai/evidence/:id returns the .aep (ZIP) or JSON with base64 file contents.
 *
 * <p>Loads the stored response by id, recomputes canonical bytes, gathers signature and TSA token from DB,
 * loads public key, builds the Evidence Package (DP2.1.1), and returns either ZIP or JSON.
 */
@RestController
@RequestMapping("/api/ai")
public class AiEvidenceController {

    private static final Logger log = LoggerFactory.getLogger(AiEvidenceController.class);

    private final AiResponseRepository repository;
    private final CanonicalizationService canonicalizationService;
    private final SignatureService signatureService;
    private final EvidencePackageService evidencePackageService;

    public AiEvidenceController(
            AiResponseRepository repository,
            CanonicalizationService canonicalizationService,
            SignatureService signatureService,
            EvidencePackageService evidencePackageService) {
        this.repository = repository;
        this.canonicalizationService = canonicalizationService;
        this.signatureService = signatureService;
        this.evidencePackageService = evidencePackageService;
    }

    @Operation(summary = "Evidence Package", description = "Build and return Evidence Package (.aep) for a stored response by id. Returns ZIP or JSON with base64 file contents.")
    @ApiResponse(responseCode = "200", description = "ZIP (application/zip) or JSON with base64 files")
    @ApiResponse(responseCode = "404", description = "Record not found")
    @ApiResponse(responseCode = "503", description = "Signing key not configured")
    @GetMapping(value = "/evidence/{id}", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Object> evidence(
            @PathVariable Long id,
            @RequestParam(name = "format", required = false) String format) {

        Optional<AiResponse> opt = repository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ErrorResponse.notFound("Record not found", id));
        }
        AiResponse entity = opt.get();

        String publicKeyPem;
        try {
            publicKeyPem = signatureService.getPublicKeyPem();
        } catch (IllegalStateException e) {
            log.warn("Evidence package requested but signing key not configured: {}", e.getMessage());
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Signing key not configured",
                    "message", "Evidence Package requires a configured signing key to include public_key.pem."
            ));
        }

        byte[] responseCanonical = canonicalizationService.canonicalize(entity.getResponse());
        byte[] canonicalBytes;
        String claim = entity.getClaim();
        Double confidence = entity.getConfidence();
        String policyVersion = entity.getPolicyVersion();

        if (claim != null || policyVersion != null) {
            byte[] claimBytes = ClaimCanonical.toCanonicalBytes(
                    claim, confidence, entity.getLlmModel(), policyVersion);
            canonicalBytes = new byte[responseCanonical.length + 1 + claimBytes.length];
            System.arraycopy(responseCanonical, 0, canonicalBytes, 0, responseCanonical.length);
            canonicalBytes[responseCanonical.length] = '\n';
            System.arraycopy(claimBytes, 0, canonicalBytes, responseCanonical.length + 1, claimBytes.length);
        } else {
            canonicalBytes = responseCanonical;
        }

        byte[] signatureBytes = entity.getSignature() != null && !entity.getSignature().isBlank()
                ? Base64.getDecoder().decode(entity.getSignature())
                : null;
        byte[] tsaTokenBytes = entity.getTsaToken() != null && !entity.getTsaToken().isBlank()
                ? Base64.getDecoder().decode(entity.getTsaToken())
                : null;

        Map<String, byte[]> files = (claim != null || policyVersion != null)
                ? evidencePackageService.buildPackage(
                entity.getResponse(),
                canonicalBytes,
                entity.getResponseHash(),
                signatureBytes,
                tsaTokenBytes,
                entity.getLlmModel(),
                entity.getCreatedAt(),
                entity.getId(),
                publicKeyPem,
                claim,
                confidence,
                policyVersion)
                : evidencePackageService.buildPackage(
                entity.getResponse(),
                canonicalBytes,
                entity.getResponseHash(),
                signatureBytes,
                tsaTokenBytes,
                entity.getLlmModel(),
                entity.getCreatedAt(),
                entity.getId(),
                publicKeyPem
        );

        boolean wantJson = "json".equalsIgnoreCase(format);

        if (wantJson) {
            Map<String, String> json = new LinkedHashMap<>();
            files.forEach((name, content) -> json.put(name, Base64.getEncoder().encodeToString(content)));
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        }

        byte[] zip = evidencePackageService.toZip(files);
        String filename = "aletheia-evidence-" + id + ".aep";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(zip);
    }
}
