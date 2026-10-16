import { http } from "@/lib/http";
import { jsonBody } from "@/lib/query";
import type { Success } from "@/lib/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { ContactFormValues } from "./schemas";
import type { Contact, ContactList } from "./types";

const url = (customerId: string, contactId?: string) =>
  `/api/customer/customers/${encodeURIComponent(customerId)}/contacts${
    contactId ? `/${encodeURIComponent(contactId)}` : ""
  }`;

/** Form values → request body: blank strings become `null`. */
const toPayload = (v: ContactFormValues) => {
  const nul = (value: string) => value.trim() || null;
  return {
    name: v.name,
    job_title: nul(v.job_title),
    email: nul(v.email),
    phone: nul(v.phone),
    notes: nul(v.notes),
    is_primary: v.is_primary,
  };
};

export const contactKeys = {
  all: ["contacts"] as const,
  list: (customerId: string) => [...contactKeys.all, customerId] as const,
};

export const useContacts = (customerId: string) =>
  useQuery({
    queryKey: contactKeys.list(customerId),
    queryFn: () => http<ContactList>(url(customerId)),
    select: (list) => list.data,
  });

/** Saving one contact can change the primary flag of the others, so the whole list is refreshed. */
const useContactMutation = <TVariables, TResult>(
  customerId: string,
  mutationFn: (variables: TVariables) => Promise<TResult>,
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: contactKeys.list(customerId) }),
  });
};

export const useAddContact = (customerId: string) =>
  useContactMutation(customerId, (values: ContactFormValues) =>
    http<Contact>(url(customerId), { method: "POST", body: jsonBody(toPayload(values)) }),
  );

export const useUpdateContact = (customerId: string) =>
  useContactMutation(customerId, ({ id, values }: { id: string; values: ContactFormValues }) =>
    http<Contact>(url(customerId, id), { method: "PUT", body: jsonBody(toPayload(values)) }),
  );

export const useDeleteContact = (customerId: string) =>
  useContactMutation(customerId, (contactId: string) =>
    http<Success>(url(customerId, contactId), { method: "DELETE" }),
  );
