package ai.aletheia.api;

import ai.aletheia.api.dto.AiVerifyResponse;
import ai.aletheia.api.dto.ErrorResponse;
import ai.aletheia.db.AiResponseRepository;
import ai.aletheia.db.entity.AiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Returns stored audit record by id for the verification page.
 *
 * <p>No verification logic (hash check, signature check) — only data retrieval.
 * Returns 404 with JSON body when id not found.
 */
@RestController
@RequestMapping("/api/ai")
public class AiVerifyController {

    private final AiResponseRepository repository;

    public AiVerifyController(AiResponseRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/verify/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> verify(@PathVariable Long id) {
        var opt = repository.findById(id);
        if (opt.isPresent()) {
            return ResponseEntity.ok(toResponse(opt.get()));
        }
        return ResponseEntity.status(404).body(ErrorResponse.notFound("Record not found", id));
    }

    private AiVerifyResponse toResponse(AiResponse e) {
        return new AiVerifyResponse(
                e.getId(),
                e.getPrompt(),
                e.getResponse(),
                e.getResponseHash(),
                e.getSignature(),
                e.getTsaToken(),
                e.getLlmModel(),
                e.getCreatedAt(),
                e.getRequestId(),
                e.getTemperature(),
                e.getSystemPrompt(),
                e.getVersion()
        );
    }
}
