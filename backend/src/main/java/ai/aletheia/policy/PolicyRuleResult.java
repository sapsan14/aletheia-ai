package ai.aletheia.policy;

/**
 * Phase 4 demo policy rule evaluation result.
 *
 * @param ruleId stable rule identifier (e.g. "R1")
 * @param status evaluation status: "pass" or "not_evaluated"
 */
public record PolicyRuleResult(String ruleId, String status) {}
