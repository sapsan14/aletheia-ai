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
     */
    public static byte[] toCanonicalBytes(String claim, Double confidence, String model, String policyVersion) {
        // Deterministic JSON: keys alphabetically ordered. Escape strings for JSON.
        String c = escapeJson(claim != null ? claim : "");
        String m = escapeJson(model != null ? model : "");
        String p = escapeJson(policyVersion != null ? policyVersion : "");
        double conf = confidence != null ? confidence : 0.0;
        String json = "{\"claim\":\"" + c + "\",\"confidence\":" + conf + ",\"model\":\"" + m + "\",\"policy_version\":\"" + p + "\"}";
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
