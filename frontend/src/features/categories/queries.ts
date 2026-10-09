import { createCrud } from "@/lib/crud";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import type { CategoryFormValues } from "./schemas";
import type { Category } from "./types";

export const categories = createCrud<Category, CategoryFormValues>({
  key: "categories",
  base: "/api/catalog/categories",
  toPayload: ({ description, parent_category_id }) => ({
    description,
    parent_category_id: parent_category_id || null,
  }),
});

export const useCategories = categories.useList;

/** Up to 100 categories (the service's max page size) for selects and filters. */
export const useCategoryOptions = () =>
  useQuery({
    queryKey: [...categories.keys.all, "options"],
    queryFn: () => categories.api.list({ q: "", page: 1, pageSize: 100 }),
    placeholderData: keepPreviousData,
    select: (page) => page.data,
  });
