import { http } from "@/lib/http";
import { jsonBody, toSearch } from "@/lib/query";
import type { Page, Success } from "@/lib/types";
import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import type { CustomerFormValues } from "./schemas";
import type { Customer, CustomerFilters, CustomerStatus } from "./types";

const BASE = "/api/customer/customers";
const url = (id: string, suffix = "") => `${BASE}/${encodeURIComponent(id)}${suffix}`;

export interface ListCustomersParams extends CustomerFilters {
  page: number;
  pageSize: number;
}

/** Form values → request body: blank strings become `null`. */
const toPayload = (v: CustomerFormValues) => {
  const nul = (value: string) => value.trim() || null;
  return {
    type: v.type,
    name: v.name,
    trade_name: nul(v.trade_name),
    document: v.document,
    state_registration: nul(v.state_registration),
    birth_date: nul(v.birth_date),
    email: nul(v.email),
    phone: nul(v.phone),
  };
};

export const customerKeys = {
  all: ["customers"] as const,
  list: (params: ListCustomersParams) => [...customerKeys.all, "list", params] as const,
  detail: (id: string) => [...customerKeys.all, "detail", id] as const,
};

export const useCustomer = (id: string) =>
  useQuery({ queryKey: customerKeys.detail(id), queryFn: () => http<Customer>(url(id)) });

/** Paginated, filtered customer list; keeps the previous page while the next one loads. */
export const useCustomers = ({ pageSize, ...rest }: ListCustomersParams) =>
  useQuery({
    queryKey: customerKeys.list({ pageSize, ...rest }),
    queryFn: () => http<Page<Customer>>(`${BASE}${toSearch({ ...rest, page_size: pageSize })}`),
    placeholderData: keepPreviousData,
  });

/** Every mutation refreshes all cached customer lists. */
const useCustomerMutation = <TVariables, TResult>(
  mutationFn: (variables: TVariables) => Promise<TResult>,
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: customerKeys.all }),
  });
};

export const useCreateCustomer = () =>
  useCustomerMutation((values: CustomerFormValues) =>
    http<Customer>(BASE, { method: "POST", body: jsonBody(toPayload(values)) }),
  );

export const useUpdateCustomer = () =>
  useCustomerMutation(({ id, values }: { id: string; values: CustomerFormValues }) =>
    http<Customer>(url(id), { method: "PUT", body: jsonBody(toPayload(values)) }),
  );

export const useChangeCustomerStatus = () =>
  useCustomerMutation(({ id, status }: { id: string; status: CustomerStatus }) =>
    http<Customer>(url(id, "/status"), { method: "PATCH", body: jsonBody({ status }) }),
  );

export const useDeleteCustomer = () =>
  useCustomerMutation((id: string) => http<Success>(url(id), { method: "DELETE" }));
