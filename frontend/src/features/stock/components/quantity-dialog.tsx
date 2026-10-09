import { TextField } from "@/components/resource/form-fields";
import { FormDialogShell } from "@/components/resource/form-dialog-shell";
import { errorMessage } from "@/lib/query";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { useRelease, useReserve } from "../queries";
import { type QuantityFormValues, quantitySchema } from "../schemas";

export type QuantityAction = "reserve" | "release";

interface QuantityDialogProps {
  open: boolean;
  action: QuantityAction;
  stockId: string;
  productId: string;
  /** Product name shown in the description. */
  productName: string;
  onClose: () => void;
  onSaved: (message: string) => void;
}

/** Reserve (hold available units for an order) or release (give a reservation back). */
export function QuantityDialog({
  open,
  action,
  stockId,
  productId,
  productName,
  onClose,
  onSaved,
}: QuantityDialogProps) {
  const reserve = useReserve();
  const release = useRelease();
  const mutation = action === "reserve" ? reserve : release;
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<QuantityFormValues>({
    resolver: zodResolver(quantitySchema),
    defaultValues: { quantity: 1 },
  });

  const submit = handleSubmit(({ quantity }) =>
    mutation.mutate(
      { stockId, productId, quantity },
      {
        onSuccess: () =>
          onSaved(action === "reserve" ? "Quantidade reservada" : "Reserva liberada"),
        onError: (e) =>
          setError("root.server", { message: errorMessage(e, "Erro ao atualizar a reserva") }),
      },
    ),
  );

  return (
    <FormDialogShell
      open={open}
      title={action === "reserve" ? "Reservar estoque" : "Liberar reserva"}
      description={productName}
      submitLabel={action === "reserve" ? "Reservar" : "Liberar"}
      pending={mutation.isPending}
      serverError={errors.root?.server?.message}
      onSubmit={submit}
      onClose={onClose}
    >
      <TextField
        id="quantity-value"
        label="Quantidade"
        type="number"
        min="1"
        step="1"
        autoFocus
        error={errors.quantity?.message}
        {...register("quantity", { valueAsNumber: true })}
      />
    </FormDialogShell>
  );
}
