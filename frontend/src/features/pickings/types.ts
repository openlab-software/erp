// stock-service serializes snake_case; the BFF passes it through untouched.
export type PickingType = "RECEIPT" | "DELIVERY" | "INTERNAL";
export type PickingState = "DRAFT" | "CONFIRMED" | "DONE" | "CANCELLED";

export interface PickingLine {
  product_id: string;
  description: string | null;
  demand: number;
  /** Moved quantity; 0 until the picking is validated. */
  done: number;
}

export interface Picking {
  picking_id: string;
  number: string;
  type: PickingType;
  state: PickingState;
  source_stock_id: string | null;
  destination_stock_id: string | null;
  partner_id: string | null;
  partner_name: string | null;
  origin_type: string | null;
  origin_id: string | null;
  origin_number: string | null;
  backorder_of: string | null;
  scheduled_date: string | null;
  note: string | null;
  lines: PickingLine[];
  created_at: string;
  updated_at: string | null;
  done_at: string | null;
}

export interface ValidateResult {
  picking: Picking;
  backorder: Picking | null;
}

export interface ReplenishmentItem {
  stock_id: string;
  product_id: string;
  current_value: number;
  reserved_value: number;
  min_value: number | null;
  max_value: number | null;
  suggested_quantity: number;
}
