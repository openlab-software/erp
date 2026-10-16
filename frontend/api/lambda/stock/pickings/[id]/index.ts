import { upstreamGet, upstreamRequest } from '../../../../lib/upstream.ts';

/** GET /api/stock/pickings/:id */
export const get = async (id: string) =>
  upstreamGet('stock', `/v1/pickings/${encodeURIComponent(id)}`);

/** PUT /api/stock/pickings/:id */
export const put = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest('stock', 'PUT', `/v1/pickings/${encodeURIComponent(id)}`, {
    body: data,
  });

/** DELETE /api/stock/pickings/:id -> { success: true } */
export const del = async (id: string) => {
  await upstreamRequest<void>(
    'stock',
    'DELETE',
    `/v1/pickings/${encodeURIComponent(id)}`,
  );
  return { success: true };
};
