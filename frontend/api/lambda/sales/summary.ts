import { upstreamGet } from '../../lib/upstream.ts';

/** GET /api/sales/summary */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('sales', '/v1/sales-orders/summary', query);
