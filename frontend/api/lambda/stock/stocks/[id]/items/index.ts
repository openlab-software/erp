import { upstreamGet } from '../../../../../lib/upstream.ts';

/** GET /api/stock/stocks/:id/items?product_id=&below_min=&above_max=&page=&page_size= */
export const get = async (
  id: string,
  { query }: { query?: Record<string, string> } = {},
) => upstreamGet('stock', `/v1/stocks/${encodeURIComponent(id)}/items`, query);
