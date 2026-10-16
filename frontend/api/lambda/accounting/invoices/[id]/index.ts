import { upstreamGet, upstreamRequest } from '../../../../lib/upstream.ts';

/** GET /api/accounting/invoices/:id */
export const get = async (id: string) =>
  upstreamGet('accounting', `/v1/invoices/${encodeURIComponent(id)}`);

/** PUT /api/accounting/invoices/:id */
export const put = async (
  id: string,
  { data }: { data: Record<string, unknown> },
) =>
  upstreamRequest(
    'accounting',
    'PUT',
    `/v1/invoices/${encodeURIComponent(id)}`,
    { body: data },
  );

/** DELETE /api/accounting/invoices/:id -> { success: true } */
export const del = async (id: string) => {
  await upstreamRequest<void>(
    'accounting',
    'DELETE',
    `/v1/invoices/${encodeURIComponent(id)}`,
  );
  return { success: true };
};
