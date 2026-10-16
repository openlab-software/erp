import { http } from "@/lib/http";
import { jsonBody, toSearch } from "@/lib/query";
import type { Page, Success } from "@/lib/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { num } from "@/lib/money";
import type {
  Activity,
  ActivityType,
  Lead,
  LeadFormValues,
  LostReason,
  Pipeline,
  Stage,
} from "./types";

const BASE = "/api/crm";
const leadUrl = (id: string, suffix = "") => `${BASE}/leads/${encodeURIComponent(id)}${suffix}`;

export const crmKeys = {
  all: ["crm"] as const,
  pipeline: () => [...crmKeys.all, "pipeline"] as const,
  stages: () => [...crmKeys.all, "stages"] as const,
  lostReasons: () => [...crmKeys.all, "lost-reasons"] as const,
  leads: () => [...crmKeys.all, "leads"] as const,
  activities: (leadId: string) => [...crmKeys.all, "activities", leadId] as const,
};

export const useStages = () =>
  useQuery({
    queryKey: crmKeys.stages(),
    queryFn: () => http<{ data: Stage[] }>(`${BASE}/stages`),
    select: (r) => r.data,
  });

export const useLostReasons = () =>
  useQuery({
    queryKey: crmKeys.lostReasons(),
    queryFn: () => http<{ data: LostReason[] }>(`${BASE}/lost-reasons`),
    select: (r) => r.data.filter((reason) => reason.active),
  });

/** Forecast: open leads per stage and in total. */
export const usePipeline = () =>
  useQuery({ queryKey: crmKeys.pipeline(), queryFn: () => http<Pipeline>(`${BASE}/pipeline`) });

/** The open leads shown on the board (the 100 most important: priority, then newest). */
export const useOpenLeads = (q: string, assignedTo: string) =>
  useQuery({
    queryKey: [...crmKeys.leads(), "open", q, assignedTo],
    queryFn: () =>
      http<Page<Lead>>(
        `${BASE}/leads${toSearch({ status: "OPEN", q, assigned_to: assignedTo, page_size: 100 })}`,
      ),
    select: (page) => page.data,
  });

const tagsOf = (text: string) =>
  text
    .split(",")
    .map((t) => t.trim())
    .filter(Boolean);

const toPayload = (v: LeadFormValues) => {
  const nul = (value: string) => value.trim() || null;
  return {
    name: v.name,
    type: v.type,
    stage_id: v.stage_id || null,
    customer_id: nul(v.customer_id),
    contact_name: nul(v.contact_name),
    company_name: nul(v.company_name),
    email: nul(v.email),
    phone: nul(v.phone),
    expected_revenue: num(v.expected_revenue),
    probability: v.probability.trim() === "" ? null : num(v.probability),
    priority: Number.parseInt(v.priority, 10) || 0,
    expected_closing: nul(v.expected_closing),
    assigned_to: nul(v.assigned_to),
    tags: tagsOf(v.tags),
    description: nul(v.description),
  };
};

/** Every CRM mutation refreshes the board, the forecast and the activities. */
const useCrmMutation = <TVariables, TResult>(
  mutationFn: (variables: TVariables) => Promise<TResult>,
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: crmKeys.all }),
  });
};

export const useSaveLead = () =>
  useCrmMutation(({ id, values }: { id?: string; values: LeadFormValues }) =>
    http<Lead>(id ? leadUrl(id) : `${BASE}/leads`, {
      method: id ? "PUT" : "POST",
      body: jsonBody(toPayload(values)),
    }),
  );

export const useMoveLead = () =>
  useCrmMutation(({ id, stageId }: { id: string; stageId: string }) =>
    http<Lead>(leadUrl(id, "/stage"), { method: "PATCH", body: jsonBody({ stage_id: stageId }) }),
  );

export const useWinLead = () =>
  useCrmMutation((id: string) => http<Lead>(leadUrl(id, "/win"), { method: "POST", body: jsonBody({}) }));

export const useLoseLead = () =>
  useCrmMutation(({ id, lostReasonId }: { id: string; lostReasonId: string | null }) =>
    http<Lead>(leadUrl(id, "/lose"), {
      method: "POST",
      body: jsonBody({ lost_reason_id: lostReasonId }),
    }),
  );

export const useDeleteLead = () =>
  useCrmMutation((id: string) => http<Success>(leadUrl(id), { method: "DELETE" }));

export const useActivities = (leadId: string | undefined) =>
  useQuery({
    queryKey: crmKeys.activities(leadId ?? ""),
    queryFn: () => http<{ data: Activity[] }>(leadUrl(leadId as string, "/activities")),
    select: (r) => r.data,
    enabled: Boolean(leadId),
  });

export const useAddActivity = () =>
  useCrmMutation(
    ({
      leadId,
      ...body
    }: {
      leadId: string;
      type: ActivityType;
      summary: string;
      due_date: string | null;
    }) => http<Activity>(leadUrl(leadId, "/activities"), { method: "POST", body: jsonBody(body) }),
  );

export const useCompleteActivity = () =>
  useCrmMutation(({ leadId, activityId }: { leadId: string; activityId: string }) =>
    http<Activity>(leadUrl(leadId, `/activities/${encodeURIComponent(activityId)}/done`), {
      method: "POST",
      body: jsonBody({}),
    }),
  );
