import { upstreamRequest } from '../../../../lib/upstream.ts';

/** PATCH /api/catalog/products/:id/price  { sale_price, cost_price } */
export const patch = async (
  id: string,
  { data }: { data: { sale_price: number; cost_price: number } },
) =>
  upstreamRequest(
    'catalog',
    'PATCH',
    `/v1/products/${encodeURIComponent(id)}/price`,
    { body: data },
  );
