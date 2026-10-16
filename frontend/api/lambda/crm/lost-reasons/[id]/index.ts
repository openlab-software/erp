import { upstreamGet, upstreamRequest } from '../../../../lib/upstream.ts';

/** GET /api/crm/lost-reasons/:id */
export const get = async (id: string) =>
  upstreamGet('crm', `/v1/lost-reasons/${encodeURIComponent(id)}`);

/** PUT /api/crm/lost-reasons/:id */
export const put = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest('crm', 'PUT', `/v1/lost-reasons/${encodeURIComponent(id)}`, {
    body: data,
  });

/** DELETE /api/crm/lost-reasons/:id -> { success: true } */
export const del = async (id: string) => {
  await upstreamRequest<void>(
    'crm',
    'DELETE',
    `/v1/lost-reasons/${encodeURIComponent(id)}`,
  );
  return { success: true };
};
