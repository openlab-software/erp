import { http } from "@/lib/http";
import { jsonBody, toSearch } from "@/lib/query";
import type { Page, Success } from "@/lib/types";
import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import type { Invoice, InvoiceFilters, InvoiceKind, PaymentMethod, Position } from "./types";

const BASE = "/api/accounting/invoices";
const url = (id: string, suffix = "") => `${BASE}/${encodeURIComponent(id)}${suffix}`;

export const invoiceKeys = { all: ["invoices"] as const };

export const useInvoices = ({
  page,
  pageSize,
  overdue,
  ...rest
}: InvoiceFilters & { page: number; pageSize: number }) =>
  useQuery({
    queryKey: [...invoiceKeys.all, "list", { page, pageSize, overdue, ...rest }],
    queryFn: () =>
      http<Page<Invoice>>(
        `${BASE}${toSearch({ ...rest, overdue: overdue || undefined, page, page_size: pageSize })}`,
      ),
    placeholderData: keepPreviousData,
  });

export const useInvoice = (id: string | undefined) =>
  useQuery({
    queryKey: [...invoiceKeys.all, "detail", id],
    queryFn: () => http<Invoice>(url(id as string)),
    enabled: Boolean(id),
  });

/** Receivable and payable position, for the cards on top of the list. */
export const usePosition = () =>
  useQuery({
    queryKey: [...invoiceKeys.all, "position"],
    queryFn: () => http<{ data: Position[] }>("/api/accounting/position"),
    select: (r) => r.data,
  });

const useRefreshing = <TVariables, TResult>(
  mutationFn: (variables: TVariables) => Promise<TResult>,
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: invoiceKeys.all }),
  });
};

export interface InvoiceDraft {
  kind: InvoiceKind;
  partner_id: string;
  partner_name: string;
  invoice_date: string;
  due_date: string;
  reference: string;
  note: string;
  lines: {
    product_id: string | null;
    description: string;
    quantity: number;
    unit_price: number;
    discount_percent: number;
    tax_percent: number;
  }[];
}

const nul = (value: string) => value.trim() || null;

const toPayload = (d: InvoiceDraft) => ({
  kind: d.kind,
  partner_id: d.partner_id,
  partner_name: d.partner_name,
  currency: "BRL",
  invoice_date: nul(d.invoice_date),
  due_date: nul(d.due_date),
  reference: nul(d.reference),
  note: nul(d.note),
  lines: d.lines,
});

export const useSaveInvoice = () =>
  useRefreshing(({ id, draft }: { id?: string; draft: InvoiceDraft }) =>
    http<Invoice>(id ? url(id) : BASE, {
      method: id ? "PUT" : "POST",
      body: jsonBody(toPayload(draft)),
    }),
  );

/** post / cancel / draft */
export const useInvoiceAction = () =>
  useRefreshing(({ id, action }: { id: string; action: string }) =>
    http<Invoice>(url(id, `/${action}`), { method: "POST", body: jsonBody({}) }),
  );

export const useRegisterPayment = () =>
  useRefreshing(
    ({
      id,
      ...payment
    }: {
      id: string;
      amount: number;
      paid_on: string;
      method: PaymentMethod;
      reference: string | null;
    }) => http<Invoice>(url(id, "/payments"), { method: "POST", body: jsonBody(payment) }),
  );

export const useDeleteInvoice = () =>
  useRefreshing((id: string) => http<Success>(url(id), { method: "DELETE" }));
