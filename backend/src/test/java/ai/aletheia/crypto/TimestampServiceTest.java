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
 * Verifies: deterministic token, parseable structure, same input → same output.
 */
@SpringBootTest
class TimestampServiceTest {

    @Autowired
    private TimestampService timestampService;

    @Test
    void timestamp_sameInput_returnsIdenticalToken() {
        byte[] data = "hello".getBytes();
        byte[] token1 = timestampService.timestamp(data);
        byte[] token2 = timestampService.timestamp(data);
        assertThat(token1).isEqualTo(token2);
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
