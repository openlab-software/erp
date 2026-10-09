import { upstreamRequest } from '../../../../../../lib/upstream.ts';

/** POST /api/stock/stocks/:id/items/:productId/reserve  { quantity } */
export const post = async (
  id: string,
  productId: string,
  { data }: { data: { quantity: number } },
) =>
  upstreamRequest(
    'stock',
    'POST',
    `/v1/stocks/${encodeURIComponent(id)}/items/${encodeURIComponent(productId)}/reserve`,
    { body: data },
  );
