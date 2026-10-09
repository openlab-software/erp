import { http } from "@/lib/http";
import { jsonBody, toSearch } from "@/lib/query";
import type { Page } from "@/lib/types";
import type {
  CountFormValues,
  MovementFormValues,
  TransferFormValues,
} from "./schemas";
import type {
  MovementType,
  ProductBalance,
  Reassignment,
  StockCount,
  StockItem,
  StockMovement,
} from "./types";

const stockUrl = (stockId: string, suffix = "") =>
  `/api/stock/stocks/${encodeURIComponent(stockId)}${suffix}`;

export interface ListItemsParams {
  stockId: string;
  page: number;
  pageSize: number;
  productId?: string;
  belowMin?: boolean;
  aboveMax?: boolean;
}

export const listStockItems = ({
  stockId,
  page,
  pageSize,
  productId,
  belowMin,
  aboveMax,
}: ListItemsParams) =>
  http<Page<StockItem>>(
    stockUrl(
      stockId,
      `/items${toSearch({
        page,
        page_size: pageSize,
        product_id: productId,
        below_min: belowMin ? true : undefined,
        above_max: aboveMax ? true : undefined,
      })}`,
    ),
  );

export interface ListMovementsParams {
  stockId: string;
  page: number;
  pageSize: number;
  productId?: string;
  type?: MovementType | "";
  /** ISO-8601 instants. */
  from?: string;
  to?: string;
}

export const listStockMovements = ({
  stockId,
  page,
  pageSize,
  productId,
  type,
  from,
  to,
}: ListMovementsParams) =>
  http<Page<StockMovement>>(
    stockUrl(
      stockId,
      `/movements${toSearch({ page, page_size: pageSize, product_id: productId, type, from, to })}`,
    ),
  );

export const createStockMovement = (
  stockId: string,
  { reason_id, reference, ...rest }: MovementFormValues,
) =>
  http<StockMovement>(stockUrl(stockId, "/movements"), {
    method: "POST",
    body: jsonBody({
      ...rest,
      reason_id: reason_id || null,
      reference: reference || null,
    }),
  });

export interface ListCountsParams {
  stockId: string;
  page: number;
  pageSize: number;
  productId?: string;
}

export const listStockCounts = ({
  stockId,
  page,
  pageSize,
  productId,
}: ListCountsParams) =>
  http<Page<StockCount>>(
    stockUrl(
      stockId,
      `/counts${toSearch({ page, page_size: pageSize, product_id: productId })}`,
    ),
  );

export const createStockCount = (stockId: string, values: CountFormValues) =>
  http<StockCount>(stockUrl(stockId, "/counts"), {
    method: "POST",
    body: jsonBody(values),
  });

const itemUrl = (stockId: string, productId: string, action: string) =>
  stockUrl(stockId, `/items/${encodeURIComponent(productId)}/${action}`);

export const reserveStock = (stockId: string, productId: string, quantity: number) =>
  http<StockItem>(itemUrl(stockId, productId, "reserve"), {
    method: "POST",
    body: jsonBody({ quantity }),
  });

export const releaseStock = (stockId: string, productId: string, quantity: number) =>
  http<StockItem>(itemUrl(stockId, productId, "release"), {
    method: "POST",
    body: jsonBody({ quantity }),
  });

export const createReassignment = (values: TransferFormValues) =>
  http<Reassignment>("/api/stock/reassignments", {
    method: "POST",
    body: jsonBody(values),
  });

export const getProductBalance = (productId: string) =>
  http<ProductBalance>(
    `/api/stock/products/${encodeURIComponent(productId)}/balance`,
  );
