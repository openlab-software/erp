import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/catalog/suppliers?q=&page=&page_size= */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('catalog', '/v1/suppliers', query);

/** POST /api/catalog/suppliers */
export const post = async ({ data }: { data: Record<string, unknown> }) =>
  upstreamRequest('catalog', 'POST', '/v1/suppliers', { body: data });
