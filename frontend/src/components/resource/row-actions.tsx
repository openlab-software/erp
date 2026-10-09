import { Icon } from "@/components/icon";
import { Button } from "@openlab-ui/react";

interface RowActionsProps {
  label: string;
  onEdit?: () => void;
  onDelete?: () => void;
}

export function RowActions({ label, onEdit, onDelete }: RowActionsProps) {
  return (
    <div className="row-actions">
      {onEdit && (
        <Button
          variant="ghost"
          size="icon"
          aria-label={`Editar ${label}`}
          onClick={onEdit}
        >
          <Icon name="edit" size={13} />
        </Button>
      )}
      {onDelete && (
        <Button
          variant="ghost"
          size="icon"
          aria-label={`Excluir ${label}`}
          onClick={onDelete}
        >
          <Icon name="trash" size={13} />
        </Button>
      )}
    </div>
  );
}
