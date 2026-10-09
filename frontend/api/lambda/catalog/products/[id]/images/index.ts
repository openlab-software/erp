import { upstreamRequest } from '../../../../../lib/upstream.ts';

/** POST /api/catalog/products/:id/images  { url } */
export const post = async (id: string, { data }: { data: { url: string } }) =>
  upstreamRequest(
    'catalog',
    'POST',
    `/v1/products/${encodeURIComponent(id)}/images`,
    { body: data },
  );
