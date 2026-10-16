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
  Stat,
  StatGrid,
  StatLabel,
  StatValue,
  Subtitle,
  T,
  TableToolbar,
  TableWrap,
} from "@/components/ui";
import { formatDateTime } from "@/lib/format";
import { brl, formatDate } from "@/lib/money";
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
import type { OrderHooks } from "../queries";
import type { OrderKind } from "../types";

const PAGE_SIZE = 10;

interface OrdersPageProps {
  kind: OrderKind;
  hooks: OrderHooks;
}

/** List of quotations / orders of one kind with search, state filter, totals and a link to each document. */
export function OrdersPage({ kind, hooks }: OrdersPageProps) {
  const navigate = useNavigate();
  const [search, setSearch] = useState("");
  const [state, setState] = useState("");
  const [page, setPage] = useState(1);

  const q = useDebouncedValue(search.trim());
  const signature = JSON.stringify({ q, state });
  const [lastSignature, setLastSignature] = useState(signature);
  if (signature !== lastSignature) {
    setLastSignature(signature);
    setPage(1);
  }

  const orders = hooks.useOrders({ q, state, page, pageSize: PAGE_SIZE });
  const summary = hooks.useSummary();
  const rows = orders.data?.data ?? [];
  const total = orders.data?.total ?? 0;
  const filtering = Boolean(q || state);

  const sum = (states: string[], field: "count" | "total_amount") =>
    (summary.data ?? [])
      .filter((t) => states.includes(t.state))
      .reduce((acc, t) => acc + t[field], 0);

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>{kind.eyebrow}</SectionLabel>
          <h1>{kind.title}</h1>
          <Subtitle>
            <span className="mono">{total}</span> documento{total === 1 ? "" : "s"}{" "}
            {filtering ? "encontrado" : "cadastrado"}
            {total === 1 ? "" : "s"}
          </Subtitle>
        </div>
        <PageActions>
          <Button onClick={() => navigate(`${kind.route}/novo`)}>
            <Icon name="plus" /> {kind.newLabel}
          </Button>
        </PageActions>
      </PageHead>

      <StatGrid>
        {kind.summary.map((s) => (
          <Stat key={s.label}>
            <StatLabel>{s.label}</StatLabel>
            <StatValue>{sum(s.states, "count")}</StatValue>
            <div style={{ fontSize: 12, color: "var(--ink-3)" }}>
              {brl(sum(s.states, "total_amount"))}
            </div>
          </Stat>
        ))}
      </StatGrid>

      <div style={{ height: 20 }} />

      <TableWrap>
        <TableToolbar>
          <SearchWrap>
            <Field>
              <InputGroup>
                <InputGroupAddon>
                  <Icon name="search" size={13} />
                </InputGroupAddon>
                <InputGroupInput
                  style={{ width: "340px" }}
                  placeholder={`Buscar por número ou ${kind.partnerLabel.toLowerCase()}…`}
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
              </InputGroup>
            </Field>
          </SearchWrap>
          <FSelect
            aria-label="Estado"
            style={{ width: 170, marginLeft: 8 }}
            value={state}
            onChange={(e) => setState(e.target.value)}
          >
            <option value="">Todos os estados</option>
            {Object.entries(kind.states).map(([value, { label }]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </FSelect>
          <div style={{ flex: 1 }} />
          {filtering && (
            <Button
              variant="ghost"
              size="xs"
              onClick={() => {
                setSearch("");
                setState("");
              }}
            >
              <Icon name="x" size={12} /> Limpar
            </Button>
          )}
        </TableToolbar>

        <T>
          <thead>
            <tr>
              <th>Número</th>
              <th>{kind.partnerLabel}</th>
              <th>{kind.dateLabel}</th>
              <th>Estado</th>
              <th>{kind.fulfilmentLabel}</th>
              <th>{kind.billingLabel}</th>
              <th className="num">Total</th>
              <th>Criado em</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((o) => (
              <tr key={o.id}>
                <td>
                  <Link
                    to={`${kind.route}/${o.id}`}
                    className="mono"
                    style={{ color: "var(--ink-1)", fontWeight: 500 }}
                  >
                    {o.number}
                  </Link>
                </td>
                <td>{o.partnerName}</td>
                <td style={{ color: "var(--ink-3)" }}>{formatDate(o.date)}</td>
                <td>
                  <StatusPill value={o.state} map={kind.states} />
                </td>
                <td>
                  <StatusPill value={o.fulfilmentStatus} map={kind.fulfilment} />
                </td>
                <td>
                  <StatusPill value={o.billingStatus} map={kind.billing} />
                </td>
                <td className="num">{brl(o.total)}</td>
                <td style={{ color: "var(--ink-3)" }}>{formatDateTime(o.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </T>

        <TableState
          pending={orders.isPending}
          error={
            orders.isError ? errorMessage(orders.error, "Erro ao carregar os documentos") : null
          }
          empty={rows.length === 0}
          emptyMessage={
            filtering ? "Nada encontrado para os filtros." : `Nenhum documento ainda. Use "${kind.newLabel}".`
          }
          onRetry={() => orders.refetch()}
        />

        <Pagination
          page={page}
          total={total}
          pageSize={PAGE_SIZE}
          fetching={orders.isFetching}
          onPageChange={setPage}
        />
      </TableWrap>
    </Page>
  );
}
