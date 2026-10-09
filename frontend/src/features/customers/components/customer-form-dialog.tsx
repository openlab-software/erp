import { SelectField, TextField } from "@/components/resource/form-fields";
import {
  type FormFieldsProps,
  ResourceFormDialog,
} from "@/components/resource/resource-form-dialog";
import { formatDocument } from "@/lib/format";
import { useWatch } from "react-hook-form";
import { DOCUMENT_LABEL, TYPE_LABEL } from "../labels";
import { useCreateCustomer, useUpdateCustomer } from "../queries";
import { type CustomerFormValues, customerSchema } from "../schemas";
import type { Customer } from "../types";

const TWO_COLS = { display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 } as const;

function CustomerFields({
  register,
  errors,
  control,
  editing,
}: FormFieldsProps<CustomerFormValues> & { editing: boolean }) {
  const type = useWatch({ control, name: "type" });
  const company = type === "COMPANY";

  return (
    // The form is tall: scroll inside the dialog instead of growing past the viewport.
    <div style={{ display: "grid", gap: 14, maxHeight: "60vh", overflowY: "auto", paddingRight: 4 }}>
      {/* The type decides which document and extra fields apply, and cannot change after creation. */}
      {!editing && (
        <SelectField
          id="customer-type"
          label="Tipo de cliente"
          options={Object.entries(TYPE_LABEL).map(([value, label]) => ({ value, label }))}
          {...register("type")}
        />
      )}

      <TextField
        id="customer-name"
        label={company ? "Razão social" : "Nome completo"}
        autoFocus
        error={errors.name?.message}
        {...register("name")}
      />
      {company && (
        <TextField
          id="customer-trade-name"
          label="Nome fantasia"
          error={errors.trade_name?.message}
          {...register("trade_name")}
        />
      )}

      <div style={TWO_COLS}>
        <TextField
          id="customer-document"
          label={DOCUMENT_LABEL[type]}
          placeholder={company ? "00.000.000/0000-00" : "000.000.000-00"}
          error={errors.document?.message}
          {...register("document")}
        />
        {company ? (
          <TextField
            id="customer-state-registration"
            label="Inscrição estadual"
            hint="Opcional (ou ISENTO)"
            error={errors.state_registration?.message}
            {...register("state_registration")}
          />
        ) : (
          <TextField
            id="customer-birth-date"
            label="Data de nascimento"
            type="date"
            error={errors.birth_date?.message}
            {...register("birth_date")}
          />
        )}
      </div>

      <div style={TWO_COLS}>
        <TextField
          id="customer-email"
          label="E-mail"
          type="email"
          error={errors.email?.message}
          {...register("email")}
        />
        <TextField
          id="customer-phone"
          label="Telefone"
          error={errors.phone?.message}
          {...register("phone")}
        />
      </div>

    </div>
  );
}

const defaults = (c?: Customer): CustomerFormValues => ({
  type: c?.type ?? "INDIVIDUAL",
  name: c?.name ?? "",
  trade_name: c?.trade_name ?? "",
  document: c ? formatDocument(c.document) : "",
  state_registration: c?.state_registration ?? "",
  birth_date: c?.birth_date ?? "",
  email: c?.email ?? "",
  phone: c?.phone ?? "",
});

interface CustomerFormDialogProps {
  open: boolean;
  /** Customer being edited; omit to create. Remount with a new `key` to reset. */
  customer?: Customer;
  onClose: () => void;
  onSaved: (message: string) => void;
}

export function CustomerFormDialog({
  open,
  customer,
  onClose,
  onSaved,
}: CustomerFormDialogProps) {
  return (
    <ResourceFormDialog<Customer, CustomerFormValues>
      open={open}
      item={customer}
      noun="cliente"
      schema={customerSchema}
      defaults={defaults}
      getId={(c) => c.customer_id}
      useCreate={useCreateCustomer}
      useUpdate={useUpdateCustomer}
      renderFields={(props) => <CustomerFields {...props} editing={!!customer} />}
      onClose={onClose}
      onSaved={onSaved}
    />
  );
}
