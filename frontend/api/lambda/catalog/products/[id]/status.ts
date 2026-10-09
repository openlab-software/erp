import { upstreamRequest } from '../../../../lib/upstream.ts';

/** PATCH /api/catalog/products/:id/status  { status } */
export const patch = async (
  id: string,
  { data }: { data: { status: string } },
) =>
  upstreamRequest(
    'catalog',
    'PATCH',
    `/v1/products/${encodeURIComponent(id)}/status`,
    { body: data },
  );
