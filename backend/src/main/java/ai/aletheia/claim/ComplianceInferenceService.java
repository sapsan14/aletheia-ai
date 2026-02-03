package ai.aletheia.claim;

import org.springframework.stereotype.Service;

/**
 * DP2.4 variant 4: Infer compliance mode from prompt (no UI selector).
 * Keywords: GDPR, comply, compliance, clause, AI Act, etc.
 */
@Service
public class ComplianceInferenceService {

    private static final double DEFAULT_CONFIDENCE = 0.85;
    private static final int CLAIM_MAX_LENGTH = 500;

    /**
     * Infer a minimal claim from prompt and response. Returns null if prompt does not suggest compliance context.
     */
    public ComplianceClaim infer(String prompt, String response) {
        if (prompt == null || response == null) return null;
        String lower = prompt.toLowerCase().strip();
        if (lower.isEmpty()) return null;

        boolean hasCompliance = lower.contains("gdpr") || lower.contains("comply") || lower.contains("compliance")
                || lower.contains("clause") || lower.contains("ai act") || lower.contains("legal")
                || lower.contains("regulatory") || lower.contains("compliant");
        if (!hasCompliance) return null;

    // TODO: add more policies here?
        String policyVersion = "compliance-2024";
        if (lower.contains("gdpr")) policyVersion = "gdpr-2024";
        else if (lower.contains("ai act")) policyVersion = "ai-act-2024";

        String claim = firstSentenceOrTruncate(response.trim());
        if (claim.isEmpty()) claim = response.trim().length() > CLAIM_MAX_LENGTH
                ? response.trim().substring(0, CLAIM_MAX_LENGTH) + "..."
                : response.trim();

        return new ComplianceClaim(claim, DEFAULT_CONFIDENCE, policyVersion);
    }

    private static String firstSentenceOrTruncate(String text) {
        if (text == null || text.isEmpty()) return "";
        int end = text.indexOf('.');
        if (end > 0) return text.substring(0, end + 1).trim();
        if (text.length() > CLAIM_MAX_LENGTH) return text.substring(0, CLAIM_MAX_LENGTH) + "...";
        return text;
    }
}
