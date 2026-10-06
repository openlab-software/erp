import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import {
  type ListBrandsParams,
  createBrand,
  deleteBrand,
  listBrands,
  updateBrand,
} from "./api";

export const brandKeys = {
  all: ["brands"] as const,
  list: (params: ListBrandsParams) =>
    [...brandKeys.all, "list", params] as const,
};

/** Paginated brand list; keeps the previous page on screen while the next one loads. */
export const useBrands = (params: ListBrandsParams) =>
  useQuery({
    queryKey: brandKeys.list(params),
    queryFn: () => listBrands(params),
    placeholderData: keepPreviousData,
  });

/** Every mutation refreshes all cached brand lists. */
const useBrandMutation = <TVariables, TResult>(
  mutationFn: (variables: TVariables) => Promise<TResult>,
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: brandKeys.all }),
  });
};

export const useCreateBrand = () => useBrandMutation(createBrand);

export const useUpdateBrand = () =>
  useBrandMutation(({ id, description }: { id: string; description: string }) =>
    updateBrand(id, description),
  );

export const useDeleteBrand = () => useBrandMutation(deleteBrand);
