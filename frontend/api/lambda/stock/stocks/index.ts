import { upstreamGet } from '../../../lib/upstream.ts';

/** GET /api/stock/stocks?page=&page_size= */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('stock', '/v1/stocks', query);
