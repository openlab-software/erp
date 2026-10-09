import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/stock/stocks?q=&page=&page_size= */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('stock', '/v1/stocks', query);

/** POST /api/stock/stocks  { description } */
export const post = async ({ data }: { data: { description: string } }) =>
  upstreamRequest('stock', 'POST', '/v1/stocks', { body: data });
