import { createCrud } from "@/lib/crud";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import type { WarehouseFormValues } from "./schemas";
import type { Stock } from "./types";

/** A "stock" in stock-service is what the UI calls an armazém. */
export const warehouses = createCrud<Stock, WarehouseFormValues>({
  key: "stocks",
  base: "/api/stock/stocks",
  toPayload: ({ description }) => ({ description }),
});

/** Every stock (up to 100) — feeds the warehouse selector of the Estoque page. */
export const useStockOptions = () =>
  useQuery({
    queryKey: [...warehouses.keys.all, "options"],
    queryFn: () => warehouses.api.list({ q: "", page: 1, pageSize: 100 }),
    placeholderData: keepPreviousData,
    select: (page) => page.data,
  });
