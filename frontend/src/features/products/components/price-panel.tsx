import { Icon } from "@/components/icon";
import { TextField } from "@/components/resource/form-fields";
import { Card, CardHead, Empty, Kv, T } from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { fmtBRL } from "@/data";
import { formatDateTime } from "@/lib/format";
import { errorMessage } from "@/lib/query";
import { zodResolver } from "@hookform/resolvers/zod";
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
import { useState } from "react";
import { useForm } from "react-hook-form";
import { useChangeProductPrice, usePriceHistory } from "../queries";
import { type PriceFormValues, priceSchema } from "../schemas";
import type { Product } from "../types";

function PriceDialog({
  product,
  open,
  onClose,
}: {
  product: Product;
  open: boolean;
  onClose: () => void;
}) {
  const { showToast } = useToast();
  const change = useChangeProductPrice();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<PriceFormValues>({
    resolver: zodResolver(priceSchema),
    defaultValues: {
      sale_price: product.sale_price,
      cost_price: product.cost_price,
    },
  });

  const submit = handleSubmit((values) =>
    change.mutate(
      { id: product.product_id, values },
      {
        onSuccess: () => {
          showToast("Preços atualizados");
          onClose();
        },
        onError: (e) =>
          setError("root.server", { message: errorMessage(e, "Erro ao salvar preços") }),
      },
    ),
  );

  return (
    <Dialog open={open} onOpenChange={(next) => !next && !change.isPending && onClose()}>
      <DialogContent>
        <form onSubmit={submit}>
          <DialogHeader>
            <DialogTitle>Alterar preços</DialogTitle>
            <DialogDescription>
              A alteração fica registrada no histórico de preços.
            </DialogDescription>
          </DialogHeader>
          <div style={{ display: "grid", gap: 14, marginBottom: 16 }}>
            <TextField
              id="price-sale"
              label="Preço de venda (R$)"
              type="number"
              step="0.01"
              min="0"
              autoFocus
              error={errors.sale_price?.message}
              {...register("sale_price", { valueAsNumber: true })}
            />
            <TextField
              id="price-cost"
              label="Custo (R$)"
              type="number"
              step="0.01"
              min="0"
              error={errors.cost_price?.message}
              {...register("cost_price", { valueAsNumber: true })}
            />
            {errors.root?.server && (
              <FieldError errors={[{ message: errors.root.server.message }]} />
            )}
          </div>
          <DialogFooter>
            <Button type="button" variant="ghost" onClick={onClose} disabled={change.isPending}>
              Cancelar
            </Button>
            <Button type="submit" disabled={change.isPending}>
              {change.isPending ? "Salvando…" : "Salvar"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export function margin(product: Product) {
  return product.sale_price > 0
    ? `${(((product.sale_price - product.cost_price) / product.sale_price) * 100).toFixed(1)}%`
    : "—";
}

export function PricePanel({ product }: { product: Product }) {
  const history = usePriceHistory(product.product_id);
  const [dialog, setDialog] = useState({ open: false, key: 0 });
  const rows = history.data ?? [];

  return (
    <div style={{ display: "grid", gap: 20 }}>
      <Card>
        <CardHead>
          <h3>Preço atual</h3>
          <Button
            variant="ghost"
            size="xs"
            onClick={() => setDialog((d) => ({ open: true, key: d.key + 1 }))}
          >
            <Icon name="edit" size={12} /> Alterar preços
          </Button>
        </CardHead>
        <div style={{ padding: 18 }}>
          <Kv>
            <dt>Preço de venda</dt>
            <dd className="mono">{fmtBRL(product.sale_price)}</dd>
            <dt>Custo</dt>
            <dd className="mono">{fmtBRL(product.cost_price)}</dd>
            <dt>Margem</dt>
            <dd className="mono" style={{ color: "var(--pos)" }}>
              {margin(product)}
            </dd>
          </Kv>
        </div>
      </Card>

      <Card>
        <CardHead>
          <h3>Histórico de preços</h3>
        </CardHead>
        <T>
          <thead>
            <tr>
              <th>Alterado em</th>
              <th className="num">Preço de venda</th>
              <th className="num">Custo</th>
            </tr>
          </thead>
          <tbody>
            {[...rows]
              .sort((a, b) => b.changed_at.localeCompare(a.changed_at))
              .map((h) => (
                <tr key={h.price_history_id}>
                  <td className="mono">{formatDateTime(h.changed_at)}</td>
                  <td className="num">{fmtBRL(h.sale_price)}</td>
                  <td className="num">{fmtBRL(h.cost_price)}</td>
                </tr>
              ))}
          </tbody>
        </T>
        {history.isPending && <Empty>Carregando…</Empty>}
        {history.isError && (
          <Empty>{errorMessage(history.error, "Erro ao carregar o histórico")}</Empty>
        )}
        {!history.isPending && !history.isError && rows.length === 0 && (
          <Empty>Nenhuma alteração de preço registrada.</Empty>
        )}
      </Card>

      <PriceDialog
        key={dialog.key}
        product={product}
        open={dialog.open}
        onClose={() => setDialog((d) => ({ ...d, open: false }))}
      />
    </div>
  );
}
