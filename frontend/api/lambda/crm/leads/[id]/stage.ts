import { upstreamRequest } from '../../../../lib/upstream.ts';

/** PATCH /api/crm/leads/:id/stage */
export const patch = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest('crm', 'PATCH', `/v1/leads/${encodeURIComponent(id)}/stage`, {
    body: data,
  });
