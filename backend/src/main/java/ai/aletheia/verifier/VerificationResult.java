package ai.aletheia.verifier;

import java.util.List;

/**
 * Result of offline Evidence Package verification (DP2.2).
 *
 * @param valid        true if all checks passed
 * @param report       lines describing each check (e.g. "hash OK", "signature OK", "timestamp 2025-01-15T12:00:00Z")
 * @param failureReason short reason when valid is false (e.g. "signature invalid", "hash mismatch")
 */
public record VerificationResult(
        boolean valid,
        List<String> report,
        String failureReason
) {
    public static VerificationResult valid(List<String> report) {
        return new VerificationResult(true, report, null);
    }

    public static VerificationResult invalid(List<String> report, String failureReason) {
        return new VerificationResult(false, report, failureReason);
    }
}
