import { TextField } from "@/components/resource/form-fields";
import { ResourcePage } from "@/components/resource/resource-page";
import { formatDateTime } from "@/lib/format";
import { movementReasons } from "../queries";
import { type MovementReasonFormValues, movementReasonSchema } from "../schemas";
import type { MovementReason } from "../types";

export function MovementReasonsPage() {
  return (
    <ResourcePage<MovementReason, MovementReasonFormValues>
      eyebrow="CADASTRO · MOTIVOS DE MOVIMENTAÇÃO"
      title="Motivos de movimentação"
      noun="motivo"
      searchPlaceholder="Buscar por descrição…"
      columns={[
        { header: "Código", className: "id", cell: (r) => r.reason_id },
        { header: "Descrição", cell: (r) => <b style={{ fontWeight: 500 }}>{r.description}</b> },
        { header: "Criado em", muted: true, cell: (r) => formatDateTime(r.created_at) },
      ]}
      getId={(r) => r.reason_id}
      getLabel={(r) => r.description}
      useList={movementReasons.useList}
      useCreate={movementReasons.useCreate}
      useUpdate={movementReasons.useUpdate}
      useRemove={movementReasons.useRemove}
      schema={movementReasonSchema}
      defaults={(r) => ({ description: r?.description ?? "" })}
      renderFields={({ register, errors }) => (
        <TextField
          id="reason-description"
          label="Descrição"
          autoFocus
          error={errors.description?.message}
          {...register("description")}
        />
      )}
      deleteHint="Motivos já usados em movimentações não podem ser excluídos."
    />
  );
}
