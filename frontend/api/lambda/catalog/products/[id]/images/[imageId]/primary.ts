import { upstreamRequest } from '../../../../../../lib/upstream.ts';

/** PATCH /api/catalog/products/:id/images/:imageId/primary -> { success: true } */
export const patch = async (id: string, imageId: string) => {
  await upstreamRequest<unknown>(
    'catalog',
    'PATCH',
    `/v1/products/${encodeURIComponent(id)}/images/${encodeURIComponent(imageId)}/primary`,
  );
  return { success: true };
};
