import { upstreamRequest } from '../../../lib/upstream.ts';

/** POST /api/stock/reassignments  { from_stock_id, to_stock_id, items: [{ product_id, quantity }] } */
export const post = async ({ data }: { data: Record<string, unknown> }) =>
  upstreamRequest('stock', 'POST', '/v1/reassignments', { body: data });
