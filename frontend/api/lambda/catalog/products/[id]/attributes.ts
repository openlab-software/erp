import { upstreamRequest } from '../../../../lib/upstream.ts';

/** PUT /api/catalog/products/:id/attributes  { attributes: [{ name, value }] } */
export const put = async (
  id: string,
  { data }: { data: { attributes: { name: string; value: string }[] } },
) =>
  upstreamRequest(
    'catalog',
    'PUT',
    `/v1/products/${encodeURIComponent(id)}/attributes`,
    { body: data },
  );
