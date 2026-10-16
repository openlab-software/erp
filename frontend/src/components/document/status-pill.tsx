import { Status } from "@/components/ui";

export type Variant = "pos" | "neg" | "warn" | "info" | "";

export type StatusMap = Record<string, { label: string; variant: Variant }>;

/** A status badge from a label map; unknown values are shown as they are. */
export function StatusPill({ value, map }: { value: string; map: StatusMap }) {
  const entry = map[value] ?? { label: value, variant: "" as Variant };
  return <Status variant={entry.variant || undefined}>{entry.label}</Status>;
}
