import { upstreamRequest } from '../../../../../../lib/upstream.ts';

/** POST /api/crm/leads/:id/activities/:activityId/done */
export const post = async (
  id: string,
  activityId: string,
  { data }: { data?: Record<string, unknown> } = {},
) =>
  upstreamRequest(
    'crm',
    'POST',
    `/v1/leads/${encodeURIComponent(id)}/activities/${encodeURIComponent(activityId)}/done`,
    { body: data ?? {} },
  );
