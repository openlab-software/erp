/** "12,5" / "12.5" → 12.5; anything that is not a number → 0. */
export const num = (text: string | number | null | undefined): number => {
  if (typeof text === "number") return Number.isFinite(text) ? text : 0;
  const value = Number((text ?? "").toString().trim().replace(",", "."));
  return Number.isFinite(value) ? value : 0;
};

const round2 = (n: number) => Math.round((n + Number.EPSILON) * 100) / 100;

export interface PricedLine {
  quantity: string | number;
  unit_price: string | number;
  discount_percent: string | number;
  tax_percent: string | number;
}

/** quantity x unit price, less the discount: the same rule as the services. */
export const lineSubtotal = (l: PricedLine) => {
  const gross = num(l.quantity) * num(l.unit_price);
  return round2(gross - round2((gross * num(l.discount_percent)) / 100));
};

export const lineTax = (l: PricedLine) =>
  round2((lineSubtotal(l) * num(l.tax_percent)) / 100);

export const documentTotals = (lines: PricedLine[]) => {
  const untaxed = round2(lines.reduce((sum, l) => sum + lineSubtotal(l), 0));
  const tax = round2(lines.reduce((sum, l) => sum + lineTax(l), 0));
  return { untaxed, tax, total: round2(untaxed + tax) };
};

export const brl = (n: number | null | undefined) =>
  `R$ ${(n ?? 0).toLocaleString("pt-BR", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;

/** "2026-10-16" → "16/10/2026" (the date is not shifted by the time zone). */
export const formatDate = (date: string | null | undefined) =>
  date ? new Date(`${date}T00:00:00`).toLocaleDateString("pt-BR") : "—";
