package ai.aletheia.evidence;

import ai.aletheia.policy.PolicyRuleResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Builds an Evidence Package (.aep): the set of files required for offline verification.
 *
 * <p>Format (DP2.1.1): response.txt, canonical.bin, hash.sha256, signature.sig,
 * timestamp.tsr, metadata.json, public_key.pem. Optional PQC (PQC.4): signature_pqc.sig,
 * pqc_public_key.pem, pqc_algorithm.json when PQC was used.
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
            String publicKeyPem,
            Double policyCoverage,
            List<PolicyRuleResult> policyRulesEvaluated);

    /**
     * Build the Evidence Package with optional PQC artifacts (PQC.4). When signaturePqcBytes is non-null,
     * adds signature_pqc.sig, pqc_public_key.pem, pqc_algorithm.json.
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
            String publicKeyPem,
            Double policyCoverage,
            List<PolicyRuleResult> policyRulesEvaluated,
            byte[] signaturePqcBytes,
            String pqcPublicKeyPem,
            String pqcAlgorithmName);

    /**
     * Build the Evidence Package with DP2.4 claim metadata (claim, confidence, policy_version in metadata.json).
     * canonicalBytes must be the combined signed payload (canonical response + canonical claim metadata).
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
            String publicKeyPem,
            String claim,
            Double confidence,
            String policyVersion,
            Double policyCoverage,
            List<PolicyRuleResult> policyRulesEvaluated);

    /**
     * Build with DP2.4 claim metadata and optional PQC artifacts (PQC.4).
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
            String publicKeyPem,
            String claim,
            Double confidence,
            String policyVersion,
            Double policyCoverage,
            List<PolicyRuleResult> policyRulesEvaluated,
            byte[] signaturePqcBytes,
            String pqcPublicKeyPem,
            String pqcAlgorithmName);

    /**
     * Pack the evidence file map into a ZIP (e.g. .aep).
     *
     * @param files map from filename to content (e.g. from {@link #buildPackage})
     * @return ZIP bytes
     */
    byte[] toZip(Map<String, byte[]> files);
}
