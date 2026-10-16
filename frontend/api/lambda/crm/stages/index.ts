import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

/** GET /api/crm/stages */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('crm', '/v1/stages', query);

/** POST /api/crm/stages */
export const post = async ({ data }: { data: Record<string, unknown> }) =>
  upstreamRequest('crm', 'POST', '/v1/stages', { body: data });
