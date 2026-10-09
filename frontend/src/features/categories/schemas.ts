import { z } from "zod";

/** Mirrors catalog-service's Create/UpdateCategoryRequest (`@NotBlank description`, optional parent). */
export const categorySchema = z.object({
  description: z.string().trim().min(1, "Informe a descrição da categoria"),
  /** Empty string = top-level category. */
  parent_category_id: z.string(),
});

export type CategoryFormValues = z.infer<typeof categorySchema>;
