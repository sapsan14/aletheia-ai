/**
 * PQC.6 — Quantum-Resistant / PQC Verified badge.
 * Shown when response has a post-quantum signature (signaturePqc).
 * Includes tooltip and aria-label for accessibility.
 */

"use client";

import { TOOLTIPS } from "@/lib/tooltips";

/** Minimal atom-style icon (quantum + long-term). */
function PqcIcon({ className }: { className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden
    >
      {/* Nucleus */}
      <circle cx="12" cy="12" r="2.2" fill="currentColor" />
      {/* Orbit 1 */}
      <ellipse cx="12" cy="12" rx="8" ry="4" />
      {/* Orbit 2 — rotated */}
      <ellipse cx="12" cy="12" rx="8" ry="4" transform="rotate(60 12 12)" />
      <ellipse cx="12" cy="12" rx="8" ry="4" transform="rotate(120 12 12)" />
    </svg>
  );
}

interface PqcBadgeProps {
  /** "default" = full "Quantum-Resistant" pill; "compact" = small inline for landing */
  variant?: "default" | "compact";
  /** Override tooltip (default from TOOLTIPS.pqc_badge) */
  tooltip?: string;
  className?: string;
}

export function PqcBadge({
  variant = "default",
  tooltip = TOOLTIPS.pqc_badge,
  className = "",
}: PqcBadgeProps) {
  const ariaLabel =
    "Post-quantum signature present: this response remains verifiable with quantum-resistant cryptography.";
  const label =
    variant === "compact" ? "PQC" : "Quantum-Resistant";
  const subtext =
    variant === "compact"
      ? null
      : "Post-quantum signature included";

  return (
    <span
      role="status"
      aria-label={ariaLabel}
      title={tooltip}
      tabIndex={0}
      className={`
        inline-flex items-center gap-1.5 rounded-md font-medium
        bg-teal-50 text-teal-800 dark:bg-teal-950/60 dark:text-teal-200
        border border-teal-200/80 dark:border-teal-700/80
        ${variant === "compact" ? "px-2 py-0.5 text-xs" : "px-3 py-1.5 text-sm"}
        ${className}
      `}
    >
      <PqcIcon
        className={
          variant === "compact" ? "h-3 w-3 shrink-0" : "h-4 w-4 shrink-0 text-teal-600 dark:text-teal-400"
        }
      />
      <span>{label}</span>
      {subtext && (
        <span className="text-teal-600/90 dark:text-teal-400/90 font-normal hidden sm:inline">
          — {subtext}
        </span>
      )}
    </span>
  );
}
