import { upstreamGet, upstreamRequest } from '../../../../../lib/upstream.ts';

/** GET /api/customer/customers/:id/contacts */
export const get = async (id: string) =>
  upstreamGet('customer', `/v1/customers/${encodeURIComponent(id)}/contacts`);

/** POST /api/customer/customers/:id/contacts */
export const post = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest(
    'customer',
    'POST',
    `/v1/customers/${encodeURIComponent(id)}/contacts`,
    { body: data },
  );
