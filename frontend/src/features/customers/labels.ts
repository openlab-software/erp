import type { CustomerStatus, CustomerType } from "./types";

export const TYPE_LABEL: Record<CustomerType, string> = {
  INDIVIDUAL: "Pessoa física",
  COMPANY: "Pessoa jurídica",
};

export const DOCUMENT_LABEL: Record<CustomerType, string> = {
  INDIVIDUAL: "CPF",
  COMPANY: "CNPJ",
};

export const STATUS_LABEL: Record<
  CustomerStatus,
  { label: string; variant: "pos" | "warn" | "" }
> = {
  ACTIVE: { label: "Ativo", variant: "pos" },
  INACTIVE: { label: "Inativo", variant: "warn" },
};
