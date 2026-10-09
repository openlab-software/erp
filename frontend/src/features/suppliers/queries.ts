import { createCrud } from "@/lib/crud";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import type { SupplierFormValues } from "./schemas";
import type { Supplier } from "./types";

export const suppliers = createCrud<Supplier, SupplierFormValues>({
  key: "suppliers",
  base: "/api/catalog/suppliers",
  toPayload: ({ name, document }) => ({ name, document }),
});

/** Up to 100 suppliers (the service's max page size) for selects. */
export const useSupplierOptions = () =>
  useQuery({
    queryKey: [...suppliers.keys.all, "options"],
    queryFn: () => suppliers.api.list({ q: "", page: 1, pageSize: 100 }),
    placeholderData: keepPreviousData,
    select: (page) => page.data,
  });
