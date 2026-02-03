import { describe, expect, it, vi } from "vitest";

import { AletheiaAPIError, AletheiaClient } from "../src/index";

describe("AletheiaClient", () => {
  it("sign sends correct payload", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ id: 1 }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      })
    );

    const client = new AletheiaClient("http://localhost:8080", { fetch: fetchMock });
    const result = await client.sign("hello", { modelId: "external", policyId: "policy-1" });

    expect(result.id).toBe(1);
    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe("http://localhost:8080/api/sign");
    expect(init?.method).toBe("POST");
    expect(init?.headers).toEqual({ "Content-Type": "application/json" });
    expect(init?.body).toContain("\"response\":\"hello\"");
    expect(init?.body).toContain("\"modelId\":\"external\"");
    expect(init?.body).toContain("\"policyId\":\"policy-1\"");
  });

  it("verify calls correct URL", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ id: 123 }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      })
    );

    const client = new AletheiaClient("http://localhost:8080", { fetch: fetchMock });
    const result = await client.verify(123);

    expect(result.id).toBe(123);
    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/ai/verify/123",
      expect.objectContaining({})
    );
  });

  it("getEvidence returns binary data", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(new Uint8Array([1, 2, 3]), { status: 200 })
    );

    const client = new AletheiaClient("http://localhost:8080", { fetch: fetchMock });
    const data = await client.getEvidence("abc");

    expect(data.byteLength).toBe(3);
    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/ai/evidence/abc",
      expect.objectContaining({})
    );
  });

  it("non-2xx responses throw AletheiaAPIError", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ error: "Bad request" }), {
        status: 400,
        headers: { "Content-Type": "application/json" },
      })
    );

    const client = new AletheiaClient("http://localhost:8080", { fetch: fetchMock });
    await expect(client.sign("hello")).rejects.toBeInstanceOf(AletheiaAPIError);
  });
});
