import { z } from "zod";

/** Mirrors catalog-service's Create/UpdateUnitOfMeasureRequest (`@NotBlank code, description`). */
export const unitSchema = z.object({
  code: z.string().trim().min(1, "Informe o código (ex.: un, kg, m)"),
  description: z.string().trim().min(1, "Informe a descrição da unidade"),
});

export type UnitFormValues = z.infer<typeof unitSchema>;
