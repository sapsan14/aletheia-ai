package ai.aletheia.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link SignatureServiceImpl}.
 * Verifies: sign known hash, verify returns true; tampered signature returns false.
 * Uses test PEM key from classpath (see application.properties in test/resources).
 */
@SpringBootTest
@TestPropertySource(properties = "ai.aletheia.signing.key-path=classpath:test-signing-key.pem")
class SignatureServiceTest {

    private static final String KNOWN_HASH_HEX =
            "5891b5b522d5df086d0ff0b110fbd9d21bb4fc7163af34d08286a2e846f6be03"; // SHA-256 of "hello\n"

    @Autowired
    private SignatureService signatureService;

    @Test
    void sign_thenVerify_returnsTrue() {
        String signatureBase64 = signatureService.sign(KNOWN_HASH_HEX);
        assertThat(signatureBase64).isNotBlank();
        assertThat(signatureService.verify(KNOWN_HASH_HEX, signatureBase64)).isTrue();
    }

    @Test
    void sign_bytes_thenVerify_bytes_returnsTrue() {
        byte[] hashBytes = hexToBytes(KNOWN_HASH_HEX);
        byte[] signature = signatureService.sign(hashBytes);
        assertThat(signature).isNotEmpty();
        assertThat(signatureService.verify(hashBytes, signature)).isTrue();
    }

    @Test
    void verify_tamperedSignature_returnsFalse() {
        String signatureBase64 = signatureService.sign(KNOWN_HASH_HEX);
        byte[] decoded = Base64.getDecoder().decode(signatureBase64);
        decoded[decoded.length - 1] ^= 0x01; // flip one byte
        String tampered = Base64.getEncoder().encodeToString(decoded);
        assertThat(signatureService.verify(KNOWN_HASH_HEX, tampered)).isFalse();
    }

    @Test
    void verify_differentHash_returnsFalse() {
        String signatureBase64 = signatureService.sign(KNOWN_HASH_HEX);
        String otherHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"; // empty digest
        assertThat(signatureService.verify(otherHash, signatureBase64)).isFalse();
    }

    @Test
    void sign_nullHash_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> signatureService.sign((String) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("64");
    }

    @Test
    void sign_invalidLengthHex_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> signatureService.sign("abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("64");
    }

    @Test
    void sign_invalidHex_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> signatureService.sign("x".repeat(64)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hex");
    }

    @Test
    void sign_bytes_wrongLength_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> signatureService.sign(new byte[31]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32");
        assertThatThrownBy(() -> signatureService.sign(new byte[33]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32");
    }

    @Test
    void verify_nullOrEmptySignature_returnsFalse() {
        assertThat(signatureService.verify(KNOWN_HASH_HEX, (String) null)).isFalse();
        assertThat(signatureService.verify(KNOWN_HASH_HEX, "")).isFalse();
    }

    @Test
    void verify_hashBytesNull_returnsFalse() {
        byte[] sig = signatureService.sign(hexToBytes(KNOWN_HASH_HEX));
        assertThat(signatureService.verify((byte[]) null, sig)).isFalse();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
