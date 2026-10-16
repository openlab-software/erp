import { LinesEditor, emptyLine } from "@/components/document/lines-editor";
import type { LineDraft } from "@/components/document/lines-editor";
import { StatusPill } from "@/components/document/status-pill";
import { Totals } from "@/components/document/totals";
import { Icon } from "@/components/icon";
import { ResourceDeleteDialog } from "@/components/resource/resource-delete-dialog";
import {
  Card,
  CardBody,
  CardHead,
  Empty,
  FInputStandalone,
  FTextarea,
  Page,
  PageActions,
  PageHead,
  SectionLabel,
  Status,
  Subtitle,
  T,
} from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { CustomerPicker } from "@/features/customers";
import { SupplierSelect } from "@/features/suppliers";
import { formatDateTime } from "@/lib/format";
import { brl, documentTotals, formatDate, num } from "@/lib/money";
import { errorMessage } from "@/lib/query";
import { useNavigate, useSearchParams } from "@modern-js/runtime/router";
import { Button, Field, FieldLabel } from "@openlab-ui/react";
import { useState } from "react";
import { KIND_LABEL, METHOD_LABEL, PAYMENT_LABEL, STATE_LABEL } from "../labels";
import { PaymentDialog } from "../components/payment-dialog";
import {
  useDeleteInvoice,
  useInvoice,
  useInvoiceAction,
  useSaveInvoice,
} from "../queries";
import type { Invoice, InvoiceKind } from "../types";

interface InvoicePageProps {
  /** Absent on the "new" page. */
  invoiceId?: string;
}

export function InvoicePage({ invoiceId }: InvoicePageProps) {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const query = useInvoice(invoiceId);
  const newKind: InvoiceKind = params.get("kind") === "VENDOR_BILL" ? "VENDOR_BILL" : "CUSTOMER_INVOICE";

  const back = (
    <div style={{ marginBottom: 14 }}>
      <Button
        variant="ghost"
        onClick={() => navigate("/financeiro")}
        style={{ marginLeft: -10, color: "var(--ink-3)" }}
      >
        ← Voltar para o Financeiro
      </Button>
    </div>
  );

  if (invoiceId && !query.data) {
    return (
      <Page>
        {back}
        <Empty>
          {query.isError ? errorMessage(query.error, "Erro ao carregar a fatura") : "Carregando…"}
        </Empty>
      </Page>
    );
  }

  // The key remounts the editor with fresh form state after every save / action.
  return (
    <Page>
      {back}
      <InvoiceEditor
        key={`${query.data?.invoice_id}:${query.data?.state}:${query.dataUpdatedAt}`}
        invoice={query.data}
        newKind={newKind}
      />
    </Page>
  );
}

const toLineDrafts = (invoice?: Invoice): LineDraft[] =>
  invoice && invoice.lines.length > 0
    ? invoice.lines.map((l) => ({
        ...emptyLine(),
        product_id: l.product_id ?? "",
        description: l.description,
        quantity: String(l.quantity),
        unit_price: String(l.unit_price),
        discount_percent: String(l.discount_percent),
        tax_percent: String(l.tax_percent),
      }))
    : [emptyLine()];

function InvoiceEditor({ invoice, newKind }: { invoice?: Invoice; newKind: InvoiceKind }) {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const save = useSaveInvoice();
  const action = useInvoiceAction();

  const kind = invoice?.kind ?? newKind;
  const labels = KIND_LABEL[kind];
  const editable = !invoice || invoice.state === "DRAFT";
  const state = invoice?.state ?? "DRAFT";

  const [partner, setPartner] = useState({ id: invoice?.partner_id ?? "", name: invoice?.partner_name ?? "" });
  const [invoiceDate, setInvoiceDate] = useState(invoice?.invoice_date ?? "");
  const [dueDate, setDueDate] = useState(invoice?.due_date ?? "");
  const [reference, setReference] = useState(invoice?.reference ?? "");
  const [note, setNote] = useState(invoice?.note ?? "");
  const [lines, setLines] = useState<LineDraft[]>(() => toLineDrafts(invoice));
  const [errors, setErrors] = useState<{ partner?: string; lines: Record<number, string> }>({ lines: {} });
  const [paying, setPaying] = useState({ open: false, key: 0 });
  const [deleting, setDeleting] = useState<Invoice | null>(null);

  const busy = save.isPending || action.isPending;
  const totals = documentTotals(lines);

  const validate = () => {
    const next: typeof errors = { lines: {} };
    if (!partner.id) next.partner = `Selecione o ${labels.partner.toLowerCase()}`;
    for (const l of lines.filter((x) => x.product_id || x.description.trim())) {
      if (!l.description.trim()) next.lines[l.key] = "Informe a descrição";
      else if (num(l.quantity) <= 0) next.lines[l.key] = "Quantidade deve ser maior que zero";
    }
    setErrors(next);
    return !next.partner && Object.keys(next.lines).length === 0;
  };

  const saveDraft = async (): Promise<Invoice | null> => {
    if (!validate()) return null;
    try {
      return await save.mutateAsync({
        id: invoice?.invoice_id,
        draft: {
          kind,
          partner_id: partner.id,
          partner_name: partner.name,
          invoice_date: invoiceDate,
          due_date: dueDate,
          reference,
          note,
          lines: lines
            .filter((l) => l.product_id || l.description.trim())
            .map((l) => ({
              product_id: l.product_id || null,
              description: l.description.trim(),
              quantity: num(l.quantity),
              unit_price: num(l.unit_price),
              discount_percent: num(l.discount_percent),
              tax_percent: num(l.tax_percent),
            })),
        },
      });
    } catch (e) {
      showToast(errorMessage(e, "Erro ao salvar"));
      return null;
    }
  };

  const onSave = async () => {
    const saved = await saveDraft();
    if (!saved) return;
    showToast("Salvo");
    if (!invoice) navigate(`/financeiro/${saved.invoice_id}`, { replace: true });
  };

  const run = async (name: string, message: string, saveFirst = false) => {
    let id = invoice?.invoice_id;
    if (saveFirst && editable) {
      const saved = await saveDraft();
      if (!saved) return;
      id = saved.invoice_id;
    }
    if (!id) return;
    try {
      await action.mutateAsync({ id, action: name });
      showToast(message);
      if (!invoice) navigate(`/financeiro/${id}`, { replace: true });
    } catch (e) {
      showToast(errorMessage(e, "Não foi possível concluir a ação"));
    }
  };

  return (
    <>
      <PageHead>
        <div>
          <SectionLabel style={{ fontFamily: "var(--font-mono)" }}>
            {invoice ? (invoice.number ?? "RASCUNHO") : labels.newLabel.toUpperCase()}
          </SectionLabel>
          <h1>{invoice ? invoice.partner_name : labels.newLabel}</h1>
          <Subtitle>
            <span style={{ display: "inline-flex", gap: 8, alignItems: "center" }}>
              {labels.one} · <StatusPill value={state} map={STATE_LABEL} />
              {invoice?.state === "POSTED" && <StatusPill value={invoice.payment_state} map={PAYMENT_LABEL} />}
              {invoice?.overdue && <Status variant="neg">Vencida</Status>}
              {invoice?.origin_number && (
                <span className="mono" style={{ color: "var(--ink-3)" }}>
                  origem {invoice.origin_number}
                </span>
              )}
            </span>
          </Subtitle>
        </div>
        <PageActions>
          {editable && (
            <Button variant="ghost" disabled={busy} onClick={onSave}>
              {save.isPending ? "Salvando…" : "Salvar"}
            </Button>
          )}
          {editable && (
            <Button disabled={busy} onClick={() => run("post", "Fatura lançada", true)}>
              <Icon name="check" size={13} /> Lançar
            </Button>
          )}
          {invoice && invoice.state === "POSTED" && invoice.payment_state !== "PAID" && (
            <Button disabled={busy} onClick={() => setPaying((p) => ({ open: true, key: p.key + 1 }))}>
              <Icon name="wallet" size={13} /> {labels.pay}
            </Button>
          )}
          {invoice && invoice.state !== "CANCELLED" && invoice.payments.length === 0 && (
            <Button variant="ghost" disabled={busy} onClick={() => run("cancel", "Fatura cancelada")}>
              Cancelar fatura
            </Button>
          )}
          {invoice && invoice.state === "CANCELLED" && (
            <Button variant="ghost" disabled={busy} onClick={() => run("draft", "Voltou para rascunho")}>
              Voltar para rascunho
            </Button>
          )}
          {invoice && invoice.state === "DRAFT" && !invoice.number && (
            <Button variant="ghost" disabled={busy} onClick={() => setDeleting(invoice)}>
              <Icon name="trash" size={13} /> Excluir
            </Button>
          )}
        </PageActions>
      </PageHead>

      <Card style={{ marginBottom: 20 }}>
        <CardHead>
          <h3>Dados</h3>
        </CardHead>
        <CardBody>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: 14 }}>
            <Field>
              <FieldLabel>{labels.partner}</FieldLabel>
              {kind === "CUSTOMER_INVOICE" ? (
                <CustomerPicker
                  value={partner.id}
                  label={partner.name}
                  disabled={!editable}
                  invalid={Boolean(errors.partner)}
                  onSelect={(c) => setPartner({ id: c.customer_id, name: c.name })}
                  onClear={() => setPartner({ id: "", name: "" })}
                />
              ) : (
                <SupplierSelect
                  value={partner.id}
                  label={partner.name}
                  disabled={!editable}
                  invalid={Boolean(errors.partner)}
                  onChange={(id, name) => setPartner({ id, name })}
                />
              )}
              {errors.partner && <span style={{ fontSize: 11, color: "var(--neg)" }}>{errors.partner}</span>}
            </Field>
            <Field>
              <FieldLabel>{kind === "VENDOR_BILL" ? "Nº da fatura do fornecedor" : "Referência de pagamento"}</FieldLabel>
              <FInputStandalone
                value={reference}
                readOnly={!editable}
                onChange={(e) => setReference(e.target.value)}
              />
            </Field>
            <Field>
              <FieldLabel>Data da fatura</FieldLabel>
              <FInputStandalone
                type="date"
                value={invoiceDate}
                readOnly={!editable}
                onChange={(e) => setInvoiceDate(e.target.value)}
              />
            </Field>
            <Field>
              <FieldLabel>Vencimento</FieldLabel>
              <FInputStandalone
                type="date"
                value={dueDate}
                readOnly={!editable}
                onChange={(e) => setDueDate(e.target.value)}
              />
              {editable && (
                <span style={{ fontSize: 11, color: "var(--ink-4)" }}>
                  Em branco: 30 dias após a data da fatura.
                </span>
              )}
            </Field>
          </div>
          <Field style={{ marginTop: 14 }}>
            <FieldLabel>Observações</FieldLabel>
            <FTextarea value={note} readOnly={!editable} onChange={(e) => setNote(e.target.value)} />
          </Field>
        </CardBody>
      </Card>

      <Card>
        <CardHead>
          <h3>Linhas ({lines.filter((l) => l.description.trim()).length})</h3>
        </CardHead>
        <LinesEditor
          lines={lines}
          onChange={setLines}
          readOnly={!editable}
          priceSource={kind === "CUSTOMER_INVOICE" ? "sale_price" : "cost_price"}
          productOptional
          errors={errors.lines}
        />
        <Totals
          untaxed={invoice && !editable ? invoice.untaxed_amount : totals.untaxed}
          tax={invoice && !editable ? invoice.tax_amount : totals.tax}
          total={invoice && !editable ? invoice.total_amount : totals.total}
          extra={
            invoice && invoice.state === "POSTED"
              ? [
                  { label: labels.paid, value: invoice.paid_amount },
                  { label: "Em aberto", value: invoice.residual_amount, strong: true },
                ]
              : []
          }
        />
      </Card>

      {invoice && invoice.payments.length > 0 && (
        <Card style={{ marginTop: 20 }}>
          <CardHead>
            <h3>Pagamentos ({invoice.payments.length})</h3>
          </CardHead>
          <T>
            <thead>
              <tr>
                <th>Data</th>
                <th>Forma</th>
                <th>Referência</th>
                <th className="num">Valor</th>
                <th>Registrado em</th>
              </tr>
            </thead>
            <tbody>
              {invoice.payments.map((p) => (
                <tr key={p.payment_id}>
                  <td>{formatDate(p.paid_on)}</td>
                  <td>{METHOD_LABEL[p.method]}</td>
                  <td style={{ color: "var(--ink-3)" }}>{p.reference ?? "—"}</td>
                  <td className="num">{brl(p.amount)}</td>
                  <td style={{ color: "var(--ink-3)" }}>{formatDateTime(p.created_at)}</td>
                </tr>
              ))}
            </tbody>
          </T>
        </Card>
      )}

      {invoice && (
        <PaymentDialog
          key={paying.key}
          open={paying.open}
          invoice={invoice}
          onClose={() => setPaying((p) => ({ ...p, open: false }))}
        />
      )}

      <ResourceDeleteDialog<Invoice>
        item={deleting}
        noun="fatura"
        feminine
        getId={(i) => i.invoice_id}
        getLabel={(i) => i.partner_name}
        useRemove={useDeleteInvoice}
        onClose={() => setDeleting(null)}
        onDeleted={() => navigate("/financeiro")}
      />
    </>
  );
}
