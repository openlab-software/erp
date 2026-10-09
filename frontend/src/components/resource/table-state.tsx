import { Icon } from "@/components/icon";
import { Empty } from "@/components/ui";
import { Button } from "@openlab-ui/react";

interface TableStateProps {
  pending: boolean;
  error: string | null;
  empty: boolean;
  emptyMessage: string;
  onRetry: () => void;
}

/** Loading / error / empty message shown under a table. */
export function TableState({
  pending,
  error,
  empty,
  emptyMessage,
  onRetry,
}: TableStateProps) {
  if (pending) return <Empty>Carregando…</Empty>;
  if (error) {
    return (
      <Empty>
        {error}{" "}
        <Button variant="ghost" size="xs" onClick={onRetry}>
          <Icon name="refresh" size={12} /> Tentar novamente
        </Button>
      </Empty>
    );
  }
  return empty ? <Empty>{emptyMessage}</Empty> : null;
}
