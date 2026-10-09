// stock-service serializes snake_case as well.
export interface Stock {
  stock_id: string;
  description: string;
  created_at: string;
  /** `null` until the stock is edited for the first time. */
  modified_at: string | null;
}
