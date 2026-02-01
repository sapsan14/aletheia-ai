/**
 * Runtime API proxy: /api/* → BACKEND_INTERNAL_URL/api/*
 *
 * All requests to /api/... (e.g. /api/ai/ask, /api/ai/verify/:id) are forwarded
 * to the backend at BACKEND_INTERNAL_URL. The URL is read at request time from
 * process.env, so Docker Compose can set BACKEND_INTERNAL_URL=http://backend:8080
 * in the frontend container without rebuilding the image.
 *
 * Why runtime proxy instead of next.config rewrites:
 * - Next.js rewrites() are evaluated at build time. If BACKEND_INTERNAL_URL
 *   was not passed at build time, the client would proxy to localhost:8080
 *   inside the container (wrong), causing ECONNREFUSED when using Docker.
 * - This route handler reads BACKEND_INTERNAL_URL when each request is made,
 *   so the same image works in dev (localhost:8080) and in Docker (backend:8080).
 *
 * See: README.md (Docker build), deploy/ansible/README.md (ngrok, CORS).
 */
import { NextRequest, NextResponse } from "next/server";

const BACKEND =
  process.env.BACKEND_INTERNAL_URL || "http://localhost:8080";

/** Copy headers but omit hop-by-hop ones (host, connection, etc.) to avoid proxy issues. */
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

/** Forward the request to the backend and return the response (status, headers, body). */
async function proxy(
  request: NextRequest,
  pathSegments: string[]
): Promise<Response> {
  const path = pathSegments.join("/");
  const url = `${BACKEND.replace(/\/$/, "")}/api/${path}`;
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

export async function GET(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  return proxy(request, path);
}

export async function POST(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  return proxy(request, path);
}

export async function PUT(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  return proxy(request, path);
}

export async function PATCH(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  return proxy(request, path);
}

export async function DELETE(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  return proxy(request, path);
}

export async function OPTIONS(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  return proxy(request, path);
}
