import { upstreamGet } from '../../lib/upstream.ts';

/** GET /api/purchase/summary */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('purchase', '/v1/purchase-orders/summary', query);
