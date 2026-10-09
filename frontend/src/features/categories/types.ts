// catalog-service serializes snake_case; the BFF passes it through untouched.
export interface Category {
  category_id: string;
  description: string;
  /** `null` for a top-level category. */
  parent_category_id: string | null;
  created_at: string;
  updated_at: string | null;
}
