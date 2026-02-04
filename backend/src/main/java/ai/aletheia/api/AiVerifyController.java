package ai.aletheia.api;

import ai.aletheia.claim.ClaimCanonical;
import ai.aletheia.api.dto.AiVerifyResponse;
import ai.aletheia.api.dto.ErrorResponse;
import ai.aletheia.crypto.CanonicalizationService;
import ai.aletheia.crypto.HashService;
import ai.aletheia.crypto.SignatureService;
import ai.aletheia.db.AiResponseRepository;
import ai.aletheia.db.entity.AiResponse;
import ai.aletheia.policy.PolicyEvaluationService;
import ai.aletheia.policy.PolicyRuleResult;
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

import java.util.List;

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
    private final PolicyEvaluationService policyEvaluationService;

    public AiVerifyController(
            AiResponseRepository repository,
            CanonicalizationService canonicalizationService,
            HashService hashService,
            SignatureService signatureService,
            PolicyEvaluationService policyEvaluationService) {
        this.repository = repository;
        this.canonicalizationService = canonicalizationService;
        this.hashService = hashService;
        this.signatureService = signatureService;
        this.policyEvaluationService = policyEvaluationService;
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

    private static final String PQC_ALGORITHM = "ML-DSA (Dilithium3)";

    private AiVerifyResponse toResponse(AiResponse e) {
        String computedHash = computeHashValue(e);
        String stored = e.getResponseHash();
        boolean hashMatch = computedHash != null && computedHash.equalsIgnoreCase(stored);
        if (!hashMatch && stored != null && !stored.isBlank()) {
            String legacyHash = computeHashValueLegacy(e);
            if (legacyHash != null && legacyHash.equalsIgnoreCase(stored)) {
                hashMatch = true;
            }
        }
        if (!hashMatch && stored != null && !stored.isBlank()) {
            String responseOnlyHash = computeHashResponseOnly(e);
            if (responseOnlyHash != null && responseOnlyHash.equalsIgnoreCase(stored)) {
                hashMatch = true;
                log.debug("Record id={} verified with response-only hash (claim present but was not included at save time)", e.getId());
            }
        }
        String signatureValid = computeSignatureValid(e);
        String signaturePqc = e.getSignaturePqc() != null && !e.getSignaturePqc().isBlank() ? e.getSignaturePqc() : null;
        String pqcAlgorithm = signaturePqc != null ? PQC_ALGORITHM : null;
        Double policyCoverage = e.getPolicyCoverage();
        List<PolicyRuleResult> policyRules = policyEvaluationService.fromJson(e.getPolicyRulesEvaluated());
        return new AiVerifyResponse(
                e.getId(),
                e.getPrompt(),
                e.getResponse(),
                e.getResponseHash(),
                computedHash,
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
                signatureValid,
                policyCoverage,
                policyRules,
                signaturePqc,
                pqcAlgorithm
        );
    }

    /** Returns the hash recomputed from stored response (and claim if present), or null on error. Uses current format (%.6f for confidence). */
    private String computeHashValue(AiResponse e) {
        return computeHashValueWithClaimFormat(e, true);
    }

    /** Same as computeHashValue but with legacy confidence format (for records saved before %.6f). */
    private String computeHashValueLegacy(AiResponse e) {
        return computeHashValueWithClaimFormat(e, false);
    }

    /** Hash of response only (no claim). For records that were saved with response-only hash but have claim data. */
    private String computeHashResponseOnly(AiResponse e) {
        if (e.getResponseHash() == null || e.getResponseHash().isBlank()) return null;
        try {
            byte[] canonical = canonicalizationService.canonicalize(e.getResponse());
            return hashService.hash(canonical);
        } catch (Exception ex) {
            log.warn("Hash (response-only) computation failed for id={}: {}", e.getId(), ex.getMessage());
            return null;
        }
    }

    private String computeHashValueWithClaimFormat(AiResponse e, boolean useFixedFormat) {
        String stored = e.getResponseHash();
        if (stored == null || stored.isBlank()) return null;
        try {
            byte[] canonical = canonicalizationService.canonicalize(e.getResponse());
            boolean hasClaim = (e.getClaim() != null && !e.getClaim().isBlank())
                    || (e.getPolicyVersion() != null && !e.getPolicyVersion().isBlank());
            if (hasClaim) {
                byte[] claimBytes = useFixedFormat
                        ? ClaimCanonical.toCanonicalBytes(e.getClaim(), e.getConfidence(), e.getLlmModel(), e.getPolicyVersion())
                        : ClaimCanonical.toCanonicalBytesLegacy(e.getClaim(), e.getConfidence(), e.getLlmModel(), e.getPolicyVersion());
                byte[] bytesToSign = new byte[canonical.length + 1 + claimBytes.length];
                System.arraycopy(canonical, 0, bytesToSign, 0, canonical.length);
                bytesToSign[canonical.length] = '\n';
                System.arraycopy(claimBytes, 0, bytesToSign, canonical.length + 1, claimBytes.length);
                return hashService.hash(bytesToSign);
            }
            return hashService.hash(canonical);
        } catch (Exception ex) {
            log.warn("Hash computation failed for id={}: {}", e.getId(), ex.getMessage());
            return null;
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
