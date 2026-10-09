import { createCrud } from "@/lib/crud";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import type { UnitFormValues } from "./schemas";
import type { UnitOfMeasure } from "./types";

export const units = createCrud<UnitOfMeasure, UnitFormValues>({
  key: "units-of-measure",
  base: "/api/catalog/units-of-measure",
  toPayload: ({ code, description }) => ({ code, description }),
});

/** Up to 100 units (the service's max page size) for selects. */
export const useUnitOptions = () =>
  useQuery({
    queryKey: [...units.keys.all, "options"],
    queryFn: () => units.api.list({ q: "", page: 1, pageSize: 100 }),
    placeholderData: keepPreviousData,
    select: (page) => page.data,
  });
