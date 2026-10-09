import { Icon } from "@/components/icon";
import { Pagination } from "@/components/resource/pagination";
import { RowActions } from "@/components/resource/row-actions";
import { TableState } from "@/components/resource/table-state";
import { ResourceDeleteDialog } from "@/components/resource/resource-delete-dialog";
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
import { fmtBRL } from "@/data";
import { useBrandOptions } from "@/features/brands";
import { useCategoryOptions } from "@/features/categories";
import { useToast } from "@/contexts/toast-context";
import { errorMessage } from "@/lib/query";
import { useDebouncedValue } from "@/lib/use-debounced-value";
import { Link } from "@modern-js/runtime/router";
import {
  Button,
  Field,
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from "@openlab-ui/react";
import { useState } from "react";
import { exportProducts } from "../api";
import { ImportProductsDialog } from "../components/import-products-dialog";
import { ProductFormDialog } from "../components/product-form-dialog";
import { STATUS_LABEL, TYPE_LABEL } from "../labels";
import { useDeleteProduct, useProducts } from "../queries";
import type { Product, ProductFilters } from "../types";

const PAGE_SIZE = 10;
const NO_FILTERS: ProductFilters = {
  q: "",
  category_id: "",
  status: "",
  type: "",
  brand_id: "",
};

/** `product` set = editing, unset = creating; `key` changes on every open so the form remounts. */
interface FormState {
  open: boolean;
  product?: Product;
  key: number;
}

export function ProductsPage() {
  const { showToast } = useToast();
  const [search, setSearch] = useState("");
  const [filters, setFilters] = useState(NO_FILTERS);
  const [page, setPage] = useState(1);
  const [form, setForm] = useState<FormState>({ open: false, key: 0 });
  const [toDelete, setToDelete] = useState<Product | null>(null);
  const [importing, setImporting] = useState(false);
  const [exporting, setExporting] = useState(false);

  const q = useDebouncedValue(search.trim());
  const applied: ProductFilters = { ...filters, q };
  // Any change of the applied filters goes back to the first page (adjusted during render).
  const signature = JSON.stringify(applied);
  const [lastSignature, setLastSignature] = useState(signature);
  if (signature !== lastSignature) {
    setLastSignature(signature);
    setPage(1);
  }

  const categories = useCategoryOptions();
  const brands = useBrandOptions();
  const products = useProducts({ ...applied, page, pageSize: PAGE_SIZE });
  const rows = products.data?.data ?? [];
  const total = products.data?.total ?? 0;
  const filtering = Object.values(applied).some(Boolean);

  const setFilter = (key: keyof ProductFilters) => (value: string) =>
    setFilters((f) => ({ ...f, [key]: value }));
  const openForm = (product?: Product) =>
    setForm((f) => ({ open: true, product, key: f.key + 1 }));

  const exportCsv = async () => {
    setExporting(true);
    try {
      const { csv } = await exportProducts(applied);
      const link = document.createElement("a");
      link.href = URL.createObjectURL(new Blob([csv], { type: "text/csv" }));
      link.download = "produtos.csv";
      link.click();
      URL.revokeObjectURL(link.href);
    } catch (e) {
      showToast(errorMessage(e, "Erro ao exportar produtos"));
    } finally {
      setExporting(false);
    }
  };

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>CADASTRO · PRODUTOS</SectionLabel>
          <h1>Produtos</h1>
          <Subtitle>
            <span className="mono">{total}</span> produto
            {total === 1 ? "" : "s"} {filtering ? "encontrado" : "cadastrado"}
            {total === 1 ? "" : "s"}
          </Subtitle>
        </div>
        <PageActions>
          <Button variant="ghost" onClick={() => setImporting(true)}>
            <Icon name="upload" /> Importar
          </Button>
          <Button variant="ghost" onClick={exportCsv} disabled={exporting}>
            <Icon name="download" /> {exporting ? "Exportando…" : "Exportar"}
          </Button>
          <Button onClick={() => openForm()}>
            <Icon name="plus" /> Novo produto
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
                  style={{ width: "320px" }}
                  placeholder="Buscar por descrição…"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
              </InputGroup>
            </Field>
          </SearchWrap>
          <div style={{ display: "flex", gap: 8, marginLeft: 8 }}>
            <FSelect
              aria-label="Categoria"
              style={{ width: 170 }}
              value={filters.category_id}
              onChange={(e) => setFilter("category_id")(e.target.value)}
            >
              <option value="">Todas as categorias</option>
              {(categories.data ?? []).map((c) => (
                <option key={c.category_id} value={c.category_id}>
                  {c.description}
                </option>
              ))}
            </FSelect>
            <FSelect
              aria-label="Marca"
              style={{ width: 150 }}
              value={filters.brand_id}
              onChange={(e) => setFilter("brand_id")(e.target.value)}
            >
              <option value="">Todas as marcas</option>
              {(brands.data ?? []).map((b) => (
                <option key={b.brand_id} value={b.brand_id}>
                  {b.description}
                </option>
              ))}
            </FSelect>
            <FSelect
              aria-label="Status"
              style={{ width: 140 }}
              value={filters.status}
              onChange={(e) => setFilter("status")(e.target.value)}
            >
              <option value="">Todos os status</option>
              {Object.entries(STATUS_LABEL).map(([value, { label }]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </FSelect>
            <FSelect
              aria-label="Tipo"
              style={{ width: 120 }}
              value={filters.type}
              onChange={(e) => setFilter("type")(e.target.value)}
            >
              <option value="">Todos os tipos</option>
              {Object.entries(TYPE_LABEL).map(([value, label]) => (
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
              <th>Produto</th>
              <th>Categoria</th>
              <th>Marca</th>
              <th>Tipo</th>
              <th className="num">Custo</th>
              <th className="num">Preço</th>
              <th>Status</th>
              <th style={{ width: 110 }} />
            </tr>
          </thead>
          <tbody>
            {rows.map((p) => {
              const status = STATUS_LABEL[p.status];
              return (
                <tr key={p.product_id}>
                  <td className="id">{p.product_id}</td>
                  <td>
                    <Link
                      to={`/produtos/${p.product_id}`}
                      style={{ color: "var(--ink-1)", fontWeight: 500 }}
                    >
                      {p.description}
                    </Link>
                  </td>
                  <td style={{ color: "var(--ink-3)" }}>
                    {p.category.description}
                  </td>
                  <td style={{ color: "var(--ink-3)" }}>
                    {p.brand?.description ?? "—"}
                  </td>
                  <td style={{ color: "var(--ink-3)" }}>{TYPE_LABEL[p.type]}</td>
                  <td className="num">{fmtBRL(p.cost_price)}</td>
                  <td className="num">{fmtBRL(p.sale_price)}</td>
                  <td>
                    <Status variant={status.variant || undefined}>
                      {status.label}
                    </Status>
                  </td>
                  <td>
                    <div style={{ display: "flex", alignItems: "center", gap: 2 }}>
                      <Link to={`/produtos/${p.product_id}`}>
                        <Button
                          variant="ghost"
                          size="icon"
                          aria-label={`Abrir ${p.description}`}
                        >
                          <Icon name="external" size={13} />
                        </Button>
                      </Link>
                      <RowActions
                        label={p.description}
                        onEdit={() => openForm(p)}
                        onDelete={() => setToDelete(p)}
                      />
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </T>

        <TableState
          pending={products.isPending}
          error={
            products.isError
              ? errorMessage(products.error, "Erro ao carregar produtos")
              : null
          }
          empty={rows.length === 0}
          emptyMessage={
            filtering
              ? "Nenhum produto encontrado para os filtros."
              : "Nenhum produto cadastrado."
          }
          onRetry={() => products.refetch()}
        />

        <Pagination
          page={page}
          total={total}
          pageSize={PAGE_SIZE}
          fetching={products.isFetching}
          onPageChange={setPage}
        />
      </TableWrap>

      <ProductFormDialog
        key={form.key}
        open={form.open}
        product={form.product}
        onClose={() => setForm((f) => ({ ...f, open: false }))}
        onSaved={(message) => {
          setForm((f) => ({ ...f, open: false }));
          showToast(message);
        }}
      />

      <ResourceDeleteDialog<Product>
        item={toDelete}
        noun="produto"
        getId={(p) => p.product_id}
        getLabel={(p) => p.description}
        hint="Produtos com saldo em estoque podem não ser excluídos."
        useRemove={useDeleteProduct}
        onClose={() => setToDelete(null)}
        onDeleted={() => rows.length === 1 && page > 1 && setPage(page - 1)}
      />

      <ImportProductsDialog open={importing} onClose={() => setImporting(false)} />
    </Page>
  );
}
