/**
 * Phase 4.5 — Tests for Policy coverage block and "Why is confidence not 100%?" (4.1).
 * Renders the verify page with mocked API; asserts Coverage-policy block and toggle are present.
 */

import "@testing-library/jest-dom";
import { render, screen, waitFor } from "@testing-library/react";
import VerifyPage from "../page";

const mockVerifyRecord = {
  id: 1,
  prompt: "Test prompt",
  response: "Test response",
  responseHash: "a".repeat(64),
  signature: "sig",
  tsaToken: "tsa",
  llmModel: "gpt-4",
  createdAt: "2026-01-15T12:00:00Z",
  hashMatch: true,
  signatureValid: "valid",
  policyCoverage: 0.5,
  policyRulesEvaluated: [
    { ruleId: "R1", status: "pass" },
    { ruleId: "R2", status: "pass" },
    { ruleId: "R3", status: "not_evaluated" },
    { ruleId: "R4", status: "not_evaluated" },
  ],
};

beforeEach(() => {
  const mockFetch = (url: string | URL) => {
    const path = typeof url === "string" ? url : url.toString();
    if (path.includes("/api/ai/verify/1")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve(mockVerifyRecord),
      } as Response);
    }
    return Promise.reject(new Error("Unexpected fetch: " + path));
  };
  if (typeof globalThis.fetch === "undefined") {
    (globalThis as unknown as { fetch: typeof fetch }).fetch = mockFetch as typeof fetch;
  } else {
    jest.spyOn(globalThis, "fetch").mockImplementation(mockFetch as typeof fetch);
  }
});

afterEach(() => {
  if (typeof globalThis.fetch !== "undefined" && jest.isMockFunction(globalThis.fetch)) {
    (globalThis.fetch as jest.Mock).mockRestore();
  }
});

jest.mock("next/navigation", () => ({
  useSearchParams: () => new URLSearchParams("id=1"),
}));

describe("Verify page — Policy coverage block", () => {
  it("renders Coverage-policy (demo) section when record has policy data", async () => {
    render(<VerifyPage />);

    await waitFor(
      () => {
        expect(screen.getByText(/Coverage-policy \(demo\)/)).toBeInTheDocument();
      },
      { timeout: 3000 }
    );
  });

  it("renders 'Why is confidence not 100%?' toggle", async () => {
    render(<VerifyPage />);

    await waitFor(
      () => {
        const button = screen.getByRole("button", {
          name: /Why is confidence not 100%?/i,
        });
        expect(button).toBeInTheDocument();
      },
      { timeout: 3000 }
    );
  });

  it("shows policy coverage percentage when policy data is present", async () => {
    render(<VerifyPage />);

    await waitFor(
      () => {
        expect(screen.getByText(/50% — 2 of 4 rules checked/)).toBeInTheDocument();
      },
      { timeout: 3000 }
    );
  });
});
