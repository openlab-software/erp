import {
  BarFill,
  BarTrack,
  Card,
  CardBody,
  CardHead,
  Empty,
  Stat,
  StatGrid,
  StatLabel,
  StatValue,
  T,
} from "@/components/ui";
import type { ExtraTab } from "@/features/products";
import { useStockOptions } from "@/features/warehouses";
import { formatDateTime } from "@/lib/format";
import { errorMessage } from "@/lib/query";
import { Status } from "@/components/ui";
import { useQueries } from "@tanstack/react-query";
import { listStockMovements } from "../api";
import { MOVEMENT_LABEL, signedQuantity } from "../labels";
import { stockKeys, useProductBalance } from "../queries";

function StockBalanceTab({ productId }: { productId: string }) {
  const balance = useProductBalance(productId);
  const stocks = useStockOptions();
  const names = new Map((stocks.data ?? []).map((s) => [s.stock_id, s.description]));

  if (balance.isPending) return <Empty>Carregando…</Empty>;
  if (balance.isError || !balance.data) {
    return <Empty>{errorMessage(balance.error, "Erro ao carregar o saldo")}</Empty>;
  }
  const { total_current_value, total_reserved_value, total_available_value, by_stock } =
    balance.data;
  const max = Math.max(1, ...by_stock.map((s) => s.current_value));

  return (
    <div style={{ display: "grid", gap: 20 }}>
      <StatGrid>
        <Stat>
          <StatLabel>Saldo total</StatLabel>
          <StatValue>{total_current_value}</StatValue>
        </Stat>
        <Stat>
          <StatLabel>Reservado</StatLabel>
          <StatValue>{total_reserved_value}</StatValue>
        </Stat>
        <Stat>
          <StatLabel>Disponível</StatLabel>
          <StatValue>{total_available_value}</StatValue>
        </Stat>
      </StatGrid>
      <Card>
        <CardHead>
          <h3>Saldo por armazém</h3>
        </CardHead>
        <CardBody style={{ padding: 0 }}>
          <T>
            <thead>
              <tr>
                <th>Armazém</th>
                <th>Distribuição</th>
                <th className="num">Atual</th>
                <th className="num">Reservado</th>
                <th className="num">Disponível</th>
              </tr>
            </thead>
            <tbody>
              {by_stock.map((s) => (
                <tr key={s.stock_id}>
                  <td>{names.get(s.stock_id) ?? s.stock_id}</td>
                  <td style={{ minWidth: 160 }}>
                    <BarTrack>
                      <BarFill variant="pos" style={{ width: `${(s.current_value / max) * 100}%` }} />
                    </BarTrack>
                  </td>
                  <td className="num"><b>{s.current_value}</b></td>
                  <td className="num">{s.reserved_value}</td>
                  <td className="num">{s.available_value}</td>
                </tr>
              ))}
            </tbody>
          </T>
          {by_stock.length === 0 && <Empty>Produto sem saldo em nenhum armazém.</Empty>}
        </CardBody>
      </Card>
    </div>
  );
}

/** Latest movements of one product, gathered from every stock (the service lists them per stock). */
function ProductMovementsTab({ productId }: { productId: string }) {
  const stocks = useStockOptions();
  const list = stocks.data ?? [];
  const results = useQueries({
    queries: list.map((s) => {
      const params = { stockId: s.stock_id, page: 1, pageSize: 20, productId };
      return {
        queryKey: stockKeys.movements(params),
        queryFn: () => listStockMovements(params),
      };
    }),
  });

  const rows = results
    .flatMap((r, i) =>
      (r.data?.data ?? []).map((m) => ({ ...m, stockName: list[i]?.description ?? m.stock_id })),
    )
    .sort((a, b) => b.created_at.localeCompare(a.created_at))
    .slice(0, 20);
  const loading = stocks.isPending || results.some((r) => r.isPending);

  return (
    <Card>
      <CardHead>
        <h3>Movimentações recentes</h3>
      </CardHead>
      <T>
        <thead>
          <tr>
            <th>Data</th>
            <th>Armazém</th>
            <th>Tipo</th>
            <th className="num">Qtd</th>
            <th className="num">Saldo após</th>
            <th>Referência</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((m) => {
            const info = MOVEMENT_LABEL[m.type];
            const qty = signedQuantity(m.type, m.quantity);
            return (
              <tr key={m.movement_id}>
                <td className="mono">{formatDateTime(m.created_at)}</td>
                <td>{m.stockName}</td>
                <td><Status variant={info.variant || undefined}>{info.label}</Status></td>
                <td className="num" style={{ color: qty < 0 ? "var(--neg)" : "var(--ink-1)" }}>
                  <b>{qty > 0 ? "+" : ""}{qty}</b>
                </td>
                <td className="num">{m.resulting_balance}</td>
                <td className="id">{m.reference ?? "—"}</td>
              </tr>
            );
          })}
        </tbody>
      </T>
      {loading && <Empty>Carregando…</Empty>}
      {!loading && rows.length === 0 && <Empty>Sem movimentações para este produto.</Empty>}
    </Card>
  );
}

/** Tabs the product detail page shows after "Geral". */
export const productStockTabs: ExtraTab[] = [
  { id: "estoque", label: "Estoque", render: (id) => <StockBalanceTab productId={id} /> },
  { id: "movimentacoes", label: "Movimentações", render: (id) => <ProductMovementsTab productId={id} /> },
];
