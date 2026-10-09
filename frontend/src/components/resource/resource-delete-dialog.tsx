import { useToast } from "@/contexts/toast-context";
import { errorMessage } from "@/lib/query";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@openlab-ui/react";
import type { UseMutationResult } from "@tanstack/react-query";
import type { ReactNode } from "react";
import { wording } from "./resource-form-dialog";

interface ResourceDeleteDialogProps<T> {
  /** Item awaiting confirmation; `null` keeps the dialog closed. */
  item: T | null;
  noun: string;
  feminine?: boolean;
  getId: (item: T) => string;
  getLabel: (item: T) => string;
  /** Extra sentence about the backend's restriction, e.g. "Em uso por produtos não pode ser excluída." */
  hint?: ReactNode;
  useRemove: () => UseMutationResult<unknown, Error, string>;
  onClose: () => void;
  onDeleted?: () => void;
}

export function ResourceDeleteDialog<T>({
  item,
  noun,
  feminine,
  getId,
  getLabel,
  hint,
  useRemove,
  onClose,
  onDeleted,
}: ResourceDeleteDialogProps<T>) {
  const { showToast } = useToast();
  const remove = useRemove();
  const words = wording(noun, feminine);

  const confirm = () => {
    if (!item) return;
    remove.mutate(getId(item), {
      onSuccess: () => {
        showToast(`${noun} "${getLabel(item)}" ${words.deleted}`.replace(/^./, (c) => c.toUpperCase()));
        onDeleted?.();
      },
      onError: (e) => showToast(errorMessage(e, `Erro ao excluir ${noun}`)),
      onSettled: onClose,
    });
  };

  return (
    <AlertDialog
      open={item !== null}
      onOpenChange={(open) => !open && onClose()}
    >
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Excluir {noun}?</AlertDialogTitle>
          <AlertDialogDescription>
            {feminine ? "A" : "O"} {noun} <b>{item ? getLabel(item) : ""}</b>{" "}
            será {words.deleted}. {hint}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Cancelar</AlertDialogCancel>
          <AlertDialogAction onClick={confirm}>Excluir</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
