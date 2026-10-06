import { ApiError } from "@/lib/http";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Field,
  FieldError,
  FieldLabel,
  Input,
} from "@openlab-ui/react";
import { useForm } from "react-hook-form";
import { useCreateBrand, useUpdateBrand } from "../queries";
import { type BrandFormValues, brandSchema } from "../schemas";
import type { Brand } from "../types";

interface BrandFormDialogProps {
  open: boolean;
  /** Brand being edited; omit to create a new one. */
  brand?: Brand;
  onClose: () => void;
  onSaved: (message: string) => void;
}

/**
 * Keep this mounted and drive it with `open` (instead of unmounting it while open) so the
 * dialog can run its own close/cleanup. Remount with a new `key` to reset the form.
 */
export function BrandFormDialog({
  open,
  brand,
  onClose,
  onSaved,
}: BrandFormDialogProps) {
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<BrandFormValues>({
    resolver: zodResolver(brandSchema),
    defaultValues: { description: brand?.description ?? "" },
  });
  const createBrand = useCreateBrand();
  const updateBrand = useUpdateBrand();
  const saving = createBrand.isPending || updateBrand.isPending;

  // Runs only with values that already passed the zod schema (trimmed, non-empty).
  const submit = handleSubmit(({ description }) => {
    const options = {
      // Server-side rejections (e.g. duplicated description) show up on the field.
      onError: (err: Error) =>
        setError("description", {
          message:
            err instanceof ApiError ? err.message : "Erro ao salvar marca",
        }),
    };
    if (brand) {
      updateBrand.mutate(
        { id: brand.brand_id, description },
        { ...options, onSuccess: () => onSaved("Marca atualizada") },
      );
    } else {
      createBrand.mutate(description, {
        ...options,
        onSuccess: () => onSaved("Marca criada"),
      });
    }
  });

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => !next && !saving && onClose()}
    >
      <DialogContent>
        <form onSubmit={submit}>
          <DialogHeader>
            <DialogTitle>{brand ? "Editar marca" : "Nova marca"}</DialogTitle>
            <DialogDescription>
              {brand ? brand.brand_id : "Informe a descrição da marca."}
            </DialogDescription>
          </DialogHeader>
          <Field style={{ marginBottom: 16 }}>
            <FieldLabel htmlFor="brand-description">Descrição</FieldLabel>
            <Input
              id="brand-description"
              autoFocus
              aria-invalid={errors.description ? true : undefined}
              {...register("description")}
            />
            <FieldError errors={[errors.description]} />
          </Field>
          <DialogFooter>
            <Button
              type="button"
              variant="ghost"
              onClick={onClose}
              disabled={saving}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? "Salvando…" : "Salvar"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
