import { ApiError } from "@/lib/http";
import { useToast } from "@/contexts/toast-context";
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
import { useDeleteBrand } from "../queries";
import type { Brand } from "../types";

interface DeleteBrandDialogProps {
  /** Brand awaiting confirmation; `null` keeps the dialog closed. */
  brand: Brand | null;
  onClose: () => void;
  onDeleted: () => void;
}

export function DeleteBrandDialog({
  brand,
  onClose,
  onDeleted,
}: DeleteBrandDialogProps) {
  const { showToast } = useToast();
  const deleteBrand = useDeleteBrand();

  const confirm = () => {
    if (!brand) return;
    deleteBrand.mutate(brand.brand_id, {
      onSuccess: () => {
        showToast(`Marca "${brand.description}" excluída`);
        onDeleted();
      },
      onError: (e) =>
        showToast(e instanceof ApiError ? e.message : "Erro ao excluir marca"),
      onSettled: onClose,
    });
  };

  return (
    <AlertDialog
      open={brand !== null}
      onOpenChange={(open) => !open && onClose()}
    >
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Excluir marca?</AlertDialogTitle>
          <AlertDialogDescription>
            A marca <b>{brand?.description}</b> será excluída. Marcas em uso por
            produtos não podem ser excluídas.
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
