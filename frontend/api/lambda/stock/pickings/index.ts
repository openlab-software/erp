import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/stock/pickings */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('stock', '/v1/pickings', query);

/** POST /api/stock/pickings */
export const post = async ({ data }: { data: Record<string, unknown> }) =>
  upstreamRequest('stock', 'POST', '/v1/pickings', { body: data });
