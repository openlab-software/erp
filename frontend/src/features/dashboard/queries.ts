import { listProducts } from "@/features/products";
import { listStockItems, listStockMovements, stockKeys } from "@/features/stock";
import type { StockItem, StockMovement } from "@/features/stock";
import { useStockOptions } from "@/features/warehouses";
import { http } from "@/lib/http";
import { toSearch } from "@/lib/query";
import type { Page } from "@/lib/types";
import { useQueries, useQuery } from "@tanstack/react-query";

const STATUSES = ["DRAFT", "PUBLISHED", "INACTIVE"] as const;
const NO_FILTERS = { q: "", category_id: "", type: "", brand_id: "" };

/** Total of a catalog listing without downloading it (`page_size=1`, read `total`). */
const totalOf = (path: string) =>
  http<Page<unknown>>(`${path}${toSearch({ page: 1, page_size: 1 })}`).then((p) => p.total);

export interface DashboardData {
  loading: boolean;
  products?: number;
  suppliers?: number;
  warehouses?: number;
  belowMin?: number;
  productsByStatus: { status: (typeof STATUSES)[number]; total?: number }[];
  /** Items under their minimum, across every warehouse, emptiest first. */
  critical: (StockItem & { stockName: string })[];
  /** Newest movements across every warehouse. */
  recent: (StockMovement & { stockName: string })[];
  /** Units in / out per day for the last 7 days (oldest first), from each warehouse's latest 100 movements. */
  week: { label: string; inbound: number; outbound: number }[];
}

const DAYS = 7;

/** Adjustments count as inbound when positive and outbound when negative. */
const bucketWeek = (movements: StockMovement[]) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const days = Array.from({ length: DAYS }, (_, i) => {
    const day = new Date(today);
    day.setDate(today.getDate() - (DAYS - 1 - i));
    return { key: day.toDateString(), label: String(day.getDate()).padStart(2, "0"), inbound: 0, outbound: 0 };
  });
  for (const m of movements) {
    const day = days.find((d) => d.key === new Date(m.created_at).toDateString());
    if (!day) continue;
    const outbound = m.type === "EXIT" || m.type === "TRANSFER_OUT" || (m.type === "ADJUSTMENT" && m.quantity < 0);
    if (outbound) day.outbound += Math.abs(m.quantity);
    else day.inbound += Math.abs(m.quantity);
  }
  return days.map(({ label, inbound, outbound }) => ({ label, inbound, outbound }));
};

/**
 * Everything the dashboard shows comes from the two services: catalog totals, then one
 * below-min and one latest-movements query per warehouse (the services have no aggregate endpoint).
 */
export function useDashboard(): DashboardData {
  const stocks = useStockOptions();
  const list = stocks.data ?? [];

  const products = useQuery({
    queryKey: ["dashboard", "products"],
    queryFn: () => totalOf("/api/catalog/products"),
  });
  const suppliers = useQuery({
    queryKey: ["dashboard", "suppliers"],
    queryFn: () => totalOf("/api/catalog/suppliers"),
  });
  const byStatus = useQueries({
    queries: STATUSES.map((status) => ({
      queryKey: ["dashboard", "products-status", status],
      queryFn: () =>
        listProducts({ ...NO_FILTERS, status, page: 1, pageSize: 1 }).then((p) => p.total),
    })),
  });
  const below = useQueries({
    queries: list.map((s) => {
      const params = { stockId: s.stock_id, page: 1, pageSize: 5, belowMin: true };
      return {
        queryKey: stockKeys.items(params),
        queryFn: () => listStockItems(params),
      };
    }),
  });
  const moves = useQueries({
    queries: list.map((s) => {
      const params = { stockId: s.stock_id, page: 1, pageSize: 100 };
      return {
        queryKey: stockKeys.movements(params),
        queryFn: () => listStockMovements(params),
      };
    }),
  });

  const critical = below
    .flatMap((q, i) =>
      (q.data?.data ?? []).map((item) => ({ ...item, stockName: list[i]?.description ?? "" })),
    )
    .sort((a, b) => a.current_value - b.current_value)
    .slice(0, 6);
  const allMoves = moves.flatMap((q) => q.data?.data ?? []);
  const recent = moves
    .flatMap((q, i) =>
      (q.data?.data ?? []).map((m) => ({ ...m, stockName: list[i]?.description ?? "" })),
    )
    .sort((a, b) => b.created_at.localeCompare(a.created_at))
    .slice(0, 6);

  return {
    loading: stocks.isPending || products.isPending,
    products: products.data,
    suppliers: suppliers.data,
    warehouses: stocks.data ? list.length : undefined,
    belowMin: below.every((q) => q.data) ? below.reduce((n, q) => n + (q.data?.total ?? 0), 0) : undefined,
    productsByStatus: STATUSES.map((status, i) => ({ status, total: byStatus[i]?.data })),
    critical,
    recent,
    week: bucketWeek(allMoves),
  };
}
