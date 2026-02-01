package ai.aletheia.api;

import ai.aletheia.api.dto.CryptoDemoRequest;
import ai.aletheia.api.dto.CryptoDemoResponse;
import ai.aletheia.crypto.CanonicalizationService;
import ai.aletheia.crypto.HashService;
import ai.aletheia.crypto.SignatureService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

/**
 * Demo endpoint that exposes the crypto pipeline: canonicalize → hash → sign.
 * Used for manual verification of the pipeline without LLM or TSA.
 * <p>
 * Works without signing key: returns hash; signature is null when key not configured.
 */
@RestController
@RequestMapping("/api/crypto")
public class CryptoDemoController {

    private static final String STATUS_SIGNED = "SIGNED";
    private static final String STATUS_KEY_NOT_CONFIGURED = "KEY_NOT_CONFIGURED";

    private final CanonicalizationService canonicalizationService;
    private final HashService hashService;
    private final SignatureService signatureService;

    public CryptoDemoController(
            CanonicalizationService canonicalizationService,
            HashService hashService,
            SignatureService signatureService) {
        this.canonicalizationService = canonicalizationService;
        this.hashService = hashService;
        this.signatureService = signatureService;
    }

    @PostMapping(value = "/demo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CryptoDemoResponse> demo(@RequestBody CryptoDemoRequest request) {
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
            if (e.getMessage() != null && e.getMessage().contains("Signing key not configured")) {
                // Expected when key path not set
            } else {
                throw e;
            }
        }

        String canonicalBase64 = Base64.getEncoder().encodeToString(canonical);
        return ResponseEntity.ok(new CryptoDemoResponse(
                request.text(),
                canonicalBase64,
                hash,
                signature,
                signatureStatus
        ));
    }
}
