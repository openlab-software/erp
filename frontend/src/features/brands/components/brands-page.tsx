import { Icon } from "@/components/icon";
import {
  Page,
  PageActions,
  PageHead,
  SearchWrap,
  SectionLabel,
  Subtitle,
  TableToolbar,
  TableWrap,
} from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { ApiError } from "@/lib/http";
import { useDebouncedValue } from "@/lib/use-debounced-value";
import {
  Button,
  Field,
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from "@openlab-ui/react";
import { useState } from "react";
import { useBrands } from "../queries";
import type { Brand } from "../types";
import { BrandFormDialog } from "./brand-form-dialog";
import { BrandsTable } from "./brands-table";
import { DeleteBrandDialog } from "./delete-brand-dialog";

const PAGE_SIZE = 10;

/**
 * `brand` set = editing, unset = creating. `key` changes on every open so the form
 * remounts with fresh values; closing only flips `open`, leaving the dialog mounted.
 */
interface FormState {
  open: boolean;
  brand?: Brand;
  key: number;
}

export function BrandsPage() {
  const { showToast } = useToast();
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(1);
  const [form, setForm] = useState<FormState>({ open: false, key: 0 });
  const openForm = (brand?: Brand) =>
    setForm((f) => ({ open: true, brand, key: f.key + 1 }));
  const closeForm = () => setForm((f) => ({ ...f, open: false }));
  const [toDelete, setToDelete] = useState<Brand | null>(null);

  const query = useDebouncedValue(search.trim());
  const [lastQuery, setLastQuery] = useState(query);
  // A new search term goes back to the first page (adjusted during render, no effect).
  if (query !== lastQuery) {
    setLastQuery(query);
    setPage(1);
  }

  const brandsQuery = useBrands({ q: query, page, pageSize: PAGE_SIZE });
  const brands = brandsQuery.data?.data ?? [];
  const total = brandsQuery.data?.total ?? 0;
  const lastPage = Math.max(1, Math.ceil(total / PAGE_SIZE));
  const error = brandsQuery.isError
    ? brandsQuery.error instanceof ApiError
      ? brandsQuery.error.message
      : "Erro ao carregar marcas"
    : null;

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>CADASTRO · MARCAS</SectionLabel>
          <h1>Marcas</h1>
          <Subtitle>
            <span className="mono">{total}</span> marca{total === 1 ? "" : "s"}{" "}
            cadastrada{total === 1 ? "" : "s"}
          </Subtitle>
        </div>
        <PageActions>
          <Button onClick={() => openForm()}>
            <Icon name="plus" /> Nova marca
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
                  placeholder="Buscar por descrição…"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
              </InputGroup>
            </Field>
          </SearchWrap>
        </TableToolbar>

        <BrandsTable
          brands={brands}
          page={page}
          lastPage={lastPage}
          fetching={brandsQuery.isFetching}
          pending={brandsQuery.isPending}
          error={error}
          searching={query !== ""}
          onRetry={() => brandsQuery.refetch()}
          onPageChange={setPage}
          onEdit={openForm}
          onDelete={setToDelete}
        />
      </TableWrap>

      <BrandFormDialog
        key={form.key}
        open={form.open}
        brand={form.brand}
        onClose={closeForm}
        onSaved={(message) => {
          closeForm();
          showToast(message);
        }}
      />

      <DeleteBrandDialog
        brand={toDelete}
        onClose={() => setToDelete(null)}
        // Deleting the last row of a page falls back to the previous one.
        onDeleted={() => brands.length === 1 && page > 1 && setPage(page - 1)}
      />
    </Page>
  );
}
