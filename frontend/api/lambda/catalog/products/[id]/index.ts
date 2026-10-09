import { upstreamGet, upstreamRequest } from '../../../../lib/upstream.ts';

const path = (id: string) => `/v1/products/${encodeURIComponent(id)}`;

/** GET /api/catalog/products/:id */
export const get = async (id: string) => upstreamGet('catalog', path(id));

/** PUT /api/catalog/products/:id */
export const put = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) => upstreamRequest('catalog', 'PUT', path(id), { body: data });

/** DELETE /api/catalog/products/:id -> { success: true } */
export const del = async (id: string) => {
  await upstreamRequest<void>('catalog', 'DELETE', path(id));
  return { success: true };
};
