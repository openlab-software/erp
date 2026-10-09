import { upstreamRequest } from '../../lib/upstream.ts';

/** GET /api/catalog/product-export?q=&category_id=&status=&type=&brand_id= -> { csv } */
export const get = async ({ query }: { query?: Record<string, string> } = {}) => ({
  csv: await upstreamRequest<string>('catalog', 'GET', '/v1/products/export', {
    query,
    text: true,
  }),
});
