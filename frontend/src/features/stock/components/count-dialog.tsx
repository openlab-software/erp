import { FieldShell, TextField } from "@/components/resource/form-fields";
import { FormDialogShell } from "@/components/resource/form-dialog-shell";
import { ProductPicker } from "@/features/products";
import { errorMessage } from "@/lib/query";
import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { useCreateCount } from "../queries";
import { type CountFormValues, countSchema } from "../schemas";

interface CountDialogProps {
  open: boolean;
  stockId: string;
  stockName: string;
  onClose: () => void;
  onSaved: (message: string) => void;
}

/** Registers an inventory count; the service stores the system value next to the counted one. */
export function CountDialog({ open, stockId, stockName, onClose, onSaved }: CountDialogProps) {
  const create = useCreateCount();
  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<CountFormValues>({
    resolver: zodResolver(countSchema),
    defaultValues: { product_id: "", counted_value: 0 },
  });

  const submit = handleSubmit((values) =>
    create.mutate(
      { stockId, values },
      {
        onSuccess: () => onSaved("Contagem registrada"),
        onError: (e) =>
          setError("root.server", { message: errorMessage(e, "Erro ao registrar contagem") }),
      },
    ),
  );

  return (
    <FormDialogShell
      open={open}
      title="Nova contagem"
      description={`Armazém: ${stockName}`}
      submitLabel="Registrar"
      pending={create.isPending}
      serverError={errors.root?.server?.message}
      onSubmit={submit}
      onClose={onClose}
    >
      <FieldShell id="count-product" label="Produto" error={errors.product_id?.message}>
        <Controller
          control={control}
          name="product_id"
          render={({ field }) => (
            <ProductPicker
              id="count-product"
              value={field.value}
              onChange={field.onChange}
              invalid={!!errors.product_id}
            />
          )}
        />
      </FieldShell>
      <TextField
        id="count-value"
        label="Quantidade contada"
        type="number"
        step="1"
        min="0"
        error={errors.counted_value?.message}
        {...register("counted_value", { valueAsNumber: true })}
      />
    </FormDialogShell>
  );
}
