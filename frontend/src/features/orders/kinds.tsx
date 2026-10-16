import type { StatusMap } from "@/components/document/status-pill";
import { CustomerPicker } from "@/features/customers";
import { SupplierSelect } from "@/features/suppliers";
import type { Order, OrderDraft, OrderKind, OrderLine } from "./types";

type Raw = Record<string, unknown>;

const str = (v: unknown) => (typeof v === "string" ? v : "");
const strOrNull = (v: unknown) => (typeof v === "string" && v ? v : null);
const n = (v: unknown) => (typeof v === "number" ? v : Number(v ?? 0) || 0);

const parseLine = (fulfilledField: string) => (raw: Raw): OrderLine => ({
  product_id: str(raw.product_id),
  sku: strOrNull(raw.sku),
  description: str(raw.description),
  quantity: n(raw.quantity),
  unit_price: n(raw.unit_price),
  discount_percent: n(raw.discount_percent),
  tax_percent: n(raw.tax_percent),
  product_type: str(raw.product_type) || "GOOD",
  fulfilled: n(raw[fulfilledField]),
});

const orderStates = (confirmed: string): StatusMap => ({
  DRAFT: { label: "Rascunho", variant: "" },
  SENT: { label: "Enviada", variant: "info" },
  CONFIRMED: { label: confirmed, variant: "pos" },
  DONE: { label: "Bloqueado", variant: "pos" },
  CANCELLED: { label: "Cancelado", variant: "neg" },
});

const lineBody = (l: OrderDraft["lines"][number]) => ({
  product_id: l.product_id,
  sku: l.sku,
  description: l.description,
  quantity: l.quantity,
  unit_price: l.unit_price,
  discount_percent: l.discount_percent,
  tax_percent: l.tax_percent,
  product_type: l.product_type,
});

const nul = (value: string) => value.trim() || null;

export const salesKind: OrderKind = {
  module: "sales",
  api: "/api/sales/sales-orders",
  summaryApi: "/api/sales/summary",
  route: "/vendas",
  eyebrow: "COMERCIAL · VENDAS",
  title: "Pedidos de venda",
  noun: "pedido de venda",
  newLabel: "Nova cotação",
  partnerLabel: "Cliente",
  dateLabel: "Validade da cotação",
  personLabel: "Vendedor",
  addressLabel: "Endereço de entrega",
  fulfilmentLabel: "Entrega",
  billingLabel: "Faturamento",
  priceSource: "sale_price",
  states: orderStates("Pedido de venda"),
  fulfilment: {
    NONE: { label: "—", variant: "" },
    PENDING: { label: "A entregar", variant: "warn" },
    PARTIAL: { label: "Entrega parcial", variant: "warn" },
    DONE: { label: "Entregue", variant: "pos" },
  },
  billing: {
    NONE: { label: "—", variant: "" },
    TO_INVOICE: { label: "A faturar", variant: "warn" },
    INVOICED: { label: "Faturado", variant: "pos" },
  },
  summary: [
    { label: "Cotações em aberto", states: ["DRAFT", "SENT"] },
    { label: "Pedidos confirmados", states: ["CONFIRMED", "DONE"] },
  ],
  parse: (raw): Order => ({
    id: str(raw.sales_order_id),
    number: str(raw.number),
    state: str(raw.state),
    partnerId: str(raw.customer_id),
    partnerName: str(raw.customer_name),
    date: strOrNull(raw.valid_until),
    address: strOrNull(raw.delivery_address),
    person: strOrNull(raw.salesperson),
    note: strOrNull(raw.note),
    fulfilmentStatus: str(raw.delivery_status),
    billingStatus: str(raw.invoice_status),
    untaxed: n(raw.untaxed_amount),
    tax: n(raw.tax_amount),
    total: n(raw.total_amount),
    lines: ((raw.lines as Raw[]) ?? []).map(parseLine("delivered_quantity")),
    createdAt: str(raw.created_at),
    confirmedAt: strOrNull(raw.confirmed_at),
  }),
  toPayload: (d) => ({
    customer_id: d.partnerId,
    customer_name: d.partnerName,
    currency: "BRL",
    valid_until: nul(d.date),
    delivery_address: nul(d.address),
    salesperson: nul(d.person),
    note: nul(d.note),
    lines: d.lines.map(lineBody),
  }),
  renderPartner: ({ value, label, disabled, invalid, onChange }) => (
    <CustomerPicker
      value={value}
      label={label}
      disabled={disabled}
      invalid={invalid}
      onSelect={(c) => onChange(c.customer_id, c.name)}
      onClear={() => onChange("", "")}
    />
  ),
};

export const purchaseKind: OrderKind = {
  module: "purchase",
  api: "/api/purchase/purchase-orders",
  summaryApi: "/api/purchase/summary",
  route: "/compras",
  eyebrow: "SUPRIMENTOS · COMPRAS",
  title: "Ordens de compra",
  noun: "ordem de compra",
  newLabel: "Novo pedido de cotação",
  partnerLabel: "Fornecedor",
  dateLabel: "Recebimento previsto",
  personLabel: "Comprador",
  addressLabel: "Endereço de recebimento",
  fulfilmentLabel: "Recebimento",
  billingLabel: "Fatura do fornecedor",
  priceSource: "cost_price",
  states: orderStates("Ordem de compra"),
  fulfilment: {
    NONE: { label: "—", variant: "" },
    PENDING: { label: "A receber", variant: "warn" },
    PARTIAL: { label: "Recebimento parcial", variant: "warn" },
    DONE: { label: "Recebido", variant: "pos" },
  },
  billing: {
    NONE: { label: "—", variant: "" },
    TO_BILL: { label: "A conferir", variant: "warn" },
    BILLED: { label: "Faturado", variant: "pos" },
  },
  summary: [
    { label: "Cotações em aberto", states: ["DRAFT", "SENT"] },
    { label: "Ordens confirmadas", states: ["CONFIRMED", "DONE"] },
  ],
  parse: (raw): Order => ({
    id: str(raw.purchase_order_id),
    number: str(raw.number),
    state: str(raw.state),
    partnerId: str(raw.supplier_id),
    partnerName: str(raw.supplier_name),
    date: strOrNull(raw.expected_date),
    address: strOrNull(raw.ship_to_address),
    person: strOrNull(raw.buyer),
    note: strOrNull(raw.note),
    fulfilmentStatus: str(raw.receipt_status),
    billingStatus: str(raw.bill_status),
    untaxed: n(raw.untaxed_amount),
    tax: n(raw.tax_amount),
    total: n(raw.total_amount),
    lines: ((raw.lines as Raw[]) ?? []).map(parseLine("received_quantity")),
    createdAt: str(raw.created_at),
    confirmedAt: strOrNull(raw.confirmed_at),
  }),
  toPayload: (d) => ({
    supplier_id: d.partnerId,
    supplier_name: d.partnerName,
    currency: "BRL",
    expected_date: nul(d.date),
    ship_to_address: nul(d.address),
    buyer: nul(d.person),
    note: nul(d.note),
    lines: d.lines.map(lineBody),
  }),
  renderPartner: ({ value, label, disabled, invalid, onChange }) => (
    <SupplierSelect
      value={value}
      label={label}
      disabled={disabled}
      invalid={invalid}
      onChange={(id, name) => onChange(id, name)}
    />
  ),
};
