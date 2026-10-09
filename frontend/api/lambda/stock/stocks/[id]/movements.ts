import { upstreamGet, upstreamRequest } from '../../../../lib/upstream.ts';

const path = (id: string) => `/v1/stocks/${encodeURIComponent(id)}/movements`;

/** GET /api/stock/stocks/:id/movements?product_id=&type=&from=&to=&page=&page_size= */
export const get = async (
  id: string,
  { query }: { query?: Record<string, string> } = {},
) => upstreamGet('stock', path(id), query);

/** POST /api/stock/stocks/:id/movements  { product_id, type, quantity, reason_id?, reference? } */
export const post = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) => upstreamRequest('stock', 'POST', path(id), { body: data });
