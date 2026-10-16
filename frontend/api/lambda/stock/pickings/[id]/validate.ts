import { upstreamRequest } from '../../../../lib/upstream.ts';

/** POST /api/stock/pickings/:id/validate */
export const post = async (
  id: string,
  { data }: { data?: Record<string, unknown> } = {},
) =>
  upstreamRequest(
    'stock',
    'POST',
    `/v1/pickings/${encodeURIComponent(id)}/validate`,
    { body: data ?? {} },
  );
