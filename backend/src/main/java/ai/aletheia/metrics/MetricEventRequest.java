package ai.aletheia.metrics;

/**
 * JSON body for POST /api/metrics/event.
 */
public record MetricEventRequest(
        String event,
        Long responseId
) {}
