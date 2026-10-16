import type { StatusMap } from "@/components/document/status-pill";
import type { PickingType } from "./types";

export const STATE_LABEL: StatusMap = {
  DRAFT: { label: "Rascunho", variant: "" },
  CONFIRMED: { label: "Pronta", variant: "info" },
  DONE: { label: "Concluída", variant: "pos" },
  CANCELLED: { label: "Cancelada", variant: "neg" },
};

export const TYPE_LABEL: Record<
  PickingType,
  { tab: string; one: string; newLabel: string; partner: string; origin: string }
> = {
  RECEIPT: {
    tab: "Recebimentos",
    one: "Recebimento",
    newLabel: "Novo recebimento",
    partner: "Fornecedor",
    origin: "Entrada no estoque",
  },
  DELIVERY: {
    tab: "Entregas",
    one: "Entrega",
    newLabel: "Nova entrega",
    partner: "Cliente",
    origin: "Saída do estoque",
  },
  INTERNAL: {
    tab: "Internas",
    one: "Transferência interna",
    newLabel: "Nova transferência interna",
    partner: "Responsável",
    origin: "Entre armazéns",
  },
};

export const ORIGIN_LABEL: Record<string, string> = {
  SALES_ORDER: "Pedido de venda",
  PURCHASE_ORDER: "Ordem de compra",
};
