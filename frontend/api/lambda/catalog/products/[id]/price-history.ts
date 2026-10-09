import { upstreamGet } from '../../../../lib/upstream.ts';

/** GET /api/catalog/products/:id/price-history */
export const get = async (id: string) =>
  upstreamGet(
    'catalog',
    `/v1/products/${encodeURIComponent(id)}/price-history`,
  );
