import { upstreamGet, upstreamRequest } from '../../../../lib/upstream.ts';

const path = (id: string) => `/v1/stocks/${encodeURIComponent(id)}/counts`;

/** GET /api/stock/stocks/:id/counts?product_id=&page=&page_size= */
export const get = async (
  id: string,
  { query }: { query?: Record<string, string> } = {},
) => upstreamGet('stock', path(id), query);

/** POST /api/stock/stocks/:id/counts  { product_id, counted_value } */
export const post = async (
  id: string,
  { data }: { data: { product_id: string; counted_value: number } },
) => upstreamRequest('stock', 'POST', path(id), { body: data });
