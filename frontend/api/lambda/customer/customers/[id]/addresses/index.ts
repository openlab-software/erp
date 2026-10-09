import { upstreamGet, upstreamRequest } from '../../../../../lib/upstream.ts';

const path = (id: string) => `/v1/customers/${encodeURIComponent(id)}/addresses`;

/** GET /api/customer/customers/:id/addresses -> { data: [...] } */
export const get = async (id: string) => upstreamGet('customer', path(id));

/** POST /api/customer/customers/:id/addresses */
export const post = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) => upstreamRequest('customer', 'POST', path(id), { body: data });
