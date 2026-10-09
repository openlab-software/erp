import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import {
  type ListCountsParams,
  type ListItemsParams,
  type ListMovementsParams,
  createReassignment,
  createStockCount,
  createStockMovement,
  getProductBalance,
  listStockCounts,
  listStockItems,
  listStockMovements,
  releaseStock,
  reserveStock,
} from "./api";
import type { CountFormValues, MovementFormValues, TransferFormValues } from "./schemas";

// Everything stock-related lives under ["stock", …] so one invalidation refreshes all of it
// (a movement changes items, balances, counts and the movement list at once).
export const stockKeys = {
  all: ["stock"] as const,
  items: (params: ListItemsParams) => [...stockKeys.all, "items", params] as const,
  movements: (params: ListMovementsParams) =>
    [...stockKeys.all, "movements", params] as const,
  counts: (params: ListCountsParams) => [...stockKeys.all, "counts", params] as const,
  balance: (productId: string) => [...stockKeys.all, "balance", productId] as const,
};

export const useStockItems = (params: ListItemsParams, enabled = true) =>
  useQuery({
    queryKey: stockKeys.items(params),
    queryFn: () => listStockItems(params),
    placeholderData: keepPreviousData,
    enabled: enabled && !!params.stockId,
  });

export const useStockMovements = (params: ListMovementsParams, enabled = true) =>
  useQuery({
    queryKey: stockKeys.movements(params),
    queryFn: () => listStockMovements(params),
    placeholderData: keepPreviousData,
    enabled: enabled && !!params.stockId,
  });

export const useStockCounts = (params: ListCountsParams, enabled = true) =>
  useQuery({
    queryKey: stockKeys.counts(params),
    queryFn: () => listStockCounts(params),
    placeholderData: keepPreviousData,
    enabled: enabled && !!params.stockId,
  });

export const useProductBalance = (productId: string) =>
  useQuery({
    queryKey: stockKeys.balance(productId),
    queryFn: () => getProductBalance(productId),
  });

const useStockMutation = <TVariables, TResult>(
  mutationFn: (variables: TVariables) => Promise<TResult>,
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: stockKeys.all }),
  });
};

export const useCreateMovement = () =>
  useStockMutation(({ stockId, values }: { stockId: string; values: MovementFormValues }) =>
    createStockMovement(stockId, values),
  );

export const useCreateCount = () =>
  useStockMutation(({ stockId, values }: { stockId: string; values: CountFormValues }) =>
    createStockCount(stockId, values),
  );

export const useReserve = () =>
  useStockMutation(
    ({ stockId, productId, quantity }: { stockId: string; productId: string; quantity: number }) =>
      reserveStock(stockId, productId, quantity),
  );

export const useRelease = () =>
  useStockMutation(
    ({ stockId, productId, quantity }: { stockId: string; productId: string; quantity: number }) =>
      releaseStock(stockId, productId, quantity),
  );

export const useCreateReassignment = () =>
  useStockMutation((values: TransferFormValues) => createReassignment(values));
