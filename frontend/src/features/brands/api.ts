import { http } from "@/lib/http";
import type { Brand, BrandPage } from "./types";

const BASE = "/api/catalog/brands";

export interface ListBrandsParams {
  q: string;
  page: number;
  pageSize: number;
}

export const listBrands = ({ q, page, pageSize }: ListBrandsParams) => {
  const search = new URLSearchParams({
    page: String(page),
    page_size: String(pageSize),
  });
  if (q) search.set("q", q);
  return http<BrandPage>(`${BASE}?${search}`);
};

export const createBrand = (description: string) =>
  http<Brand>(BASE, { method: "POST", body: JSON.stringify({ description }) });

export const updateBrand = (id: string, description: string) =>
  http<Brand>(`${BASE}/${encodeURIComponent(id)}`, {
    method: "PUT",
    body: JSON.stringify({ description }),
  });

export const deleteBrand = (id: string) =>
  http<{ success: boolean }>(`${BASE}/${encodeURIComponent(id)}`, {
    method: "DELETE",
  });
