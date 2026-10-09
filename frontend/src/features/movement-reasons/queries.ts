import { createCrud } from "@/lib/crud";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import type { MovementReasonFormValues } from "./schemas";
import type { MovementReason } from "./types";

export const movementReasons = createCrud<
  MovementReason,
  MovementReasonFormValues
>({
  key: "movement-reasons",
  base: "/api/stock/movement-reasons",
  toPayload: ({ description }) => ({ description }),
});

/** Up to 100 reasons — feeds the "Nova movimentação" dialog (ADJUSTMENT requires one). */
export const useMovementReasonOptions = () =>
  useQuery({
    queryKey: [...movementReasons.keys.all, "options"],
    queryFn: () =>
      movementReasons.api.list({ q: "", page: 1, pageSize: 100 }),
    placeholderData: keepPreviousData,
    select: (page) => page.data,
  });
