import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/accounting/invoices */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('accounting', '/v1/invoices', query);

/** POST /api/accounting/invoices */
export const post = async ({ data }: { data: Record<string, unknown> }) =>
  upstreamRequest('accounting', 'POST', '/v1/invoices', { body: data });
