import { upstreamRequest } from '../../../../lib/upstream.ts';

/** POST /api/crm/leads/:id/reopen */
export const post = async (
  id: string,
  { data }: { data?: Record<string, unknown> } = {},
) =>
  upstreamRequest('crm', 'POST', `/v1/leads/${encodeURIComponent(id)}/reopen`, {
    body: data ?? {},
  });
