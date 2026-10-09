import { http } from "@/lib/http";
import { jsonBody } from "@/lib/query";
import type { Success } from "@/lib/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { AddressFormValues } from "./schemas";
import type { Address, AddressList } from "./types";

const url = (customerId: string, addressId?: string) =>
  `/api/customer/customers/${encodeURIComponent(customerId)}/addresses${
    addressId ? `/${encodeURIComponent(addressId)}` : ""
  }`;

/** Form values → request body: blank strings become `null`. */
const toPayload = (v: AddressFormValues) => {
  const nul = (value: string) => value.trim() || null;
  return {
    label: nul(v.label),
    zip_code: nul(v.zip_code),
    street: nul(v.street),
    number: nul(v.number),
    complement: nul(v.complement),
    neighborhood: nul(v.neighborhood),
    city: nul(v.city),
    state: nul(v.state),
    is_default: v.is_default,
  };
};

export const addressKeys = {
  all: ["addresses"] as const,
  list: (customerId: string) => [...addressKeys.all, customerId] as const,
};

export const useAddresses = (customerId: string) =>
  useQuery({
    queryKey: addressKeys.list(customerId),
    queryFn: () => http<AddressList>(url(customerId)),
    select: (list) => list.data,
  });

/** Saving one address can change the default flag of the others, so the whole list is refreshed. */
const useAddressMutation = <TVariables, TResult>(
  customerId: string,
  mutationFn: (variables: TVariables) => Promise<TResult>,
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: addressKeys.list(customerId) }),
  });
};

export const useAddAddress = (customerId: string) =>
  useAddressMutation(customerId, (values: AddressFormValues) =>
    http<Address>(url(customerId), { method: "POST", body: jsonBody(toPayload(values)) }),
  );

export const useUpdateAddress = (customerId: string) =>
  useAddressMutation(customerId, ({ id, values }: { id: string; values: AddressFormValues }) =>
    http<Address>(url(customerId, id), { method: "PUT", body: jsonBody(toPayload(values)) }),
  );

export const useDeleteAddress = (customerId: string) =>
  useAddressMutation(customerId, (addressId: string) =>
    http<Success>(url(customerId, addressId), { method: "DELETE" }),
  );
