package ai.aletheia.policy;

import java.util.List;

/**
 * Phase 4 demo policy evaluation summary for a response.
 *
 * @param policyId demo policy id (e.g. "aletheia-demo")
 * @param policyVersion demo policy version (e.g. "2026-01")
 * @param coverage share of rules that passed, in [0,1]
 * @param rules per-rule results (ruleId + status)
 */
public record PolicyEvaluationResult(
        String policyId,
        String policyVersion,
        double coverage,
        List<PolicyRuleResult> rules
) {}
