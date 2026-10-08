import { HttpError } from '@modern-js/bff-core';

/**
 * Base URLs of the backend services, resolved server-side only (never shipped to the
 * browser). In Kubernetes they are the cluster DNS names of the Services, e.g.
 * http://catalog-service.erp.svc.cluster.local — see .devops/k8s/frontend/deployment.yaml.
 * Locally they come from frontend/.env.development (the `make catalog` / `make stock` dev servers).
 * There are no defaults: both variables must be set.
 */
const upstreams = {
  catalog: process.env.CATALOG_SERVICE_URL,
  stock: process.env.STOCK_SERVICE_URL,
} as const;

export type Upstream = keyof typeof upstreams;

const TIMEOUT_MS = Number(process.env.UPSTREAM_TIMEOUT_MS ?? 10_000);

interface RequestOptions {
  query?: Record<string, unknown>;
  body?: unknown;
}

export async function upstreamRequest<T>(
  service: Upstream,
  method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE',
  path: string,
  { query, body }: RequestOptions = {},
): Promise<T> {
  const url = new URL(`${upstreams[service]}${path}`);
  for (const [key, value] of Object.entries(query ?? {})) {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, String(value));
    }
  }

  let response: Response;
  try {
    response = await fetch(url, {
      method,
      headers: {
        accept: 'application/json',
        ...(body !== undefined && { 'content-type': 'application/json' }),
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal: AbortSignal.timeout(TIMEOUT_MS),
    });
  } catch (error) {
    console.error(`[bff] ${service} unreachable: ${method} ${url}`, error);
    throw new HttpError(502, `${service}-service unavailable`);
  }

  if (!response.ok) {
    // Business errors come as {"message": "..."}; bean-validation errors as
    // {"mensagem": "...", "erros": {...}}.
    const payload = await response.json().catch(() => ({}));
    throw new HttpError(
      response.status,
      payload.message ?? payload.mensagem ?? response.statusText,
    );
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

export const upstreamGet = <T>(
  service: Upstream,
  path: string,
  query?: Record<string, unknown>,
) => upstreamRequest<T>(service, 'GET', path, { query });
