package ai.aletheia.crypto;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * SHA-256 hash using standard {@link MessageDigest}. No BouncyCastle required for hashing.
 * Output is 64-character lowercase hex.
 * <p>
 * Input size is limited to {@value #MAX_CANONICAL_BYTES} bytes to prevent DoS.
 */
@Service
public class HashServiceImpl implements HashService {

    /** Maximum canonical byte array length to prevent DoS. Aligns with canonicalization limit. */
    public static final int MAX_CANONICAL_BYTES = 512 * 1024;

    private static final String SHA_256 = "SHA-256";
    private static final HexFormat HEX_LOWER = HexFormat.of().withLowerCase();

    private final CanonicalizationService canonicalizationService;

    public HashServiceImpl(CanonicalizationService canonicalizationService) {
        this.canonicalizationService = canonicalizationService;
    }

    @Override
    public String hash(byte[] canonicalBytes) {
        byte[] bytes = canonicalBytes != null ? canonicalBytes : new byte[0];
        if (bytes.length > MAX_CANONICAL_BYTES) {
            throw new IllegalArgumentException(
                    "Canonical bytes exceed maximum length: " + bytes.length + " > " + MAX_CANONICAL_BYTES);
        }
        try {
            MessageDigest md = MessageDigest.getInstance(SHA_256);
            byte[] digest = md.digest(bytes);
            return HEX_LOWER.formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(SHA_256 + " not available", e);
        }
    }

    @Override
    public String hashFromString(String input) {
        byte[] canonical = canonicalizationService.canonicalize(input);
        return hash(canonical);
    }
}
