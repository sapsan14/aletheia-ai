package ai.aletheia.claim;

/**
 * DP2.4: Minimal AI Claim — structured claim inferred from prompt/response (variant 4: no UI mode).
 *
 * @param claim         claim text (e.g. first sentence of response)
 * @param confidence    confidence in [0,1]
 * @param policyVersion e.g. "gdpr-2024", "ai-act-2024", "compliance-2024"
 */
public record ComplianceClaim(String claim, double confidence, String policyVersion) {}
