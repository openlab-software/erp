import { Icon } from "@/components/icon";
import { SelectField, TextField } from "@/components/resource/form-fields";
import { ResourceDeleteDialog } from "@/components/resource/resource-delete-dialog";
import { ResourceFormDialog } from "@/components/resource/resource-form-dialog";
import { RowActions } from "@/components/resource/row-actions";
import { Card, CardHead, Empty, Status, T } from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { errorMessage } from "@/lib/query";
import { Button } from "@openlab-ui/react";
import { useState } from "react";
import { useAddAddress, useAddresses, useDeleteAddress, useUpdateAddress } from "../queries";
import { type AddressFormValues, MAX_ADDRESSES, UFS, addressSchema } from "../schemas";
import type { Address } from "../types";

const TWO_COLS = { display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 } as const;

const defaults = (a?: Address): AddressFormValues => ({
  label: a?.label ?? "",
  zip_code: a?.zip_code ?? "",
  street: a?.street ?? "",
  number: a?.number ?? "",
  complement: a?.complement ?? "",
  neighborhood: a?.neighborhood ?? "",
  city: a?.city ?? "",
  state: a?.state ?? "",
  is_default: a?.is_default ?? false,
});

/** "Av. Paulista, 1000 — Bela Vista" / "São Paulo/SP · 01310-100". */
const lines = (a: Address) => {
  const street = [a.street, a.number].filter(Boolean).join(", ");
  const first = [street, a.complement, a.neighborhood].filter(Boolean).join(" — ");
  const zip = a.zip_code ? a.zip_code.replace(/(\d{5})(\d{3})/, "$1-$2") : null;
  const city = a.city ? `${a.city}${a.state ? `/${a.state}` : ""}` : a.state;
  const second = [city, zip].filter(Boolean).join(" · ");
  return { first: first || "—", second };
};

interface AddressesPanelProps {
  customerId: string;
}

/** The customer's addresses: add, edit, delete and choose the default (none or one). */
export function AddressesPanel({ customerId }: AddressesPanelProps) {
  const { showToast } = useToast();
  const query = useAddresses(customerId);
  const addresses = query.data ?? [];
  const [form, setForm] = useState<{ open: boolean; address?: Address; key: number }>({
    open: false,
    key: 0,
  });
  const [toDelete, setToDelete] = useState<Address | null>(null);

  const openForm = (address?: Address) =>
    setForm((f) => ({ open: true, address, key: f.key + 1 }));
  const closeForm = () => setForm((f) => ({ ...f, open: false }));

  return (
    <Card>
      <CardHead>
        <h3>Endereços ({addresses.length})</h3>
        <Button
          size="xs"
          disabled={addresses.length >= MAX_ADDRESSES}
          onClick={() => openForm()}
        >
          <Icon name="plus" size={12} /> Adicionar endereço
        </Button>
      </CardHead>

      <T>
        <thead>
          <tr>
            <th>Identificação</th>
            <th>Endereço</th>
            <th>Cidade / CEP</th>
            <th>Padrão</th>
            <th style={{ width: 90 }} />
          </tr>
        </thead>
        <tbody>
          {addresses.map((a) => {
            const text = lines(a);
            return (
              <tr key={a.address_id}>
                <td style={{ fontWeight: 500 }}>{a.label ?? "—"}</td>
                <td>{text.first}</td>
                <td style={{ color: "var(--ink-3)" }}>{text.second || "—"}</td>
                <td>
                  {a.is_default ? (
                    <Status variant="pos">Padrão</Status>
                  ) : (
                    <span style={{ color: "var(--ink-4)" }}>—</span>
                  )}
                </td>
                <td>
                  <RowActions
                    label={a.label ?? text.first}
                    onEdit={() => openForm(a)}
                    onDelete={() => setToDelete(a)}
                  />
                </td>
              </tr>
            );
          })}
        </tbody>
      </T>
      {query.isPending && <Empty>Carregando…</Empty>}
      {query.isError && (
        <Empty>{errorMessage(query.error, "Erro ao carregar os endereços")}</Empty>
      )}
      {!query.isPending && !query.isError && addresses.length === 0 && (
        <Empty>Nenhum endereço cadastrado.</Empty>
      )}

      <ResourceFormDialog<Address, AddressFormValues>
        key={form.key}
        open={form.open}
        item={form.address}
        noun="endereço"
        schema={addressSchema}
        defaults={defaults}
        getId={(a) => a.address_id}
        useCreate={() => useAddAddress(customerId)}
        useUpdate={() => useUpdateAddress(customerId)}
        renderFields={({ register, errors }) => (
          <div style={{ display: "grid", gap: 14, maxHeight: "60vh", overflowY: "auto", paddingRight: 4 }}>
            <TextField
              id="address-label"
              label="Identificação"
              placeholder="Casa, Matriz, Entrega…"
              autoFocus
              error={errors.label?.message}
              {...register("label")}
            />
            <div style={{ display: "grid", gridTemplateColumns: "1fr 2fr 1fr", gap: 14 }}>
              <TextField
                id="address-zip"
                label="CEP"
                placeholder="00000-000"
                error={errors.zip_code?.message}
                {...register("zip_code")}
              />
              <TextField
                id="address-street"
                label="Logradouro"
                error={errors.street?.message}
                {...register("street")}
              />
              <TextField
                id="address-number"
                label="Número"
                error={errors.number?.message}
                {...register("number")}
              />
            </div>
            <div style={TWO_COLS}>
              <TextField
                id="address-complement"
                label="Complemento"
                error={errors.complement?.message}
                {...register("complement")}
              />
              <TextField
                id="address-neighborhood"
                label="Bairro"
                error={errors.neighborhood?.message}
                {...register("neighborhood")}
              />
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "2fr 1fr", gap: 14 }}>
              <TextField
                id="address-city"
                label="Cidade"
                error={errors.city?.message}
                {...register("city")}
              />
              <SelectField
                id="address-state"
                label="UF"
                placeholder="—"
                options={UFS.map((uf) => ({ value: uf, label: uf }))}
                error={errors.state?.message}
                {...register("state")}
              />
            </div>
            <label style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 13 }}>
              <input type="checkbox" {...register("is_default")} />
              Endereço padrão do cliente
              <span style={{ fontSize: 11, color: "var(--ink-4)" }}>
                (substitui o padrão atual, se houver)
              </span>
            </label>
          </div>
        )}
        onClose={closeForm}
        onSaved={(message) => {
          closeForm();
          showToast(message);
        }}
      />

      <ResourceDeleteDialog<Address>
        item={toDelete}
        noun="endereço"
        getId={(a) => a.address_id}
        getLabel={(a) => a.label ?? lines(a).first}
        useRemove={() => useDeleteAddress(customerId)}
        onClose={() => setToDelete(null)}
      />
    </Card>
  );
}
