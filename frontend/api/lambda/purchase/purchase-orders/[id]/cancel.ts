import { upstreamRequest } from '../../../../lib/upstream.ts';

/** POST /api/purchase/purchase-orders/:id/cancel */
export const post = async (
  id: string,
  { data }: { data?: Record<string, unknown> } = {},
) =>
  upstreamRequest(
    'purchase',
    'POST',
    `/v1/purchase-orders/${encodeURIComponent(id)}/cancel`,
    { body: data ?? {} },
  );
