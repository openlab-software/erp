import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/catalog/categories?q=&page=&page_size= */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('catalog', '/v1/categories', query);

/** POST /api/catalog/categories */
export const post = async ({ data }: { data: Record<string, unknown> }) =>
  upstreamRequest('catalog', 'POST', '/v1/categories', { body: data });
