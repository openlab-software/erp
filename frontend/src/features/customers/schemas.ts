import { z } from "zod";
import { isValidCnpj, isValidCpf } from "./document";

/**
 * Mirrors customer-service's Create/UpdateCustomerRequest + CustomerDetails rules: the document
 * must be a valid CPF (individual) or CNPJ (company). Empty strings mean "not informed".
 * Addresses are managed on their own (features/addresses).
 */
export const customerSchema = z
  .object({
    type: z.enum(["INDIVIDUAL", "COMPANY"]),
    name: z.string().trim().min(1, "Informe o nome"),
    trade_name: z.string().trim(),
    document: z.string().trim().min(1, "Informe o documento"),
    state_registration: z.string().trim(),
    birth_date: z.string(),
    email: z.string().trim(),
    phone: z.string().trim(),
  })
  .superRefine((v, ctx) => {
    const company = v.type === "COMPANY";
    if (v.document && !(company ? isValidCnpj(v.document) : isValidCpf(v.document))) {
      ctx.addIssue({
        code: "custom",
        path: ["document"],
        message: company ? "CNPJ inválido" : "CPF inválido",
      });
    }
    if (v.email && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(v.email)) {
      ctx.addIssue({ code: "custom", path: ["email"], message: "E-mail inválido" });
    }
    if (!company && v.birth_date && v.birth_date > new Date().toISOString().slice(0, 10)) {
      ctx.addIssue({
        code: "custom",
        path: ["birth_date"],
        message: "A data de nascimento não pode estar no futuro",
      });
    }
  });

export type CustomerFormValues = z.infer<typeof customerSchema>;
