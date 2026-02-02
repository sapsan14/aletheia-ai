package ai.aletheia.verifier;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * CLI entrypoint for offline Evidence Package verification (DP2.2.2).
 *
 * <p>Usage: run with one argument — path to a directory containing the package files,
 * or path to a .aep (ZIP) file.
 *
 * <p>Exit code: 0 = VALID, 1 = INVALID or error. No backend call.
 *
 * <p>Run from backend directory:
 * <pre>
 * mvn exec:java -Dexec.mainClass="ai.aletheia.verifier.VerifierMain" -Dexec.args="/path/to/package"
 * </pre>
 * Or with a run JAR:
 * <pre>
 * java -cp target/backend-*.jar ai.aletheia.verifier.VerifierMain /path/to/package
 * </pre>
 */
public final class VerifierMain {

    private VerifierMain() {}

    public static void main(String[] args) {
        if (args == null || args.length != 1 || args[0] == null || args[0].isBlank()) {
            System.err.println("Usage: VerifierMain <path-to-evidence-dir-or-.aep>");
            System.exit(1);
        }

        Path path = Paths.get(args[0].trim());
        if (!java.nio.file.Files.exists(path)) {
            System.err.println("Path does not exist: " + path);
            System.exit(1);
        }

        EvidenceVerifier verifier = new EvidenceVerifierImpl();
        VerificationResult result = verifier.verify(path);

        for (String line : result.report()) {
            System.out.println(line);
        }

        if (result.valid()) {
            System.out.println("VALID");
            System.exit(0);
        } else {
            System.err.println("INVALID: " + (result.failureReason() != null ? result.failureReason() : "verification failed"));
            System.exit(1);
        }
    }
}
