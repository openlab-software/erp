import { upstreamGet, upstreamRequest } from '../../../lib/upstream.ts';

// Same shape as catalog-service's JSON (snake_case): the BFF passes it through untouched.
export interface Brand {
  brand_id: string;
  description: string;
  created_at: string;
  /** `null` until the brand is edited for the first time. */
  updated_at: string | null;
}

export interface BrandPage {
  data: Brand[];
  page: number;
  page_size: number;
  total: number;
}

/** GET /api/catalog/brands?q=&page=&page_size= */
export const get = async ({ query }: { query?: Record<string, string> } = {}) =>
  upstreamGet<BrandPage>('catalog', '/v1/brands', query);

/** POST /api/catalog/brands  { description } */
export const post = async ({ data }: { data: { description: string } }) =>
  upstreamRequest<Brand>('catalog', 'POST', '/v1/brands', { body: data });
