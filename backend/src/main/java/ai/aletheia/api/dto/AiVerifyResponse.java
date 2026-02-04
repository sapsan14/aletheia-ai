package ai.aletheia.api.dto;

import ai.aletheia.policy.PolicyRuleResult;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Response for GET /api/ai/verify/:id.
 * Full record for the verification page.
 *
 * @param hashMatch       true if recomputed hash equals stored responseHash; false otherwise
 * @param signatureValid  "valid" | "invalid" | "n_a" (not applicable: no signature or key not configured)
 * @param claim           DP2.4: minimal AI claim text (null when not compliance)
 * @param confidence      DP2.4: confidence in [0,1] (null when not compliance)
 * @param policyVersion   DP2.4: e.g. "gdpr-2024" (null when not compliance)
 * @param policyCoverage  Phase 4: coverage ratio (0..1) for demo policy
 * @param policyRulesEvaluated Phase 4: list of rule results (ruleId + status)
 * @param signaturePqc    PQC.5: Base64 ML-DSA signature (null when PQC disabled)
 * @param pqcAlgorithm    PQC.5: e.g. "ML-DSA (Dilithium3)" (null when no PQC)
 * @param computedHash    Backend-recomputed hash (for debugging mismatch; same algorithm as at save time)
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record AiVerifyResponse(
        Long id,
        String prompt,
        String response,
        String responseHash,
        String computedHash,
        String signature,
        String tsaToken,
        String llmModel,
        Instant createdAt,
        String requestId,
        Double temperature,
        String systemPrompt,
        Integer version,
        String claim,
        Double confidence,
        String policyVersion,
        Boolean hashMatch,
        String signatureValid,
        Double policyCoverage,
        List<PolicyRuleResult> policyRulesEvaluated,
        String signaturePqc,
        String pqcAlgorithm
) {}
