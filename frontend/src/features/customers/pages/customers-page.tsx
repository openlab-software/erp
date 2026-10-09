import { Icon } from "@/components/icon";
import { Pagination } from "@/components/resource/pagination";
import { ResourceDeleteDialog } from "@/components/resource/resource-delete-dialog";
import { RowActions } from "@/components/resource/row-actions";
import { TableState } from "@/components/resource/table-state";
import {
  FSelect,
  Page,
  PageActions,
  PageHead,
  SearchWrap,
  SectionLabel,
  Status,
  Subtitle,
  T,
  TableToolbar,
  TableWrap,
} from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { formatDateTime, formatDocument } from "@/lib/format";
import { errorMessage } from "@/lib/query";
import { useDebouncedValue } from "@/lib/use-debounced-value";
import {
  Button,
  Field,
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from "@openlab-ui/react";
import { Link } from "@modern-js/runtime/router";
import { useState } from "react";
import { CustomerFormDialog } from "../components/customer-form-dialog";
import { DOCUMENT_LABEL, STATUS_LABEL, TYPE_LABEL } from "../labels";
import {
  useChangeCustomerStatus,
  useCustomers,
  useDeleteCustomer,
} from "../queries";
import type { Customer, CustomerFilters } from "../types";

const PAGE_SIZE = 10;
const NO_FILTERS = { type: "", status: "" };

/** `customer` set = editing, unset = creating; `key` changes on every open so the form remounts. */
interface FormState {
  open: boolean;
  customer?: Customer;
  key: number;
}

export function CustomersPage() {
  const { showToast } = useToast();
  const [search, setSearch] = useState("");
  const [filters, setFilters] = useState(NO_FILTERS);
  const [page, setPage] = useState(1);
  const [form, setForm] = useState<FormState>({ open: false, key: 0 });
  const [toDelete, setToDelete] = useState<Customer | null>(null);
  const changeStatus = useChangeCustomerStatus();

  const q = useDebouncedValue(search.trim());
  const applied: CustomerFilters = { ...filters, q };
  // Any change of the applied filters goes back to the first page (adjusted during render).
  const signature = JSON.stringify(applied);
  const [lastSignature, setLastSignature] = useState(signature);
  if (signature !== lastSignature) {
    setLastSignature(signature);
    setPage(1);
  }

  const customers = useCustomers({ ...applied, page, pageSize: PAGE_SIZE });
  const rows = customers.data?.data ?? [];
  const total = customers.data?.total ?? 0;
  const filtering = Object.values(applied).some(Boolean);

  const openForm = (customer?: Customer) =>
    setForm((f) => ({ open: true, customer, key: f.key + 1 }));
  const closeForm = () => setForm((f) => ({ ...f, open: false }));

  const toggleStatus = (c: Customer) => {
    const status = c.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    changeStatus.mutate(
      { id: c.customer_id, status },
      {
        onSuccess: () =>
          showToast(status === "ACTIVE" ? "Cliente ativado" : "Cliente inativado"),
        onError: (e) => showToast(errorMessage(e, "Erro ao alterar o status")),
      },
    );
  };

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>CADASTRO · CLIENTES</SectionLabel>
          <h1>Clientes</h1>
          <Subtitle>
            <span className="mono">{total}</span> cliente{total === 1 ? "" : "s"}{" "}
            {filtering ? "encontrado" : "cadastrado"}
            {total === 1 ? "" : "s"}
          </Subtitle>
        </div>
        <PageActions>
          <Button onClick={() => openForm()}>
            <Icon name="plus" /> Novo cliente
          </Button>
        </PageActions>
      </PageHead>

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
                  placeholder="Buscar por nome, documento ou e-mail…"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
              </InputGroup>
            </Field>
          </SearchWrap>
          <div style={{ display: "flex", gap: 8, marginLeft: 8 }}>
            <FSelect
              aria-label="Tipo"
              style={{ width: 160 }}
              value={filters.type}
              onChange={(e) => setFilters((f) => ({ ...f, type: e.target.value }))}
            >
              <option value="">Todos os tipos</option>
              {Object.entries(TYPE_LABEL).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </FSelect>
            <FSelect
              aria-label="Status"
              style={{ width: 140 }}
              value={filters.status}
              onChange={(e) => setFilters((f) => ({ ...f, status: e.target.value }))}
            >
              <option value="">Todos os status</option>
              {Object.entries(STATUS_LABEL).map(([value, { label }]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </FSelect>
          </div>
          <div style={{ flex: 1 }} />
          {filtering && (
            <Button
              variant="ghost"
              size="xs"
              onClick={() => {
                setFilters(NO_FILTERS);
                setSearch("");
              }}
            >
              <Icon name="x" size={12} /> Limpar
            </Button>
          )}
        </TableToolbar>

        <T>
          <thead>
            <tr>
              <th>Código</th>
              <th>Cliente</th>
              <th>Tipo</th>
              <th>Documento</th>
              <th>Contato</th>
              <th>Status</th>
              <th>Cadastrado em</th>
              <th style={{ width: 130 }} />
            </tr>
          </thead>
          <tbody>
            {rows.map((c) => {
              const status = STATUS_LABEL[c.status];
              return (
                <tr key={c.customer_id}>
                  <td className="id">{c.customer_id}</td>
                  <td>
                    <Link
                      to={`/clientes/${c.customer_id}`}
                      style={{ color: "var(--ink-1)", fontWeight: 500 }}
                    >
                      {c.name}
                    </Link>
                    {c.trade_name && (
                      <div style={{ fontSize: 11.5, color: "var(--ink-3)" }}>
                        {c.trade_name}
                      </div>
                    )}
                  </td>
                  <td style={{ color: "var(--ink-3)" }}>{TYPE_LABEL[c.type]}</td>
                  <td className="mono">
                    <span style={{ color: "var(--ink-3)" }}>{DOCUMENT_LABEL[c.type]} </span>
                    {formatDocument(c.document)}
                  </td>
                  <td style={{ color: "var(--ink-3)" }}>
                    {c.email ?? c.phone ?? "—"}
                  </td>
                  <td>
                    <Status variant={status.variant || undefined}>{status.label}</Status>
                  </td>
                  <td style={{ color: "var(--ink-3)" }}>{formatDateTime(c.created_at)}</td>
                  <td>
                    <div style={{ display: "flex", alignItems: "center", gap: 2 }}>
                      <Button
                        variant="ghost"
                        size="xs"
                        disabled={changeStatus.isPending}
                        onClick={() => toggleStatus(c)}
                      >
                        {c.status === "ACTIVE" ? "Inativar" : "Ativar"}
                      </Button>
                      <RowActions
                        label={c.name}
                        onEdit={() => openForm(c)}
                        onDelete={() => setToDelete(c)}
                      />
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </T>

        <TableState
          pending={customers.isPending}
          error={
            customers.isError
              ? errorMessage(customers.error, "Erro ao carregar clientes")
              : null
          }
          empty={rows.length === 0}
          emptyMessage={
            filtering
              ? "Nenhum cliente encontrado para os filtros."
              : "Nenhum cliente cadastrado."
          }
          onRetry={() => customers.refetch()}
        />

        <Pagination
          page={page}
          total={total}
          pageSize={PAGE_SIZE}
          fetching={customers.isFetching}
          onPageChange={setPage}
        />
      </TableWrap>

      <CustomerFormDialog
        key={form.key}
        open={form.open}
        customer={form.customer}
        onClose={closeForm}
        onSaved={(message) => {
          closeForm();
          showToast(message);
        }}
      />

      <ResourceDeleteDialog<Customer>
        item={toDelete}
        noun="cliente"
        getId={(c) => c.customer_id}
        getLabel={(c) => c.name}
        hint="Para manter o histórico, prefira inativar o cliente."
        useRemove={useDeleteCustomer}
        onClose={() => setToDelete(null)}
        onDeleted={() => rows.length === 1 && page > 1 && setPage(page - 1)}
      />
    </Page>
  );
}
