import { Status } from "./ui";

interface StatusBadgeProps {
  variant?: "pos" | "neg" | "warn" | "info" | "";
  label: string;
}

export function StatusBadge({ variant, label }: StatusBadgeProps) {
  return <Status variant={variant || undefined}>{label}</Status>;
}

const MOVE_MAP: Record<
  string,
  { label: string; variant: "pos" | "neg" | "warn" | "info" | "" }
> = {
  entrada: { label: "Entrada", variant: "pos" },
  saida: { label: "Saída", variant: "warn" },
  ajuste: { label: "Ajuste", variant: "" },
  transferencia: { label: "Transferência", variant: "info" },
};

export function MoveBadge({ type }: { type: string }) {
  const m = MOVE_MAP[type] ?? MOVE_MAP["ajuste"];
  return <StatusBadge variant={m.variant} label={m.label} />;
}
