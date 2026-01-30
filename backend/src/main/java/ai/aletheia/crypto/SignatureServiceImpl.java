package ai.aletheia.crypto;

import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.HexFormat;

/**
 * RSA signature over a SHA-256 hash using PKCS#1 v1.5 (DigestInfo).
 * Private key is loaded from PEM file; public key is derived from private for verification.
 * <p>
 * Key generation (document for operators):
 * <pre>
 * openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
 * </pre>
 * Or PKCS#8 from existing key: {@code openssl pkcs8 -topk8 -inform PEM -in ai.key -out ai-pkcs8.key -nocrypt}
 */
@Service
public class SignatureServiceImpl implements SignatureService {

    private static final String SIGNATURE_ALG = "NONEwithRSA";
    private static final String PROVIDER = "BC";
    private static final int SHA256_DIGEST_LENGTH = 32;
    private static final HexFormat HEX = HexFormat.of().withLowerCase();

    static {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private final String keyPath;
    private final ResourceLoader resourceLoader;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Autowired
    public SignatureServiceImpl(
            @Value("${ai.aletheia.signing.key-path:}") String keyPath,
            ResourceLoader resourceLoader) {
        this.keyPath = keyPath == null ? "" : keyPath.strip();
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    void loadKeys() {
        if (keyPath == null || keyPath.isEmpty()) {
            return; // key optional at startup; sign/verify will fail with clear message when called
        }
        try {
            Reader reader;
            if (keyPath.strip().startsWith("classpath:")) {
                Resource resource = resourceLoader.getResource(keyPath.strip());
                if (!resource.exists()) {
                    throw new IllegalStateException("Signing key resource not found: " + keyPath);
                }
                reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            } else {
                Path path = Path.of(keyPath);
                if (!Files.isRegularFile(path)) {
                    throw new IllegalStateException("Signing key file not found: " + keyPath);
                }
                reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
            }
            try (Reader r = reader;
                 PEMParser parser = new PEMParser(r)) {
            Object obj = parser.readObject();
            if (obj == null) {
                throw new IllegalStateException("No PEM object in key file: " + keyPath);
            }
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(PROVIDER);
            if (obj instanceof PEMKeyPair keyPair) {
                KeyPair kp = converter.getKeyPair(keyPair);
                this.privateKey = kp.getPrivate();
                this.publicKey = kp.getPublic();
            } else if (obj instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo privateKeyInfo) {
                this.privateKey = converter.getPrivateKey(privateKeyInfo);
                this.publicKey = derivePublicFromPrivate(this.privateKey);
            } else {
                throw new IllegalStateException("Unsupported PEM content: " + (obj == null ? "null" : obj.getClass().getName()));
            }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load signing key from " + keyPath, e);
        }
    }

    private static PublicKey derivePublicFromPrivate(PrivateKey privateKey) {
        if (privateKey instanceof java.security.interfaces.RSAPrivateCrtKey crt) {
            try {
                java.security.spec.RSAPublicKeySpec spec =
                        new java.security.spec.RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent());
                return java.security.KeyFactory.getInstance("RSA").generatePublic(spec);
            } catch (Exception e) {
                throw new IllegalStateException("Could not derive public key from RSA private key", e);
            }
        }
        throw new IllegalStateException("Only RSA private keys are supported; cannot derive public key");
    }

    @Override
    public String sign(String hashHex) {
        byte[] hashBytes = decodeHashHex(hashHex);
        return Base64.getEncoder().encodeToString(sign(hashBytes));
    }

    @Override
    public byte[] sign(byte[] hashBytes) {
        if (hashBytes == null || hashBytes.length != SHA256_DIGEST_LENGTH) {
            throw new IllegalArgumentException(
                    "Hash must be exactly " + SHA256_DIGEST_LENGTH + " bytes, got " + (hashBytes == null ? "null" : hashBytes.length));
        }
        ensureKeysLoaded();
        try {
            byte[] digestInfo = buildDigestInfo(hashBytes);
            Signature sig = Signature.getInstance(SIGNATURE_ALG, PROVIDER);
            sig.initSign(privateKey);
            sig.update(digestInfo);
            return sig.sign();
        } catch (Exception e) {
            throw new IllegalStateException("Signing failed", e);
        }
    }

    @Override
    public boolean verify(String hashHex, String signatureBase64) {
        if (hashHex == null || signatureBase64 == null || signatureBase64.isBlank()) {
            return false;
        }
        try {
            byte[] hashBytes = decodeHashHex(hashHex);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            return verify(hashBytes, signatureBytes);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean verify(byte[] hashBytes, byte[] signatureBytes) {
        if (hashBytes == null || hashBytes.length != SHA256_DIGEST_LENGTH) {
            return false;
        }
        if (signatureBytes == null || signatureBytes.length == 0) {
            return false;
        }
        ensureKeysLoaded();
        try {
            byte[] expectedDigestInfo = buildDigestInfo(hashBytes);
            Signature sig = Signature.getInstance(SIGNATURE_ALG, PROVIDER);
            sig.initVerify(publicKey);
            sig.update(expectedDigestInfo);
            return sig.verify(signatureBytes);
        } catch (SignatureException e) {
            return false;
        } catch (Exception e) {
            throw new IllegalStateException("Verification failed", e);
        }
    }

    private static byte[] buildDigestInfo(byte[] hash) {
        AlgorithmIdentifier algId = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256);
        DigestInfo digestInfo = new DigestInfo(algId, hash);
        try {
            return digestInfo.getEncoded();
        } catch (IOException e) {
            throw new IllegalStateException("DigestInfo encoding failed", e);
        }
    }

    private static byte[] decodeHashHex(String hashHex) {
        if (hashHex == null || hashHex.length() != 64) {
            throw new IllegalArgumentException(
                    "Hash must be 64-character hex string, got " + (hashHex == null ? "null" : "length " + hashHex.length()));
        }
        if (!hashHex.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("Hash must be hexadecimal");
        }
        return HEX.parseHex(hashHex);
    }

    private void ensureKeysLoaded() {
        if (privateKey == null) {
            loadKeys();
        }
        if (privateKey == null) {
            throw new IllegalStateException(
                    "Signing key not configured: set ai.aletheia.signing.key-path to PEM file path");
        }
    }
}
