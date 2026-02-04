package ai.aletheia.metrics;

import java.util.Arrays;
import java.util.Optional;

/**
 * Allowed analytics events for Phase 4.
 */
public enum MetricEventType {
    LANDING_VIEW("landing_view"),
    CTA_CLICK("cta_click"),
    DEMO_VIEW("demo_view"),
    DOWNLOAD_EVIDENCE_CLICK("download_evidence_click"),
    // Phase 4.5 funnel (3.6.1)
    PAGE_VIEW_MAIN("page_view_main"),
    PAGE_VIEW_VERIFY("page_view_verify"),
    PAGE_VIEW_USE_CASES("page_view_use_cases"),
    DOWNLOAD_EVIDENCE("download_evidence"),
    DOWNLOAD_VERIFIER("download_verifier"),
    VIEW_USE_CASES("view_use_cases");

    private final String value;

    MetricEventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Optional<MetricEventType> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(v -> v.value.equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
