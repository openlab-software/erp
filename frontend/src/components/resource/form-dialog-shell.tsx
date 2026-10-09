import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  FieldError,
} from "@openlab-ui/react";
import type { FormEventHandler, ReactNode } from "react";

interface FormDialogShellProps {
  open: boolean;
  title: string;
  description?: ReactNode;
  submitLabel: string;
  pending: boolean;
  serverError?: string;
  onSubmit: FormEventHandler<HTMLFormElement>;
  onClose: () => void;
  children: ReactNode;
}

/** Dialog chrome shared by the stock action forms (movement, count, transfer, reserve…). */
export function FormDialogShell({
  open,
  title,
  description,
  submitLabel,
  pending,
  serverError,
  onSubmit,
  onClose,
  children,
}: FormDialogShellProps) {
  return (
    <Dialog open={open} onOpenChange={(next) => !next && !pending && onClose()}>
      <DialogContent>
        <form onSubmit={onSubmit}>
          <DialogHeader>
            <DialogTitle>{title}</DialogTitle>
            {description && <DialogDescription>{description}</DialogDescription>}
          </DialogHeader>
          <div style={{ display: "grid", gap: 14, marginBottom: 16 }}>
            {children}
            {serverError && <FieldError errors={[{ message: serverError }]} />}
          </div>
          <DialogFooter>
            <Button type="button" variant="ghost" onClick={onClose} disabled={pending}>
              Cancelar
            </Button>
            <Button type="submit" disabled={pending}>
              {pending ? "Salvando…" : submitLabel}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
