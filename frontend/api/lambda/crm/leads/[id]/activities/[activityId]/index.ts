import { upstreamRequest } from '../../../../../../lib/upstream.ts';

/** PUT /api/crm/leads/:id/activities/:activityId */
export const put = async (
  id: string,
  activityId: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest(
    'crm',
    'PUT',
    `/v1/leads/${encodeURIComponent(id)}/activities/${encodeURIComponent(activityId)}`,
    { body: data },
  );

/** DELETE /api/crm/leads/:id/activities/:activityId -> { success: true } */
export const del = async (id: string, activityId: string) => {
  await upstreamRequest<void>(
    'crm',
    'DELETE',
    `/v1/leads/${encodeURIComponent(id)}/activities/${encodeURIComponent(activityId)}`,
  );
  return { success: true };
};
