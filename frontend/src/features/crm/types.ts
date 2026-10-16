// crm-service serializes snake_case; the BFF passes it through untouched.
export type LeadType = "LEAD" | "OPPORTUNITY";
export type LeadStatus = "OPEN" | "WON" | "LOST";
export type ActivityType = "TODO" | "CALL" | "MEETING" | "EMAIL";

export interface Stage {
  stage_id: string;
  name: string;
  position: number;
  probability: number;
  won: boolean;
}

export interface LostReason {
  lost_reason_id: string;
  name: string;
  active: boolean;
}

export interface Lead {
  lead_id: string;
  name: string;
  type: LeadType;
  status: LeadStatus;
  stage_id: string;
  customer_id: string | null;
  contact_name: string | null;
  company_name: string | null;
  email: string | null;
  phone: string | null;
  expected_revenue: number;
  /** 0-100. */
  probability: number;
  /** 0 (normal) to 3 (very high). */
  priority: number;
  expected_closing: string | null;
  description: string | null;
  assigned_to: string | null;
  tags: string[];
  lost_reason_id: string | null;
  created_at: string;
  updated_at: string | null;
  closed_at: string | null;
}

export interface PipelineColumn {
  stage_id: string;
  name: string;
  position: number;
  probability: number;
  won: boolean;
  count: number;
  expected_revenue: number;
  weighted_revenue: number;
}

export interface Pipeline {
  columns: PipelineColumn[];
  count: number;
  expected_revenue: number;
  weighted_revenue: number;
}

export interface Activity {
  activity_id: string;
  lead_id: string;
  type: ActivityType;
  summary: string;
  note: string | null;
  due_date: string | null;
  assigned_to: string | null;
  done: boolean;
  overdue: boolean;
  done_at: string | null;
  created_at: string;
}

export interface LeadFormValues {
  name: string;
  type: LeadType;
  stage_id: string;
  customer_id: string;
  customer_name: string;
  contact_name: string;
  company_name: string;
  email: string;
  phone: string;
  expected_revenue: string;
  probability: string;
  priority: string;
  expected_closing: string;
  assigned_to: string;
  tags: string;
  description: string;
}
