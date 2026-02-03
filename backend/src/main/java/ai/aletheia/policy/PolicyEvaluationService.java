package ai.aletheia.policy;

import ai.aletheia.db.entity.AiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase 4 demo policy evaluator.
 *
 * <p>Coverage formula: pass_count / total_rules. Rules marked "not_evaluated"
 * do not contribute to coverage and keep the UI honest about gaps.
 */
@Service
public class PolicyEvaluationService {

    public static final String DEMO_POLICY_ID = "aletheia-demo";
    public static final String DEMO_POLICY_VERSION = "2026-01";
    private static final int TOTAL_RULES = 4;

    private final ObjectMapper objectMapper;

    public PolicyEvaluationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PolicyEvaluationResult evaluate(AiResponse response) {
        List<PolicyRuleResult> rules = new ArrayList<>();

        boolean hasSignature = response.getSignature() != null && !response.getSignature().isBlank();
        boolean hasTimestamp = response.getTsaToken() != null && !response.getTsaToken().isBlank();
        boolean hasModel = response.getLlmModel() != null && !response.getLlmModel().isBlank();

        rules.add(new PolicyRuleResult("R1", hasSignature && hasTimestamp ? "pass" : "not_evaluated"));
        rules.add(new PolicyRuleResult("R2", hasModel ? "pass" : "not_evaluated"));
        rules.add(new PolicyRuleResult("R3", "not_evaluated"));
        rules.add(new PolicyRuleResult("R4", "not_evaluated"));

        long passCount = rules.stream().filter(rule -> "pass".equals(rule.status())).count();
        double coverage = (double) passCount / TOTAL_RULES;

        return new PolicyEvaluationResult(
                DEMO_POLICY_ID,
                DEMO_POLICY_VERSION,
                coverage,
                rules
        );
    }

    public String toJson(List<PolicyRuleResult> rules) {
        if (rules == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(rules);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize policy rules", e);
        }
    }

    public List<PolicyRuleResult> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<PolicyRuleResult>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse policy rules JSON", e);
        }
    }
}
