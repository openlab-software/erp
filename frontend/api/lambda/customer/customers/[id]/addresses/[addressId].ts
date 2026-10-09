import { upstreamRequest } from '../../../../../lib/upstream.ts';

const path = (id: string, addressId: string) =>
  `/v1/customers/${encodeURIComponent(id)}/addresses/${encodeURIComponent(addressId)}`;

/** PUT /api/customer/customers/:id/addresses/:addressId */
export const put = async (
  id: string,
  addressId: string,
  { data }: { data: Record<string, unknown> },
) => upstreamRequest('customer', 'PUT', path(id, addressId), { body: data });

/** DELETE /api/customer/customers/:id/addresses/:addressId -> { success: true } */
export const del = async (id: string, addressId: string) => {
  await upstreamRequest<void>('customer', 'DELETE', path(id, addressId));
  return { success: true };
};
