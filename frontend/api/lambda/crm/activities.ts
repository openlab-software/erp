import { upstreamGet } from '../../lib/upstream.ts';

/** GET /api/crm/activities */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('crm', '/v1/activities', query);
