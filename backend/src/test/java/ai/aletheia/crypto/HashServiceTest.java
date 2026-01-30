package ai.aletheia.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link HashServiceImpl}.
 * Verifies: 64-char hex, known SHA-256 for empty and "hello\n", deterministic, hashFromString.
 */
class HashServiceTest {

    private static final String SHA256_EMPTY = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final String SHA256_HELLO_N = "5891b5b522d5df086d0ff0b110fbd9d21bb4fc7163af34d08286a2e846f6be03";

    private HashService hashService;
    private CanonicalizationService canonicalizationService;

    @BeforeEach
    void setUp() {
        canonicalizationService = new CanonicalizationServiceImpl();
        hashService = new HashServiceImpl(canonicalizationService);
    }

    @Test
    void hash_returns64CharLowercaseHex() {
        byte[] input = "hello\n".getBytes(StandardCharsets.UTF_8);
        String result = hashService.hash(input);
        assertThat(result).hasSize(64).matches("[0-9a-f]+");
    }

    @Test
    void hash_knownStringHelloNewline_returnsKnownSha256() {
        byte[] canonicalHelloN = "hello\n".getBytes(StandardCharsets.UTF_8);
        String result = hashService.hash(canonicalHelloN);
        assertThat(result).isEqualTo(SHA256_HELLO_N);
    }

    @Test
    void hash_emptyBytes_returnsKnownEmptyDigest() {
        String result = hashService.hash(new byte[0]);
        assertThat(result).isEqualTo(SHA256_EMPTY);
    }

    @Test
    void hash_null_returnsEmptyDigest() {
        String result = hashService.hash(null);
        assertThat(result).isEqualTo(SHA256_EMPTY);
    }

    @Test
    void hash_sameInputAlwaysSameOutput() {
        byte[] input = "hello\n".getBytes(StandardCharsets.UTF_8);
        String a = hashService.hash(input);
        String b = hashService.hash(input);
        assertThat(a).isEqualTo(b);
    }

    @Test
    void hashFromString_canonicalizesThenHashes() {
        String raw = "hello\n";
        String fromString = hashService.hashFromString(raw);
        byte[] canonical = canonicalizationService.canonicalize(raw);
        String fromBytes = hashService.hash(canonical);
        assertThat(fromString).isEqualTo(fromBytes).isEqualTo(SHA256_HELLO_N);
    }

    @Test
    void hashFromString_nullOrEmpty_returnsEmptyDigest() {
        assertThat(hashService.hashFromString(null)).isEqualTo(SHA256_EMPTY);
        assertThat(hashService.hashFromString("")).isEqualTo(SHA256_EMPTY);
    }

    @Test
    void hash_canonicalBytesExceedingMaxLength_throwsIllegalArgumentException() {
        int max = HashServiceImpl.MAX_CANONICAL_BYTES;
        byte[] tooLong = new byte[max + 1];
        assertThatThrownBy(() -> hashService.hash(tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximum length")
                .hasMessageContaining(String.valueOf(max + 1))
                .hasMessageContaining(String.valueOf(max));
    }

    @Test
    void hash_canonicalBytesAtMaxLength_succeeds() {
        byte[] atLimit = new byte[HashServiceImpl.MAX_CANONICAL_BYTES];
        String result = hashService.hash(atLimit);
        assertThat(result).hasSize(64).matches("[0-9a-f]+");
    }
}
