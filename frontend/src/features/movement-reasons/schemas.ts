import { z } from "zod";

/** Mirrors stock-service's Create/UpdateMovementReasonRequest (`@NotBlank description`). */
export const movementReasonSchema = z.object({
  description: z.string().trim().min(1, "Informe a descrição do motivo"),
});

export type MovementReasonFormValues = z.infer<typeof movementReasonSchema>;
