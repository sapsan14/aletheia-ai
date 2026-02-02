package ai.aletheia.evidence;

import java.time.Instant;
import java.util.Map;

/**
 * Builds an Evidence Package (.aep): the set of files required for offline verification.
 *
 * <p>Format (DP2.1.1): response.txt, canonical.bin, hash.sha256, signature.sig,
 * timestamp.tsr, metadata.json, public_key.pem. All keys are filenames; values are file contents (bytes).
 */
public interface EvidencePackageService {

    /**
     * Build the Evidence Package as a map of (filename, content).
     *
     * @param responseText   raw AI response text (for response.txt)
     * @param canonicalBytes exact bytes used for hashing (for canonical.bin)
     * @param hashHex        64-char SHA-256 hex of canonical bytes (for hash.sha256)
     * @param signatureBytes signature over the hash; may be null (then signature.sig is empty)
     * @param tsaTokenBytes  RFC 3161 TSA token; may be null (then timestamp.tsr is empty)
     * @param model          LLM model id (for metadata.json)
     * @param createdAt      record creation time (for metadata.json, ISO 8601)
     * @param responseId     optional record id (for metadata.json)
     * @param publicKeyPem   PEM of the signing public key (for public_key.pem)
     * @return map with keys: response.txt, canonical.bin, hash.sha256, signature.sig, timestamp.tsr, metadata.json, public_key.pem
     */
    Map<String, byte[]> buildPackage(
            String responseText,
            byte[] canonicalBytes,
            String hashHex,
            byte[] signatureBytes,
            byte[] tsaTokenBytes,
            String model,
            Instant createdAt,
            Long responseId,
            String publicKeyPem);

    /**
     * Pack the evidence file map into a ZIP (e.g. .aep).
     *
     * @param files map from filename to content (e.g. from {@link #buildPackage})
     * @return ZIP bytes
     */
    byte[] toZip(Map<String, byte[]> files);
}
