import { FormDialogShell } from "@/components/resource/form-dialog-shell";
import { FSelect } from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { errorMessage } from "@/lib/query";
import { Field, FieldLabel } from "@openlab-ui/react";
import { useState } from "react";
import { useLoseLead, useLostReasons } from "../queries";
import type { Lead } from "../types";

interface LoseDialogProps {
  open: boolean;
  lead: Lead;
  onClose: () => void;
}

/** Marks an opportunity as lost, optionally with the reason (kept for the lost-deals analysis). */
export function LoseDialog({ open, lead, onClose }: LoseDialogProps) {
  const { showToast } = useToast();
  const reasons = useLostReasons();
  const lose = useLoseLead();
  const [reasonId, setReasonId] = useState("");
  const [error, setError] = useState<string>();

  return (
    <FormDialogShell
      open={open}
      title="Marcar como perdida"
      description={lead.name}
      submitLabel="Marcar como perdida"
      pending={lose.isPending}
      serverError={error}
      onSubmit={(event) => {
        event.preventDefault();
        lose.mutate(
          { id: lead.lead_id, lostReasonId: reasonId || null },
          {
            onSuccess: () => {
              showToast("Oportunidade marcada como perdida");
              onClose();
            },
            onError: (e) => setError(errorMessage(e, "Erro ao marcar como perdida")),
          },
        );
      }}
      onClose={onClose}
    >
      <Field>
        <FieldLabel>Motivo da perda</FieldLabel>
        <FSelect value={reasonId} onChange={(e) => setReasonId(e.target.value)}>
          <option value="">Sem motivo informado</option>
          {(reasons.data ?? []).map((r) => (
            <option key={r.lost_reason_id} value={r.lost_reason_id}>
              {r.name}
            </option>
          ))}
        </FSelect>
      </Field>
    </FormDialogShell>
  );
}
