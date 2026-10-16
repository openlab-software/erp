import { FormDialogShell } from "@/components/resource/form-dialog-shell";
import { FInputStandalone, FSelect } from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { brl, num } from "@/lib/money";
import { errorMessage } from "@/lib/query";
import { Field, FieldLabel } from "@openlab-ui/react";
import { useState } from "react";
import { KIND_LABEL, METHOD_LABEL } from "../labels";
import { useRegisterPayment } from "../queries";
import type { Invoice, PaymentMethod } from "../types";

interface PaymentDialogProps {
  open: boolean;
  invoice: Invoice;
  onClose: () => void;
}

const today = () => new Date().toISOString().slice(0, 10);

/** Registers a payment received (invoice) or made (vendor bill) against what is still due. */
export function PaymentDialog({ open, invoice, onClose }: PaymentDialogProps) {
  const { showToast } = useToast();
  const register = useRegisterPayment();
  const labels = KIND_LABEL[invoice.kind];
  const [amount, setAmount] = useState(String(invoice.residual_amount));
  const [paidOn, setPaidOn] = useState(today());
  const [method, setMethod] = useState<PaymentMethod>("PIX");
  const [reference, setReference] = useState("");
  const [error, setError] = useState<string>();

  const submit = (event: React.FormEvent) => {
    event.preventDefault();
    const value = num(amount);
    if (value <= 0) return setError("Informe um valor maior que zero");
    if (value > invoice.residual_amount) {
      return setError(`O valor não pode passar do saldo em aberto (${brl(invoice.residual_amount)})`);
    }
    setError(undefined);
    register.mutate(
      {
        id: invoice.invoice_id,
        amount: value,
        paid_on: paidOn,
        method,
        reference: reference.trim() || null,
      },
      {
        onSuccess: () => {
          showToast(`${labels.paid}: ${brl(value)}`);
          onClose();
        },
        onError: (e) => setError(errorMessage(e, "Erro ao registrar o pagamento")),
      },
    );
  };

  return (
    <FormDialogShell
      open={open}
      title={labels.pay}
      description={`Saldo em aberto: ${brl(invoice.residual_amount)}`}
      submitLabel="Registrar"
      pending={register.isPending}
      serverError={error}
      onSubmit={submit}
      onClose={onClose}
    >
      <Field>
        <FieldLabel>Valor</FieldLabel>
        <FInputStandalone inputMode="decimal" value={amount} onChange={(e) => setAmount(e.target.value)} />
      </Field>
      <Field>
        <FieldLabel>Data</FieldLabel>
        <FInputStandalone type="date" value={paidOn} onChange={(e) => setPaidOn(e.target.value)} />
      </Field>
      <Field>
        <FieldLabel>Forma</FieldLabel>
        <FSelect value={method} onChange={(e) => setMethod(e.target.value as PaymentMethod)}>
          {Object.entries(METHOD_LABEL).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </FSelect>
      </Field>
      <Field>
        <FieldLabel>Referência</FieldLabel>
        <FInputStandalone
          placeholder="Comprovante, nº do boleto…"
          value={reference}
          onChange={(e) => setReference(e.target.value)}
        />
      </Field>
    </FormDialogShell>
  );
}
