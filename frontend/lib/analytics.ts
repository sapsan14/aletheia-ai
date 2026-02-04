/**
 * Phase 4.5 minimal analytics (3.6.1).
 * Sends events to backend /api/metrics/event; logs to console in dev.
 * Provider-agnostic: no external analytics keys; plug in a real provider later if needed.
 */

export type MetricsEvent =
  | "page_view"
  | "cta_click"
  | "download_evidence"
  | "download_verifier"
  | "view_use_cases"
  // Legacy (still accepted)
  | "landing_view"
  | "demo_view"
  | "download_evidence_click";

export interface TrackEventProps {
  /** For page_view: which page (main, verify, use_cases). Sent as event name suffix to backend. */
  page?: string;
  /** Optional response id (e.g. for download_evidence). */
  responseId?: number;
  [key: string]: unknown;
}

const isDev =
  typeof process !== "undefined" && process.env.NODE_ENV === "development";

/**
 * Track an analytics event. In development, logs to console. Always sends to backend when event is not empty.
 */
export function trackEvent(
  event: MetricsEvent | string,
  props?: TrackEventProps
): void {
  const payload = { event, ...props };

  if (isDev) {
    // eslint-disable-next-line no-console
    console.log("[analytics]", payload);
  }

  const backendEvent =
    event === "page_view" && props?.page
      ? `page_view_${props.page}`
      : event;
  const responseId =
    props?.responseId ?? (props as { responseId?: number } | undefined)?.responseId;

  sendToBackend(backendEvent, responseId);
}

async function sendToBackend(
  event: string,
  responseId?: number
): Promise<void> {
  try {
    await fetch("/api/metrics/event", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        event,
        responseId: responseId ?? null,
      }),
      keepalive: true,
    });
  } catch {
    // Analytics should never block the UI.
  }
}
