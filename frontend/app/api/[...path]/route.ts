/**
 * Runtime API proxy: /api/* → BACKEND_INTERNAL_URL/api/*
 *
 * Catch-all for /api/... (e.g. /api/ai/verify/:id, /api/ai/evidence/:id).
 * POST /api/ai/ask is also served by app/api/ai/ask/route.ts (explicit route
 * for reliability behind ngrok/production).
 *
 * See: lib/apiProxy.ts, deploy/ansible/README.md (ngrok, CORS).
 */
import { NextRequest } from "next/server";
import { proxyToBackend } from "@/lib/apiProxy";

export async function GET(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  return proxyToBackend(request, path);
}

export async function POST(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  return proxyToBackend(request, path);
}

export async function PUT(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  return proxyToBackend(request, path);
}

export async function PATCH(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  return proxyToBackend(request, path);
}

export async function DELETE(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  return proxyToBackend(request, path);
}

export async function OPTIONS(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> }
) {
  const { path } = await context.params;
  return proxyToBackend(request, path);
}
