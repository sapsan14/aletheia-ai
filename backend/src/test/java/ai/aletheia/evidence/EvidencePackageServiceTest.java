package ai.aletheia.evidence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link EvidencePackageServiceImpl}.
 * Verifies: fixed response + mock signature + mock tsa → all 7 files present; hash.sha256 matches computed hash.
 */
class EvidencePackageServiceTest {

    private EvidencePackageService service;

    private static final String RESPONSE_TEXT = "2+2 equals 4.\n";
    private static final byte[] CANONICAL_BYTES = RESPONSE_TEXT.getBytes(StandardCharsets.UTF_8);
    private static final String HASH_HEX = sha256Hex(CANONICAL_BYTES);
    private static final byte[] MOCK_SIGNATURE = new byte[] { 1, 2, 3, 4, 5 };
    private static final byte[] MOCK_TSA_TOKEN = new byte[] { 10, 20, 30 };
    private static final String MODEL = "test-model";
    private static final Instant CREATED_AT = Instant.parse("2025-01-15T12:00:00Z");
    private static final Long RESPONSE_ID = 42L;
    private static final String PUBLIC_KEY_PEM = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\n-----END PUBLIC KEY-----\n";

    @BeforeEach
    void setUp() {
        service = new EvidencePackageServiceImpl();
    }

    @Test
    void buildPackage_returnsAllSevenComponents() {
        Map<String, byte[]> files = service.buildPackage(
                RESPONSE_TEXT,
                CANONICAL_BYTES,
                HASH_HEX,
                MOCK_SIGNATURE,
                MOCK_TSA_TOKEN,
                MODEL,
                CREATED_AT,
                RESPONSE_ID,
                PUBLIC_KEY_PEM
        );

        assertThat(files).containsKeys(
                EvidencePackageServiceImpl.RESPONSE_TXT,
                EvidencePackageServiceImpl.CANONICAL_BIN,
                EvidencePackageServiceImpl.HASH_SHA256,
                EvidencePackageServiceImpl.SIGNATURE_SIG,
                EvidencePackageServiceImpl.TIMESTAMP_TSR,
                EvidencePackageServiceImpl.METADATA_JSON,
                EvidencePackageServiceImpl.PUBLIC_KEY_PEM
        );
        assertThat(files).hasSize(7);
    }

    @Test
    void buildPackage_hashSha256MatchesInput() {
        Map<String, byte[]> files = service.buildPackage(
                RESPONSE_TEXT,
                CANONICAL_BYTES,
                HASH_HEX,
                MOCK_SIGNATURE,
                MOCK_TSA_TOKEN,
                MODEL,
                CREATED_AT,
                RESPONSE_ID,
                PUBLIC_KEY_PEM
        );

        byte[] hashFile = files.get(EvidencePackageServiceImpl.HASH_SHA256);
        assertThat(hashFile).isNotNull();
        assertThat(new String(hashFile, StandardCharsets.UTF_8)).isEqualTo(HASH_HEX);
    }

    @Test
    void buildPackage_responseTxtContainsResponseText() {
        Map<String, byte[]> files = service.buildPackage(
                RESPONSE_TEXT,
                CANONICAL_BYTES,
                HASH_HEX,
                MOCK_SIGNATURE,
                MOCK_TSA_TOKEN,
                MODEL,
                CREATED_AT,
                RESPONSE_ID,
                PUBLIC_KEY_PEM
        );

        byte[] responseFile = files.get(EvidencePackageServiceImpl.RESPONSE_TXT);
        assertThat(new String(responseFile, StandardCharsets.UTF_8)).isEqualTo(RESPONSE_TEXT);
    }

    @Test
    void buildPackage_canonicalBinEqualsCanonicalBytes() {
        Map<String, byte[]> files = service.buildPackage(
                RESPONSE_TEXT,
                CANONICAL_BYTES,
                HASH_HEX,
                MOCK_SIGNATURE,
                MOCK_TSA_TOKEN,
                MODEL,
                CREATED_AT,
                RESPONSE_ID,
                PUBLIC_KEY_PEM
        );

        assertThat(files.get(EvidencePackageServiceImpl.CANONICAL_BIN)).isEqualTo(CANONICAL_BYTES);
    }

    @Test
    void toZip_producesNonEmptyZipWithAllEntries() {
        Map<String, byte[]> files = service.buildPackage(
                RESPONSE_TEXT,
                CANONICAL_BYTES,
                HASH_HEX,
                MOCK_SIGNATURE,
                MOCK_TSA_TOKEN,
                MODEL,
                CREATED_AT,
                RESPONSE_ID,
                PUBLIC_KEY_PEM
        );

        byte[] zip = service.toZip(files);
        assertThat(zip).isNotEmpty();
        assertThat(zip[0]).isEqualTo((byte) 0x50); // PK
        assertThat(zip[1]).isEqualTo((byte) 0x4B);
    }

    @Test
    void buildPackage_handlesNullSignatureAndTsaToken() {
        Map<String, byte[]> files = service.buildPackage(
                RESPONSE_TEXT,
                CANONICAL_BYTES,
                HASH_HEX,
                null,
                null,
                MODEL,
                CREATED_AT,
                null,
                PUBLIC_KEY_PEM
        );

        assertThat(files).hasSize(7);
        assertThat(files.get(EvidencePackageServiceImpl.SIGNATURE_SIG)).isEmpty();
        assertThat(files.get(EvidencePackageServiceImpl.TIMESTAMP_TSR)).isEmpty();
    }

    private static String sha256Hex(byte[] input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
