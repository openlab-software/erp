import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/stock/movement-reasons?q=&page=&page_size= */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('stock', '/v1/movement-reasons', query);

/** POST /api/stock/movement-reasons  { description } */
export const post = async ({ data }: { data: { description: string } }) =>
  upstreamRequest('stock', 'POST', '/v1/movement-reasons', { body: data });
