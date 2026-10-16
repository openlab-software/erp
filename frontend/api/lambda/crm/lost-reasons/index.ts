import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/crm/lost-reasons */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('crm', '/v1/lost-reasons', query);

/** POST /api/crm/lost-reasons */
export const post = async ({ data }: { data: Record<string, unknown> }) =>
  upstreamRequest('crm', 'POST', '/v1/lost-reasons', { body: data });
