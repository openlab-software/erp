import { upstreamRequest } from '../../../lib/upstream.ts';

const path = (id: string) => `/v1/movement-reasons/${encodeURIComponent(id)}`;

/** PUT /api/stock/movement-reasons/:id  { description } */
export const put = async (
  id: string,
  { data }: { data: { description: string } },
) => upstreamRequest('stock', 'PUT', path(id), { body: data });

/** DELETE /api/stock/movement-reasons/:id -> { success: true } */
export const del = async (id: string) => {
  await upstreamRequest<void>('stock', 'DELETE', path(id));
  return { success: true };
};
