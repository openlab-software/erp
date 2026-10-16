import { upstreamRequest } from '../../../../lib/upstream.ts';

/** POST /api/stock/pickings/:id/cancel */
export const post = async (
  id: string,
  { data }: { data?: Record<string, unknown> } = {},
) =>
  upstreamRequest(
    'stock',
    'POST',
    `/v1/pickings/${encodeURIComponent(id)}/cancel`,
    { body: data ?? {} },
  );
