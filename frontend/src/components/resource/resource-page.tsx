import { Icon } from "@/components/icon";
import {
  Page as PageShell,
  PageActions,
  PageHead,
  SearchWrap,
  SectionLabel,
  Subtitle,
  T,
  TableToolbar,
  TableWrap,
} from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import type { ListParams } from "@/lib/crud";
import { errorMessage } from "@/lib/query";
import type { Page } from "@/lib/types";
import { useDebouncedValue } from "@/lib/use-debounced-value";
import {
  Button,
  Field,
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from "@openlab-ui/react";
import type { UseMutationResult, UseQueryResult } from "@tanstack/react-query";
import { type ReactNode, useState } from "react";
import type { DefaultValues, FieldValues } from "react-hook-form";
import type { ZodType, ZodTypeDef } from "zod";
import { Pagination } from "./pagination";
import { ResourceDeleteDialog } from "./resource-delete-dialog";
import {
  type CreateHook,
  type FormFieldsProps,
  type UpdateHook,
  ResourceFormDialog,
} from "./resource-form-dialog";
import { RowActions } from "./row-actions";
import { TableState } from "./table-state";

const PAGE_SIZE = 10;

export interface Column<T> {
  header: string;
  cell: (item: T) => ReactNode;
  className?: string;
  muted?: boolean;
}

export interface ResourcePageProps<T, V extends FieldValues> {
  /** "CADASTRO · MARCAS" */
  eyebrow: string;
  title: string;
  /** "marca" — lower case, singular. */
  noun: string;
  feminine?: boolean;
  searchPlaceholder: string;
  columns: Column<T>[];
  getId: (item: T) => string;
  getLabel: (item: T) => string;
  useList: (params: ListParams) => UseQueryResult<Page<T>, Error>;
  useCreate: CreateHook<V>;
  useUpdate: UpdateHook<V>;
  useRemove: () => UseMutationResult<unknown, Error, string>;
  schema: ZodType<V, ZodTypeDef, V>;
  defaults: (item?: T) => DefaultValues<V>;
  renderFields: (props: FormFieldsProps<V>) => ReactNode;
  /** Sentence under the delete confirmation. */
  deleteHint?: ReactNode;
}

/**
 * List + search + pagination + create/edit/delete dialogs for a simple
 * catalog/stock cadastro. Rows are server-paginated and server-searched (`q`).
 */
export function ResourcePage<T, V extends FieldValues>({
  eyebrow,
  title,
  noun,
  feminine,
  searchPlaceholder,
  columns,
  getId,
  getLabel,
  useList,
  useCreate,
  useUpdate,
  useRemove,
  schema,
  defaults,
  renderFields,
  deleteHint,
}: ResourcePageProps<T, V>) {
  const { showToast } = useToast();
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const [form, setForm] = useState<{ open: boolean; item?: T; key: number }>({
    open: false,
    key: 0,
  });
  const openForm = (item?: T) =>
    setForm((f) => ({ open: true, item, key: f.key + 1 }));
  const closeForm = () => setForm((f) => ({ ...f, open: false }));
  const [toDelete, setToDelete] = useState<T | null>(null);

  const query = useDebouncedValue(search.trim());
  const [lastQuery, setLastQuery] = useState(query);
  // A new search term goes back to the first page (adjusted during render, no effect).
  if (query !== lastQuery) {
    setLastQuery(query);
    setPage(1);
  }

  const list = useList({ q: query, page, pageSize: PAGE_SIZE });
  const items = list.data?.data ?? [];
  const total = list.data?.total ?? 0;
  const plural = `${noun}s`;
  const article = feminine ? "a" : "o";

  return (
    <PageShell>
      <PageHead>
        <div>
          <SectionLabel>{eyebrow}</SectionLabel>
          <h1>{title}</h1>
          <Subtitle>
            <span className="mono">{total}</span>{" "}
            {total === 1 ? noun : plural}{" "}
            {total === 1 ? `cadastrad${article}` : `cadastrad${article}s`}
          </Subtitle>
        </div>
        <PageActions>
          <Button onClick={() => openForm()}>
            <Icon name="plus" /> {feminine ? "Nova" : "Novo"} {noun}
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
                  style={{ width: "400px" }}
                  placeholder={searchPlaceholder}
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
              </InputGroup>
            </Field>
          </SearchWrap>
        </TableToolbar>

        <T>
          <thead>
            <tr>
              {columns.map((c) => (
                <th key={c.header} className={c.className}>
                  {c.header}
                </th>
              ))}
              <th style={{ width: 90 }} />
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={getId(item)}>
                {columns.map((c) => (
                  <td
                    key={c.header}
                    className={c.className}
                    style={c.muted ? { color: "var(--ink-3)" } : undefined}
                  >
                    {c.cell(item)}
                  </td>
                ))}
                <td>
                  <RowActions
                    label={getLabel(item)}
                    onEdit={() => openForm(item)}
                    onDelete={() => setToDelete(item)}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </T>

        <TableState
          pending={list.isPending}
          error={
            list.isError ? errorMessage(list.error, `Erro ao carregar ${plural}`) : null
          }
          empty={items.length === 0}
          emptyMessage={
            query
              ? `Nenhum${feminine ? "a" : ""} ${noun} encontrad${article} para a busca.`
              : `Nenhum${feminine ? "a" : ""} ${noun} cadastrad${article}.`
          }
          onRetry={() => list.refetch()}
        />

        <Pagination
          page={page}
          total={total}
          pageSize={PAGE_SIZE}
          fetching={list.isFetching}
          onPageChange={setPage}
        />
      </TableWrap>

      <ResourceFormDialog<T, V>
        key={form.key}
        open={form.open}
        item={form.item}
        noun={noun}
        feminine={feminine}
        schema={schema}
        defaults={defaults}
        getId={getId}
        useCreate={useCreate}
        useUpdate={useUpdate}
        renderFields={renderFields}
        onClose={closeForm}
        onSaved={(message) => {
          closeForm();
          showToast(message);
        }}
      />

      <ResourceDeleteDialog<T>
        item={toDelete}
        noun={noun}
        feminine={feminine}
        getId={getId}
        getLabel={getLabel}
        hint={deleteHint}
        useRemove={useRemove}
        onClose={() => setToDelete(null)}
        // Deleting the last row of a page falls back to the previous one.
        onDeleted={() => items.length === 1 && page > 1 && setPage(page - 1)}
      />
    </PageShell>
  );
}
