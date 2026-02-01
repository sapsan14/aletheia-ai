package ai.aletheia.crypto;

import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.tsp.TimeStampToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for TimestampService (MockTsaServiceImpl in test profile).
 *
 * <p>Verifies: parseable RFC 3161 token structure, semantic determinism (same input → same
 * genTime/serial/digest), and input validation.
 *
 * <p><b>Why we don't assert byte-identical tokens:</b> RSA PKCS#1 v1.5 signatures use random
 * padding. Each call to sign() produces different bytes, even for identical data. We verify
 * semantic equivalence instead: both tokens parse correctly and contain the same genTime,
 * serialNumber, and messageImprint digest. See docs/en/CRYPTO_REFERENCE.md § RSA signature
 * randomness.
 */
@SpringBootTest
class TimestampServiceTest {

    @Autowired
    private TimestampService timestampService;

    /**
     * Same input → semantically equivalent tokens (same genTime, serial, digest).
     *
     * <p>We do NOT assert byte-identical tokens: RSA signature padding is random (SecureRandom),
     * so token bytes differ between calls. What matters for MOCK_TSA determinism is that both
     * tokens attest the same time, serial, and content digest — i.e. they are logically
     * interchangeable for audit/verification purposes.
     */
    @Test
    void timestamp_sameInput_returnsSemanticallyIdenticalToken() throws Exception {
        byte[] data = "hello".getBytes();
        byte[] token1 = timestampService.timestamp(data);
        byte[] token2 = timestampService.timestamp(data);

        assertThat(token1).isNotEmpty();
        assertThat(token2).isNotEmpty();

        TimeStampToken ts1 = new TimeStampToken(new CMSSignedData(token1));
        TimeStampToken ts2 = new TimeStampToken(new CMSSignedData(token2));

        // Same attested time (MOCK_TSA uses fixed 2026-01-01)
        assertThat(ts1.getTimeStampInfo().getGenTime())
                .isEqualTo(ts2.getTimeStampInfo().getGenTime());

        // Same serial (derived from digest in MockTsaServiceImpl)
        assertThat(ts1.getTimeStampInfo().getSerialNumber())
                .isEqualTo(ts2.getTimeStampInfo().getSerialNumber());

        // Same digest — both tokens attest the same content (SHA-256 of "hello")
        assertThat(ts1.getTimeStampInfo().getMessageImprintDigest())
                .isEqualTo(ts2.getTimeStampInfo().getMessageImprintDigest());
    }

    @Test
    void timestamp_returnsParseableToken() throws Exception {
        byte[] data = "test".getBytes();
        byte[] token = timestampService.timestamp(data);
        assertThat(token).isNotEmpty();

        TimeStampToken tsToken = new TimeStampToken(new CMSSignedData(token));
        assertThat(tsToken.getTimeStampInfo().getGenTime()).isNotNull();
    }

    @Test
    void timestamp_nullInput_throws() {
        assertThatThrownBy(() -> timestampService.timestamp(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    void timestamp_emptyInput_throws() {
        assertThatThrownBy(() -> timestampService.timestamp(new byte[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or empty");
    }
}
