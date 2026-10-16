import { upstreamGet, upstreamRequest } from '../../../../lib/upstream.ts';

/** GET /api/sales/sales-orders/:id */
export const get = async (id: string) =>
  upstreamGet('sales', `/v1/sales-orders/${encodeURIComponent(id)}`);

/** PUT /api/sales/sales-orders/:id */
export const put = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest(
    'sales',
    'PUT',
    `/v1/sales-orders/${encodeURIComponent(id)}`,
    { body: data },
  );

/** DELETE /api/sales/sales-orders/:id -> { success: true } */
export const del = async (id: string) => {
  await upstreamRequest<void>(
    'sales',
    'DELETE',
    `/v1/sales-orders/${encodeURIComponent(id)}`,
  );
  return { success: true };
};
