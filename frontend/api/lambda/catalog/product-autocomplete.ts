import { upstreamGet } from '../../lib/upstream.ts';

/** GET /api/catalog/product-autocomplete?q=&limit= */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('catalog', '/v1/products/autocomplete', query);
