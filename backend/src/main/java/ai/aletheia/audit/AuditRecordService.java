package ai.aletheia.audit;

import ai.aletheia.audit.dto.AuditRecordRequest;
import ai.aletheia.db.AiResponseRepository;
import ai.aletheia.db.entity.AiResponse;
import ai.aletheia.policy.PolicyEvaluationResult;
import ai.aletheia.policy.PolicyEvaluationService;
import org.springframework.stereotype.Service;

/**
 * Orchestrates persistence of verifiable AI response records.
 *
 * <p>Maps {@link AuditRecordRequest} to {@link AiResponse} entity and saves via
 * {@link AiResponseRepository}. No LLM or crypto logic — only persistence.
 * Called from the API layer after hash, sign, and timestamp are computed.
 */
@Service
public class AuditRecordService {

    private final AiResponseRepository repository;
    private final PolicyEvaluationService policyEvaluationService;

    public AuditRecordService(AiResponseRepository repository,
                              PolicyEvaluationService policyEvaluationService) {
        this.repository = repository;
        this.policyEvaluationService = policyEvaluationService;
    }

    /**
     * Saves the audit record and returns the generated id.
     *
     * @param request audit data (prompt, response, hash, signature, tsaToken, etc.)
     * @return the saved entity's id
     */
    public Long save(AuditRecordRequest request) {
        AiResponse entity = mapToEntity(request);
        PolicyEvaluationResult evaluation = policyEvaluationService.evaluate(entity);
        entity.setPolicyCoverage(evaluation.coverage());
        entity.setPolicyRulesEvaluated(policyEvaluationService.toJson(evaluation.rules()));
        AiResponse saved = repository.save(entity);
        return saved.getId();
    }

    private AiResponse mapToEntity(AuditRecordRequest r) {
        AiResponse e = new AiResponse(r.prompt(), r.response(), r.responseHash());
        e.setSignature(r.signature());
        e.setSignaturePqc(r.signaturePqc());
        e.setPqcPublicKeyPem(r.pqcPublicKeyPem());
        e.setTsaToken(r.tsaToken());
        e.setLlmModel(r.llmModel());
        e.setRequestId(r.requestId());
        e.setTemperature(r.temperature());
        e.setSystemPrompt(r.systemPrompt());
        e.setVersion(r.version() != null ? r.version() : 1);
        e.setClaim(r.claim());
        e.setConfidence(r.confidence());
        e.setPolicyVersion(r.policyVersion());
        return e;
    }
}
