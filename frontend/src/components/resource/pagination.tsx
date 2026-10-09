import { TableFooter } from "@/components/ui";
import { Button } from "@openlab-ui/react";

interface PaginationProps {
  page: number;
  total: number;
  pageSize: number;
  /** A request is in flight (page change, refetch). */
  fetching?: boolean;
  onPageChange: (page: number) => void;
}

export const lastPageOf = (total: number, pageSize: number) =>
  Math.max(1, Math.ceil(total / pageSize));

export function Pagination({
  page,
  total,
  pageSize,
  fetching,
  onPageChange,
}: PaginationProps) {
  const lastPage = lastPageOf(total, pageSize);
  return (
    <TableFooter>
      <span>
        Página <b style={{ color: "var(--ink-1)" }}>{page}</b> de {lastPage} ·{" "}
        {total} registro{total === 1 ? "" : "s"}
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
  );
}
