import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/catalog/categories/:id */
export const get = async (id: string) =>
  upstreamGet('catalog', `/v1/categories/${encodeURIComponent(id)}`);

/** PUT /api/catalog/categories/:id */
export const put = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest(
    'catalog',
    'PUT',
    `/v1/categories/${encodeURIComponent(id)}`,
    { body: data },
  );

/** DELETE /api/catalog/categories/:id -> { success: true } (the service answers 204) */
export const del = async (id: string) => {
  await upstreamRequest<void>(
    'catalog',
    'DELETE',
    `/v1/categories/${encodeURIComponent(id)}`,
  );
  return { success: true };
};
