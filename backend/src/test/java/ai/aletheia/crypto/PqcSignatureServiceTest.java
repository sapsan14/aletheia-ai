package ai.aletheia.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Files;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PqcSignatureService}.
 * Verifies: sign(32-byte hash) returns non-empty bytes; verify(sameHash, signature) returns true;
 * tampered signature returns false. Uses test PQC key from fixtures/pqc/.
 */
@SpringBootTest
class PqcSignatureServiceTest {

    /** 32-byte SHA-256 digest (arbitrary). */
    private static final byte[] HASH_32 = HexFormat.of().parseHex(
            "5891b5b522d5df086d0ff0b110fbd9d21bb4fc7163af34d08286a2e846f6be00");

    @Autowired(required = false)
    private PqcSignatureService pqcSignatureService;

    @DynamicPropertySource
    static void pqcProperties(DynamicPropertyRegistry registry) {
        try {
            String path = new ClassPathResource("fixtures/pqc/ai_pqc.key").getFile().getAbsolutePath();
            if (Files.exists(java.nio.file.Path.of(path))) {
                registry.add("ai.aletheia.signing.pqc-enabled", () -> "true");
                registry.add("ai.aletheia.signing.pqc-key-path", () -> path);
            }
        } catch (Exception ignored) {
            // No PQC key in classpath (e.g. clean clone); tests that need it will be skipped
        }
    }

    @Test
    void sign_returnsNonEmpty_verify_returnsTrue() {
        if (pqcSignatureService == null || !pqcSignatureService.isAvailable()) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "PQC key not available (generate with PqcKeyGen into src/test/resources/fixtures/pqc)");
        }
        byte[] signature = pqcSignatureService.sign(HASH_32);
        assertThat(signature).isNotEmpty();
        assertThat(pqcSignatureService.verify(HASH_32, signature)).isTrue();
    }

    @Test
    void verify_tamperedSignature_returnsFalse() {
        if (pqcSignatureService == null || !pqcSignatureService.isAvailable()) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "PQC key not available");
        }
        byte[] signature = pqcSignatureService.sign(HASH_32);
        signature[signature.length - 1] ^= 0x01;
        assertThat(pqcSignatureService.verify(HASH_32, signature)).isFalse();
    }

    @Test
    void verify_wrongHash_returnsFalse() {
        if (pqcSignatureService == null || !pqcSignatureService.isAvailable()) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "PQC key not available");
        }
        byte[] signature = pqcSignatureService.sign(HASH_32);
        byte[] otherHash = HASH_32.clone();
        otherHash[0] ^= 0x01;
        assertThat(pqcSignatureService.verify(otherHash, signature)).isFalse();
    }

    @Test
    void getPublicKeyPem_returnsPemString() {
        if (pqcSignatureService == null || !pqcSignatureService.isAvailable()) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "PQC key not available");
        }
        String pem = pqcSignatureService.getPublicKeyPem();
        assertThat(pem).contains("-----BEGIN PUBLIC KEY-----");
        assertThat(pem).contains("-----END PUBLIC KEY-----");
    }

    @Test
    void sign_wrongHashLength_throws() {
        if (pqcSignatureService == null || !pqcSignatureService.isAvailable()) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "PQC key not available");
        }
        assertThatThrownBy(() -> pqcSignatureService.sign(new byte[31]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> pqcSignatureService.sign(new byte[33]))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
