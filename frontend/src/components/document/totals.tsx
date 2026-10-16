import { brl } from "@/lib/money";

interface TotalsProps {
  untaxed: number;
  tax: number;
  total: number;
  /** Extra rows under the total, e.g. what was paid and what is still due. */
  extra?: { label: string; value: number; strong?: boolean }[];
}

/** Untaxed / tax / total block under the lines of a document. */
export function Totals({ untaxed, tax, total, extra = [] }: TotalsProps) {
  const row = (label: string, value: number, strong?: boolean) => (
    <div
      key={label}
      style={{
        display: "flex",
        justifyContent: "space-between",
        gap: 32,
        padding: "3px 0",
        fontWeight: strong ? 600 : 400,
        fontSize: strong ? 15 : 13,
        color: strong ? "var(--ink-1)" : "var(--ink-3)",
      }}
    >
      <span>{label}</span>
      <span className="mono">{brl(value)}</span>
    </div>
  );
  return (
    <div style={{ display: "flex", justifyContent: "flex-end", padding: "8px 16px 16px" }}>
      <div style={{ minWidth: 260 }}>
        {row("Total sem impostos", untaxed)}
        {row("Impostos", tax)}
        <div style={{ borderTop: "1px solid var(--line)", margin: "4px 0" }} />
        {row("Total", total, true)}
        {extra.map((e) => row(e.label, e.value, e.strong))}
      </div>
    </div>
  );
}
