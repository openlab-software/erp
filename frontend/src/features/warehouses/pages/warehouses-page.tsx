import { TextField } from "@/components/resource/form-fields";
import { ResourcePage } from "@/components/resource/resource-page";
import { formatDateTime } from "@/lib/format";
import { warehouses } from "../queries";
import { type WarehouseFormValues, warehouseSchema } from "../schemas";
import type { Stock } from "../types";

export function WarehousesPage() {
  return (
    <ResourcePage<Stock, WarehouseFormValues>
      eyebrow="CADASTRO · ARMAZÉNS"
      title="Armazéns"
      noun="armazém"
      searchPlaceholder="Buscar por descrição…"
      columns={[
        { header: "Código", className: "id", cell: (s) => s.stock_id },
        { header: "Descrição", cell: (s) => <b style={{ fontWeight: 500 }}>{s.description}</b> },
        { header: "Criado em", muted: true, cell: (s) => formatDateTime(s.created_at) },
        { header: "Atualizado em", muted: true, cell: (s) => formatDateTime(s.modified_at) },
      ]}
      getId={(s) => s.stock_id}
      getLabel={(s) => s.description}
      useList={warehouses.useList}
      useCreate={warehouses.useCreate}
      useUpdate={warehouses.useUpdate}
      useRemove={warehouses.useRemove}
      schema={warehouseSchema}
      defaults={(s) => ({ description: s?.description ?? "" })}
      renderFields={({ register, errors }) => (
        <TextField
          id="warehouse-description"
          label="Descrição"
          autoFocus
          error={errors.description?.message}
          {...register("description")}
        />
      )}
      deleteHint="Armazéns com itens ou movimentações não podem ser excluídos."
    />
  );
}
