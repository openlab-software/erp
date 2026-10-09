import { upstreamRequest } from '../../../../../lib/upstream.ts';

/** DELETE /api/catalog/products/:id/barcodes/:barcodeId -> { success: true } */
export const del = async (id: string, barcodeId: string) => {
  await upstreamRequest<void>(
    'catalog',
    'DELETE',
    `/v1/products/${encodeURIComponent(id)}/barcodes/${encodeURIComponent(barcodeId)}`,
  );
  return { success: true };
};
