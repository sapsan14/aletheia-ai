package ai.aletheia.verifier;

import java.util.List;

/**
 * Result of offline Evidence Package verification (DP2.2).
 *
 * @param valid         true if all checks passed
 * @param report        lines describing each check (e.g. "hash OK", "signature OK", "PQC signature: valid")
 * @param failureReason short reason when valid is false (e.g. "signature invalid", "hash mismatch")
 * @param pqcValid      PQC.8: true if PQC signature was present and valid, false if present and invalid, null if not present
 */
public record VerificationResult(
        boolean valid,
        List<String> report,
        String failureReason,
        Boolean pqcValid
) {
    public static VerificationResult valid(List<String> report) {
        return new VerificationResult(true, report, null, null);
    }

    public static VerificationResult valid(List<String> report, Boolean pqcValid) {
        return new VerificationResult(true, report, null, pqcValid);
    }

    public static VerificationResult invalid(List<String> report, String failureReason) {
        return new VerificationResult(false, report, failureReason, null);
    }
}
