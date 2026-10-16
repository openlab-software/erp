import { upstreamGet, upstreamRequest } from '../../../../../lib/upstream.ts';

/** GET /api/crm/leads/:id/activities */
export const get = async (id: string) =>
  upstreamGet('crm', `/v1/leads/${encodeURIComponent(id)}/activities`);

/** POST /api/crm/leads/:id/activities */
export const post = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest(
    'crm',
    'POST',
    `/v1/leads/${encodeURIComponent(id)}/activities`,
    { body: data },
  );
