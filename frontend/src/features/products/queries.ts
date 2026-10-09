import {
  keepPreviousData,
  useMutation,
  useQueries,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import {
  type ListProductsParams,
  addBarcode,
  addImage,
  autocompleteProducts,
  changeProductPrice,
  changeProductStatus,
  createProduct,
  deleteBarcode,
  deleteImage,
  deleteProduct,
  getPriceHistory,
  getProduct,
  importProducts,
  listProducts,
  replaceAttributes,
  setPrimaryImage,
  updateProduct,
} from "./api";
import type {
  AttributeFormValues,
  BarcodeFormValues,
  PriceFormValues,
  ProductFormValues,
} from "./schemas";
import type { ProductStatus } from "./types";

export const productKeys = {
  all: ["products"] as const,
  list: (params: ListProductsParams) =>
    [...productKeys.all, "list", params] as const,
  detail: (id: string) => [...productKeys.all, "detail", id] as const,
  priceHistory: (id: string) => [...productKeys.all, "prices", id] as const,
  suggestions: (q: string) => [...productKeys.all, "suggest", q] as const,
};

/** Paginated, filtered product list; keeps the previous page while the next one loads. */
export const useProducts = (params: ListProductsParams) =>
  useQuery({
    queryKey: productKeys.list(params),
    queryFn: () => listProducts(params),
    placeholderData: keepPreviousData,
  });

export const useProduct = (id: string) =>
  useQuery({ queryKey: productKeys.detail(id), queryFn: () => getProduct(id) });

/** Products by id, for screens that only hold ids (stock items, movements, counts). */
export const useProductsById = (ids: string[]) => {
  const unique = [...new Set(ids)];
  const results = useQueries({
    queries: unique.map((id) => ({
      queryKey: productKeys.detail(id),
      queryFn: () => getProduct(id),
      staleTime: 5 * 60_000,
    })),
  });
  return new Map(
    unique.flatMap((id, i) => {
      const product = results[i]?.data;
      return product ? [[id, product] as const] : [];
    }),
  );
};

export const usePriceHistory = (id: string) =>
  useQuery({
    queryKey: productKeys.priceHistory(id),
    queryFn: () => getPriceHistory(id),
  });

/** Type-ahead suggestions for the product picker; waits for at least one character. */
export const useProductSuggestions = (q: string) =>
  useQuery({
    queryKey: productKeys.suggestions(q),
    queryFn: () => autocompleteProducts(q),
    enabled: q.length > 0,
    placeholderData: keepPreviousData,
  });

/** Every product mutation refreshes all cached product queries. */
const useProductMutation = <TVariables, TResult>(
  mutationFn: (variables: TVariables) => Promise<TResult>,
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: productKeys.all }),
  });
};

export const useCreateProduct = () => useProductMutation(createProduct);

export const useUpdateProduct = () =>
  useProductMutation(({ id, values }: { id: string; values: ProductFormValues }) =>
    updateProduct(id, values),
  );

export const useDeleteProduct = () => useProductMutation(deleteProduct);

export const useChangeProductStatus = () =>
  useProductMutation(({ id, status }: { id: string; status: ProductStatus }) =>
    changeProductStatus(id, status),
  );

export const useChangeProductPrice = () =>
  useProductMutation(({ id, values }: { id: string; values: PriceFormValues }) =>
    changeProductPrice(id, values),
  );

export const useReplaceAttributes = () =>
  useProductMutation(
    ({ id, values }: { id: string; values: AttributeFormValues }) =>
      replaceAttributes(id, values),
  );

export const useAddBarcode = () =>
  useProductMutation(({ id, values }: { id: string; values: BarcodeFormValues }) =>
    addBarcode(id, values),
  );

export const useDeleteBarcode = () =>
  useProductMutation(({ id, barcodeId }: { id: string; barcodeId: string }) =>
    deleteBarcode(id, barcodeId),
  );

export const useAddImage = () =>
  useProductMutation(({ id, url }: { id: string; url: string }) =>
    addImage(id, url),
  );

export const useSetPrimaryImage = () =>
  useProductMutation(({ id, imageId }: { id: string; imageId: string }) =>
    setPrimaryImage(id, imageId),
  );

export const useDeleteImage = () =>
  useProductMutation(({ id, imageId }: { id: string; imageId: string }) =>
    deleteImage(id, imageId),
  );

export const useImportProducts = () => useProductMutation(importProducts);
