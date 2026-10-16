import { upstreamRequest } from '../../../../lib/upstream.ts';

/** POST /api/accounting/invoices/:id/cancel */
export const post = async (
  id: string,
  { data }: { data?: Record<string, unknown> } = {},
) =>
  upstreamRequest(
    'accounting',
    'POST',
    `/v1/invoices/${encodeURIComponent(id)}/cancel`,
    { body: data ?? {} },
  );
