import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/customer/customers?q=&type=&status=&page=&page_size= */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('customer', '/v1/customers', query);

/** POST /api/customer/customers */
export const post = async ({ data }: { data: Record<string, unknown> }) =>
  upstreamRequest('customer', 'POST', '/v1/customers', { body: data });
