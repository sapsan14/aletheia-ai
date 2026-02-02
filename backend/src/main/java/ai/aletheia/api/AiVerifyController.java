package ai.aletheia.api;

import ai.aletheia.claim.ClaimCanonical;
import ai.aletheia.api.dto.AiVerifyResponse;
import ai.aletheia.api.dto.ErrorResponse;
import ai.aletheia.crypto.CanonicalizationService;
import ai.aletheia.crypto.HashService;
import ai.aletheia.crypto.SignatureService;
import ai.aletheia.db.AiResponseRepository;
import ai.aletheia.db.entity.AiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Returns stored audit record by id for the verification page.
 *
 * <p>Includes hashMatch and signatureValid: backend recomputes hash and verifies
 * signature. Returns 404 with JSON body when id not found.
 */
@RestController
@RequestMapping("/api/ai")
public class AiVerifyController {

    private static final Logger log = LoggerFactory.getLogger(AiVerifyController.class);

    private final AiResponseRepository repository;
    private final CanonicalizationService canonicalizationService;
    private final HashService hashService;
    private final SignatureService signatureService;

    public AiVerifyController(
            AiResponseRepository repository,
            CanonicalizationService canonicalizationService,
            HashService hashService,
            SignatureService signatureService) {
        this.repository = repository;
        this.canonicalizationService = canonicalizationService;
        this.hashService = hashService;
        this.signatureService = signatureService;
    }

    @Operation(summary = "Verify record", description = "Fetch stored record by id with hashMatch and signatureValid")
    @ApiResponse(responseCode = "200", description = "Full record for verification page")
    @ApiResponse(responseCode = "404", description = "Record not found")
    @GetMapping(value = "/verify/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> verify(@PathVariable Long id) {
        var opt = repository.findById(id);
        if (opt.isPresent()) {
            return ResponseEntity.ok(toResponse(opt.get()));
        }
        return ResponseEntity.status(404).body(ErrorResponse.notFound("Record not found", id));
    }

    private AiVerifyResponse toResponse(AiResponse e) {
        boolean hashMatch = computeHashMatch(e);
        String signatureValid = computeSignatureValid(e);
        return new AiVerifyResponse(
                e.getId(),
                e.getPrompt(),
                e.getResponse(),
                e.getResponseHash(),
                e.getSignature(),
                e.getTsaToken(),
                e.getLlmModel(),
                e.getCreatedAt(),
                e.getRequestId(),
                e.getTemperature(),
                e.getSystemPrompt(),
                e.getVersion(),
                e.getClaim(),
                e.getConfidence(),
                e.getPolicyVersion(),
                hashMatch,
                signatureValid
        );
    }

    private boolean computeHashMatch(AiResponse e) {
        String stored = e.getResponseHash();
        if (stored == null || stored.isBlank()) return false;
        try {
            byte[] canonical = canonicalizationService.canonicalize(e.getResponse());
            String computed;
            boolean hasClaim = (e.getClaim() != null && !e.getClaim().isBlank())
                    || (e.getPolicyVersion() != null && !e.getPolicyVersion().isBlank());
            if (hasClaim) {
                byte[] claimBytes = ClaimCanonical.toCanonicalBytes(
                        e.getClaim(), e.getConfidence(), e.getLlmModel(), e.getPolicyVersion());
                byte[] bytesToSign = new byte[canonical.length + 1 + claimBytes.length];
                System.arraycopy(canonical, 0, bytesToSign, 0, canonical.length);
                bytesToSign[canonical.length] = '\n';
                System.arraycopy(claimBytes, 0, bytesToSign, canonical.length + 1, claimBytes.length);
                computed = hashService.hash(bytesToSign);
            } else {
                computed = hashService.hash(canonical);
            }
            return computed.equalsIgnoreCase(stored);
        } catch (Exception ex) {
            log.warn("Hash verification failed for id={}: {}", e.getId(), ex.getMessage());
            return false;
        }
    }

    private String computeSignatureValid(AiResponse e) {
        String sig = e.getSignature();
        String hash = e.getResponseHash();
        if (sig == null || sig.isBlank() || hash == null || hash.isBlank()) {
            return "n_a";
        }
        try {
            return signatureService.verify(hash, sig) ? "valid" : "invalid";
        } catch (IllegalStateException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Signing key not configured")) {
                return "n_a";
            }
            throw ex;
        } catch (Exception ex) {
            log.warn("Signature verification failed for id={}: {}", e.getId(), ex.getMessage());
            return "invalid";
        }
    }
}
