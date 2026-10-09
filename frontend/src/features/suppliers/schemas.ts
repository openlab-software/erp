import { z } from "zod";

/** Mirrors catalog-service's Create/UpdateSupplierRequest (`@NotBlank name, document`). */
export const supplierSchema = z.object({
  name: z.string().trim().min(1, "Informe o nome do fornecedor"),
  document: z.string().trim().min(1, "Informe o documento (CNPJ/CPF)"),
});

export type SupplierFormValues = z.infer<typeof supplierSchema>;
