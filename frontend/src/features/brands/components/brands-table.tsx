import { Icon } from "@/components/icon";
import { Empty, T, TableFooter } from "@/components/ui";
import { formatDateTime } from "@/lib/format";
import { Button } from "@openlab-ui/react";
import type { Brand } from "../types";

interface BrandsTableProps {
  brands: Brand[];
  page: number;
  lastPage: number;
  /** A request is in flight (initial load, refetch or page change). */
  fetching: boolean;
  /** First load, nothing to show yet. */
  pending: boolean;
  error: string | null;
  searching: boolean;
  onRetry: () => void;
  onPageChange: (page: number) => void;
  onEdit: (brand: Brand) => void;
  onDelete: (brand: Brand) => void;
}

export function BrandsTable({
  brands,
  page,
  lastPage,
  fetching,
  pending,
  error,
  searching,
  onRetry,
  onPageChange,
  onEdit,
  onDelete,
}: BrandsTableProps) {
  return (
    <>
      <T>
        <thead>
          <tr>
            <th>Código</th>
            <th>Descrição</th>
            <th>Criada em</th>
            <th>Atualizada em</th>
            <th style={{ width: 90 }} />
          </tr>
        </thead>
        <tbody>
          {brands.map((b) => (
            <tr key={b.brand_id}>
              <td className="id">{b.brand_id}</td>
              <td style={{ fontWeight: 500 }}>{b.description}</td>
              <td style={{ color: "var(--ink-3)" }}>
                {formatDateTime(b.created_at)}
              </td>
              <td style={{ color: "var(--ink-3)" }}>
                {formatDateTime(b.updated_at)}
              </td>
              <td>
                <div className="row-actions">
                  <Button
                    variant="ghost"
                    size="icon"
                    aria-label={`Editar ${b.description}`}
                    onClick={() => onEdit(b)}
                  >
                    <Icon name="edit" size={13} />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    aria-label={`Excluir ${b.description}`}
                    onClick={() => onDelete(b)}
                  >
                    <Icon name="trash" size={13} />
                  </Button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </T>

      {pending && <Empty>Carregando…</Empty>}
      {error && (
        <Empty>
          {error}{" "}
          <Button variant="ghost" size="xs" onClick={onRetry}>
            <Icon name="refresh" size={12} /> Tentar novamente
          </Button>
        </Empty>
      )}
      {!pending && !error && brands.length === 0 && (
        <Empty>
          {searching
            ? "Nenhuma marca encontrada para a busca."
            : "Nenhuma marca cadastrada."}
        </Empty>
      )}

      <TableFooter>
        <span>
          Página <b style={{ color: "var(--ink-1)" }}>{page}</b> de {lastPage}
        </span>
        <div style={{ display: "flex", gap: 6 }}>
          <Button
            variant="ghost"
            size="xs"
            disabled={page <= 1 || fetching}
            onClick={() => onPageChange(page - 1)}
          >
            ‹ Anterior
          </Button>
          <Button
            variant="ghost"
            size="xs"
            disabled={page >= lastPage || fetching}
            onClick={() => onPageChange(page + 1)}
          >
            Próxima ›
          </Button>
        </div>
      </TableFooter>
    </>
  );
}
