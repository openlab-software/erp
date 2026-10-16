import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/purchase/purchase-orders */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('purchase', '/v1/purchase-orders', query);

/** POST /api/purchase/purchase-orders */
export const post = async ({ data }: { data: Record<string, unknown> }) =>
  upstreamRequest('purchase', 'POST', '/v1/purchase-orders', { body: data });
