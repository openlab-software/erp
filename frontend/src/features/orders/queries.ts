import { http } from "@/lib/http";
import { jsonBody, toSearch } from "@/lib/query";
import type { Page, Success } from "@/lib/types";
import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import type { Order, OrderDraft, OrderKind } from "./types";

export interface OrderFilters {
  q: string;
  state: string;
}

interface StateTotals {
  state: string;
  count: number;
  total_amount: number;
}

type Raw = Record<string, unknown>;

/** TanStack Query hooks for one order kind (sales or purchase). */
export function createOrderHooks(kind: OrderKind) {
  const key = ["orders", kind.module] as const;
  const url = (id: string, suffix = "") =>
    `${kind.api}/${encodeURIComponent(id)}${suffix}`;

  const useOrders = (params: OrderFilters & { page: number; pageSize: number }) =>
    useQuery({
      queryKey: [...key, "list", params],
      queryFn: async () => {
        const page = await http<Page<Raw>>(
          `${kind.api}${toSearch({
            q: params.q,
            state: params.state,
            page: params.page,
            page_size: params.pageSize,
          })}`,
        );
        return { ...page, data: page.data.map(kind.parse) };
      },
      placeholderData: keepPreviousData,
    });

  const useOrder = (id: string | undefined) =>
    useQuery({
      queryKey: [...key, "detail", id],
      queryFn: async () => kind.parse(await http<Raw>(url(id as string))),
      enabled: Boolean(id),
    });

  const useSummary = () =>
    useQuery({
      queryKey: [...key, "summary"],
      queryFn: () => http<{ data: StateTotals[] }>(kind.summaryApi),
      select: (r) => r.data,
    });

  const useRefreshing = <TVariables, TResult>(
    mutationFn: (variables: TVariables) => Promise<TResult>,
  ) => {
    const queryClient = useQueryClient();
    return useMutation({
      mutationFn,
      onSuccess: () => queryClient.invalidateQueries({ queryKey: key }),
    });
  };

  const useSaveOrder = () =>
    useRefreshing(async ({ id, draft }: { id?: string; draft: OrderDraft }) =>
      kind.parse(
        await http<Raw>(id ? url(id) : kind.api, {
          method: id ? "PUT" : "POST",
          body: jsonBody(kind.toPayload(draft)),
        }),
      ),
    );

  /** send / confirm / cancel / draft / lock */
  const useOrderAction = () =>
    useRefreshing(async ({ id, action }: { id: string; action: string }) =>
      kind.parse(
        await http<Raw>(url(id, `/${action}`), {
          method: "POST",
          body: jsonBody({}),
        }),
      ),
    );

  const useDeleteOrder = () =>
    useRefreshing((id: string) => http<Success>(url(id), { method: "DELETE" }));

  return { useOrders, useOrder, useSummary, useSaveOrder, useOrderAction, useDeleteOrder };
}

export type OrderHooks = ReturnType<typeof createOrderHooks>;
export type { Order };
