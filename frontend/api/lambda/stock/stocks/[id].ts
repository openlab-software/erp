import { upstreamGet } from '../../../lib/upstream.ts';

/** GET /api/stock/stocks/:id */
export const get = async (id: string) =>
  upstreamGet('stock', `/v1/stocks/${encodeURIComponent(id)}`);
