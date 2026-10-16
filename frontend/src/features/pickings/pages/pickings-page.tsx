import { StatusPill } from "@/components/document/status-pill";
import { Icon } from "@/components/icon";
import { Pagination } from "@/components/resource/pagination";
import { TableState } from "@/components/resource/table-state";
import {
  FSelect,
  Page,
  PageActions,
  PageHead,
  SearchWrap,
  SectionLabel,
  Subtitle,
  T,
  Tab,
  TableToolbar,
  TableWrap,
  Tabs,
} from "@/components/ui";
import { useProductsById } from "@/features/products";
import { useStockOptions } from "@/features/warehouses";
import { formatDateTime } from "@/lib/format";
import { formatDate } from "@/lib/money";
import { errorMessage } from "@/lib/query";
import { useDebouncedValue } from "@/lib/use-debounced-value";
import { Link, useNavigate } from "@modern-js/runtime/router";
import {
  Button,
  Field,
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from "@openlab-ui/react";
import { useState } from "react";
import { ORIGIN_LABEL, STATE_LABEL, TYPE_LABEL } from "../labels";
import { usePickings, useReplenishment } from "../queries";
import type { PickingType } from "../types";

const PAGE_SIZE = 10;

type Tabname = PickingType | "REPLENISHMENT";

export function PickingsPage() {
  const navigate = useNavigate();
  const [tab, setTab] = useState<Tabname>("DELIVERY");
  const [search, setSearch] = useState("");
  const [state, setState] = useState("");
  const [page, setPage] = useState(1);
  const replenishing = tab === "REPLENISHMENT";
  const type: PickingType = replenishing ? "DELIVERY" : tab;

  const q = useDebouncedValue(search.trim());
  const signature = JSON.stringify({ tab, q, state });
  const [lastSignature, setLastSignature] = useState(signature);
  if (signature !== lastSignature) {
    setLastSignature(signature);
    setPage(1);
  }

  const stocks = useStockOptions();
  const stockName = (id: string | null) =>
    id ? (stocks.data?.find((s) => s.stock_id === id)?.description ?? id) : "—";

  const pickings = usePickings({ q, type, state, page, pageSize: PAGE_SIZE });
  const replenishment = useReplenishment(page, PAGE_SIZE, replenishing);
  const productNames = useProductsById((replenishment.data?.data ?? []).map((i) => i.product_id));

  const rows = pickings.data?.data ?? [];
  const total = replenishing ? (replenishment.data?.total ?? 0) : (pickings.data?.total ?? 0);
  const labels = TYPE_LABEL[type];

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>OPERAÇÃO · TRANSFERÊNCIAS</SectionLabel>
          <h1>Transferências de estoque</h1>
          <Subtitle>
            <span className="mono">{total}</span> {replenishing ? "item(ns) abaixo do mínimo" : "documento(s)"}
          </Subtitle>
        </div>
        <PageActions>
          {!replenishing && (
            <Button onClick={() => navigate(`/transferencias/novo?type=${type}`)}>
              <Icon name="plus" /> {labels.newLabel}
            </Button>
          )}
        </PageActions>
      </PageHead>

      <Tabs>
        {(Object.keys(TYPE_LABEL) as PickingType[]).map((t) => (
          <Tab key={t} $active={tab === t} onClick={() => setTab(t)}>
            {TYPE_LABEL[t].tab}
          </Tab>
        ))}
        <Tab $active={replenishing} onClick={() => setTab("REPLENISHMENT")}>
          Reposição
        </Tab>
      </Tabs>

      {replenishing ? (
        <TableWrap>
          <T>
            <thead>
              <tr>
                <th>Armazém</th>
                <th>Produto</th>
                <th className="num">Saldo</th>
                <th className="num">Mínimo</th>
                <th className="num">Máximo</th>
                <th className="num">Sugestão de compra</th>
              </tr>
            </thead>
            <tbody>
              {(replenishment.data?.data ?? []).map((i) => (
                <tr key={`${i.stock_id}:${i.product_id}`}>
                  <td>{stockName(i.stock_id)}</td>
                  <td>{productNames.get(i.product_id)?.description ?? i.product_id}</td>
                  <td className="num" style={{ color: "var(--neg)" }}>{i.current_value}</td>
                  <td className="num">{i.min_value ?? "—"}</td>
                  <td className="num">{i.max_value ?? "—"}</td>
                  <td className="num">
                    <b>{i.suggested_quantity}</b>
                  </td>
                </tr>
              ))}
            </tbody>
          </T>
          <TableState
            pending={replenishment.isPending}
            error={
              replenishment.isError
                ? errorMessage(replenishment.error, "Erro ao carregar a reposição")
                : null
            }
            empty={(replenishment.data?.data ?? []).length === 0}
            emptyMessage="Nenhum item abaixo do mínimo. Defina mínimos e máximos nos itens de estoque."
            onRetry={() => replenishment.refetch()}
          />
          <Pagination
            page={page}
            total={total}
            pageSize={PAGE_SIZE}
            fetching={replenishment.isFetching}
            onPageChange={setPage}
          />
        </TableWrap>
      ) : (
        <TableWrap>
          <TableToolbar>
            <SearchWrap>
              <Field>
                <InputGroup>
                  <InputGroupAddon>
                    <Icon name="search" size={13} />
                  </InputGroupAddon>
                  <InputGroupInput
                    style={{ width: "320px" }}
                    placeholder="Buscar por número, parceiro ou origem…"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                  />
                </InputGroup>
              </Field>
            </SearchWrap>
            <FSelect
              aria-label="Estado"
              style={{ width: 150, marginLeft: 8 }}
              value={state}
              onChange={(e) => setState(e.target.value)}
            >
              <option value="">Todos os estados</option>
              {Object.entries(STATE_LABEL).map(([value, { label }]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </FSelect>
          </TableToolbar>

          <T>
            <thead>
              <tr>
                <th>Número</th>
                <th>{labels.partner}</th>
                <th>Origem</th>
                <th>Armazém</th>
                <th>Previsto</th>
                <th>Estado</th>
                <th>Criada em</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((p) => (
                <tr key={p.picking_id}>
                  <td>
                    <Link
                      to={`/transferencias/${p.picking_id}`}
                      className="mono"
                      style={{ color: "var(--ink-1)", fontWeight: 500 }}
                    >
                      {p.number}
                    </Link>
                  </td>
                  <td>{p.partner_name ?? "—"}</td>
                  <td style={{ color: "var(--ink-3)" }}>
                    {p.origin_number ? (
                      <>
                        {ORIGIN_LABEL[p.origin_type ?? ""] ?? p.origin_type}{" "}
                        <span className="mono">{p.origin_number}</span>
                      </>
                    ) : (
                      "—"
                    )}
                  </td>
                  <td style={{ color: "var(--ink-3)" }}>
                    {type === "INTERNAL"
                      ? `${stockName(p.source_stock_id)} → ${stockName(p.destination_stock_id)}`
                      : stockName(type === "DELIVERY" ? p.source_stock_id : p.destination_stock_id)}
                  </td>
                  <td style={{ color: "var(--ink-3)" }}>{formatDate(p.scheduled_date)}</td>
                  <td>
                    <StatusPill value={p.state} map={STATE_LABEL} />
                  </td>
                  <td style={{ color: "var(--ink-3)" }}>{formatDateTime(p.created_at)}</td>
                </tr>
              ))}
            </tbody>
          </T>

          <TableState
            pending={pickings.isPending}
            error={pickings.isError ? errorMessage(pickings.error, "Erro ao carregar as transferências") : null}
            empty={rows.length === 0}
            emptyMessage={
              q || state
                ? "Nada encontrado para os filtros."
                : "Nenhuma transferência. As entregas e recebimentos nascem dos pedidos confirmados."
            }
            onRetry={() => pickings.refetch()}
          />

          <Pagination
            page={page}
            total={total}
            pageSize={PAGE_SIZE}
            fetching={pickings.isFetching}
            onPageChange={setPage}
          />
        </TableWrap>
      )}
    </Page>
  );
}
