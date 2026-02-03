package ai.aletheia.metrics;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Phase 4 minimal analytics collector.
 */
@Service
public class MetricsService {

    private final MetricEventRepository repository;

    public MetricsService(MetricEventRepository repository) {
        this.repository = repository;
    }

    public void recordEvent(String eventName, Long responseId) {
        MetricEventType type = MetricEventType.fromValue(eventName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown event: " + eventName));
        repository.save(new MetricEvent(type.value(), responseId));
    }

    public Map<String, Long> summary() {
        Map<String, Long> out = new LinkedHashMap<>();
        for (MetricEventType type : MetricEventType.values()) {
            out.put(type.value(), repository.countByEventName(type.value()));
        }
        return out;
    }
}
