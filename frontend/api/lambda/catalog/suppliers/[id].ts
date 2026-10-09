import { upstreamRequest } from '../../../lib/upstream.ts';

/** PUT /api/catalog/suppliers/:id */
export const put = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest(
    'catalog',
    'PUT',
    `/v1/suppliers/${encodeURIComponent(id)}`,
    { body: data },
  );

/** DELETE /api/catalog/suppliers/:id -> { success: true } (the service answers 204) */
export const del = async (id: string) => {
  await upstreamRequest<void>(
    'catalog',
    'DELETE',
    `/v1/suppliers/${encodeURIComponent(id)}`,
  );
  return { success: true };
};
