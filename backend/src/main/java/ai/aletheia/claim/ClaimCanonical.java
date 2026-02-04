package ai.aletheia.claim;

import java.nio.charset.StandardCharsets;

/**
 * DP2.4: Deterministic canonical form of claim metadata for signing.
 * Keys in alphabetical order: claim, confidence, model, policy_version.
 */
public final class ClaimCanonical {

    private ClaimCanonical() {}

    /**
     * Build canonical bytes for claim metadata (used in signed payload).
     * Same order must be used when building Evidence Package canonical.bin.
     * Uses fixed "%.6f" for confidence so DB round-trip does not change the payload.
     */
    private static final java.util.Locale LOCALE = java.util.Locale.ROOT;

    public static byte[] toCanonicalBytes(String claim, Double confidence, String model, String policyVersion) {
        return toCanonicalBytesWithConfFormat(claim, confidence, model, policyVersion, true);
    }

    /**
     * Legacy format: confidence as Java default Double.toString (for verifying records saved before %.6f was introduced).
     */
    public static byte[] toCanonicalBytesLegacy(String claim, Double confidence, String model, String policyVersion) {
        return toCanonicalBytesWithConfFormat(claim, confidence, model, policyVersion, false);
    }

    private static byte[] toCanonicalBytesWithConfFormat(
            String claim, Double confidence, String model, String policyVersion, boolean useFixedFormat) {
        String c = escapeJson(claim != null ? claim : "");
        String m = escapeJson(model != null ? model : "");
        String p = escapeJson(policyVersion != null ? policyVersion : "");
        double conf = confidence != null ? confidence : 0.0;
        String confStr = useFixedFormat ? String.format(LOCALE, "%.6f", conf) : String.valueOf(conf);
        String json = "{\"claim\":\"" + c + "\",\"confidence\":" + confStr + ",\"model\":\"" + m + "\",\"policy_version\":\"" + p + "\"}";
        return json.getBytes(StandardCharsets.UTF_8);
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
