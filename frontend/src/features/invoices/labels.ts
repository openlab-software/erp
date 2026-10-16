import type { StatusMap } from "@/components/document/status-pill";
import type { InvoiceKind, PaymentMethod } from "./types";

export const STATE_LABEL: StatusMap = {
  DRAFT: { label: "Rascunho", variant: "" },
  POSTED: { label: "Lançada", variant: "info" },
  CANCELLED: { label: "Cancelada", variant: "neg" },
};

export const PAYMENT_LABEL: StatusMap = {
  NOT_PAID: { label: "Em aberto", variant: "warn" },
  PARTIAL: { label: "Pago parcialmente", variant: "warn" },
  PAID: { label: "Pago", variant: "pos" },
};

export const METHOD_LABEL: Record<PaymentMethod, string> = {
  PIX: "Pix",
  BOLETO: "Boleto",
  BANK_TRANSFER: "Transferência",
  CREDIT_CARD: "Cartão de crédito",
  CASH: "Dinheiro",
  OTHER: "Outro",
};

export const KIND_LABEL: Record<
  InvoiceKind,
  { tab: string; one: string; partner: string; newLabel: string; pay: string; paid: string }
> = {
  CUSTOMER_INVOICE: {
    tab: "A receber",
    one: "Fatura",
    partner: "Cliente",
    newLabel: "Nova fatura",
    pay: "Registrar recebimento",
    paid: "Recebido",
  },
  VENDOR_BILL: {
    tab: "A pagar",
    one: "Fatura do fornecedor",
    partner: "Fornecedor",
    newLabel: "Nova fatura de fornecedor",
    pay: "Registrar pagamento",
    paid: "Pago",
  },
};
