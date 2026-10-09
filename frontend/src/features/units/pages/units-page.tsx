import { TextField } from "@/components/resource/form-fields";
import { ResourcePage } from "@/components/resource/resource-page";
import { formatDateTime } from "@/lib/format";
import { units } from "../queries";
import { type UnitFormValues, unitSchema } from "../schemas";
import type { UnitOfMeasure } from "../types";

export function UnitsPage() {
  return (
    <ResourcePage<UnitOfMeasure, UnitFormValues>
      eyebrow="CADASTRO · UNIDADES DE MEDIDA"
      title="Unidades de medida"
      noun="unidade"
      feminine
      searchPlaceholder="Buscar por código ou descrição…"
      columns={[
        { header: "Código", className: "id", cell: (u) => u.unit_of_measure_id },
        { header: "Sigla", className: "mono", cell: (u) => <b>{u.code}</b> },
        { header: "Descrição", cell: (u) => u.description },
        { header: "Criada em", muted: true, cell: (u) => formatDateTime(u.created_at) },
      ]}
      getId={(u) => u.unit_of_measure_id}
      getLabel={(u) => u.code}
      useList={units.useList}
      useCreate={units.useCreate}
      useUpdate={units.useUpdate}
      useRemove={units.useRemove}
      schema={unitSchema}
      defaults={(u) => ({ code: u?.code ?? "", description: u?.description ?? "" })}
      renderFields={({ register, errors }) => (
        <>
          <TextField
            id="unit-code"
            label="Sigla"
            autoFocus
            error={errors.code?.message}
            {...register("code")}
          />
          <TextField
            id="unit-description"
            label="Descrição"
            error={errors.description?.message}
            {...register("description")}
          />
        </>
      )}
      deleteHint="Unidades em uso por produtos não podem ser excluídas."
    />
  );
}
