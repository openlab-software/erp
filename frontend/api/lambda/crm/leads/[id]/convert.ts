import { upstreamRequest } from '../../../../lib/upstream.ts';

/** POST /api/crm/leads/:id/convert */
export const post = async (
  id: string,
  { data }: { data?: Record<string, unknown> } = {},
) =>
  upstreamRequest(
    'crm',
    'POST',
    `/v1/leads/${encodeURIComponent(id)}/convert`,
    { body: data ?? {} },
  );
