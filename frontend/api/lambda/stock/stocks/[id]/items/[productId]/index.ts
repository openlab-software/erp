import { upstreamGet } from '../../../../../../lib/upstream.ts';

/** GET /api/stock/stocks/:id/items/:productId */
export const get = async (id: string, productId: string) =>
  upstreamGet(
    'stock',
    `/v1/stocks/${encodeURIComponent(id)}/items/${encodeURIComponent(productId)}`,
  );
