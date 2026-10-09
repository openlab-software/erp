import { z } from "zod";

/** Mirrors catalog-service's Create/UpdateProductRequest. `type` is immutable after creation. */
export const productSchema = z.object({
  description: z.string().trim().min(1, "Informe a descrição do produto"),
  short_description: z.string().trim().min(1, "Informe a descrição curta"),
  type: z.enum(["GOOD", "SERVICE"]),
  unit_of_measure_id: z.string().min(1, "Selecione a unidade de medida"),
  category_id: z.string().min(1, "Selecione a categoria"),
  /** Empty string = no brand / supplier. */
  brand_id: z.string(),
  default_supplier_id: z.string(),
});

export type ProductFormValues = z.infer<typeof productSchema>;

const money = (label: string) =>
  z
    .number({ invalid_type_error: `Informe ${label}` })
    .min(0, `${label} não pode ser negativo`);

/** Mirrors ChangePriceRequest (`@PositiveOrZero` sale and cost price). */
export const priceSchema = z.object({
  sale_price: money("o preço de venda"),
  cost_price: money("o custo"),
});

export type PriceFormValues = z.infer<typeof priceSchema>;

export const barcodeSchema = z.object({
  code: z.string().trim().min(1, "Informe o código"),
  type: z.enum(["EAN13", "EAN8", "UPC", "INTERNAL"]),
});

export type BarcodeFormValues = z.infer<typeof barcodeSchema>;

export const imageSchema = z.object({
  url: z.string().trim().url("Informe uma URL válida (https://…)"),
});

export type ImageFormValues = z.infer<typeof imageSchema>;

export const attributeSchema = z.object({
  attributes: z.array(
    z.object({
      name: z.string().trim().min(1, "Nome obrigatório"),
      value: z.string().trim().min(1, "Valor obrigatório"),
    }),
  ),
});

export type AttributeFormValues = z.infer<typeof attributeSchema>;
