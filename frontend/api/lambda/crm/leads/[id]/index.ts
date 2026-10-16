import { upstreamGet, upstreamRequest } from '../../../../lib/upstream.ts';

/** GET /api/crm/leads/:id */
export const get = async (id: string) =>
  upstreamGet('crm', `/v1/leads/${encodeURIComponent(id)}`);

/** PUT /api/crm/leads/:id */
export const put = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest('crm', 'PUT', `/v1/leads/${encodeURIComponent(id)}`, {
    body: data,
  });

/** DELETE /api/crm/leads/:id -> { success: true } */
export const del = async (id: string) => {
  await upstreamRequest<void>(
    'crm',
    'DELETE',
    `/v1/leads/${encodeURIComponent(id)}`,
  );
  return { success: true };
};
