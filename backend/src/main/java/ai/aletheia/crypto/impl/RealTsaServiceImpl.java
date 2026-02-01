package ai.aletheia.crypto.impl;

import ai.aletheia.crypto.TimestampException;
import ai.aletheia.crypto.TimestampService;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

/**
 * Real RFC 3161 TSA client. Sends HTTP POST to configured TSA URL.
 * <p>
 * Requires {@code ai.aletheia.tsa.mode=real} and {@code ai.aletheia.tsa.url} set.
 * Example URLs: DigiCert, Sectigo, GlobalSign, FreeTSA.
 *
 * @see docs/en/TIMESTAMPING.md
 */
@Service
@ConditionalOnProperty(name = "ai.aletheia.tsa.mode", havingValue = "real")
public class RealTsaServiceImpl implements TimestampService {

    private static final Logger log = LoggerFactory.getLogger(RealTsaServiceImpl.class);
    private static final String CONTENT_TYPE_TSQ = "application/timestamp-query";
    private static final String CONTENT_TYPE_TSR = "application/timestamp-reply";
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30);

    private final String tsaUrl;
    private final HttpClient httpClient;

    public RealTsaServiceImpl(@Value("${ai.aletheia.tsa.url:}") String tsaUrl) {
        String url = tsaUrl == null ? "" : tsaUrl.strip();
        if (url.isEmpty()) {
            throw new IllegalStateException(
                    "ai.aletheia.tsa.url must be set when ai.aletheia.tsa.mode=real. " +
                    "Example: http://timestamp.digicert.com");
        }
        this.tsaUrl = url;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(HTTP_TIMEOUT)
                .build();
        log.info("Real TSA client initialized: {}", this.tsaUrl);
    }

    @Override
    public byte[] timestamp(byte[] dataToTimestamp) {
        if (dataToTimestamp == null || dataToTimestamp.length == 0) {
            throw new IllegalArgumentException("Data to timestamp cannot be null or empty");
        }
        byte[] digest = sha256(dataToTimestamp);
        try {
            TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();
            TimeStampRequest request = reqGen.generate(TSPAlgorithms.SHA256, digest);
            byte[] tsqBytes = request.getEncoded();

            HttpRequest httpReq = HttpRequest.newBuilder()
                    .uri(URI.create(tsaUrl))
                    .header("Content-Type", CONTENT_TYPE_TSQ)
                    .timeout(HTTP_TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(tsqBytes))
                    .build();

            HttpResponse<byte[]> httpResp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofByteArray());

            if (httpResp.statusCode() != 200) {
                throw new TimestampException(
                        "TSA returned HTTP " + httpResp.statusCode() + " from " + tsaUrl);
            }

            byte[] tsrBytes = httpResp.body();
            TimeStampResponse response = new TimeStampResponse(tsrBytes);

            if (response.getStatus() != 0) {
                throw new TimestampException(
                        "TSA rejected request: " + response.getStatusString());
            }

            var token = response.getTimeStampToken();
            if (token == null) {
                throw new TimestampException("TSA response has no timestamp token");
            }

            byte[] tokenBytes = token.getEncoded();
            log.debug("Real TSA returned token {} bytes from {}", tokenBytes.length, tsaUrl);
            return tokenBytes;

        } catch (TimestampException e) {
            throw e;
        } catch (IOException e) {
            throw new TimestampException("TSA request failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TimestampException("TSA request interrupted", e);
        } catch (Exception e) {
            throw new TimestampException("Timestamp failed: " + e.getMessage(), e);
        }
    }

    private static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
