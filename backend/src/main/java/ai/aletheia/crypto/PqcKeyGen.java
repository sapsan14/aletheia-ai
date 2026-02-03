package ai.aletheia.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumKeyPairGenerator;
import org.bouncycastle.pqc.crypto.crystals.dilithium.DilithiumParameters;
import org.bouncycastle.pqc.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.pqc.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.Security;

/**
 * Utility to generate ML-DSA (Dilithium) key pairs for PQC signing.
 * Uses Bouncy Castle with parameter set Dilithium3 (NIST security level 3).
 *
 * <p>Run from project root or backend/:
 * <pre>
 *   mvn -q exec:java -Dexec.mainClass="ai.aletheia.crypto.PqcKeyGen" -Dexec.args="."
 * </pre>
 * Or run the main method from an IDE. Output: {@code ai_pqc.key} (private) and {@code ai_pqc_public.pem} (public).
 *
 * <p>To enable PQC signing: set {@code ai.aletheia.signing.pqc-key-path} to the path of {@code ai_pqc.key}.
 *
 * @see docs/en/PLAN_PQC.md
 */
public final class PqcKeyGen {

    /** Parameter set: Dilithium3 (NIST level 3). */
    private static final DilithiumParameters PARAMS = DilithiumParameters.dilithium3;

    private static final String DEFAULT_PRIVATE_KEY_FILE = "ai_pqc.key";
    private static final String DEFAULT_PUBLIC_KEY_FILE = "ai_pqc_public.pem";

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private PqcKeyGen() {}

    public static void main(String[] args) throws IOException {
        Path outDir = args.length > 0 ? Path.of(args[0]) : Path.of(".");
        Path privatePath = outDir.resolve(DEFAULT_PRIVATE_KEY_FILE);
        Path publicPath = outDir.resolve(DEFAULT_PUBLIC_KEY_FILE);

        DilithiumKeyPairGenerator gen = new DilithiumKeyPairGenerator();
        gen.init(new DilithiumKeyGenerationParameters(new SecureRandom(), PARAMS));
        org.bouncycastle.crypto.AsymmetricCipherKeyPair keyPair = gen.generateKeyPair();

        byte[] privateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(keyPair.getPrivate()).getEncoded();
        byte[] publicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(keyPair.getPublic()).getEncoded();

        writePem(privatePath, "PRIVATE KEY", privateKeyInfo);
        writePem(publicPath, "PUBLIC KEY", publicKeyInfo);

        System.out.println("PQC (ML-DSA Dilithium3) key pair generated:");
        System.out.println("  Private: " + privatePath.toAbsolutePath());
        System.out.println("  Public:  " + publicPath.toAbsolutePath());
        System.out.println("To enable PQC signing, set ai.aletheia.signing.pqc-key-path to the private key path.");
    }

    private static void writePem(Path path, String type, byte[] der) throws IOException {
        Files.createDirectories(path.getParent());
        try (PemWriter w = new PemWriter(new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8))) {
            w.writeObject(new PemObject(type, der));
        }
    }
}
