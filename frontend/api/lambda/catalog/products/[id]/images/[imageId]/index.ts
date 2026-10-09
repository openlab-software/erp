import { upstreamRequest } from '../../../../../../lib/upstream.ts';

/** DELETE /api/catalog/products/:id/images/:imageId -> { success: true } */
export const del = async (id: string, imageId: string) => {
  await upstreamRequest<void>(
    'catalog',
    'DELETE',
    `/v1/products/${encodeURIComponent(id)}/images/${encodeURIComponent(imageId)}`,
  );
  return { success: true };
};
