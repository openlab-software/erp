import { upstreamRequest } from '../../../lib/upstream.ts';
import type { Brand } from './index.ts';

/** PUT /api/catalog/brands/:id  { description } */
export const put = async (
  id: string,
  { data }: { data: { description: string } },
) =>
  upstreamRequest<Brand>(
    'catalog',
    'PUT',
    `/v1/brands/${encodeURIComponent(id)}`,
    { body: data },
  );

/** DELETE /api/catalog/brands/:id -> { success: true } (the service answers 204 with no body) */
export const del = async (id: string) => {
  await upstreamRequest<void>(
    'catalog',
    'DELETE',
    `/v1/brands/${encodeURIComponent(id)}`,
  );
  return { success: true };
};
