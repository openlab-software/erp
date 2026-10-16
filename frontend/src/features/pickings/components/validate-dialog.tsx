import { FormDialogShell } from "@/components/resource/form-dialog-shell";
import { T } from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { useProductsById } from "@/features/products";
import { errorMessage } from "@/lib/query";
import { Input } from "@openlab-ui/react";
import { useState } from "react";
import { useValidatePicking } from "../queries";
import type { Picking } from "../types";

interface ValidateDialogProps {
  open: boolean;
  picking: Picking;
  onClose: () => void;
  onValidated: (backorder: Picking | null) => void;
}

/** "Validate": confirm what really moved. Less than the demand leaves a backorder (or nothing, if unchecked). */
export function ValidateDialog({ open, picking, onClose, onValidated }: ValidateDialogProps) {
  const { showToast } = useToast();
  const validate = useValidatePicking();
  const products = useProductsById(picking.lines.map((l) => l.product_id));
  const [done, setDone] = useState<Record<string, string>>(() =>
    Object.fromEntries(picking.lines.map((l) => [l.product_id, String(l.demand)])),
  );
  const [backorder, setBackorder] = useState(true);
  const [error, setError] = useState<string>();

  const quantity = (productId: string) => Number.parseInt(done[productId] ?? "0", 10);
  const partial = picking.lines.some((l) => quantity(l.product_id) < l.demand);

  const submit = (event: React.FormEvent) => {
    event.preventDefault();
    const lines = picking.lines.map((l) => ({ product_id: l.product_id, quantity: quantity(l.product_id) }));
    const invalid = picking.lines.find((l) => {
      const q = quantity(l.product_id);
      return Number.isNaN(q) || q < 0 || q > l.demand;
    });
    if (invalid) {
      return setError("A quantidade feita deve ser um inteiro entre 0 e a demanda de cada linha");
    }
    if (lines.every((l) => l.quantity === 0)) {
      return setError("Nada foi movimentado; cancele a transferência se ela não vai acontecer");
    }
    setError(undefined);
    validate.mutate(
      { id: picking.picking_id, done: lines, createBackorder: backorder },
      {
        onSuccess: (result) => {
          showToast(
            result.backorder
              ? `Transferência validada. Pendência criada: ${result.backorder.number}`
              : "Transferência validada",
          );
          onValidated(result.backorder);
        },
        onError: (e) => setError(errorMessage(e, "Erro ao validar a transferência")),
      },
    );
  };

  return (
    <FormDialogShell
      open={open}
      title={`Validar ${picking.number}`}
      description="Informe a quantidade realmente movimentada em cada linha."
      submitLabel="Validar"
      pending={validate.isPending}
      serverError={error}
      onSubmit={submit}
      onClose={onClose}
    >
      <T>
        <thead>
          <tr>
            <th>Produto</th>
            <th className="num">Demanda</th>
            <th className="num">Feito</th>
          </tr>
        </thead>
        <tbody>
          {picking.lines.map((l) => (
            <tr key={l.product_id}>
              <td>{products.get(l.product_id)?.description ?? l.description ?? l.product_id}</td>
              <td className="num">{l.demand}</td>
              <td className="num">
                <Input
                  style={{ width: 80, textAlign: "right" }}
                  inputMode="numeric"
                  value={done[l.product_id] ?? ""}
                  onChange={(e) => setDone((d) => ({ ...d, [l.product_id]: e.target.value }))}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </T>
      {partial && (
        <label style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 13 }}>
          <input type="checkbox" checked={backorder} onChange={(e) => setBackorder(e.target.checked)} />
          Criar pendência com o que faltou
        </label>
      )}
    </FormDialogShell>
  );
}
