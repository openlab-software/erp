import type { MovementType, StockItem } from "./types";

export const MOVEMENT_LABEL: Record<
  MovementType,
  { label: string; variant: "pos" | "neg" | "warn" | "info" | "" }
> = {
  ENTRY: { label: "Entrada", variant: "pos" },
  EXIT: { label: "Saída", variant: "warn" },
  ADJUSTMENT: { label: "Ajuste", variant: "" },
  TRANSFER_IN: { label: "Transf. entrada", variant: "info" },
  TRANSFER_OUT: { label: "Transf. saída", variant: "info" },
};

/** Types the movement list can be filtered by. */
export const MOVEMENT_FILTERS: { id: MovementType | ""; label: string }[] = [
  { id: "", label: "Todos" },
  { id: "ENTRY", label: "Entradas" },
  { id: "EXIT", label: "Saídas" },
  { id: "ADJUSTMENT", label: "Ajustes" },
  { id: "TRANSFER_IN", label: "Transf. ent." },
  { id: "TRANSFER_OUT", label: "Transf. saí." },
];

/** Whether a movement adds to (+) or takes from (−) the balance; ADJUSTMENT carries its own sign. */
export const signedQuantity = (type: MovementType, quantity: number) =>
  type === "EXIT" || type === "TRANSFER_OUT" ? -Math.abs(quantity) : quantity;

export type ItemHealth = "out" | "low" | "high" | "ok";

export const itemHealth = (item: StockItem): ItemHealth => {
  if (item.current_value <= 0) return "out";
  if (item.min_value !== null && item.current_value < item.min_value) return "low";
  if (item.max_value !== null && item.current_value > item.max_value) return "high";
  return "ok";
};

export const HEALTH_LABEL: Record<
  ItemHealth,
  { label: string; variant: "pos" | "neg" | "warn" | "info" }
> = {
  out: { label: "Ruptura", variant: "neg" },
  low: { label: "Abaixo do mín.", variant: "warn" },
  high: { label: "Acima do máx.", variant: "info" },
  ok: { label: "OK", variant: "pos" },
};
