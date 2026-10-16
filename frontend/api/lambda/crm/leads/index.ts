import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/crm/leads */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('crm', '/v1/leads', query);

/** POST /api/crm/leads */
export const post = async ({ data }: { data: Record<string, unknown> }) =>
  upstreamRequest('crm', 'POST', '/v1/leads', { body: data });
