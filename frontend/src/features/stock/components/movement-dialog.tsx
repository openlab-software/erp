import { FieldShell, SelectField, TextField } from "@/components/resource/form-fields";
import { FormDialogShell } from "@/components/resource/form-dialog-shell";
import { useMovementReasonOptions } from "@/features/movement-reasons";
import { ProductPicker } from "@/features/products";
import { errorMessage } from "@/lib/query";
import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { useCreateMovement } from "../queries";
import { type MovementFormValues, movementSchema } from "../schemas";

interface MovementDialogProps {
  open: boolean;
  stockId: string;
  stockName: string;
  /** Pre-selected product (e.g. from a table row). Remount with a new `key` to reset. */
  productId?: string;
  onClose: () => void;
  onSaved: (message: string) => void;
}

export function MovementDialog({
  open,
  stockId,
  stockName,
  productId = "",
  onClose,
  onSaved,
}: MovementDialogProps) {
  const create = useCreateMovement();
  const reasons = useMovementReasonOptions();
  const {
    register,
    control,
    handleSubmit,
    setError,
    watch,
    formState: { errors },
  } = useForm<MovementFormValues>({
    resolver: zodResolver(movementSchema),
    defaultValues: {
      product_id: productId,
      type: "ENTRY",
      quantity: 1,
      reason_id: "",
      reference: "",
    },
  });
  const type = watch("type");

  const submit = handleSubmit((values) =>
    create.mutate(
      { stockId, values },
      {
        onSuccess: () => onSaved("Movimentação registrada"),
        onError: (e) =>
          setError("root.server", { message: errorMessage(e, "Erro ao registrar movimentação") }),
      },
    ),
  );

  return (
    <FormDialogShell
      open={open}
      title="Nova movimentação"
      description={`Armazém: ${stockName}`}
      submitLabel="Registrar"
      pending={create.isPending}
      serverError={errors.root?.server?.message}
      onSubmit={submit}
      onClose={onClose}
    >
      <FieldShell id="movement-product" label="Produto" error={errors.product_id?.message}>
        <Controller
          control={control}
          name="product_id"
          render={({ field }) => (
            <ProductPicker
              id="movement-product"
              value={field.value}
              onChange={field.onChange}
              invalid={!!errors.product_id}
            />
          )}
        />
      </FieldShell>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
        <SelectField
          id="movement-type"
          label="Tipo"
          options={[
            { value: "ENTRY", label: "Entrada" },
            { value: "EXIT", label: "Saída" },
            { value: "ADJUSTMENT", label: "Ajuste" },
          ]}
          {...register("type")}
        />
        <TextField
          id="movement-quantity"
          label="Quantidade"
          type="number"
          step="1"
          hint={type === "ADJUSTMENT" ? "Use negativo para reduzir o saldo." : undefined}
          error={errors.quantity?.message}
          {...register("quantity", { valueAsNumber: true })}
        />
      </div>
      <SelectField
        id="movement-reason"
        label={type === "ADJUSTMENT" ? "Motivo (obrigatório)" : "Motivo"}
        placeholder="— Sem motivo —"
        options={(reasons.data ?? []).map((r) => ({
          value: r.reason_id,
          label: r.description,
        }))}
        error={errors.reason_id?.message}
        {...register("reason_id")}
      />
      <TextField
        id="movement-reference"
        label="Documento / referência"
        placeholder="NF-1842, OP-0915…"
        error={errors.reference?.message}
        {...register("reference")}
      />
    </FormDialogShell>
  );
}
