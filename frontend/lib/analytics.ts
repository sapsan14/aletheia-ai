/**
 * Phase 4 minimal analytics helper.
 * Sends events to backend /api/metrics/event and fails silently on errors.
 */
export type MetricsEvent =
  | "landing_view"
  | "cta_click"
  | "demo_view"
  | "download_evidence_click";

export async function trackEvent(
  event: MetricsEvent,
  options?: { responseId?: number }
): Promise<void> {
  try {
    await fetch("/api/metrics/event", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        event,
        responseId: options?.responseId,
      }),
      keepalive: true,
    });
  } catch {
    // Analytics should never block the UI.
  }
}
