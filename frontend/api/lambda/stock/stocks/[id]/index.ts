import { upstreamGet, upstreamRequest } from '../../../../lib/upstream.ts';

const path = (id: string) => `/v1/stocks/${encodeURIComponent(id)}`;

/** GET /api/stock/stocks/:id */
export const get = async (id: string) => upstreamGet('stock', path(id));

/** PUT /api/stock/stocks/:id  { description } */
export const put = async (
  id: string,
  { data }: { data: { description: string } },
) => upstreamRequest('stock', 'PUT', path(id), { body: data });

/** DELETE /api/stock/stocks/:id -> { success: true } */
export const del = async (id: string) => {
  await upstreamRequest<void>('stock', 'DELETE', path(id));
  return { success: true };
};
