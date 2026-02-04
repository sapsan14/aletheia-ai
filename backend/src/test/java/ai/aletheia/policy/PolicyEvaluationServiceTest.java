package ai.aletheia.policy;

import ai.aletheia.db.entity.AiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyEvaluationServiceTest {

    private final PolicyEvaluationService service = new PolicyEvaluationService(new ObjectMapper());

    @Test
    void evaluate_withSignatureTimestampAndModel_setsCoverageAndRules() {
        AiResponse response = new AiResponse("Q", "A", "hash");
        response.setSignature("sig");
        response.setTsaToken("tsa");
        response.setLlmModel("gpt-4");

        PolicyEvaluationResult result = service.evaluate(response);

        assertThat(result.policyId()).isEqualTo("aletheia-demo");
        assertThat(result.policyVersion()).isEqualTo("2026-01");
        assertThat(result.coverage()).isEqualTo(0.5);
        assertThat(result.rules()).hasSize(4);
        assertThat(result.rules().get(0).ruleId()).isEqualTo("R1");
        assertThat(result.rules().get(0).status()).isEqualTo("pass");
        assertThat(result.rules().get(1).ruleId()).isEqualTo("R2");
        assertThat(result.rules().get(1).status()).isEqualTo("pass");
        assertThat(result.rules().get(2).status()).isEqualTo("not_evaluated");
        assertThat(result.rules().get(3).status()).isEqualTo("not_evaluated");
    }

    @Test
    void evaluate_noSignatureNoTimestamp_r1NotEvaluated() {
        AiResponse response = new AiResponse("Q", "A", "hash");
        response.setLlmModel("gpt-4");
        // no signature, no tsaToken

        PolicyEvaluationResult result = service.evaluate(response);

        assertThat(result.rules().get(0).ruleId()).isEqualTo("R1");
        assertThat(result.rules().get(0).status()).isEqualTo("not_evaluated");
        assertThat(result.rules().get(1).status()).isEqualTo("pass");
        assertThat(result.coverage()).isEqualTo(0.25);
    }

    @Test
    void evaluate_noModel_r2NotEvaluated() {
        AiResponse response = new AiResponse("Q", "A", "hash");
        response.setSignature("sig");
        response.setTsaToken("tsa");
        // no llmModel

        PolicyEvaluationResult result = service.evaluate(response);

        assertThat(result.rules().get(0).status()).isEqualTo("pass");
        assertThat(result.rules().get(1).ruleId()).isEqualTo("R2");
        assertThat(result.rules().get(1).status()).isEqualTo("not_evaluated");
        assertThat(result.coverage()).isEqualTo(0.25);
    }

    @Test
    void evaluate_allMissing_coverageZero() {
        AiResponse response = new AiResponse("Q", "A", "hash");
        // no signature, tsaToken, or model

        PolicyEvaluationResult result = service.evaluate(response);

        assertThat(result.coverage()).isEqualTo(0.0);
        assertThat(result.rules()).allMatch(r -> "not_evaluated".equals(r.status()));
    }

    @Test
    void evaluate_r3AndR4AlwaysNotEvaluated() {
        AiResponse response = new AiResponse("Q", "A", "hash");
        response.setSignature("sig");
        response.setTsaToken("tsa");
        response.setLlmModel("gpt-4");

        PolicyEvaluationResult result = service.evaluate(response);

        assertThat(result.rules().get(2).ruleId()).isEqualTo("R3");
        assertThat(result.rules().get(2).status()).isEqualTo("not_evaluated");
        assertThat(result.rules().get(3).ruleId()).isEqualTo("R4");
        assertThat(result.rules().get(3).status()).isEqualTo("not_evaluated");
    }
}
