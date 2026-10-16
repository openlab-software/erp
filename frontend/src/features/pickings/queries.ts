import { http } from "@/lib/http";
import { jsonBody, toSearch } from "@/lib/query";
import type { Page, Success } from "@/lib/types";
import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import type { Picking, PickingType, ReplenishmentItem, ValidateResult } from "./types";

const BASE = "/api/stock/pickings";
const url = (id: string, suffix = "") => `${BASE}/${encodeURIComponent(id)}${suffix}`;

export const pickingKeys = { all: ["pickings"] as const };

export const usePickings = (params: {
  q: string;
  type: PickingType;
  state: string;
  page: number;
  pageSize: number;
}) =>
  useQuery({
    queryKey: [...pickingKeys.all, "list", params],
    queryFn: () =>
      http<Page<Picking>>(
        `${BASE}${toSearch({
          q: params.q,
          type: params.type,
          state: params.state,
          page: params.page,
          page_size: params.pageSize,
        })}`,
      ),
    placeholderData: keepPreviousData,
  });

export const usePicking = (id: string | undefined) =>
  useQuery({
    queryKey: [...pickingKeys.all, "detail", id],
    queryFn: () => http<Picking>(url(id as string)),
    enabled: Boolean(id),
  });

/** Items below their minimum balance in any stock (the reordering rules that fired). */
export const useReplenishment = (page: number, pageSize: number, enabled: boolean) =>
  useQuery({
    queryKey: [...pickingKeys.all, "replenishment", page, pageSize],
    queryFn: () =>
      http<Page<ReplenishmentItem>>(
        `/api/stock/replenishment${toSearch({ page, page_size: pageSize })}`,
      ),
    placeholderData: keepPreviousData,
    enabled,
  });

const useRefreshing = <TVariables, TResult>(
  mutationFn: (variables: TVariables) => Promise<TResult>,
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: pickingKeys.all }),
  });
};

export interface PickingDraft {
  type: PickingType;
  source_stock_id: string;
  destination_stock_id: string;
  partner_id: string | null;
  partner_name: string;
  scheduled_date: string;
  note: string;
  lines: { product_id: string; description: string | null; demand: number }[];
}

const nul = (value: string) => value.trim() || null;

const toPayload = (d: PickingDraft) => ({
  type: d.type,
  source_stock_id: nul(d.source_stock_id),
  destination_stock_id: nul(d.destination_stock_id),
  partner_id: d.partner_id,
  partner_name: nul(d.partner_name),
  scheduled_date: nul(d.scheduled_date),
  note: nul(d.note),
  lines: d.lines,
});

export const useSavePicking = () =>
  useRefreshing(({ id, draft }: { id?: string; draft: PickingDraft }) =>
    http<Picking>(id ? url(id) : BASE, {
      method: id ? "PUT" : "POST",
      body: jsonBody(toPayload(draft)),
    }),
  );

/** confirm / cancel */
export const usePickingAction = () =>
  useRefreshing(({ id, action }: { id: string; action: string }) =>
    http<Picking>(url(id, `/${action}`), { method: "POST", body: jsonBody({}) }),
  );

export const useValidatePicking = () =>
  useRefreshing(
    ({
      id,
      done,
      createBackorder,
    }: {
      id: string;
      done: { product_id: string; quantity: number }[];
      createBackorder: boolean;
    }) =>
      http<ValidateResult>(url(id, "/validate"), {
        method: "POST",
        body: jsonBody({ done, create_backorder: createBackorder }),
      }),
  );

export const useDeletePicking = () =>
  useRefreshing((id: string) => http<Success>(url(id), { method: "DELETE" }));
