package ai.aletheia.db;

import ai.aletheia.db.entity.AiResponse;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link AiResponse} entities.
 *
 * <p>Provides standard CRUD operations. The audit layer (e.g. AuditRecordService)
 * uses this to persist verifiable AI responses after the crypto pipeline completes.
 */
public interface AiResponseRepository extends JpaRepository<AiResponse, Long> {
}
