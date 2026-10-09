import { upstreamRequest } from '../../../../lib/upstream.ts';

/** PATCH /api/customer/customers/:id/status  { status } */
export const patch = async (
  id: string,
  { data }: { data: { status: string } },
) =>
  upstreamRequest(
    'customer',
    'PATCH',
    `/v1/customers/${encodeURIComponent(id)}/status`,
    { body: data },
  );
