import { upstreamRequest } from '../../../../lib/upstream.ts';

/** POST /api/sales/sales-orders/:id/draft */
export const post = async (
  id: string,
  { data }: { data?: Record<string, unknown> } = {},
) =>
  upstreamRequest(
    'sales',
    'POST',
    `/v1/sales-orders/${encodeURIComponent(id)}/draft`,
    { body: data ?? {} },
  );
