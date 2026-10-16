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
  Subtitle,
} from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { documentTotals, num } from "@/lib/money";
import { errorMessage } from "@/lib/query";
import { useNavigate } from "@modern-js/runtime/router";
import { Button, Field, FieldLabel } from "@openlab-ui/react";
import { useState } from "react";
import type { OrderHooks } from "../queries";
import type { Order, OrderDraft, OrderKind } from "../types";

interface OrderPageProps {
  kind: OrderKind;
  hooks: OrderHooks;
  /** Absent on the "new" page. */
  orderId?: string;
}

/** Loads the order (when editing) and mounts the editor once it is there. */
export function OrderPage({ kind, hooks, orderId }: OrderPageProps) {
  const navigate = useNavigate();
  const query = hooks.useOrder(orderId);

  const back = (
    <div style={{ marginBottom: 14 }}>
      <Button
        variant="ghost"
        onClick={() => navigate(kind.route)}
        style={{ marginLeft: -10, color: "var(--ink-3)" }}
      >
        ← Voltar para {kind.title}
      </Button>
    </div>
  );

  if (orderId && !query.data) {
    return (
      <Page>
        {back}
        <Empty>
          {query.isError ? errorMessage(query.error, "Erro ao carregar o documento") : "Carregando…"}
        </Empty>
      </Page>
    );
  }

  // The key remounts the editor with fresh form state after every save / action.
  return (
    <Page>
      {back}
      <OrderEditor
        key={`${query.data?.id}:${query.data?.state}:${query.dataUpdatedAt}`}
        kind={kind}
        hooks={hooks}
        order={query.data}
      />
    </Page>
  );
}

const toLineDrafts = (order?: Order): LineDraft[] =>
  order && order.lines.length > 0
    ? order.lines.map((l) => ({
        ...emptyLine(),
        product_id: l.product_id,
        product_type: l.product_type,
        description: l.description,
        quantity: String(l.quantity),
        unit_price: String(l.unit_price),
        discount_percent: String(l.discount_percent),
        tax_percent: String(l.tax_percent),
      }))
    : [emptyLine()];

interface OrderEditorProps {
  kind: OrderKind;
  hooks: OrderHooks;
  order?: Order;
}

function OrderEditor({ kind, hooks, order }: OrderEditorProps) {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const save = hooks.useSaveOrder();
  const action = hooks.useOrderAction();

  const [partner, setPartner] = useState({
    id: order?.partnerId ?? "",
    name: order?.partnerName ?? "",
  });
  const [date, setDate] = useState(order?.date ?? "");
  const [address, setAddress] = useState(order?.address ?? "");
  const [person, setPerson] = useState(order?.person ?? "");
  const [note, setNote] = useState(order?.note ?? "");
  const [lines, setLines] = useState<LineDraft[]>(() => toLineDrafts(order));
  const [errors, setErrors] = useState<{ partner?: string; lines: Record<number, string> }>({
    lines: {},
  });
  const [deleting, setDeleting] = useState<Order | null>(null);

  const editable = !order || order.state === "DRAFT" || order.state === "SENT";
  const busy = save.isPending || action.isPending;
  const totals = documentTotals(lines);

  /** Client-side checks that mirror the service rules, so the user sees them next to the field. */
  const validate = () => {
    const next: typeof errors = { lines: {} };
    if (!partner.id) next.partner = `Selecione o ${kind.partnerLabel.toLowerCase()}`;
    const filled = lines.filter((l) => l.product_id || l.description.trim());
    for (const l of filled) {
      if (!l.product_id) next.lines[l.key] = "Escolha um produto";
      else if (!l.description.trim()) next.lines[l.key] = "Informe a descrição";
      else if (num(l.quantity) <= 0) next.lines[l.key] = "Quantidade deve ser maior que zero";
    }
    setErrors(next);
    return !next.partner && Object.keys(next.lines).length === 0;
  };

  const draft = (): OrderDraft => ({
    partnerId: partner.id,
    partnerName: partner.name,
    date,
    address,
    person,
    note,
    lines: lines
      .filter((l) => l.product_id)
      .map((l) => ({
        product_id: l.product_id,
        product_type: l.product_type,
        sku: null,
        description: l.description.trim(),
        quantity: num(l.quantity),
        unit_price: num(l.unit_price),
        discount_percent: num(l.discount_percent),
        tax_percent: num(l.tax_percent),
      })),
  });

  const saveDraft = async (): Promise<Order | null> => {
    if (!validate()) return null;
    try {
      return await save.mutateAsync({ id: order?.id, draft: draft() });
    } catch (e) {
      showToast(errorMessage(e, "Erro ao salvar"));
      return null;
    }
  };

  const onSave = async () => {
    const saved = await saveDraft();
    if (!saved) return;
    showToast("Salvo");
    if (!order) navigate(`${kind.route}/${saved.id}`, { replace: true });
  };

  const run = async (name: string, message: string, saveFirst = false) => {
    let id = order?.id;
    if (saveFirst && editable) {
      const saved = await saveDraft();
      if (!saved) return;
      id = saved.id;
    }
    if (!id) return;
    try {
      await action.mutateAsync({ id, action: name });
      showToast(message);
      if (!order) navigate(`${kind.route}/${id}`, { replace: true });
    } catch (e) {
      showToast(errorMessage(e, "Não foi possível concluir a ação"));
    }
  };

  const state = order?.state ?? "DRAFT";

  return (
    <>
      <PageHead>
        <div>
          <SectionLabel style={{ fontFamily: "var(--font-mono)" }}>
            {order ? order.number : kind.eyebrow}
          </SectionLabel>
          <h1>{order ? order.partnerName : kind.newLabel}</h1>
          <Subtitle>
            <span style={{ display: "inline-flex", gap: 8, alignItems: "center" }}>
              <StatusPill value={state} map={kind.states} />
              {order && order.state !== "DRAFT" && order.state !== "SENT" && (
                <>
                  <StatusPill value={order.fulfilmentStatus} map={kind.fulfilment} />
                  <StatusPill value={order.billingStatus} map={kind.billing} />
                </>
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
          {order && state === "DRAFT" && (
            <Button variant="ghost" disabled={busy} onClick={() => run("send", "Marcado como enviado", true)}>
              Marcar como enviada
            </Button>
          )}
          {editable && (
            <Button disabled={busy} onClick={() => run("confirm", `${kind.noun} confirmado`, true)}>
              <Icon name="check" size={13} /> Confirmar
            </Button>
          )}
          {order && state === "CONFIRMED" && (
            <Button variant="ghost" disabled={busy} onClick={() => run("lock", "Documento bloqueado")}>
              Bloquear
            </Button>
          )}
          {order && state !== "CANCELLED" && state !== "DONE" && (
            <Button variant="ghost" disabled={busy} onClick={() => run("cancel", "Documento cancelado")}>
              Cancelar documento
            </Button>
          )}
          {order && state === "CANCELLED" && (
            <Button variant="ghost" disabled={busy} onClick={() => run("draft", "Voltou para rascunho")}>
              Voltar para rascunho
            </Button>
          )}
          {order && (state === "DRAFT" || state === "CANCELLED") && (
            <Button variant="ghost" disabled={busy} onClick={() => setDeleting(order)}>
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
              <FieldLabel>{kind.partnerLabel}</FieldLabel>
              {kind.renderPartner({
                value: partner.id,
                label: partner.name,
                disabled: !editable,
                invalid: Boolean(errors.partner),
                onChange: (id, name) => setPartner({ id, name }),
              })}
              {errors.partner && (
                <span style={{ fontSize: 11, color: "var(--neg)" }}>{errors.partner}</span>
              )}
            </Field>
            <Field>
              <FieldLabel>{kind.dateLabel}</FieldLabel>
              <FInputStandalone
                type="date"
                value={date}
                readOnly={!editable}
                onChange={(e) => setDate(e.target.value)}
              />
            </Field>
            <Field>
              <FieldLabel>{kind.addressLabel}</FieldLabel>
              <FInputStandalone
                value={address}
                readOnly={!editable}
                onChange={(e) => setAddress(e.target.value)}
              />
            </Field>
            <Field>
              <FieldLabel>{kind.personLabel}</FieldLabel>
              <FInputStandalone
                value={person}
                readOnly={!editable}
                onChange={(e) => setPerson(e.target.value)}
              />
            </Field>
          </div>
          <Field style={{ marginTop: 14 }}>
            <FieldLabel>Observações</FieldLabel>
            <FTextarea
              value={note}
              readOnly={!editable}
              onChange={(e) => setNote(e.target.value)}
            />
          </Field>
        </CardBody>
      </Card>

      <Card>
        <CardHead>
          <h3>Linhas ({lines.filter((l) => l.product_id).length})</h3>
        </CardHead>
        <LinesEditor
          lines={lines}
          onChange={setLines}
          readOnly={!editable}
          priceSource={kind.priceSource}
          errors={errors.lines}
        />
        <Totals
          untaxed={order && !editable ? order.untaxed : totals.untaxed}
          tax={order && !editable ? order.tax : totals.tax}
          total={order && !editable ? order.total : totals.total}
        />
      </Card>

      {order?.lines.some((l) => l.product_type !== "SERVICE" && l.fulfilled > 0) && (
        <Card style={{ marginTop: 20 }}>
          <CardHead>
            <h3>{kind.fulfilmentLabel} por linha</h3>
          </CardHead>
          <CardBody>
            {order.lines.filter((l) => l.product_type !== "SERVICE").map((l, i) => (
              <div
                // biome-ignore lint/suspicious/noArrayIndexKey: lines have no id and are never reordered here
                key={i}
                style={{ display: "flex", justifyContent: "space-between", padding: "4px 0", fontSize: 13 }}
              >
                <span>{l.description}</span>
                <span className="mono">
                  {l.fulfilled} / {l.quantity}
                </span>
              </div>
            ))}
          </CardBody>
        </Card>
      )}

      <ResourceDeleteDialog<Order>
        item={deleting}
        noun={kind.noun}
        getId={(o) => o.id}
        getLabel={(o) => o.number}
        useRemove={hooks.useDeleteOrder}
        onClose={() => setDeleting(null)}
        onDeleted={() => navigate(kind.route)}
      />
    </>
  );
}
