package ai.aletheia.verifier;

import java.nio.file.Path;

/**
 * Offline verifier for Evidence Package (.aep or directory).
 *
 * <p>Verification order: (1) hash, (2) signature, (3) TSA token.
 * No backend call; uses only files from the package.
 */
public interface EvidenceVerifier {

    /**
     * Verify an Evidence Package at the given path (directory or .aep ZIP).
     *
     * @param path path to a directory containing the seven package files, or to a .aep (ZIP) file
     * @return result with valid/invalid, report lines, and failure reason if invalid
     */
    VerificationResult verify(Path path);
}
