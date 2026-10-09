// catalog-service serializes snake_case; the BFF passes it through untouched.
export type ProductType = "GOOD" | "SERVICE";

/** DRAFT → PUBLISHED ⇄ INACTIVE (the service rejects other transitions with 409). */
export type ProductStatus = "DRAFT" | "PUBLISHED" | "ACTIVE" | "INACTIVE";

export type BarcodeType = "EAN13" | "EAN8" | "UPC" | "INTERNAL";

export interface ProductAttribute {
  name: string;
  value: string;
}

export interface ProductBarcode {
  barcode_id: string;
  code: string;
  type: BarcodeType;
  created_at: string;
}

export interface ProductImage {
  image_id: string;
  url: string;
  is_primary: boolean;
  created_at: string;
}

export interface Product {
  product_id: string;
  description: string;
  short_description: string;
  type: ProductType;
  unit_of_measure: { unit_of_measure_id: string; code: string };
  status: ProductStatus;
  category: { category_id: string; description: string };
  brand: { brand_id: string; description: string } | null;
  default_supplier_id: string | null;
  sale_price: number;
  cost_price: number;
  attributes: ProductAttribute[];
  barcodes: ProductBarcode[];
  images: ProductImage[];
  created_at: string;
  updated_at: string | null;
}

export interface PriceHistoryEntry {
  price_history_id: string;
  sale_price: number;
  cost_price: number;
  changed_at: string;
}

export interface ProductSuggestion {
  id: string;
  description: string;
  type: ProductType;
  sale_price: number;
}

export interface ImportReport {
  total: number;
  created: number;
  failed: { line: number; error: string }[];
}

export interface ProductFilters {
  q: string;
  category_id: string;
  status: string;
  type: string;
  brand_id: string;
}
