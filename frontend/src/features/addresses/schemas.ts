import { z } from "zod";

export const MAX_ADDRESSES = 10;

// Brazilian states (UF).
export const UFS = [
  "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
  "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO",
] as const;

/**
 * Mirrors customer-service's address rules: at least one of zip code, street, number, complement,
 * neighborhood, city or state; zip code with 8 digits. Empty strings mean "not informed".
 */
export const addressSchema = z
  .object({
    label: z.string().trim(),
    zip_code: z.string().trim(),
    street: z.string().trim(),
    number: z.string().trim(),
    complement: z.string().trim(),
    neighborhood: z.string().trim(),
    city: z.string().trim(),
    state: z.string(),
    is_default: z.boolean(),
  })
  .superRefine((v, ctx) => {
    if (v.zip_code && v.zip_code.replace(/\D/g, "").length !== 8) {
      ctx.addIssue({ code: "custom", path: ["zip_code"], message: "CEP deve ter 8 dígitos" });
    }
    const informed = [v.zip_code, v.street, v.number, v.complement, v.neighborhood, v.city, v.state];
    if (!informed.some(Boolean)) {
      ctx.addIssue({
        code: "custom",
        path: ["street"],
        message: "Informe ao menos CEP, logradouro, cidade…",
      });
    }
  });

export type AddressFormValues = z.infer<typeof addressSchema>;
