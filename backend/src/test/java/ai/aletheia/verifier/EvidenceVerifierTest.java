package ai.aletheia.verifier;

import ai.aletheia.crypto.CanonicalizationService;
import ai.aletheia.crypto.HashService;
import ai.aletheia.crypto.PqcSignatureService;
import ai.aletheia.crypto.SignatureService;
import ai.aletheia.crypto.TimestampService;
import ai.aletheia.evidence.EvidencePackageService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link EvidenceVerifierImpl}.
 * Valid package → VALID; tampered signature → INVALID; tampered hash → INVALID.
 */
@SpringBootTest
class EvidenceVerifierTest {

    @TempDir
    Path tempDir;

    @Autowired
    private CanonicalizationService canonicalizationService;
    @Autowired
    private HashService hashService;
    @Autowired
    private SignatureService signatureService;
    @Autowired
    private TimestampService timestampService;
    @Autowired
    private EvidencePackageService evidencePackageService;

    @Autowired(required = false)
    private PqcSignatureService pqcSignatureService;

    private EvidenceVerifier verifier;

    @BeforeEach
    void setUp() {
        verifier = new EvidenceVerifierImpl();
    }

    @Test
    void verify_validPackage_returnsValid() throws Exception {
        String responseText = "2+2 equals 4.\n";
        byte[] canonical = canonicalizationService.canonicalize(responseText);
        String hashHex = hashService.hash(canonical);
        String sigBase64 = signatureService.sign(hashHex);
        byte[] sigBytes = Base64.getDecoder().decode(sigBase64);
        byte[] tsaToken = timestampService.timestamp(sigBytes);
        String publicKeyPem = signatureService.getPublicKeyPem();

        Map<String, byte[]> files = evidencePackageService.buildPackage(
                responseText,
                canonical,
                hashHex,
                sigBytes,
                tsaToken,
                "test-model",
                java.time.Instant.now(),
                1L,
                publicKeyPem
        );

        Path dir = tempDir.resolve("valid");
        Files.createDirectories(dir);
        for (Map.Entry<String, byte[]> e : files.entrySet()) {
            Files.write(dir.resolve(e.getKey()), e.getValue());
        }

        VerificationResult result = verifier.verify(dir);

        assertThat(result.valid()).isTrue();
        assertThat(result.report()).anyMatch(s -> s.startsWith("hash: OK"));
        assertThat(result.report()).anyMatch(s -> s.startsWith("signature: OK"));
        assertThat(result.report()).anyMatch(s -> s.startsWith("timestamp:"));
        assertThat(result.failureReason()).isNull();
        // PQC.8: package without PQC reports "not present" and pqcValid is null
        assertThat(result.report()).anyMatch(s -> s.equals("PQC signature: not present"));
        assertThat(result.pqcValid()).isNull();
    }

    @Test
    void verify_tamperedSignature_returnsInvalid() throws Exception {
        String responseText = "2+2 equals 4.\n";
        byte[] canonical = canonicalizationService.canonicalize(responseText);
        String hashHex = hashService.hash(canonical);
        byte[] sigBytes = Base64.getDecoder().decode(signatureService.sign(hashHex));
        byte[] tsaToken = timestampService.timestamp(sigBytes);
        String publicKeyPem = signatureService.getPublicKeyPem();

        Map<String, byte[]> files = evidencePackageService.buildPackage(
                responseText,
                canonical,
                hashHex,
                new byte[] { 1, 2, 3, 4, 5 }, // wrong signature
                tsaToken,
                "test-model",
                java.time.Instant.now(),
                1L,
                publicKeyPem
        );

        Path dir = tempDir.resolve("tampered-sig");
        Files.createDirectories(dir);
        for (Map.Entry<String, byte[]> e : files.entrySet()) {
            Files.write(dir.resolve(e.getKey()), e.getValue());
        }

        VerificationResult result = verifier.verify(dir);

        assertThat(result.valid()).isFalse();
        assertThat(result.failureReason()).isEqualTo("signature invalid");
        assertThat(result.report()).anyMatch(s -> s.contains("signature") && s.contains("INVALID"));
    }

    @Test
    void verify_tamperedHash_returnsInvalid() throws Exception {
        String responseText = "2+2 equals 4.\n";
        byte[] canonical = canonicalizationService.canonicalize(responseText);
        String hashHex = hashService.hash(canonical);
        byte[] sigBytes = Base64.getDecoder().decode(signatureService.sign(hashHex));
        byte[] tsaToken = timestampService.timestamp(sigBytes);
        String publicKeyPem = signatureService.getPublicKeyPem();

        Map<String, byte[]> files = evidencePackageService.buildPackage(
                responseText,
                canonical,
                "f".repeat(64), // wrong hash
                sigBytes,
                tsaToken,
                "test-model",
                java.time.Instant.now(),
                1L,
                publicKeyPem
        );

        Path dir = tempDir.resolve("tampered-hash");
        Files.createDirectories(dir);
        for (Map.Entry<String, byte[]> e : files.entrySet()) {
            Files.write(dir.resolve(e.getKey()), e.getValue());
        }

        VerificationResult result = verifier.verify(dir);

        assertThat(result.valid()).isFalse();
        assertThat(result.failureReason()).isEqualTo("hash mismatch");
        assertThat(result.report()).anyMatch(s -> s.contains("hash") && s.contains("MISMATCH"));
    }

    /** PQC.8: When package contains PQC files and PqcSignatureService is available, verifier reports PQC signature status and pqcValid. */
    @Test
    void verify_packageWithPqc_reportsPqcValid() throws Exception {
        Assumptions.assumeTrue(pqcSignatureService != null && pqcSignatureService.isAvailable(),
                "PqcSignatureService not available (PQC disabled in test); skip");

        String responseText = "2+2 equals 4.\n";
        byte[] canonical = canonicalizationService.canonicalize(responseText);
        String hashHex = hashService.hash(canonical);
        byte[] hashBytes = ai.aletheia.crypto.PqcSignatureServiceImpl.hashHexToBytes(hashHex);
        byte[] pqcSigBytes = pqcSignatureService.sign(hashBytes);
        String pqcPublicKeyPem = pqcSignatureService.getPublicKeyPem();

        String sigBase64 = signatureService.sign(hashHex);
        byte[] sigBytes = Base64.getDecoder().decode(sigBase64);
        byte[] tsaToken = timestampService.timestamp(sigBytes);
        String publicKeyPem = signatureService.getPublicKeyPem();

        Map<String, byte[]> files = evidencePackageService.buildPackage(
                responseText,
                canonical,
                hashHex,
                sigBytes,
                tsaToken,
                "test-model",
                java.time.Instant.now(),
                1L,
                publicKeyPem,
                pqcSigBytes,
                pqcPublicKeyPem,
                "ML-DSA (Dilithium3)"
        );

        Path dir = tempDir.resolve("with-pqc");
        Files.createDirectories(dir);
        for (Map.Entry<String, byte[]> e : files.entrySet()) {
            Files.write(dir.resolve(e.getKey()), e.getValue());
        }

        VerificationResult result = verifier.verify(dir);

        assertThat(result.valid()).isTrue();
        assertThat(result.report()).anyMatch(s -> s.equals("PQC signature: valid"));
        assertThat(result.pqcValid()).isTrue();
    }

    @Test
    void verify_missingHash_returnsInvalid() throws Exception {
        Path dir = tempDir.resolve("missing");
        Files.createDirectories(dir);
        Files.write(dir.resolve("canonical.bin"), "x".getBytes(StandardCharsets.UTF_8));

        VerificationResult result = verifier.verify(dir);

        assertThat(result.valid()).isFalse();
        assertThat(result.failureReason()).contains("hash");
    }
}
