package ai.aletheia.crypto;

/**
 * Signs a hash (e.g. SHA-256 hex from {@link HashService}) and verifies signatures.
 * Uses RSA with SHA-256; private key is loaded from PEM (file or env). One key per PoC; key rotation out of scope.
 */
public interface SignatureService {

    /**
     * Sign the given hash (64-char hex, e.g. from {@link HashService#hash(byte[])}).
     *
     * @param hashHex 64-character lowercase hex string (SHA-256 digest)
     * @return signature as Base64-encoded string
     * @throws IllegalArgumentException if hashHex is null, not 64 chars, or not valid hex
     */
    String sign(String hashHex);

    /**
     * Sign the given hash bytes (e.g. 32 bytes SHA-256 digest).
     *
     * @param hashBytes raw digest bytes; must be 32 bytes
     * @return signature bytes
     * @throws IllegalArgumentException if hashBytes is null or length is not 32
     */
    byte[] sign(byte[] hashBytes);

    /**
     * Verify a signature against the given hash.
     *
     * @param hashHex        64-character lowercase hex string (same format as for sign)
     * @param signatureBase64 Base64-encoded signature (as returned by {@link #sign(String)})
     * @return true if the signature is valid for this hash
     */
    boolean verify(String hashHex, String signatureBase64);

    /**
     * Verify raw signature bytes against the given hash bytes.
     *
     * @param hashBytes     expected digest (32 bytes)
     * @param signatureBytes signature bytes
     * @return true if the signature is valid
     */
    boolean verify(byte[] hashBytes, byte[] signatureBytes);

    /**
     * Export the public key used for signing as PEM (e.g. for Evidence Package).
     *
     * @return PEM string (-----BEGIN PUBLIC KEY----- ... -----END PUBLIC KEY-----)
     * @throws IllegalStateException if signing key is not configured
     */
    String getPublicKeyPem();
}
