import { upstreamGet } from '../../../lib/upstream.ts';

/** GET /api/catalog/products?q=&category_id=&status=&type=&brand_id=&page=&page_size= */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('catalog', '/v1/products', query);
