import { http } from "@/lib/http";
import { jsonBody, toSearch } from "@/lib/query";
import type { Page, Success } from "@/lib/types";
import type {
  AttributeFormValues,
  BarcodeFormValues,
  PriceFormValues,
  ProductFormValues,
} from "./schemas";
import type {
  ImportReport,
  PriceHistoryEntry,
  Product,
  ProductBarcode,
  ProductFilters,
  ProductImage,
  ProductStatus,
  ProductSuggestion,
} from "./types";

const BASE = "/api/catalog/products";
const url = (id: string, suffix = "") =>
  `${BASE}/${encodeURIComponent(id)}${suffix}`;

export interface ListProductsParams extends ProductFilters {
  page: number;
  pageSize: number;
}

export const listProducts = ({ pageSize, ...rest }: ListProductsParams) =>
  http<Page<Product>>(`${BASE}${toSearch({ ...rest, page_size: pageSize })}`);

export const getProduct = (id: string) => http<Product>(url(id));

/** Form values → request body; blank brand/supplier become `null`. */
const toPayload = (v: ProductFormValues) => ({
  description: v.description,
  short_description: v.short_description,
  type: v.type,
  unit_of_measure_id: v.unit_of_measure_id,
  category_id: v.category_id,
  brand_id: v.brand_id || null,
  default_supplier_id: v.default_supplier_id || null,
});

export const createProduct = (values: ProductFormValues) =>
  http<Product>(BASE, { method: "POST", body: jsonBody(toPayload(values)) });

export const updateProduct = (id: string, values: ProductFormValues) =>
  http<Product>(url(id), { method: "PUT", body: jsonBody(toPayload(values)) });

export const deleteProduct = (id: string) =>
  http<Success>(url(id), { method: "DELETE" });

export const changeProductStatus = (id: string, status: ProductStatus) =>
  http<Product>(url(id, "/status"), {
    method: "PATCH",
    body: jsonBody({ status }),
  });

export const changeProductPrice = (id: string, values: PriceFormValues) =>
  http<Product>(url(id, "/price"), {
    method: "PATCH",
    body: jsonBody(values),
  });

export const getPriceHistory = (id: string) =>
  http<PriceHistoryEntry[]>(url(id, "/price-history"));

export const replaceAttributes = (id: string, values: AttributeFormValues) =>
  http<{ name: string; value: string }[]>(url(id, "/attributes"), {
    method: "PUT",
    body: jsonBody(values),
  });

export const addBarcode = (id: string, values: BarcodeFormValues) =>
  http<ProductBarcode>(url(id, "/barcodes"), {
    method: "POST",
    body: jsonBody(values),
  });

export const deleteBarcode = (id: string, barcodeId: string) =>
  http<Success>(url(id, `/barcodes/${encodeURIComponent(barcodeId)}`), {
    method: "DELETE",
  });

export const addImage = (id: string, imageUrl: string) =>
  http<ProductImage>(url(id, "/images"), {
    method: "POST",
    body: jsonBody({ url: imageUrl }),
  });

export const setPrimaryImage = (id: string, imageId: string) =>
  http<Success>(url(id, `/images/${encodeURIComponent(imageId)}/primary`), {
    method: "PATCH",
  });

export const deleteImage = (id: string, imageId: string) =>
  http<Success>(url(id, `/images/${encodeURIComponent(imageId)}`), {
    method: "DELETE",
  });

export const autocompleteProducts = (q: string, limit = 10) =>
  http<ProductSuggestion[]>(
    `/api/catalog/product-autocomplete${toSearch({ q, limit })}`,
  );

export const exportProducts = (filters: ProductFilters) =>
  http<{ csv: string }>(`/api/catalog/product-export${toSearch({ ...filters })}`);

export const importProducts = (csv: string) =>
  http<ImportReport>("/api/catalog/product-import", {
    method: "POST",
    body: jsonBody({ csv }),
  });
