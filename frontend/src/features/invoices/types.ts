// accounting-service serializes snake_case; the BFF passes it through untouched.
export type InvoiceKind = "CUSTOMER_INVOICE" | "VENDOR_BILL";
export type InvoiceState = "DRAFT" | "POSTED" | "CANCELLED";
export type PaymentState = "NOT_PAID" | "PARTIAL" | "PAID";
export type PaymentMethod = "CASH" | "PIX" | "BANK_TRANSFER" | "BOLETO" | "CREDIT_CARD" | "OTHER";

export interface InvoiceLine {
  product_id: string | null;
  description: string;
  quantity: number;
  unit_price: number;
  discount_percent: number;
  tax_percent: number;
  subtotal: number;
  total: number;
}

export interface Payment {
  payment_id: string;
  amount: number;
  paid_on: string;
  method: PaymentMethod;
  reference: string | null;
  created_at: string;
}

export interface Invoice {
  invoice_id: string;
  kind: InvoiceKind;
  /** Null while DRAFT; assigned when posted. */
  number: string | null;
  state: InvoiceState;
  payment_state: PaymentState;
  partner_id: string;
  partner_name: string;
  currency: string;
  invoice_date: string | null;
  due_date: string | null;
  overdue: boolean;
  reference: string | null;
  note: string | null;
  origin_type: string | null;
  origin_id: string | null;
  origin_number: string | null;
  untaxed_amount: number;
  tax_amount: number;
  total_amount: number;
  paid_amount: number;
  residual_amount: number;
  lines: InvoiceLine[];
  payments: Payment[];
  posted_at: string | null;
  created_at: string;
  updated_at: string | null;
}

export interface Position {
  kind: InvoiceKind;
  open_count: number;
  open_amount: number;
  overdue_count: number;
  overdue_amount: number;
  draft_count: number;
}

export interface InvoiceFilters {
  q: string;
  kind: InvoiceKind;
  state: string;
  overdue: boolean;
}
