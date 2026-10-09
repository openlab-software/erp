import { Icon } from "@/components/icon";
import { RowActions } from "@/components/resource/row-actions";
import { SelectField, TextField } from "@/components/resource/form-fields";
import { Card, CardHead, Empty, T } from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { formatDateTime } from "@/lib/format";
import { errorMessage } from "@/lib/query";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button, FieldError } from "@openlab-ui/react";
import { useForm } from "react-hook-form";
import { BARCODE_TYPES } from "../labels";
import { useAddBarcode, useDeleteBarcode } from "../queries";
import { type BarcodeFormValues, barcodeSchema } from "../schemas";
import type { Product } from "../types";

export function BarcodesPanel({ product }: { product: Product }) {
  const { showToast } = useToast();
  const add = useAddBarcode();
  const remove = useDeleteBarcode();
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<BarcodeFormValues>({
    resolver: zodResolver(barcodeSchema),
    defaultValues: { code: "", type: "EAN13" },
  });

  const submit = handleSubmit((values) =>
    add.mutate(
      { id: product.product_id, values },
      {
        onSuccess: () => {
          reset();
          showToast("Código de barras adicionado");
        },
        onError: (e) =>
          setError("root.server", { message: errorMessage(e, "Erro ao adicionar código") }),
      },
    ),
  );

  return (
    <Card>
      <CardHead>
        <h3>Códigos de barras</h3>
      </CardHead>
      <form
        onSubmit={submit}
        style={{
          display: "grid",
          gridTemplateColumns: "1fr 160px auto",
          gap: 12,
          alignItems: "start",
          padding: 18,
          borderBottom: "1px solid var(--line)",
        }}
      >
        <TextField
          id="barcode-code"
          label="Código"
          error={errors.code?.message}
          {...register("code")}
        />
        <SelectField
          id="barcode-type"
          label="Tipo"
          options={BARCODE_TYPES.map((t) => ({ value: t, label: t }))}
          {...register("type")}
        />
        <Button type="submit" disabled={add.isPending} style={{ marginTop: 22 }}>
          <Icon name="plus" size={12} /> {add.isPending ? "Adicionando…" : "Adicionar"}
        </Button>
        {errors.root?.server && (
          <div style={{ gridColumn: "1 / -1" }}>
            <FieldError errors={[{ message: errors.root.server.message }]} />
          </div>
        )}
      </form>
      <T>
        <thead>
          <tr>
            <th>Código</th>
            <th>Tipo</th>
            <th>Adicionado em</th>
            <th style={{ width: 60 }} />
          </tr>
        </thead>
        <tbody>
          {product.barcodes.map((b) => (
            <tr key={b.barcode_id}>
              <td className="mono">{b.code}</td>
              <td>{b.type}</td>
              <td style={{ color: "var(--ink-3)" }}>{formatDateTime(b.created_at)}</td>
              <td>
                <RowActions
                  label={b.code}
                  onDelete={() =>
                    remove.mutate(
                      { id: product.product_id, barcodeId: b.barcode_id },
                      {
                        onSuccess: () => showToast("Código de barras removido"),
                        onError: (e) =>
                          showToast(errorMessage(e, "Erro ao remover código")),
                      },
                    )
                  }
                />
              </td>
            </tr>
          ))}
        </tbody>
      </T>
      {product.barcodes.length === 0 && (
        <Empty>Nenhum código de barras cadastrado.</Empty>
      )}
    </Card>
  );
}
