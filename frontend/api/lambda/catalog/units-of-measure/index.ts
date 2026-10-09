import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/catalog/units-of-measure?q=&page=&page_size= */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('catalog', '/v1/units-of-measure', query);

/** POST /api/catalog/units-of-measure */
export const post = async ({ data }: { data: Record<string, unknown> }) =>
  upstreamRequest('catalog', 'POST', '/v1/units-of-measure', { body: data });
