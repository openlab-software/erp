import type { BarcodeType, ProductStatus, ProductType } from "./types";

export const STATUS_LABEL: Record<
  ProductStatus,
  { label: string; variant: "pos" | "warn" | "info" | "neg" | "" }
> = {
  DRAFT: { label: "Rascunho", variant: "" },
  PUBLISHED: { label: "Publicado", variant: "pos" },
  ACTIVE: { label: "Ativo", variant: "pos" },
  INACTIVE: { label: "Inativo", variant: "warn" },
};

export const TYPE_LABEL: Record<ProductType, string> = {
  GOOD: "Bem",
  SERVICE: "Serviço",
};

export const BARCODE_TYPES: BarcodeType[] = ["EAN13", "EAN8", "UPC", "INTERNAL"];

/** Status a product can move to from `status` (mirrors ProductStatus.requireTransitionTo). */
export const NEXT_STATUS: Record<
  ProductStatus,
  { to: ProductStatus; label: string }[]
> = {
  DRAFT: [{ to: "PUBLISHED", label: "Publicar" }],
  PUBLISHED: [{ to: "INACTIVE", label: "Inativar" }],
  INACTIVE: [{ to: "PUBLISHED", label: "Reativar" }],
  ACTIVE: [],
};
