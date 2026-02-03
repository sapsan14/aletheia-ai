package ai.aletheia.crypto;

/**
 * Post-quantum (PQC) signature service: signs and verifies using ML-DSA (Dilithium)
 * over the same 32-byte SHA-256 hash as the classical path.
 *
 * <p>When enabled via {@link ai.aletheia.config.PqcSigningProperties}, the pipeline
 * produces both a classical RSA signature and a PQC signature over the same hash.
 *
 * @see docs/en/PLAN_PQC.md
 */
public interface PqcSignatureService {

    /**
     * Sign the given 32-byte hash (SHA-256 digest) with the ML-DSA private key.
     *
     * @param hashBytes raw digest, must be 32 bytes
     * @return signature bytes (Base64-encode for storage)
     * @throws IllegalArgumentException if hashBytes is null or length is not 32
     * @throws IllegalStateException if PQC key is not loaded
     */
    byte[] sign(byte[] hashBytes);

    /**
     * Verify a PQC signature against the given hash.
     *
     * @param hashBytes      expected 32-byte digest
     * @param signatureBytes signature to verify
     * @return true if the signature is valid
     */
    boolean verify(byte[] hashBytes, byte[] signatureBytes);

    /**
     * Export the PQC public key as PEM (e.g. for Evidence Package).
     *
     * @return PEM string (-----BEGIN PUBLIC KEY----- ... -----END PUBLIC KEY-----)
     * @throws IllegalStateException if PQC key is not loaded
     */
    String getPublicKeyPem();

    /**
     * Whether this service is available (key loaded and ready to sign).
     *
     * @return true if sign/verify/getPublicKeyPem can be used
     */
    boolean isAvailable();
}
