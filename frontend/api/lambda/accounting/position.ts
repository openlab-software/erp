import { upstreamGet } from '../../lib/upstream.ts';

/** GET /api/accounting/position */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet('accounting', '/v1/accounting/position', query);
