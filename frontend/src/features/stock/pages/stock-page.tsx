import { Icon } from "@/components/icon";
import { Pagination } from "@/components/resource/pagination";
import { TableState } from "@/components/resource/table-state";
import {
  BarFill,
  BarTrack,
  Card,
  CardHead,
  CardHeadSub,
  FSelect,
  Page,
  PageActions,
  PageHead,
  SectionLabel,
  Stat,
  StatDelta,
  StatGrid,
  StatLabel,
  StatValue,
  Status,
  Subtitle,
  T,
  Tab,
  TableToolbar,
  TableWrap,
  Tabs,
} from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { useMovementReasonOptions } from "@/features/movement-reasons";
import { useProductsById } from "@/features/products";
import { useStockOptions } from "@/features/warehouses";
import { formatDateTime } from "@/lib/format";
import { errorMessage } from "@/lib/query";
import { Link } from "@modern-js/runtime/router";
import { Button } from "@openlab-ui/react";
import { useState } from "react";
import { CountDialog } from "../components/count-dialog";
import { MovementDialog } from "../components/movement-dialog";
import {
  type QuantityAction,
  QuantityDialog,
} from "../components/quantity-dialog";
import { TransferDialog } from "../components/transfer-dialog";
import {
  HEALTH_LABEL,
  MOVEMENT_FILTERS,
  MOVEMENT_LABEL,
  itemHealth,
  signedQuantity,
} from "../labels";
import { useStockCounts, useStockItems, useStockMovements } from "../queries";
import type { MovementType, StockItem } from "../types";

const PAGE_SIZE = 10;
const TABS = ["posicao", "movimentacoes", "alertas", "inventario"] as const;
type TabId = (typeof TABS)[number];

/** Dialogs are kept mounted and driven by `open`; `key` remounts them with fresh form state. */
interface DialogState<T = undefined> {
  open: boolean;
  key: number;
  value?: T;
}
const closed = <T,>(): DialogState<T> => ({ open: false, key: 0 });

export function StockPage() {
  const { showToast } = useToast();
  const stocks = useStockOptions();
  const [selected, setSelected] = useState("");
  const [tab, setTab] = useState<TabId>("posicao");

  // Position / alerts / movements / counts each paginate on their own.
  const [pages, setPages] = useState({ posicao: 1, movimentacoes: 1, alertas: 1, inventario: 1 });
  const [aboveMax, setAboveMax] = useState(false);
  const [moveType, setMoveType] = useState<MovementType | "">("");

  const [movement, setMovement] = useState<DialogState<string>>(closed);
  const [transfer, setTransfer] = useState<DialogState>(closed);
  const [count, setCount] = useState<DialogState>(closed);
  const [quantity, setQuantity] = useState<DialogState<{ action: QuantityAction; item: StockItem }>>(closed);
  const open = <T,>(set: (fn: (s: DialogState<T>) => DialogState<T>) => void, value?: T) =>
    set((s) => ({ open: true, key: s.key + 1, value }));
  const close = <T,>(set: (fn: (s: DialogState<T>) => DialogState<T>) => void) =>
    set((s) => ({ ...s, open: false }));

  const stockList = stocks.data ?? [];
  const stockId = selected || stockList[0]?.stock_id || "";
  const stockName = stockList.find((s) => s.stock_id === stockId)?.description ?? "";
  const setPage = (id: TabId) => (page: number) => setPages((p) => ({ ...p, [id]: page }));
  const resetPages = () => setPages({ posicao: 1, movimentacoes: 1, alertas: 1, inventario: 1 });

  const common = { stockId, pageSize: PAGE_SIZE };
  const items = useStockItems({ ...common, page: pages.posicao, aboveMax }, tab === "posicao");
  const alerts = useStockItems({ ...common, page: pages.alertas, belowMin: true }, tab === "alertas");
  const movements = useStockMovements(
    { ...common, page: pages.movimentacoes, type: moveType },
    tab === "movimentacoes",
  );
  const counts = useStockCounts({ ...common, page: pages.inventario }, tab === "inventario");

  // Header numbers: exact totals from one-row queries (the service returns `total` with every page).
  const total = (query: { data?: { total: number } }) => query.data?.total;
  const totalItems = total(useStockItems({ stockId, page: 1, pageSize: 1 }));
  const totalBelow = total(useStockItems({ stockId, page: 1, pageSize: 1, belowMin: true }));
  const totalAbove = total(useStockItems({ stockId, page: 1, pageSize: 1, aboveMax: true }));
  const totalCounts = total(useStockCounts({ stockId, page: 1, pageSize: 1 }));

  const visible = [
    ...(items.data?.data ?? []),
    ...(alerts.data?.data ?? []),
    ...(movements.data?.data ?? []),
    ...(counts.data?.data ?? []),
  ];
  const products = useProductsById(visible.map((r) => r.product_id));
  const reasons = new Map((useMovementReasonOptions().data ?? []).map((r) => [r.reason_id, r.description]));
  const productName = (id: string) => products.get(id)?.description ?? id;
  const productCell = (id: string) => (
    <Link to={`/produtos/${id}`} style={{ color: "var(--ink-1)" }}>
      {productName(id)}
    </Link>
  );

  const queryError = (q: { isError: boolean; error: Error | null }, fallback: string) =>
    q.isError ? errorMessage(q.error, fallback) : null;

  const saved = (message: string, set: (fn: (s: DialogState<any>) => DialogState<any>) => void) => {
    close(set);
    showToast(message);
  };

  const itemRows = (rows: StockItem[], withActions: boolean) =>
    rows.map((item) => {
      const health = HEALTH_LABEL[itemHealth(item)];
      const max = item.max_value ?? 0;
      const pct = max > 0 ? Math.min(100, (item.current_value / max) * 100) : 0;
      return (
        <tr key={item.product_id}>
          <td className="id">{item.product_id}</td>
          <td>{productCell(item.product_id)}</td>
          <td style={{ minWidth: 150 }}>
            {max > 0 ? (
              <BarTrack>
                <BarFill
                  variant={health.variant === "info" ? "pos" : health.variant}
                  style={{ width: `${pct}%` }}
                />
              </BarTrack>
            ) : (
              <span style={{ color: "var(--ink-3)" }}>sem máximo</span>
            )}
          </td>
          <td className="num" style={{ color: "var(--ink-3)" }}>{item.min_value ?? "—"}</td>
          <td className="num"><b>{item.current_value}</b></td>
          <td className="num" style={{ color: "var(--ink-3)" }}>{item.max_value ?? "—"}</td>
          <td className="num">{item.reserved_value}</td>
          <td className="num">{item.available_value}</td>
          <td><Status variant={health.variant}>{health.label}</Status></td>
          {withActions && (
            <td>
              <div className="row-actions">
                <Button variant="ghost" size="xs" onClick={() => open(setMovement, item.product_id)}>
                  Movimentar
                </Button>
                <Button variant="ghost" size="xs" onClick={() => open(setQuantity, { action: "reserve", item })}>
                  Reservar
                </Button>
                <Button
                  variant="ghost"
                  size="xs"
                  disabled={item.reserved_value === 0}
                  onClick={() => open(setQuantity, { action: "release", item })}
                >
                  Liberar
                </Button>
              </div>
            </td>
          )}
        </tr>
      );
    });

  const itemHead = (withActions: boolean) => (
    <thead>
      <tr>
        <th>Código</th>
        <th>Produto</th>
        <th>Ocupação</th>
        <th className="num">Mínimo</th>
        <th className="num">Atual</th>
        <th className="num">Máximo</th>
        <th className="num">Reservado</th>
        <th className="num">Disponível</th>
        <th>Status</th>
        {withActions && <th style={{ width: 230 }} />}
      </tr>
    </thead>
  );

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>OPERAÇÃO · ESTOQUE</SectionLabel>
          <h1>Estoque</h1>
          <Subtitle>
            Posição por armazém
            {stockName && (
              <>
                {" "}· <span className="mono">{stockName}</span>
              </>
            )}
          </Subtitle>
        </div>
        <PageActions>
          <FSelect
            aria-label="Armazém"
            style={{ width: 220 }}
            value={stockId}
            onChange={(e) => {
              setSelected(e.target.value);
              resetPages();
            }}
          >
            {stockList.map((s) => (
              <option key={s.stock_id} value={s.stock_id}>{s.description}</option>
            ))}
          </FSelect>
          <Button variant="ghost" disabled={!stockId} onClick={() => open(setTransfer)}>
            <Icon name="transfer" size={13} /> Transferir
          </Button>
          <Button disabled={!stockId} onClick={() => open(setMovement, "")}>
            <Icon name="plus" /> Nova movimentação
          </Button>
        </PageActions>
      </PageHead>

      {!stocks.isPending && stockList.length === 0 ? (
        <Card style={{ padding: 40, textAlign: "center", color: "var(--ink-3)" }}>
          Nenhum armazém cadastrado. <Link to="/armazens">Cadastre um armazém</Link> para começar.
        </Card>
      ) : (
        <>
          <StatGrid>
            <Stat>
              <StatLabel>Itens no armazém</StatLabel>
              <StatValue>{totalItems ?? "—"}</StatValue>
              <StatDelta>produtos com saldo controlado</StatDelta>
            </Stat>
            <Stat>
              <StatLabel>Abaixo do mínimo</StatLabel>
              <StatValue>{totalBelow ?? "—"}</StatValue>
              <StatDelta $neg={!!totalBelow}>{totalBelow ? "requer reposição" : "tudo em dia"}</StatDelta>
            </Stat>
            <Stat>
              <StatLabel>Acima do máximo</StatLabel>
              <StatValue>{totalAbove ?? "—"}</StatValue>
              <StatDelta>excesso de estoque</StatDelta>
            </Stat>
            <Stat>
              <StatLabel>Contagens registradas</StatLabel>
              <StatValue>{totalCounts ?? "—"}</StatValue>
              <StatDelta>inventário</StatDelta>
            </Stat>
          </StatGrid>

          <div style={{ height: 24 }} />

          <Tabs>
            {(
              [
                ["posicao", "Posição"],
                ["movimentacoes", "Movimentações"],
                ["alertas", `Alertas${totalBelow ? ` (${totalBelow})` : ""}`],
                ["inventario", "Inventário"],
              ] as const
            ).map(([id, label]) => (
              <Tab key={id} $active={tab === id} onClick={() => setTab(id)}>
                {label}
              </Tab>
            ))}
          </Tabs>

          {tab === "posicao" && (
            <TableWrap>
              <TableToolbar>
                <Button
                  size="xs"
                  variant={aboveMax ? "default" : "ghost"}
                  onClick={() => {
                    setAboveMax((v) => !v);
                    setPage("posicao")(1);
                  }}
                >
                  Somente acima do máximo
                </Button>
              </TableToolbar>
              <T>
                {itemHead(true)}
                <tbody>{itemRows(items.data?.data ?? [], true)}</tbody>
              </T>
              <TableState
                pending={items.isPending}
                error={queryError(items, "Erro ao carregar a posição de estoque")}
                empty={(items.data?.data.length ?? 0) === 0}
                emptyMessage="Nenhum item neste armazém."
                onRetry={() => items.refetch()}
              />
              <Pagination
                page={pages.posicao}
                total={items.data?.total ?? 0}
                pageSize={PAGE_SIZE}
                fetching={items.isFetching}
                onPageChange={setPage("posicao")}
              />
            </TableWrap>
          )}

          {tab === "movimentacoes" && (
            <TableWrap>
              <TableToolbar>
                <div style={{ display: "flex", gap: 6 }}>
                  {MOVEMENT_FILTERS.map(({ id, label }) => (
                    <Button
                      key={id || "all"}
                      size="xs"
                      variant={moveType === id ? "default" : "ghost"}
                      onClick={() => {
                        setMoveType(id);
                        setPage("movimentacoes")(1);
                      }}
                    >
                      {label}
                    </Button>
                  ))}
                </div>
              </TableToolbar>
              <T>
                <thead>
                  <tr>
                    <th>Data</th>
                    <th>Tipo</th>
                    <th>Produto</th>
                    <th className="num">Qtd</th>
                    <th className="num">Saldo após</th>
                    <th>Motivo</th>
                    <th>Referência</th>
                  </tr>
                </thead>
                <tbody>
                  {(movements.data?.data ?? []).map((m) => {
                    const info = MOVEMENT_LABEL[m.type];
                    const qty = signedQuantity(m.type, m.quantity);
                    return (
                      <tr key={m.movement_id}>
                        <td className="mono" style={{ color: "var(--ink-3)" }}>{formatDateTime(m.created_at)}</td>
                        <td><Status variant={info.variant || undefined}>{info.label}</Status></td>
                        <td>{productCell(m.product_id)}</td>
                        <td className="num" style={{ color: qty < 0 ? "var(--neg)" : "var(--ink-1)" }}>
                          <b>{qty > 0 ? "+" : ""}{qty}</b>
                        </td>
                        <td className="num">{m.resulting_balance}</td>
                        <td style={{ color: "var(--ink-3)" }}>{m.reason_id ? (reasons.get(m.reason_id) ?? m.reason_id) : "—"}</td>
                        <td className="id">{m.reference ?? "—"}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </T>
              <TableState
                pending={movements.isPending}
                error={queryError(movements, "Erro ao carregar movimentações")}
                empty={(movements.data?.data.length ?? 0) === 0}
                emptyMessage="Nenhuma movimentação registrada."
                onRetry={() => movements.refetch()}
              />
              <Pagination
                page={pages.movimentacoes}
                total={movements.data?.total ?? 0}
                pageSize={PAGE_SIZE}
                fetching={movements.isFetching}
                onPageChange={setPage("movimentacoes")}
              />
            </TableWrap>
          )}

          {tab === "alertas" && (
            <Card>
              <CardHead>
                <div>
                  <h3>Itens que requerem atenção</h3>
                  <CardHeadSub>Abaixo do estoque mínimo configurado neste armazém</CardHeadSub>
                </div>
              </CardHead>
              <T>
                {itemHead(false)}
                <tbody>{itemRows(alerts.data?.data ?? [], false)}</tbody>
              </T>
              <TableState
                pending={alerts.isPending}
                error={queryError(alerts, "Erro ao carregar alertas")}
                empty={(alerts.data?.data.length ?? 0) === 0}
                emptyMessage="Nenhum item abaixo do mínimo."
                onRetry={() => alerts.refetch()}
              />
              <Pagination
                page={pages.alertas}
                total={alerts.data?.total ?? 0}
                pageSize={PAGE_SIZE}
                fetching={alerts.isFetching}
                onPageChange={setPage("alertas")}
              />
            </Card>
          )}

          {tab === "inventario" && (
            <TableWrap>
              <TableToolbar>
                <span style={{ fontSize: 12.5, color: "var(--ink-3)" }}>
                  Cada contagem guarda o saldo do sistema no momento e o valor contado.
                </span>
                <div style={{ flex: 1 }} />
                <Button size="xs" onClick={() => open(setCount)}>
                  <Icon name="plus" size={12} /> Nova contagem
                </Button>
              </TableToolbar>
              <T>
                <thead>
                  <tr>
                    <th>Data</th>
                    <th>Produto</th>
                    <th className="num">Sistema</th>
                    <th className="num">Contado</th>
                    <th className="num">Diferença</th>
                  </tr>
                </thead>
                <tbody>
                  {(counts.data?.data ?? []).map((c) => {
                    const diff = c.counted_value - c.system_value;
                    return (
                      <tr key={c.count_id}>
                        <td className="mono" style={{ color: "var(--ink-3)" }}>{formatDateTime(c.created_at)}</td>
                        <td>{productCell(c.product_id)}</td>
                        <td className="num">{c.system_value}</td>
                        <td className="num"><b>{c.counted_value}</b></td>
                        <td
                          className="num mono"
                          style={{ color: diff === 0 ? "var(--ink-3)" : diff > 0 ? "var(--pos)" : "var(--neg)" }}
                        >
                          {diff === 0 ? "OK" : `${diff > 0 ? "+" : ""}${diff}`}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </T>
              <TableState
                pending={counts.isPending}
                error={queryError(counts, "Erro ao carregar contagens")}
                empty={(counts.data?.data.length ?? 0) === 0}
                emptyMessage="Nenhuma contagem registrada."
                onRetry={() => counts.refetch()}
              />
              <Pagination
                page={pages.inventario}
                total={counts.data?.total ?? 0}
                pageSize={PAGE_SIZE}
                fetching={counts.isFetching}
                onPageChange={setPage("inventario")}
              />
            </TableWrap>
          )}
        </>
      )}

      <MovementDialog
        key={`m${movement.key}`}
        open={movement.open}
        stockId={stockId}
        stockName={stockName}
        productId={movement.value}
        onClose={() => close(setMovement)}
        onSaved={(m) => saved(m, setMovement)}
      />
      <TransferDialog
        key={`t${transfer.key}`}
        open={transfer.open}
        fromStockId={stockId}
        onClose={() => close(setTransfer)}
        onSaved={(m) => saved(m, setTransfer)}
      />
      <CountDialog
        key={`c${count.key}`}
        open={count.open}
        stockId={stockId}
        stockName={stockName}
        onClose={() => close(setCount)}
        onSaved={(m) => saved(m, setCount)}
      />
      {quantity.value && (
        <QuantityDialog
          key={`q${quantity.key}`}
          open={quantity.open}
          action={quantity.value.action}
          stockId={stockId}
          productId={quantity.value.item.product_id}
          productName={productName(quantity.value.item.product_id)}
          onClose={() => close(setQuantity)}
          onSaved={(m) => saved(m, setQuantity)}
        />
      )}
    </Page>
  );
}
