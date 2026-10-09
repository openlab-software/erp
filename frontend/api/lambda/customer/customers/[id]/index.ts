import { upstreamGet, upstreamRequest } from '../../../../lib/upstream.ts';

const path = (id: string) => `/v1/customers/${encodeURIComponent(id)}`;

/** GET /api/customer/customers/:id */
export const get = async (id: string) => upstreamGet('customer', path(id));

/** PUT /api/customer/customers/:id */
export const put = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) => upstreamRequest('customer', 'PUT', path(id), { body: data });

/** DELETE /api/customer/customers/:id -> { success: true } */
export const del = async (id: string) => {
  await upstreamRequest<void>('customer', 'DELETE', path(id));
  return { success: true };
};
