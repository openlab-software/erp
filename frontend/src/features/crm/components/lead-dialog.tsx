import { FormDialogShell } from "@/components/resource/form-dialog-shell";
import { FInputStandalone, FSelect, FTextarea } from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { CustomerPicker } from "@/features/customers";
import { num } from "@/lib/money";
import { errorMessage } from "@/lib/query";
import { Field, FieldLabel } from "@openlab-ui/react";
import { useState } from "react";
import { useSaveLead } from "../queries";
import type { Lead, LeadFormValues, Stage } from "../types";
import { LeadActivities } from "./lead-activities";

interface LeadDialogProps {
  open: boolean;
  /** Set = editing, unset = creating. */
  lead?: Lead;
  stages: Stage[];
  /** Stage preselected when creating from a column. */
  defaultStageId?: string;
  onClose: () => void;
}

const defaults = (lead: Lead | undefined, stages: Stage[], stageId?: string): LeadFormValues => ({
  name: lead?.name ?? "",
  type: lead?.type ?? "OPPORTUNITY",
  stage_id: lead?.stage_id ?? stageId ?? stages.find((s) => !s.won)?.stage_id ?? "",
  customer_id: lead?.customer_id ?? "",
  customer_name: "",
  contact_name: lead?.contact_name ?? "",
  company_name: lead?.company_name ?? "",
  email: lead?.email ?? "",
  phone: lead?.phone ?? "",
  expected_revenue: String(lead?.expected_revenue ?? 0),
  probability: lead ? String(lead.probability) : "",
  priority: String(lead?.priority ?? 0),
  expected_closing: lead?.expected_closing ?? "",
  assigned_to: lead?.assigned_to ?? "",
  tags: lead?.tags.join(", ") ?? "",
  description: lead?.description ?? "",
});

const row = { display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 } as const;

export function LeadDialog({ open, lead, stages, defaultStageId, onClose }: LeadDialogProps) {
  const { showToast } = useToast();
  const save = useSaveLead();
  const [values, setValues] = useState<LeadFormValues>(() => defaults(lead, stages, defaultStageId));
  const [error, setError] = useState<string>();

  const set = <K extends keyof LeadFormValues>(key: K, value: LeadFormValues[K]) =>
    setValues((v) => ({ ...v, [key]: value }));

  const submit = (event: React.FormEvent) => {
    event.preventDefault();
    if (!values.name.trim()) return setError("Informe o nome da oportunidade");
    if (num(values.expected_revenue) < 0) return setError("A receita esperada não pode ser negativa");
    setError(undefined);
    save.mutate(
      { id: lead?.lead_id, values },
      {
        onSuccess: () => {
          showToast(lead ? "Oportunidade atualizada" : "Oportunidade criada");
          onClose();
        },
        onError: (e) => setError(errorMessage(e, "Erro ao salvar a oportunidade")),
      },
    );
  };

  return (
    <FormDialogShell
      open={open}
      title={lead ? "Editar oportunidade" : "Nova oportunidade"}
      submitLabel={lead ? "Salvar" : "Criar"}
      pending={save.isPending}
      serverError={error}
      onSubmit={submit}
      onClose={onClose}
    >
      <div style={{ display: "grid", gap: 14, maxHeight: "62vh", overflowY: "auto", paddingRight: 4 }}>
        <Field>
          <FieldLabel>Oportunidade</FieldLabel>
          <FInputStandalone
            autoFocus
            placeholder="Ex.: Renovação do contrato ACME"
            value={values.name}
            onChange={(e) => set("name", e.target.value)}
          />
        </Field>
        <div style={row}>
          <Field>
            <FieldLabel>Tipo</FieldLabel>
            <FSelect
              value={values.type}
              disabled={lead?.type === "OPPORTUNITY"}
              onChange={(e) => set("type", e.target.value as LeadFormValues["type"])}
            >
              <option value="OPPORTUNITY">Oportunidade</option>
              <option value="LEAD">Lead (ainda não qualificado)</option>
            </FSelect>
          </Field>
          {!lead && (
            <Field>
              <FieldLabel>Etapa</FieldLabel>
              <FSelect value={values.stage_id} onChange={(e) => set("stage_id", e.target.value)}>
                {stages
                  .filter((s) => !s.won)
                  .map((s) => (
                    <option key={s.stage_id} value={s.stage_id}>
                      {s.name}
                    </option>
                  ))}
              </FSelect>
            </Field>
          )}
        </div>
        <Field>
          <FieldLabel>Cliente (opcional)</FieldLabel>
          <CustomerPicker
            value={values.customer_id}
            label={values.customer_name || values.customer_id}
            onSelect={(c) => {
              set("customer_id", c.customer_id);
              set("customer_name", c.name);
              if (!values.company_name) set("company_name", c.name);
              if (!values.email && c.email) set("email", c.email);
              if (!values.phone && c.phone) set("phone", c.phone);
            }}
            onClear={() => {
              set("customer_id", "");
              set("customer_name", "");
            }}
          />
        </Field>
        <div style={row}>
          <Field>
            <FieldLabel>Contato</FieldLabel>
            <FInputStandalone value={values.contact_name} onChange={(e) => set("contact_name", e.target.value)} />
          </Field>
          <Field>
            <FieldLabel>Empresa</FieldLabel>
            <FInputStandalone value={values.company_name} onChange={(e) => set("company_name", e.target.value)} />
          </Field>
          <Field>
            <FieldLabel>E-mail</FieldLabel>
            <FInputStandalone type="email" value={values.email} onChange={(e) => set("email", e.target.value)} />
          </Field>
          <Field>
            <FieldLabel>Telefone</FieldLabel>
            <FInputStandalone value={values.phone} onChange={(e) => set("phone", e.target.value)} />
          </Field>
        </div>
        <div style={row}>
          <Field>
            <FieldLabel>Receita esperada (R$)</FieldLabel>
            <FInputStandalone
              inputMode="decimal"
              value={values.expected_revenue}
              onChange={(e) => set("expected_revenue", e.target.value)}
            />
          </Field>
          <Field>
            <FieldLabel>Probabilidade (%)</FieldLabel>
            <FInputStandalone
              inputMode="numeric"
              placeholder="Da etapa"
              value={values.probability}
              onChange={(e) => set("probability", e.target.value)}
            />
          </Field>
          <Field>
            <FieldLabel>Prioridade</FieldLabel>
            <FSelect value={values.priority} onChange={(e) => set("priority", e.target.value)}>
              <option value="0">Normal</option>
              <option value="1">Baixa</option>
              <option value="2">Alta</option>
              <option value="3">Muito alta</option>
            </FSelect>
          </Field>
          <Field>
            <FieldLabel>Fechamento previsto</FieldLabel>
            <FInputStandalone
              type="date"
              value={values.expected_closing}
              onChange={(e) => set("expected_closing", e.target.value)}
            />
          </Field>
          <Field>
            <FieldLabel>Responsável</FieldLabel>
            <FInputStandalone value={values.assigned_to} onChange={(e) => set("assigned_to", e.target.value)} />
          </Field>
          <Field>
            <FieldLabel>Etiquetas</FieldLabel>
            <FInputStandalone
              placeholder="vip, indicação"
              value={values.tags}
              onChange={(e) => set("tags", e.target.value)}
            />
          </Field>
        </div>
        <Field>
          <FieldLabel>Notas</FieldLabel>
          <FTextarea value={values.description} onChange={(e) => set("description", e.target.value)} />
        </Field>
        {lead && <LeadActivities leadId={lead.lead_id} />}
      </div>
    </FormDialogShell>
  );
}
