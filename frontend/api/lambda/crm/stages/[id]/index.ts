import { upstreamGet, upstreamRequest } from '../../../../lib/upstream.ts';

/** GET /api/crm/stages/:id */
export const get = async (id: string) =>
  upstreamGet('crm', `/v1/stages/${encodeURIComponent(id)}`);

/** PUT /api/crm/stages/:id */
export const put = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest('crm', 'PUT', `/v1/stages/${encodeURIComponent(id)}`, {
    body: data,
  });

/** DELETE /api/crm/stages/:id -> { success: true } */
export const del = async (id: string) => {
  await upstreamRequest<void>(
    'crm',
    'DELETE',
    `/v1/stages/${encodeURIComponent(id)}`,
  );
  return { success: true };
};
