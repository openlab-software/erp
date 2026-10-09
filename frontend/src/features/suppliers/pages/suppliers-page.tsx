import { TextField } from "@/components/resource/form-fields";
import { ResourcePage } from "@/components/resource/resource-page";
import { formatDateTime } from "@/lib/format";
import { suppliers } from "../queries";
import { type SupplierFormValues, supplierSchema } from "../schemas";
import type { Supplier } from "../types";

export function SuppliersPage() {
  return (
    <ResourcePage<Supplier, SupplierFormValues>
      eyebrow="CADASTRO · FORNECEDORES"
      title="Fornecedores"
      noun="fornecedor"
      searchPlaceholder="Buscar por nome ou documento…"
      columns={[
        { header: "Código", className: "id", cell: (s) => s.supplier_id },
        { header: "Nome", cell: (s) => <b style={{ fontWeight: 500 }}>{s.name}</b> },
        { header: "Documento", className: "mono", muted: true, cell: (s) => s.document },
        { header: "Criado em", muted: true, cell: (s) => formatDateTime(s.created_at) },
      ]}
      getId={(s) => s.supplier_id}
      getLabel={(s) => s.name}
      useList={suppliers.useList}
      useCreate={suppliers.useCreate}
      useUpdate={suppliers.useUpdate}
      useRemove={suppliers.useRemove}
      schema={supplierSchema}
      defaults={(s) => ({ name: s?.name ?? "", document: s?.document ?? "" })}
      renderFields={({ register, errors }) => (
        <>
          <TextField
            id="supplier-name"
            label="Nome"
            autoFocus
            error={errors.name?.message}
            {...register("name")}
          />
          <TextField
            id="supplier-document"
            label="Documento (CNPJ/CPF)"
            error={errors.document?.message}
            {...register("document")}
          />
        </>
      )}
      deleteHint="Fornecedores vinculados a produtos não podem ser excluídos."
    />
  );
}
