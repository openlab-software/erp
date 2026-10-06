import { upstreamGet } from '../../../lib/upstream.ts';

/** GET /api/catalog/products/:id */
export const get = async (id: string) =>
  upstreamGet('catalog', `/v1/products/${encodeURIComponent(id)}`);
