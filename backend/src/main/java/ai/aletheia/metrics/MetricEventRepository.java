package ai.aletheia.metrics;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricEventRepository extends JpaRepository<MetricEvent, Long> {
    long countByEventName(String eventName);
}
