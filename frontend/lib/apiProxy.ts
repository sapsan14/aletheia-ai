/**
 * Runtime API proxy: forwards requests to BACKEND_INTERNAL_URL/api/<path>.
 * Used by app/api/[...path]/route.ts and explicit routes (e.g. /api/ai/ask).
 */
import { NextRequest, NextResponse } from "next/server";

const BACKEND =
  process.env.BACKEND_INTERNAL_URL || "http://localhost:8080";

function stripHopByHop(headers: Headers): Headers {
  const out = new Headers();
  const skip = new Set([
    "host",
    "connection",
    "keep-alive",
    "transfer-encoding",
    "te",
    "trailer",
    "upgrade",
    "proxy-authorization",
    "proxy-authenticate",
  ]);
  headers.forEach((value, key) => {
    if (!skip.has(key.toLowerCase())) out.set(key, value);
  });
  return out;
}

/**
 * Proxy a request to the backend. pathSegments can be "ai/ask" or ["ai", "ask"].
 */
export async function proxyToBackend(
  request: NextRequest,
  pathSegments: string | string[]
): Promise<Response> {
  const path =
    typeof pathSegments === "string"
      ? pathSegments
      : pathSegments.join("/");
  const search = request.nextUrl.searchParams.toString();
  const url = `${BACKEND.replace(/\/$/, "")}/api/${path}${search ? `?${search}` : ""}`;
  const init: RequestInit = {
    method: request.method,
    headers: stripHopByHop(request.headers),
  };
  if (request.method !== "GET" && request.method !== "HEAD") {
    init.body = await request.arrayBuffer();
  }
  const res = await fetch(url, init);
  const resHeaders = stripHopByHop(res.headers);
  return new NextResponse(res.body, {
    status: res.status,
    statusText: res.statusText,
    headers: resHeaders,
  });
}
