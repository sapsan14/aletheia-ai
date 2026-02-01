/**
 * Runtime proxy: /api/* → BACKEND_INTERNAL_URL/api/*
 * Uses env at request time so Docker works without build-time BACKEND_INTERNAL_URL.
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
