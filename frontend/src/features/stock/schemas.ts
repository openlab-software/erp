import { z } from "zod";

const quantity = (message: string) =>
  z.number({ invalid_type_error: message }).int(message);

/**
 * Mirrors stock-service's CreateStockMovementRequest + the use-case rules: ENTRY/EXIT need a
 * positive quantity, ADJUSTMENT a non-zero one (signed) and a reason.
 */
export const movementSchema = z
  .object({
    product_id: z.string().min(1, "Selecione o produto"),
    type: z.enum(["ENTRY", "EXIT", "ADJUSTMENT"]),
    quantity: quantity("Informe a quantidade"),
    reason_id: z.string(),
    reference: z.string().trim(),
  })
  .superRefine((v, ctx) => {
    if (v.type === "ADJUSTMENT") {
      if (v.quantity === 0)
        ctx.addIssue({ code: "custom", path: ["quantity"], message: "O ajuste não pode ser zero" });
      if (!v.reason_id)
        ctx.addIssue({ code: "custom", path: ["reason_id"], message: "O ajuste exige um motivo" });
    } else if (v.quantity <= 0) {
      ctx.addIssue({ code: "custom", path: ["quantity"], message: "Informe uma quantidade maior que zero" });
    }
  });

export type MovementFormValues = z.infer<typeof movementSchema>;

/** Mirrors CreateStockCountRequest (`countedValue` is a plain int). */
export const countSchema = z.object({
  product_id: z.string().min(1, "Selecione o produto"),
  counted_value: quantity("Informe a quantidade contada").min(0, "A contagem não pode ser negativa"),
});

export type CountFormValues = z.infer<typeof countSchema>;

/** Mirrors CreateReassignmentRequest: source ≠ destination and at least one item (`@Min(1)` quantity). */
export const transferSchema = z
  .object({
    from_stock_id: z.string().min(1, "Selecione o armazém de origem"),
    to_stock_id: z.string().min(1, "Selecione o armazém de destino"),
    items: z
      .array(
        z.object({
          product_id: z.string().min(1, "Selecione o produto"),
          quantity: quantity("Informe a quantidade").min(1, "Mínimo 1"),
        }),
      )
      .min(1, "Adicione ao menos um item"),
  })
  .refine((v) => v.from_stock_id !== v.to_stock_id, {
    path: ["to_stock_id"],
    message: "Origem e destino devem ser diferentes",
  });

export type TransferFormValues = z.infer<typeof transferSchema>;

/** Reserve / release a quantity of one item. */
export const quantitySchema = z.object({
  quantity: quantity("Informe a quantidade").min(1, "Informe uma quantidade maior que zero"),
});

export type QuantityFormValues = z.infer<typeof quantitySchema>;
