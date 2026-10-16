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
  StatDelta,
  StatGrid,
  StatLabel,
  StatValue,
  Subtitle,
  T,
  Tab,
  TableToolbar,
  TableWrap,
  Tabs,
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
import { KIND_LABEL, PAYMENT_LABEL, STATE_LABEL } from "../labels";
import { useInvoices, usePosition } from "../queries";
import type { InvoiceKind } from "../types";

const PAGE_SIZE = 10;

export function InvoicesPage() {
  const navigate = useNavigate();
  const [kind, setKind] = useState<InvoiceKind>("CUSTOMER_INVOICE");
  const [search, setSearch] = useState("");
  const [state, setState] = useState("");
  const [overdue, setOverdue] = useState(false);
  const [page, setPage] = useState(1);

  const q = useDebouncedValue(search.trim());
  const signature = JSON.stringify({ kind, q, state, overdue });
  const [lastSignature, setLastSignature] = useState(signature);
  if (signature !== lastSignature) {
    setLastSignature(signature);
    setPage(1);
  }

  const invoices = useInvoices({ kind, q, state, overdue, page, pageSize: PAGE_SIZE });
  const position = usePosition();
  const rows = invoices.data?.data ?? [];
  const total = invoices.data?.total ?? 0;
  const filtering = Boolean(q || state || overdue);
  const labels = KIND_LABEL[kind];

  const receivable = position.data?.find((p) => p.kind === "CUSTOMER_INVOICE");
  const payable = position.data?.find((p) => p.kind === "VENDOR_BILL");

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>ADMINISTRATIVO · FINANCEIRO</SectionLabel>
          <h1>Contas a pagar &amp; receber</h1>
          <Subtitle>
            <span className="mono">{total}</span> documento{total === 1 ? "" : "s"}
          </Subtitle>
        </div>
        <PageActions>
          <Button onClick={() => navigate(`/financeiro/novo?kind=${kind}`)}>
            <Icon name="plus" /> {labels.newLabel}
          </Button>
        </PageActions>
      </PageHead>

      <StatGrid>
        <Stat>
          <StatLabel>A receber (em aberto)</StatLabel>
          <StatValue>{brl(receivable?.open_amount)}</StatValue>
          <StatDelta>{receivable?.open_count ?? 0} faturas</StatDelta>
        </Stat>
        <Stat>
          <StatLabel>A pagar (em aberto)</StatLabel>
          <StatValue>{brl(payable?.open_amount)}</StatValue>
          <StatDelta>{payable?.open_count ?? 0} faturas</StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Recebimentos vencidos</StatLabel>
          <StatValue>{brl(receivable?.overdue_amount)}</StatValue>
          <StatDelta $neg={(receivable?.overdue_count ?? 0) > 0}>
            {receivable?.overdue_count ?? 0} fatura{receivable?.overdue_count === 1 ? "" : "s"}
          </StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Pagamentos vencidos</StatLabel>
          <StatValue>{brl(payable?.overdue_amount)}</StatValue>
          <StatDelta $neg={(payable?.overdue_count ?? 0) > 0}>
            {payable?.overdue_count ?? 0} fatura{payable?.overdue_count === 1 ? "" : "s"}
          </StatDelta>
        </Stat>
      </StatGrid>

      <div style={{ height: 24 }} />

      <Tabs>
        {(Object.keys(KIND_LABEL) as InvoiceKind[]).map((k) => (
          <Tab key={k} $active={kind === k} onClick={() => setKind(k)}>
            {KIND_LABEL[k].tab}
            {(k === "CUSTOMER_INVOICE" ? receivable : payable)?.draft_count ? (
              <span className="mono" style={{ marginLeft: 6, color: "var(--ink-4)" }}>
                {(k === "CUSTOMER_INVOICE" ? receivable : payable)?.draft_count} rascunho(s)
              </span>
            ) : null}
          </Tab>
        ))}
      </Tabs>

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
                  placeholder="Buscar por número, referência ou parte…"
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
          <label style={{ display: "flex", alignItems: "center", gap: 6, fontSize: 13, marginLeft: 8 }}>
            <input type="checkbox" checked={overdue} onChange={(e) => setOverdue(e.target.checked)} />
            Só vencidas
          </label>
        </TableToolbar>

        <T>
          <thead>
            <tr>
              <th>Número</th>
              <th>{labels.partner}</th>
              <th>Origem</th>
              <th>Vencimento</th>
              <th>Estado</th>
              <th>Pagamento</th>
              <th className="num">Total</th>
              <th className="num">Em aberto</th>
              <th>Criada em</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((i) => (
              <tr key={i.invoice_id}>
                <td>
                  <Link
                    to={`/financeiro/${i.invoice_id}`}
                    className="mono"
                    style={{ color: "var(--ink-1)", fontWeight: 500 }}
                  >
                    {i.number ?? "Rascunho"}
                  </Link>
                </td>
                <td>{i.partner_name}</td>
                <td className="mono" style={{ color: "var(--ink-3)" }}>{i.origin_number ?? "—"}</td>
                <td style={{ color: i.overdue ? "var(--neg)" : "var(--ink-3)" }}>
                  {formatDate(i.due_date)}
                </td>
                <td>
                  <StatusPill value={i.state} map={STATE_LABEL} />
                </td>
                <td>
                  {i.state === "POSTED" ? <StatusPill value={i.payment_state} map={PAYMENT_LABEL} /> : "—"}
                </td>
                <td className="num">{brl(i.total_amount)}</td>
                <td className="num">{i.state === "POSTED" ? brl(i.residual_amount) : "—"}</td>
                <td style={{ color: "var(--ink-3)" }}>{formatDateTime(i.created_at)}</td>
              </tr>
            ))}
          </tbody>
        </T>

        <TableState
          pending={invoices.isPending}
          error={invoices.isError ? errorMessage(invoices.error, "Erro ao carregar as faturas") : null}
          empty={rows.length === 0}
          emptyMessage={filtering ? "Nada encontrado para os filtros." : "Nenhuma fatura ainda."}
          onRetry={() => invoices.refetch()}
        />

        <Pagination
          page={page}
          total={total}
          pageSize={PAGE_SIZE}
          fetching={invoices.isFetching}
          onPageChange={setPage}
        />
      </TableWrap>
    </Page>
  );
}
