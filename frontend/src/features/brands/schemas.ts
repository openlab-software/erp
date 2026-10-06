import { z } from "zod";

/** Mirrors catalog-service's CreateBrandRequest/UpdateBrandRequest (`@NotBlank description`). */
export const brandSchema = z.object({
  description: z.string().trim().min(1, "Informe a descrição da marca"),
});

export type BrandFormValues = z.infer<typeof brandSchema>;
