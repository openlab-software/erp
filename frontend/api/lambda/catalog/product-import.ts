import { upstreamRequest } from '../../lib/upstream.ts';

/** POST /api/catalog/product-import  { csv } -> { total, created, failed: [{ line, error }] } */
export const post = async ({ data }: { data: { csv: string } }) => {
  const form = new FormData();
  form.append('file', new Blob([data.csv], { type: 'text/csv' }), 'products.csv');
  return upstreamRequest('catalog', 'POST', '/v1/products/import', { form });
};
