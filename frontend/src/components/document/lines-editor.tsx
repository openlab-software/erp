import { Icon } from "@/components/icon";
import { TypeaheadPicker } from "@/components/document/typeahead-picker";
import { T } from "@/components/ui";
import { useProductSuggestions } from "@/features/products";
import { getProduct } from "@/features/products";
import { brl, lineSubtotal } from "@/lib/money";
import { Button, Input } from "@openlab-ui/react";

/** A line being edited: numbers are kept as text so half-typed values ("1,") survive. */
export interface LineDraft {
  key: number;
  product_id: string;
  /** GOOD moves through the inventory; SERVICE (freight, installation…) never does. */
  product_type: string;
  description: string;
  quantity: string;
  unit_price: string;
  discount_percent: string;
  tax_percent: string;
}

let nextKey = 1;

export const emptyLine = (): LineDraft => ({
  key: nextKey++,
  product_id: "",
  product_type: "GOOD",
  description: "",
  quantity: "1",
  unit_price: "0",
  discount_percent: "0",
  tax_percent: "0",
});

interface LinesEditorProps {
  lines: LineDraft[];
  onChange: (lines: LineDraft[]) => void;
  readOnly?: boolean;
  /** Which catalog price fills a new line: what we sell for, or what we pay. */
  priceSource: "sale_price" | "cost_price";
  /** Lines without a catalog product (services, fees) are allowed on invoices. */
  productOptional?: boolean;
  showDiscount?: boolean;
  showTax?: boolean;
  errors?: Record<number, string>;
}

const cell = { padding: "6px 8px" } as const;
const num = { width: 86, textAlign: "right" } as const;

/** The lines table of an order / invoice: pick a product (fills description and price) or type a free line. */
export function LinesEditor({
  lines,
  onChange,
  readOnly,
  priceSource,
  productOptional,
  showDiscount = true,
  showTax = true,
  errors = {},
}: LinesEditorProps) {
  const update = (key: number, patch: Partial<LineDraft>) =>
    onChange(lines.map((l) => (l.key === key ? { ...l, ...patch } : l)));

  const pickProduct = async (
    key: number,
    suggestion: { id: string; description: string; sale_price: number; type: string },
  ) => {
    update(key, {
      product_id: suggestion.id,
      product_type: suggestion.type,
      description: suggestion.description,
      unit_price: String(suggestion.sale_price),
    });
    if (priceSource === "cost_price") {
      // The autocomplete only carries the sale price; the cost comes with the full product.
      try {
        const product = await getProduct(suggestion.id);
        update(key, { unit_price: String(product.cost_price) });
      } catch {
        // keep the sale price as a starting point; the user can type the cost
      }
    }
  };

  return (
    <div>
      <T>
        <thead>
          <tr>
            <th style={{ minWidth: 240 }}>Produto</th>
            <th style={{ minWidth: 180 }}>Descrição</th>
            <th className="num">Qtd.</th>
            <th className="num">Preço unit.</th>
            {showDiscount && <th className="num">Desc. %</th>}
            {showTax && <th className="num">Imposto %</th>}
            <th className="num">Subtotal</th>
            {!readOnly && <th style={{ width: 40 }} />}
          </tr>
        </thead>
        <tbody>
          {lines.map((l) => (
            <tr key={l.key}>
              <td style={cell}>
                {readOnly ? (
                  <span className="mono" style={{ color: "var(--ink-3)" }}>
                    {l.product_id || "—"}
                  </span>
                ) : (
                  <TypeaheadPicker
                    value={l.product_id}
                    selectedLabel={l.product_id ? l.description || l.product_id : undefined}
                    placeholder={
                      productOptional ? "Produto (opcional)…" : "Buscar produto…"
                    }
                    useOptions={useProductSuggestions}
                    getId={(p) => p.id}
                    renderOption={(p) => (
                      <>
                        <span>{p.description}</span>
                        <span className="mono" style={{ color: "var(--ink-3)" }}>
                          {brl(p.sale_price)}
                        </span>
                      </>
                    )}
                    onSelect={(p) => pickProduct(l.key, p)}
                    onClear={() => update(l.key, { product_id: "" })}
                    invalid={Boolean(errors[l.key]) && !l.product_id && !productOptional}
                  />
                )}
              </td>
              <td style={cell}>
                <Input
                  value={l.description}
                  readOnly={readOnly}
                  placeholder="Descrição da linha"
                  aria-invalid={errors[l.key] ? true : undefined}
                  onChange={(e) => update(l.key, { description: e.target.value })}
                />
                {errors[l.key] && (
                  <div style={{ fontSize: 11, color: "var(--neg)", marginTop: 2 }}>
                    {errors[l.key]}
                  </div>
                )}
              </td>
              <td style={cell}>
                <Input
                  style={num}
                  inputMode="decimal"
                  value={l.quantity}
                  readOnly={readOnly}
                  onChange={(e) => update(l.key, { quantity: e.target.value })}
                />
              </td>
              <td style={cell}>
                <Input
                  style={num}
                  inputMode="decimal"
                  value={l.unit_price}
                  readOnly={readOnly}
                  onChange={(e) => update(l.key, { unit_price: e.target.value })}
                />
              </td>
              {showDiscount && (
                <td style={cell}>
                  <Input
                    style={{ ...num, width: 70 }}
                    inputMode="decimal"
                    value={l.discount_percent}
                    readOnly={readOnly}
                    onChange={(e) => update(l.key, { discount_percent: e.target.value })}
                  />
                </td>
              )}
              {showTax && (
                <td style={cell}>
                  <Input
                    style={{ ...num, width: 70 }}
                    inputMode="decimal"
                    value={l.tax_percent}
                    readOnly={readOnly}
                    onChange={(e) => update(l.key, { tax_percent: e.target.value })}
                  />
                </td>
              )}
              <td className="num">{brl(lineSubtotal(l))}</td>
              {!readOnly && (
                <td style={cell}>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    aria-label="Remover linha"
                    onClick={() => onChange(lines.filter((x) => x.key !== l.key))}
                  >
                    <Icon name="trash" size={13} />
                  </Button>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </T>
      {!readOnly && (
        <div style={{ padding: "10px 12px" }}>
          <Button
            type="button"
            variant="ghost"
            size="xs"
            onClick={() => onChange([...lines, emptyLine()])}
          >
            <Icon name="plus" size={12} /> Adicionar linha
          </Button>
        </div>
      )}
    </div>
  );
}
