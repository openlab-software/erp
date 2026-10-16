import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/sales/sales-orders */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('sales', '/v1/sales-orders', query);

/** POST /api/sales/sales-orders */
export const post = async ({ data }: { data: Record<string, unknown> }) =>
  upstreamRequest('sales', 'POST', '/v1/sales-orders', { body: data });
