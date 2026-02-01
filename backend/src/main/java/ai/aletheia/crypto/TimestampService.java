package ai.aletheia.crypto;

/**
 * RFC 3161 timestamp service. Requests a timestamp token from a TSA (Time-Stamp Authority).
 * <p>
 * In the pipeline: AI text → hash → sign(hash) → <b>timestamp(signature)</b>. The TSA attests
 * that the given data existed at a specific time.
 * <p>
 * Two modes (see {@link ai.aletheia.config.TimestampConfig}):
 * <ul>
 *   <li><b>mock</b> — deterministic local implementation, no network (for tests and dev)</li>
 *   <li><b>real</b> — HTTP POST to configured TSA URL (DigiCert, Sectigo, etc.)</li>
 * </ul>
 *
 * @see docs/en/TIMESTAMPING.md
 * @see docs/en/MOCK_TSA.md
 */
public interface TimestampService {

    /**
     * Request an RFC 3161 timestamp for the given data.
     * <p>
     * In the main pipeline, the input is <b>signature bytes</b> (output of SignatureService.sign).
     * The service will SHA-256 the input and send that digest to the TSA.
     *
     * @param dataToTimestamp bytes to be timestamped (typically signature bytes)
     * @return timestamp token as opaque bytes (ASN.1 DER); store as byte[] or Base64
     * @throws TimestampException if TSA is unreachable, returns error, or token is invalid
     */
    byte[] timestamp(byte[] dataToTimestamp);
}
