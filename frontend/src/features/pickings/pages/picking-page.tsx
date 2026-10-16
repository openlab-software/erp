import { TypeaheadPicker } from "@/components/document/typeahead-picker";
import { StatusPill } from "@/components/document/status-pill";
import { Icon } from "@/components/icon";
import { ResourceDeleteDialog } from "@/components/resource/resource-delete-dialog";
import {
  Card,
  CardBody,
  CardHead,
  Empty,
  FInputStandalone,
  FSelect,
  FTextarea,
  Page,
  PageActions,
  PageHead,
  SectionLabel,
  Subtitle,
  T,
} from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { useProductSuggestions } from "@/features/products";
import { useStockOptions } from "@/features/warehouses";
import { brl } from "@/lib/money";
import { errorMessage } from "@/lib/query";
import { useNavigate, useSearchParams } from "@modern-js/runtime/router";
import { Button, Field, FieldLabel, Input } from "@openlab-ui/react";
import { useState } from "react";
import { ValidateDialog } from "../components/validate-dialog";
import { ORIGIN_LABEL, STATE_LABEL, TYPE_LABEL } from "../labels";
import {
  useDeletePicking,
  usePicking,
  usePickingAction,
  useSavePicking,
} from "../queries";
import type { Picking, PickingType } from "../types";

interface PickingPageProps {
  /** Absent on the "new" page. */
  pickingId?: string;
}

export function PickingPage({ pickingId }: PickingPageProps) {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const query = usePicking(pickingId);
  const rawType = params.get("type");
  const newType: PickingType = rawType === "RECEIPT" || rawType === "INTERNAL" ? rawType : "DELIVERY";

  const back = (
    <div style={{ marginBottom: 14 }}>
      <Button
        variant="ghost"
        onClick={() => navigate("/transferencias")}
        style={{ marginLeft: -10, color: "var(--ink-3)" }}
      >
        ← Voltar para Transferências
      </Button>
    </div>
  );

  if (pickingId && !query.data) {
    return (
      <Page>
        {back}
        <Empty>
          {query.isError ? errorMessage(query.error, "Erro ao carregar a transferência") : "Carregando…"}
        </Empty>
      </Page>
    );
  }

  return (
    <Page>
      {back}
      <PickingEditor
        key={`${query.data?.picking_id}:${query.data?.state}:${query.dataUpdatedAt}`}
        picking={query.data}
        newType={newType}
      />
    </Page>
  );
}

interface LineDraft {
  key: number;
  product_id: string;
  description: string;
  demand: string;
}

let nextKey = 1;
const emptyLine = (): LineDraft => ({ key: nextKey++, product_id: "", description: "", demand: "1" });

function PickingEditor({ picking, newType }: { picking?: Picking; newType: PickingType }) {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const stocks = useStockOptions();
  const save = useSavePicking();
  const action = usePickingAction();

  const type = picking?.type ?? newType;
  const labels = TYPE_LABEL[type];
  const state = picking?.state ?? "DRAFT";
  const editable = !picking || state === "DRAFT" || state === "CONFIRMED";
  const needsSource = type !== "RECEIPT";
  const needsDestination = type !== "DELIVERY";

  const [source, setSource] = useState(picking?.source_stock_id ?? "");
  const [destination, setDestination] = useState(picking?.destination_stock_id ?? "");
  const [partnerName, setPartnerName] = useState(picking?.partner_name ?? "");
  const [scheduled, setScheduled] = useState(picking?.scheduled_date ?? "");
  const [note, setNote] = useState(picking?.note ?? "");
  const [lines, setLines] = useState<LineDraft[]>(() =>
    picking && picking.lines.length > 0
      ? picking.lines.map((l) => ({
          key: nextKey++,
          product_id: l.product_id,
          description: l.description ?? "",
          demand: String(l.demand),
        }))
      : [emptyLine()],
  );
  const [error, setError] = useState<string>();
  const [validating, setValidating] = useState({ open: false, key: 0 });
  const [deleting, setDeleting] = useState<Picking | null>(null);

  const busy = save.isPending || action.isPending;
  const update = (key: number, patch: Partial<LineDraft>) =>
    setLines((ls) => ls.map((l) => (l.key === key ? { ...l, ...patch } : l)));

  const saveDraft = async (): Promise<Picking | null> => {
    const filled = lines.filter((l) => l.product_id);
    const badQuantity = filled.find((l) => !(Number.parseInt(l.demand, 10) > 0));
    if (badQuantity) {
      setError("A quantidade de cada linha deve ser um inteiro maior que zero");
      return null;
    }
    if (type === "INTERNAL" && source && source === destination) {
      setError("Origem e destino devem ser armazéns diferentes");
      return null;
    }
    setError(undefined);
    try {
      return await save.mutateAsync({
        id: picking?.picking_id,
        draft: {
          type,
          source_stock_id: needsSource ? source : "",
          destination_stock_id: needsDestination ? destination : "",
          partner_id: picking?.partner_id ?? null,
          partner_name: partnerName,
          scheduled_date: scheduled,
          note,
          lines: filled.map((l) => ({
            product_id: l.product_id,
            description: l.description.trim() || null,
            demand: Number.parseInt(l.demand, 10),
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
    if (!picking) navigate(`/transferencias/${saved.picking_id}`, { replace: true });
  };

  const run = async (name: string, message: string, saveFirst = false) => {
    let id = picking?.picking_id;
    if (saveFirst && editable) {
      const saved = await saveDraft();
      if (!saved) return;
      id = saved.picking_id;
    }
    if (!id) return;
    try {
      await action.mutateAsync({ id, action: name });
      showToast(message);
      if (!picking) navigate(`/transferencias/${id}`, { replace: true });
    } catch (e) {
      showToast(errorMessage(e, "Não foi possível concluir a ação"));
    }
  };

  const stockSelect = (value: string, onChange: (v: string) => void) => (
    <FSelect value={value} disabled={!editable} onChange={(e) => onChange(e.target.value)}>
      <option value="">Selecione o armazém</option>
      {(stocks.data ?? []).map((s) => (
        <option key={s.stock_id} value={s.stock_id}>
          {s.description}
        </option>
      ))}
    </FSelect>
  );

  return (
    <>
      <PageHead>
        <div>
          <SectionLabel style={{ fontFamily: "var(--font-mono)" }}>
            {picking ? picking.number : labels.newLabel.toUpperCase()}
          </SectionLabel>
          <h1>{picking ? labels.one : labels.newLabel}</h1>
          <Subtitle>
            <span style={{ display: "inline-flex", gap: 8, alignItems: "center" }}>
              <StatusPill value={state} map={STATE_LABEL} />
              {picking?.origin_number && (
                <span style={{ color: "var(--ink-3)" }}>
                  {ORIGIN_LABEL[picking.origin_type ?? ""] ?? picking.origin_type}{" "}
                  <span className="mono">{picking.origin_number}</span>
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
          {picking && state === "DRAFT" && (
            <Button variant="ghost" disabled={busy} onClick={() => run("confirm", "Transferência confirmada", true)}>
              Confirmar
            </Button>
          )}
          {picking && editable && (
            <Button disabled={busy} onClick={() => setValidating((v) => ({ open: true, key: v.key + 1 }))}>
              <Icon name="check" size={13} /> Validar
            </Button>
          )}
          {picking && state !== "DONE" && state !== "CANCELLED" && (
            <Button variant="ghost" disabled={busy} onClick={() => run("cancel", "Transferência cancelada")}>
              Cancelar
            </Button>
          )}
          {picking && (state === "DRAFT" || state === "CANCELLED") && (
            <Button variant="ghost" disabled={busy} onClick={() => setDeleting(picking)}>
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
            {needsSource && (
              <Field>
                <FieldLabel>Armazém de origem</FieldLabel>
                {stockSelect(source, setSource)}
              </Field>
            )}
            {needsDestination && (
              <Field>
                <FieldLabel>Armazém de destino</FieldLabel>
                {stockSelect(destination, setDestination)}
              </Field>
            )}
            <Field>
              <FieldLabel>{labels.partner}</FieldLabel>
              <FInputStandalone
                value={partnerName}
                readOnly={!editable}
                onChange={(e) => setPartnerName(e.target.value)}
              />
            </Field>
            <Field>
              <FieldLabel>Data prevista</FieldLabel>
              <FInputStandalone
                type="date"
                value={scheduled}
                readOnly={!editable}
                onChange={(e) => setScheduled(e.target.value)}
              />
            </Field>
          </div>
          <Field style={{ marginTop: 14 }}>
            <FieldLabel>Observações</FieldLabel>
            <FTextarea value={note} readOnly={!editable} onChange={(e) => setNote(e.target.value)} />
          </Field>
          {error && <div style={{ marginTop: 10, fontSize: 12, color: "var(--neg)" }}>{error}</div>}
        </CardBody>
      </Card>

      <Card>
        <CardHead>
          <h3>Produtos ({lines.filter((l) => l.product_id).length})</h3>
        </CardHead>
        <T>
          <thead>
            <tr>
              <th style={{ minWidth: 280 }}>Produto</th>
              <th className="num">Demanda</th>
              {state === "DONE" && <th className="num">Feito</th>}
              {editable && <th style={{ width: 40 }} />}
            </tr>
          </thead>
          <tbody>
            {lines.map((l, i) => (
              <tr key={l.key}>
                <td style={{ padding: "6px 8px" }}>
                  {editable ? (
                    <TypeaheadPicker
                      value={l.product_id}
                      selectedLabel={l.description || l.product_id}
                      placeholder="Buscar produto…"
                      useOptions={useProductSuggestions}
                      getId={(p) => p.id}
                      renderOption={(p) => (
                        <>
                          <span>{p.description}</span>
                          <span className="mono" style={{ color: "var(--ink-3)" }}>
                            {brl(p.sale_price)}
                          </span>
                        </>
                      )}
                      onSelect={(p) => update(l.key, { product_id: p.id, description: p.description })}
                      onClear={() => update(l.key, { product_id: "", description: "" })}
                    />
                  ) : (
                    l.description || l.product_id
                  )}
                </td>
                <td style={{ padding: "6px 8px" }} className="num">
                  {editable ? (
                    <Input
                      style={{ width: 90, textAlign: "right" }}
                      inputMode="numeric"
                      value={l.demand}
                      onChange={(e) => update(l.key, { demand: e.target.value })}
                    />
                  ) : (
                    l.demand
                  )}
                </td>
                {state === "DONE" && <td className="num">{picking?.lines[i]?.done ?? 0}</td>}
                {editable && (
                  <td style={{ padding: "6px 8px" }}>
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      aria-label="Remover linha"
                      onClick={() => setLines((ls) => ls.filter((x) => x.key !== l.key))}
                    >
                      <Icon name="trash" size={13} />
                    </Button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </T>
        {editable && (
          <div style={{ padding: "10px 12px" }}>
            <Button type="button" variant="ghost" size="xs" onClick={() => setLines((ls) => [...ls, emptyLine()])}>
              <Icon name="plus" size={12} /> Adicionar produto
            </Button>
          </div>
        )}
      </Card>

      {picking && (
        <ValidateDialog
          key={validating.key}
          open={validating.open}
          picking={picking}
          onClose={() => setValidating((v) => ({ ...v, open: false }))}
          onValidated={(backorder) => {
            setValidating((v) => ({ ...v, open: false }));
            if (backorder) navigate(`/transferencias/${backorder.picking_id}`);
          }}
        />
      )}

      <ResourceDeleteDialog<Picking>
        item={deleting}
        noun="transferência"
        feminine
        getId={(p) => p.picking_id}
        getLabel={(p) => p.number}
        useRemove={useDeletePicking}
        onClose={() => setDeleting(null)}
        onDeleted={() => navigate("/transferencias")}
      />
    </>
  );
}
