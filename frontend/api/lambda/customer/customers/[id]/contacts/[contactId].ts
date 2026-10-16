import { upstreamRequest } from '../../../../../lib/upstream.ts';

/** PUT /api/customer/customers/:id/contacts/:contactId */
export const put = async (
  id: string,
  contactId: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest(
    'customer',
    'PUT',
    `/v1/customers/${encodeURIComponent(id)}/contacts/${encodeURIComponent(contactId)}`,
    { body: data },
  );

/** DELETE /api/customer/customers/:id/contacts/:contactId -> { success: true } */
export const del = async (id: string, contactId: string) => {
  await upstreamRequest<void>(
    'customer',
    'DELETE',
    `/v1/customers/${encodeURIComponent(id)}/contacts/${encodeURIComponent(contactId)}`,
  );
  return { success: true };
};
