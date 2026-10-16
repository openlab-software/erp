import { Icon } from "@/components/icon";
import {
  Page,
  PageActions,
  PageHead,
  SearchWrap,
  SectionLabel,
  Stat,
  StatGrid,
  StatLabel,
  StatValue,
  Status,
  Subtitle,
} from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { brl, formatDate } from "@/lib/money";
import { errorMessage } from "@/lib/query";
import { useDebouncedValue } from "@/lib/use-debounced-value";
import { Button, Field, InputGroup, InputGroupAddon, InputGroupInput } from "@openlab-ui/react";
import styled from "@xstyled/emotion";
import { useState } from "react";
import { LeadDialog } from "../components/lead-dialog";
import { LoseDialog } from "../components/lose-dialog";
import { useMoveLead, useOpenLeads, usePipeline, useStages, useWinLead } from "../queries";
import type { Lead, PipelineColumn } from "../types";

const Board = styled.div`
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: minmax(260px, 1fr);
  gap: 14px;
  overflow-x: auto;
  padding-bottom: 12px;
  align-items: start;
`;

const Column = styled.div<{ $over?: boolean }>`
  background: ${({ $over }) => ($over ? "var(--surface-sunken)" : "var(--surface-2)")};
  border: 1px solid ${({ $over }) => ($over ? "var(--accent)" : "var(--line)")};
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  min-height: 220px;
  transition: border-color 100ms, background 100ms;
`;

const ColumnHead = styled.div`
  padding: 12px 14px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  h3 {
    margin: 0;
    font-size: 13px;
    font-weight: 600;
    display: flex;
    justify-content: space-between;
  }
  span.sub {
    font-size: 11.5px;
    color: var(--ink-3);
  }
`;

const Card = styled.div<{ $dragging?: boolean }>`
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 10px 12px;
  margin: 0 10px 10px;
  cursor: grab;
  opacity: ${({ $dragging }) => ($dragging ? 0.4 : 1)};
  &:hover {
    border-color: var(--line-strong);
  }
  .name {
    display: block;
    width: 100%;
    padding: 0;
    border: 0;
    background: none;
    text-align: left;
    font: inherit;
    font-weight: 500;
    font-size: 13px;
    color: var(--ink-1);
    cursor: pointer;
  }
  .meta {
    font-size: 11.5px;
    color: var(--ink-3);
    margin-top: 2px;
  }
  .foot {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;
    font-size: 12px;
  }
`;

const stars = (priority: number) => "★".repeat(priority) + "☆".repeat(3 - priority);

export function PipelinePage() {
  const { showToast } = useToast();
  const [search, setSearch] = useState("");
  const [assignee, setAssignee] = useState("");
  const q = useDebouncedValue(search.trim());
  const assignedTo = useDebouncedValue(assignee.trim());

  const stages = useStages();
  const pipeline = usePipeline();
  const leads = useOpenLeads(q, assignedTo);
  const move = useMoveLead();
  const win = useWinLead();

  const [dragging, setDragging] = useState<string | null>(null);
  const [over, setOver] = useState<string | null>(null);
  const [form, setForm] = useState<{ open: boolean; lead?: Lead; stageId?: string; key: number }>({
    open: false,
    key: 0,
  });
  const [losing, setLosing] = useState<{ lead: Lead; key: number } | null>(null);

  const openForm = (lead?: Lead, stageId?: string) =>
    setForm((f) => ({ open: true, lead, stageId, key: f.key + 1 }));

  const columns: PipelineColumn[] = pipeline.data?.columns ?? [];
  const byStage = (stageId: string) => (leads.data ?? []).filter((l) => l.stage_id === stageId);

  const moveTo = (leadId: string, stageId: string) => {
    const lead = (leads.data ?? []).find((l) => l.lead_id === leadId);
    if (!lead || lead.stage_id === stageId) return;
    move.mutate(
      { id: leadId, stageId },
      {
        onSuccess: (moved) => showToast(moved.status === "WON" ? "Oportunidade ganha 🎉" : "Oportunidade movida"),
        onError: (e) => showToast(errorMessage(e, "Erro ao mover a oportunidade")),
      },
    );
  };

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>COMERCIAL · CRM</SectionLabel>
          <h1>Pipeline de vendas</h1>
          <Subtitle>
            <span className="mono">{pipeline.data?.count ?? 0}</span> oportunidades abertas
          </Subtitle>
        </div>
        <PageActions>
          <Button onClick={() => openForm()}>
            <Icon name="plus" /> Nova oportunidade
          </Button>
        </PageActions>
      </PageHead>

      <StatGrid>
        <Stat>
          <StatLabel>Em aberto</StatLabel>
          <StatValue>{pipeline.data?.count ?? 0}</StatValue>
        </Stat>
        <Stat>
          <StatLabel>Receita esperada</StatLabel>
          <StatValue>{brl(pipeline.data?.expected_revenue)}</StatValue>
        </Stat>
        <Stat>
          <StatLabel>Previsão ponderada</StatLabel>
          <StatValue>{brl(pipeline.data?.weighted_revenue)}</StatValue>
          <div style={{ fontSize: 12, color: "var(--ink-3)" }}>receita x probabilidade</div>
        </Stat>
      </StatGrid>

      <div style={{ display: "flex", gap: 8, margin: "20px 0 14px" }}>
        <SearchWrap>
          <Field>
            <InputGroup>
              <InputGroupAddon>
                <Icon name="search" size={13} />
              </InputGroupAddon>
              <InputGroupInput
                style={{ width: "300px" }}
                placeholder="Buscar por nome, contato ou empresa…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </InputGroup>
          </Field>
        </SearchWrap>
        <Field>
          <InputGroup>
            <InputGroupInput
              style={{ width: "180px" }}
              placeholder="Responsável"
              value={assignee}
              onChange={(e) => setAssignee(e.target.value)}
            />
          </InputGroup>
        </Field>
      </div>

      {(pipeline.isError || leads.isError) && (
        <div style={{ color: "var(--neg)", marginBottom: 12, fontSize: 13 }}>
          {errorMessage(pipeline.error ?? leads.error, "Erro ao carregar o pipeline")}
        </div>
      )}

      <Board>
        {columns.map((col) => (
          <Column
            key={col.stage_id}
            $over={over === col.stage_id}
            onDragOver={(e) => {
              e.preventDefault();
              setOver(col.stage_id);
            }}
            onDragLeave={() => setOver((o) => (o === col.stage_id ? null : o))}
            onDrop={(e) => {
              e.preventDefault();
              setOver(null);
              const id = e.dataTransfer.getData("text/plain");
              if (id) moveTo(id, col.stage_id);
            }}
          >
            <ColumnHead>
              <h3>
                <span>
                  {col.name} {col.won && <Status variant="pos">Ganho</Status>}
                </span>
                <span className="mono">{col.count}</span>
              </h3>
              <span className="sub">
                {brl(col.expected_revenue)} · {col.probability}%
              </span>
            </ColumnHead>
            {byStage(col.stage_id).map((lead) => (
              <Card
                key={lead.lead_id}
                draggable
                $dragging={dragging === lead.lead_id}
                onDragStart={(e) => {
                  e.dataTransfer.setData("text/plain", lead.lead_id);
                  setDragging(lead.lead_id);
                }}
                onDragEnd={() => setDragging(null)}
              >
                <button type="button" className="name" onClick={() => openForm(lead)}>
                  {lead.name}
                </button>
                <div className="meta">
                  {[lead.company_name, lead.contact_name].filter(Boolean).join(" · ") || "Sem contato"}
                  {lead.type === "LEAD" && " · lead"}
                </div>
                {lead.tags.length > 0 && (
                  <div style={{ marginTop: 6, display: "flex", gap: 4, flexWrap: "wrap" }}>
                    {lead.tags.map((t) => (
                      <Status key={t}>{t}</Status>
                    ))}
                  </div>
                )}
                <div className="foot">
                  <b className="mono">{brl(lead.expected_revenue)}</b>
                  <span title="Prioridade" style={{ color: "var(--warn)" }}>
                    {stars(lead.priority)}
                  </span>
                </div>
                <div className="meta" style={{ display: "flex", justifyContent: "space-between" }}>
                  <span>{lead.assigned_to ?? "Sem responsável"}</span>
                  <span>{lead.expected_closing ? formatDate(lead.expected_closing) : `${lead.probability}%`}</span>
                </div>
                <div style={{ display: "flex", gap: 4, marginTop: 8 }}>
                  <Button
                    variant="ghost"
                    size="xs"
                    disabled={win.isPending}
                    onClick={() =>
                      win.mutate(lead.lead_id, {
                        onSuccess: () => showToast("Oportunidade ganha 🎉"),
                        onError: (e) => showToast(errorMessage(e, "Erro ao ganhar a oportunidade")),
                      })
                    }
                  >
                    Ganhar
                  </Button>
                  <Button variant="ghost" size="xs" onClick={() => setLosing((l) => ({ lead, key: (l?.key ?? 0) + 1 }))}>
                    Perder
                  </Button>
                </div>
              </Card>
            ))}
            {!col.won && (
              <div style={{ padding: "0 10px 10px" }}>
                <Button variant="ghost" size="xs" onClick={() => openForm(undefined, col.stage_id)}>
                  <Icon name="plus" size={12} /> Adicionar
                </Button>
              </div>
            )}
          </Column>
        ))}
      </Board>
      {pipeline.isPending && <div style={{ color: "var(--ink-3)" }}>Carregando…</div>}

      <LeadDialog
        key={form.key}
        open={form.open}
        lead={form.lead}
        stages={stages.data ?? []}
        defaultStageId={form.stageId}
        onClose={() => setForm((f) => ({ ...f, open: false }))}
      />
      {losing && <LoseDialog key={losing.key} open lead={losing.lead} onClose={() => setLosing(null)} />}
    </Page>
  );
}
