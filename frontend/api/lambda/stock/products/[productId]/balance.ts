import { upstreamGet } from '../../../../lib/upstream.ts';

/** GET /api/stock/products/:productId/balance */
export const get = async (productId: string) =>
  upstreamGet('stock', `/v1/products/${encodeURIComponent(productId)}/balance`);
