import type { StatusMap } from "@/components/document/status-pill";
import type { ReactNode } from "react";

/** One line of a sales / purchase order, as the screens see it (the two services name some fields differently). */
export interface OrderLine {
  product_id: string;
  sku: string | null;
  description: string;
  quantity: number;
  unit_price: number;
  discount_percent: number;
  tax_percent: number;
  /** GOOD or SERVICE: services are never delivered / received. */
  product_type: string;
  /** Delivered (sales) or received (purchase) so far. */
  fulfilled: number;
}

export interface Order {
  id: string;
  number: string;
  state: string;
  partnerId: string;
  partnerName: string;
  /** Quotation validity (sales) or expected receipt date (purchase), `YYYY-MM-DD`. */
  date: string | null;
  address: string | null;
  /** Salesperson (sales) or buyer (purchase). */
  person: string | null;
  note: string | null;
  fulfilmentStatus: string;
  billingStatus: string;
  untaxed: number;
  tax: number;
  total: number;
  lines: OrderLine[];
  createdAt: string;
  confirmedAt: string | null;
}

/** What the editor sends back; each kind turns it into its service's body. */
export interface OrderDraft {
  partnerId: string;
  partnerName: string;
  date: string;
  address: string;
  person: string;
  note: string;
  lines: {
    product_id: string;
    sku: string | null;
    description: string;
    quantity: number;
    unit_price: number;
    discount_percent: number;
    tax_percent: number;
    product_type: string;
  }[];
}

/**
 * Sales orders and purchase orders are the same document (header + lines + quotation/confirmed/cancelled flow)
 * in two services. A kind holds what differs: endpoints, field names, wording and the partner selector.
 */
export interface OrderKind {
  /** Module name (modules/<name>/module.json) that must be installed. */
  module: string;
  /** BFF base, e.g. /api/sales/sales-orders. */
  api: string;
  summaryApi: string;
  /** Page route, e.g. /vendas. */
  route: string;
  eyebrow: string;
  title: string;
  /** "pedido de venda" */
  noun: string;
  newLabel: string;
  partnerLabel: string;
  dateLabel: string;
  personLabel: string;
  addressLabel: string;
  fulfilmentLabel: string;
  billingLabel: string;
  /** Catalog price that fills a new line. */
  priceSource: "sale_price" | "cost_price";
  states: StatusMap;
  fulfilment: StatusMap;
  billing: StatusMap;
  /** Order totals shown on the list: [label, states summed]. */
  summary: { label: string; states: string[] }[];
  parse: (raw: Record<string, unknown>) => Order;
  toPayload: (draft: OrderDraft) => Record<string, unknown>;
  /** The partner selector of the form (customer or supplier). */
  renderPartner: (props: {
    value: string;
    label: string;
    disabled: boolean;
    invalid: boolean;
    onChange: (partnerId: string, partnerName: string) => void;
  }) => ReactNode;
}
