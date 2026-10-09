// customer-service serializes snake_case; the BFF passes it through untouched.
/** INDIVIDUAL = pessoa física (CPF); COMPANY = pessoa jurídica (CNPJ). Immutable after creation. */
export type CustomerType = "INDIVIDUAL" | "COMPANY";

export type CustomerStatus = "ACTIVE" | "INACTIVE";

export interface Customer {
  customer_id: string;
  type: CustomerType;
  status: CustomerStatus;
  name: string;
  /** Companies only. */
  trade_name: string | null;
  /** CPF or CNPJ, digits only. */
  document: string;
  /** Companies only. */
  state_registration: string | null;
  /** Individuals only, `YYYY-MM-DD`. */
  birth_date: string | null;
  email: string | null;
  phone: string | null;
  created_at: string;
  updated_at: string | null;
}

export interface CustomerFilters {
  q: string;
  type: string;
  status: string;
}
