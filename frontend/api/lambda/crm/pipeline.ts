import { upstreamGet } from '../../lib/upstream.ts';

/** GET /api/crm/pipeline */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('crm', '/v1/pipeline', query);
