import { z } from "zod";

/** Mirrors stock-service's Create/UpdateStockRequest (`@NotBlank description`). */
export const warehouseSchema = z.object({
  description: z.string().trim().min(1, "Informe a descrição do armazém"),
});

export type WarehouseFormValues = z.infer<typeof warehouseSchema>;
