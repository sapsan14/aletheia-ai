package ai.aletheia.audit;

import ai.aletheia.audit.dto.AuditRecordRequest;
import ai.aletheia.db.AiResponseRepository;
import ai.aletheia.db.entity.AiResponse;
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

    public AuditRecordService(AiResponseRepository repository) {
        this.repository = repository;
    }

    /**
     * Saves the audit record and returns the generated id.
     *
     * @param request audit data (prompt, response, hash, signature, tsaToken, etc.)
     * @return the saved entity's id
     */
    public Long save(AuditRecordRequest request) {
        AiResponse entity = mapToEntity(request);
        AiResponse saved = repository.save(entity);
        return saved.getId();
    }

    private AiResponse mapToEntity(AuditRecordRequest r) {
        AiResponse e = new AiResponse(r.prompt(), r.response(), r.responseHash());
        e.setSignature(r.signature());
        e.setTsaToken(r.tsaToken());
        e.setLlmModel(r.llmModel());
        e.setRequestId(r.requestId());
        e.setTemperature(r.temperature());
        e.setSystemPrompt(r.systemPrompt());
        e.setVersion(r.version() != null ? r.version() : 1);
        return e;
    }
}
