import { upstreamRequest } from '../../../../../lib/upstream.ts';

/** POST /api/catalog/products/:id/barcodes  { code, type } */
export const post = async (
  id: string,
  { data }: { data: { code: string; type: string } },
) =>
  upstreamRequest(
    'catalog',
    'POST',
    `/v1/products/${encodeURIComponent(id)}/barcodes`,
    { body: data },
  );
