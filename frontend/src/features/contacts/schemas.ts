import { z } from "zod";

export const MAX_CONTACTS = 20;

/** Mirrors customer-service's contact rules: a name, and a valid e-mail when informed. */
export const contactSchema = z.object({
  name: z.string().trim().min(1, "Informe o nome"),
  job_title: z.string().trim(),
  email: z
    .string()
    .trim()
    .refine((v) => v === "" || /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(v), "E-mail inválido"),
  phone: z.string().trim(),
  notes: z.string().trim(),
  is_primary: z.boolean(),
});

export type ContactFormValues = z.infer<typeof contactSchema>;
