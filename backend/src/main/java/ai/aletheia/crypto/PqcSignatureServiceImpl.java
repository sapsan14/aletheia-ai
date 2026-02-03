package ai.aletheia.crypto;

import ai.aletheia.config.PqcSigningProperties;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumPublicKeyParameters;
import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumSigner;
import org.bouncycastle.pqc.crypto.util.PrivateKeyFactory;
import org.bouncycastle.pqc.crypto.util.PublicKeyFactory;
import org.bouncycastle.pqc.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;

/**
 * ML-DSA (Dilithium) signature service. Signs the same 32-byte SHA-256 hash
 * as the classical path. Active only when {@code ai.aletheia.signing.pqc-enabled=true}
 * and {@code ai.aletheia.signing.pqc-key-path} points to a valid private key PEM.
 *
 * @see PqcSignatureService
 * @see docs/en/PLAN_PQC.md
 */
@Service
@ConditionalOnProperty(name = "ai.aletheia.signing.pqc-enabled", havingValue = "true")
public class PqcSignatureServiceImpl implements PqcSignatureService {

    private static final Logger log = LoggerFactory.getLogger(PqcSignatureServiceImpl.class);
    private static final int SHA256_DIGEST_LENGTH = 32;
    private static final HexFormat HEX = HexFormat.of().withLowerCase();

    /** By convention: same directory as private key, this filename for public key. */
    private static final String DEFAULT_PUBLIC_KEY_FILENAME = "ai_pqc_public.pem";

    private final PqcSigningProperties props;
    private DilithiumPrivateKeyParameters privateKeyParams;
    private DilithiumPublicKeyParameters publicKeyParams;
    /** Cached PEM for getPublicKeyPem (from file or built from publicKeyParams). */
    private String publicKeyPemCache;

    public PqcSignatureServiceImpl(PqcSigningProperties props) {
        this.props = props;
    }

    @PostConstruct
    void loadKeys() {
        String keyPath = props.getPqcKeyPath();
        if (keyPath == null || keyPath.isBlank()) {
            log.warn("PQC enabled but pqc-key-path is empty; PQC signing will be unavailable");
            return;
        }
        Path path = Path.of(keyPath.strip());
        if (!Files.isRegularFile(path)) {
            log.warn("PQC key file not found: {}; PQC signing will be unavailable", path.toAbsolutePath());
            return;
        }
        try {
            try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8);
                 PEMParser parser = new PEMParser(r)) {
                Object obj = parser.readObject();
                if (obj == null) {
                    log.warn("No PEM object in PQC key file: {}", keyPath);
                    return;
                }
                if (obj instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo privateKeyInfo) {
                    CipherParameters priv = PrivateKeyFactory.createKey(privateKeyInfo);
                    if (!(priv instanceof DilithiumPrivateKeyParameters dilithiumPrivate)) {
                        log.warn("PQC key is not Dilithium: {}; PQC signing unavailable", priv.getClass().getSimpleName());
                        return;
                    }
                    this.privateKeyParams = dilithiumPrivate;
                } else {
                    log.warn("Unsupported PEM content in PQC key file: {}", obj.getClass().getName());
                    return;
                }
            }

            // Load public key: same directory, ai_pqc_public.pem
            Path publicPath = path.getParent().resolve(DEFAULT_PUBLIC_KEY_FILENAME);
            if (Files.isRegularFile(publicPath)) {
                try (Reader r = Files.newBufferedReader(publicPath, StandardCharsets.UTF_8);
                     PEMParser parser = new PEMParser(r)) {
                    Object obj = parser.readObject();
                    if (obj instanceof SubjectPublicKeyInfo subjectPublicKeyInfo) {
                        CipherParameters pub = PublicKeyFactory.createKey(subjectPublicKeyInfo);
                        if (pub instanceof DilithiumPublicKeyParameters dilithiumPublic) {
                            this.publicKeyParams = dilithiumPublic;
                            this.publicKeyPemCache = Files.readString(publicPath, StandardCharsets.UTF_8).trim();
                        }
                    }
                }
            }
            if (publicKeyParams == null) {
                // Derive public from private (Dilithium key pair contains both)
                this.publicKeyParams = privateKeyParams.getPublicKeyParameters();
                this.publicKeyPemCache = toPem(SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(publicKeyParams).getEncoded());
            }

            log.info("PQC (ML-DSA Dilithium) signing enabled; key loaded from {}", path.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Failed to load PQC key from {}: {}; PQC signing unavailable", keyPath, e.getMessage());
        }
    }

    private static String toPem(byte[] publicKeyDer) throws IOException {
        StringWriter sw = new StringWriter();
        try (PemWriter w = new PemWriter(sw)) {
            w.writeObject(new PemObject("PUBLIC KEY", publicKeyDer));
        }
        return sw.toString();
    }

    @Override
    public boolean isAvailable() {
        return privateKeyParams != null && publicKeyParams != null;
    }

    @Override
    public byte[] sign(byte[] hashBytes) {
        if (hashBytes == null || hashBytes.length != SHA256_DIGEST_LENGTH) {
            throw new IllegalArgumentException(
                    "Hash must be exactly " + SHA256_DIGEST_LENGTH + " bytes, got " + (hashBytes == null ? "null" : hashBytes.length));
        }
        if (!isAvailable()) {
            throw new IllegalStateException("PQC signing key not loaded; check pqc-enabled and pqc-key-path");
        }
        DilithiumSigner signer = new DilithiumSigner();
        signer.init(true, privateKeyParams);
        return signer.generateSignature(hashBytes);
    }

    @Override
    public boolean verify(byte[] hashBytes, byte[] signatureBytes) {
        if (hashBytes == null || hashBytes.length != SHA256_DIGEST_LENGTH) {
            return false;
        }
        if (signatureBytes == null || signatureBytes.length == 0) {
            return false;
        }
        if (!isAvailable()) {
            return false;
        }
        try {
            DilithiumSigner signer = new DilithiumSigner();
            signer.init(false, publicKeyParams);
            return signer.verifySignature(hashBytes, signatureBytes);
        } catch (Exception e) {
            log.debug("PQC verify failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getPublicKeyPem() {
        if (!isAvailable() || publicKeyPemCache == null) {
            throw new IllegalStateException("PQC key not loaded; cannot export public key");
        }
        return publicKeyPemCache;
    }

    /**
     * Decode 64-char hex hash to 32 bytes (for callers that have hex only).
     */
    public static byte[] hashHexToBytes(String hashHex) {
        if (hashHex == null || hashHex.length() != 64) {
            throw new IllegalArgumentException(
                    "Hash must be 64-character hex string, got " + (hashHex == null ? "null" : "length " + hashHex.length()));
        }
        if (!hashHex.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("Hash must be hexadecimal");
        }
        return HEX.parseHex(hashHex);
    }
}
