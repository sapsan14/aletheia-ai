/**
 * Explicit route for POST /api/ai/ask (and GET/OPTIONS for CORS).
 * Proxies to backend. Exists so this path is matched reliably in production (ngrok).
 */
import { NextRequest } from "next/server";
import { proxyToBackend } from "@/lib/apiProxy";

export async function GET(request: NextRequest) {
  return proxyToBackend(request, "ai/ask");
}

export async function POST(request: NextRequest) {
  return proxyToBackend(request, "ai/ask");
}

export async function OPTIONS(request: NextRequest) {
  return proxyToBackend(request, "ai/ask");
}
