import { upstreamGet, upstreamRequest } from '../../../../lib/upstream.ts';

/** GET /api/purchase/purchase-orders/:id */
export const get = async (id: string) =>
  upstreamGet('purchase', `/v1/purchase-orders/${encodeURIComponent(id)}`);

/** PUT /api/purchase/purchase-orders/:id */
export const put = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest(
    'purchase',
    'PUT',
    `/v1/purchase-orders/${encodeURIComponent(id)}`,
    { body: data },
  );

/** DELETE /api/purchase/purchase-orders/:id -> { success: true } */
export const del = async (id: string) => {
  await upstreamRequest<void>(
    'purchase',
    'DELETE',
    `/v1/purchase-orders/${encodeURIComponent(id)}`,
  );
  return { success: true };
};
