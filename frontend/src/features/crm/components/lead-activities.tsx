import { FInputStandalone, FSelect, Status } from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { formatDate } from "@/lib/money";
import { errorMessage } from "@/lib/query";
import { Button } from "@openlab-ui/react";
import { useState } from "react";
import { useActivities, useAddActivity, useCompleteActivity } from "../queries";
import type { ActivityType } from "../types";

const TYPE_LABEL: Record<ActivityType, string> = {
  TODO: "Tarefa",
  CALL: "Ligação",
  MEETING: "Reunião",
  EMAIL: "E-mail",
};

/** Follow-ups of a lead: what is pending (overdue ones highlighted), what was done, and a quick add. */
export function LeadActivities({ leadId }: { leadId: string }) {
  const { showToast } = useToast();
  const activities = useActivities(leadId);
  const add = useAddActivity();
  const complete = useCompleteActivity();
  const [type, setType] = useState<ActivityType>("CALL");
  const [summary, setSummary] = useState("");
  const [dueDate, setDueDate] = useState("");

  const submit = () => {
    if (!summary.trim()) return;
    add.mutate(
      { leadId, type, summary: summary.trim(), due_date: dueDate || null },
      {
        onSuccess: () => {
          setSummary("");
          setDueDate("");
        },
        onError: (e) => showToast(errorMessage(e, "Erro ao agendar a atividade")),
      },
    );
  };

  return (
    <div style={{ borderTop: "1px solid var(--line)", paddingTop: 14 }}>
      <div style={{ fontWeight: 600, fontSize: 13, marginBottom: 8 }}>Atividades</div>
      {(activities.data ?? []).map((a) => (
        <div
          key={a.activity_id}
          style={{
            display: "flex",
            alignItems: "center",
            gap: 8,
            padding: "5px 0",
            fontSize: 13,
            opacity: a.done ? 0.55 : 1,
          }}
        >
          <Status variant={a.done ? "pos" : a.overdue ? "neg" : undefined}>{TYPE_LABEL[a.type]}</Status>
          <span style={{ flex: 1, textDecoration: a.done ? "line-through" : undefined }}>{a.summary}</span>
          <span style={{ color: a.overdue ? "var(--neg)" : "var(--ink-3)" }}>{formatDate(a.due_date)}</span>
          {!a.done && (
            <Button
              type="button"
              variant="ghost"
              size="xs"
              disabled={complete.isPending}
              onClick={() =>
                complete.mutate(
                  { leadId, activityId: a.activity_id },
                  { onError: (e) => showToast(errorMessage(e, "Erro ao concluir a atividade")) },
                )
              }
            >
              Concluir
            </Button>
          )}
        </div>
      ))}
      {activities.data?.length === 0 && (
        <div style={{ fontSize: 12, color: "var(--ink-4)", marginBottom: 8 }}>Nenhuma atividade agendada.</div>
      )}
      <div style={{ display: "grid", gridTemplateColumns: "110px 1fr 140px auto", gap: 8, marginTop: 8 }}>
        <FSelect value={type} onChange={(e) => setType(e.target.value as ActivityType)}>
          {Object.entries(TYPE_LABEL).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </FSelect>
        <FInputStandalone
          placeholder="O que fazer?"
          value={summary}
          onChange={(e) => setSummary(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              e.preventDefault();
              submit();
            }
          }}
        />
        <FInputStandalone type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
        <Button type="button" variant="ghost" disabled={add.isPending} onClick={submit}>
          Agendar
        </Button>
      </div>
    </div>
  );
}
