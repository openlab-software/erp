import { upstreamGet } from '../../lib/upstream.ts';

/** GET /api/stock/replenishment */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('stock', '/v1/replenishment', query);
