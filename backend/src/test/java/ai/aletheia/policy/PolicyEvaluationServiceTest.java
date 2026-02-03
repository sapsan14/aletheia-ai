package ai.aletheia.policy;

import ai.aletheia.db.entity.AiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyEvaluationServiceTest {

    @Test
    void evaluate_withSignatureTimestampAndModel_setsCoverageAndRules() {
        AiResponse response = new AiResponse("Q", "A", "hash");
        response.setSignature("sig");
        response.setTsaToken("tsa");
        response.setLlmModel("gpt-4");

        PolicyEvaluationService service = new PolicyEvaluationService(new ObjectMapper());
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
}
