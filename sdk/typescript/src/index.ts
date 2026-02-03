export type FetchLike = typeof fetch;

export interface SignResponse {
  id: number;
  responseHash: string;
  signature: string;
  tsaToken: string;
  claim?: string | null;
  confidence?: number | null;
  policyVersion?: string | null;
  modelId?: string;
  createdAt?: string;
}

export interface VerifyResponse {
  id: number;
  prompt: string;
  response: string;
  responseHash: string;
  signature: string | null;
  tsaToken: string | null;
  llmModel: string;
  createdAt: string;
  requestId?: string | null;
  temperature?: number | null;
  systemPrompt?: string | null;
  version?: number | null;
  claim?: string | null;
  confidence?: number | null;
  policyVersion?: string | null;
  hashMatch?: boolean;
  signatureValid?: string;
  signaturePqc?: string | null;
  pqcAlgorithm?: string | null;
}

export class AletheiaAPIError extends Error {
  status: number;
  payload?: unknown;

  constructor(status: number, message: string, payload?: unknown) {
    super(`Aletheia API error (${status}): ${message}`);
    this.status = status;
    this.payload = payload;
  }
}

export class AletheiaClient {
  private baseUrl: string;
  private timeout: number;
  private fetchFn: FetchLike;

  constructor(
    baseUrl: string,
    options?: {
      timeout?: number;
      fetch?: FetchLike;
    }
  ) {
    if (!baseUrl) {
      throw new Error("baseUrl is required");
    }
    this.baseUrl = baseUrl.replace(/\/+$/, "");
    this.timeout = options?.timeout ?? 30_000;
    this.fetchFn = options?.fetch ?? fetch;
  }

  async sign(
    response: string,
    options?: {
      modelId?: string;
      policyId?: string;
      prompt?: string;
      requestId?: string;
    }
  ): Promise<SignResponse> {
    const payload: Record<string, string> = { response };
    if (options?.modelId) payload.modelId = options.modelId;
    if (options?.policyId) payload.policyId = options.policyId;
    if (options?.prompt) payload.prompt = options.prompt;
    if (options?.requestId) payload.requestId = options.requestId;
    return this.requestJson("/api/sign", {
      method: "POST",
      body: JSON.stringify(payload),
      headers: { "Content-Type": "application/json" },
    });
  }

  async verify(id: number | string): Promise<VerifyResponse> {
    return this.requestJson(`/api/ai/verify/${id}`);
  }

  async getEvidence(id: number | string): Promise<ArrayBuffer> {
    return this.requestBinary(`/api/ai/evidence/${id}`);
  }

  private async requestJson<T>(path: string, init: RequestInit = {}): Promise<T> {
    const response = await this.request(path, init);
    const text = await response.text();
    if (!response.ok) {
      throw this.buildError(response.status, text);
    }
    return text ? (JSON.parse(text) as T) : ({} as T);
  }

  private async requestBinary(path: string, init: RequestInit = {}): Promise<ArrayBuffer> {
    const response = await this.request(path, init);
    if (!response.ok) {
      const text = await response.text();
      throw this.buildError(response.status, text);
    }
    return response.arrayBuffer();
  }

  private async request(path: string, init: RequestInit): Promise<Response> {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), this.timeout);
    try {
      return await this.fetchFn(`${this.baseUrl}${path}`, {
        ...init,
        signal: controller.signal,
      });
    } finally {
      clearTimeout(timer);
    }
  }

  private buildError(status: number, bodyText: string): AletheiaAPIError {
    try {
      const payload = JSON.parse(bodyText);
      if (payload && typeof payload === "object") {
        for (const key of ["message", "error", "code", "details"]) {
          if (payload[key]) {
            return new AletheiaAPIError(status, String(payload[key]), payload);
          }
        }
      }
      return new AletheiaAPIError(status, bodyText, payload);
    } catch {
      return new AletheiaAPIError(status, bodyText || "Unknown error");
    }
  }
}

export function createClient(baseUrl?: string): AletheiaClient {
  const resolved = baseUrl ?? process.env.ALETHEIA_API_URL;
  if (!resolved) {
    throw new Error("baseUrl is required (or set ALETHEIA_API_URL)");
  }
  return new AletheiaClient(resolved);
}

export async function sign(
  response: string,
  options?: {
    modelId?: string;
    policyId?: string;
    prompt?: string;
    requestId?: string;
    baseUrl?: string;
  }
): Promise<SignResponse> {
  const client = createClient(options?.baseUrl);
  return client.sign(response, options);
}

export async function verify(
  id: number | string,
  baseUrl?: string
): Promise<VerifyResponse> {
  const client = createClient(baseUrl);
  return client.verify(id);
}

export async function getEvidence(
  id: number | string,
  baseUrl?: string
): Promise<ArrayBuffer> {
  const client = createClient(baseUrl);
  return client.getEvidence(id);
}
