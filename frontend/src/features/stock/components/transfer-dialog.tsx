import { Icon } from "@/components/icon";
import { FieldShell, SelectField } from "@/components/resource/form-fields";
import { FormDialogShell } from "@/components/resource/form-dialog-shell";
import { ProductPicker } from "@/features/products";
import { useStockOptions } from "@/features/warehouses";
import { errorMessage } from "@/lib/query";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button, FieldError, Input } from "@openlab-ui/react";
import { Controller, useFieldArray, useForm } from "react-hook-form";
import { useCreateReassignment } from "../queries";
import { type TransferFormValues, transferSchema } from "../schemas";

interface TransferDialogProps {
  open: boolean;
  /** Armazém de origem sugerido (o selecionado na página). */
  fromStockId: string;
  onClose: () => void;
  onSaved: (message: string) => void;
}

/** Moves quantities of one or more products between two stocks (creates a reassignment). */
export function TransferDialog({ open, fromStockId, onClose, onSaved }: TransferDialogProps) {
  const create = useCreateReassignment();
  const stocks = useStockOptions();
  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<TransferFormValues>({
    resolver: zodResolver(transferSchema),
    defaultValues: {
      from_stock_id: fromStockId,
      to_stock_id: "",
      items: [{ product_id: "", quantity: 1 }],
    },
  });
  const { fields, append, remove } = useFieldArray({ control, name: "items" });
  const options = (stocks.data ?? []).map((s) => ({
    value: s.stock_id,
    label: s.description,
  }));

  const submit = handleSubmit((values) =>
    create.mutate(values, {
      onSuccess: () => onSaved("Transferência realizada"),
      onError: (e) =>
        setError("root.server", { message: errorMessage(e, "Erro ao transferir") }),
    }),
  );

  return (
    <FormDialogShell
      open={open}
      title="Transferir entre armazéns"
      description="Gera uma saída na origem e uma entrada no destino."
      submitLabel="Transferir"
      pending={create.isPending}
      serverError={errors.root?.server?.message}
      onSubmit={submit}
      onClose={onClose}
    >
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
        <SelectField
          id="transfer-from"
          label="Origem"
          placeholder="Selecione…"
          options={options}
          error={errors.from_stock_id?.message}
          {...register("from_stock_id")}
        />
        <SelectField
          id="transfer-to"
          label="Destino"
          placeholder="Selecione…"
          options={options}
          error={errors.to_stock_id?.message}
          {...register("to_stock_id")}
        />
      </div>

      <FieldShell id="transfer-items" label="Itens" error={errors.items?.root?.message ?? errors.items?.message}>
        <div style={{ display: "grid", gap: 8 }}>
          {fields.map((field, i) => (
            <div
              key={field.id}
              style={{ display: "grid", gridTemplateColumns: "1fr 90px auto", gap: 8, alignItems: "start" }}
            >
              <div>
                <Controller
                  control={control}
                  name={`items.${i}.product_id`}
                  render={({ field: f }) => (
                    <ProductPicker
                      value={f.value}
                      onChange={f.onChange}
                      invalid={!!errors.items?.[i]?.product_id}
                    />
                  )}
                />
                <FieldError errors={[errors.items?.[i]?.product_id]} />
              </div>
              <div>
                <Input
                  type="number"
                  min="1"
                  step="1"
                  aria-label="Quantidade"
                  aria-invalid={errors.items?.[i]?.quantity ? true : undefined}
                  {...register(`items.${i}.quantity`, { valueAsNumber: true })}
                />
                <FieldError errors={[errors.items?.[i]?.quantity]} />
              </div>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                aria-label="Remover item"
                disabled={fields.length === 1}
                onClick={() => remove(i)}
              >
                <Icon name="trash" size={13} />
              </Button>
            </div>
          ))}
          <div>
            <Button
              type="button"
              variant="ghost"
              size="xs"
              onClick={() => append({ product_id: "", quantity: 1 })}
            >
              <Icon name="plus" size={12} /> Adicionar item
            </Button>
          </div>
        </div>
      </FieldShell>
    </FormDialogShell>
  );
}
