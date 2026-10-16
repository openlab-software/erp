import { Icon } from "@/components/icon";
import { TextField } from "@/components/resource/form-fields";
import { ResourceDeleteDialog } from "@/components/resource/resource-delete-dialog";
import { ResourceFormDialog } from "@/components/resource/resource-form-dialog";
import { RowActions } from "@/components/resource/row-actions";
import { Card, CardHead, Empty, Status, T } from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { errorMessage } from "@/lib/query";
import { Button } from "@openlab-ui/react";
import { useState } from "react";
import { useAddContact, useContacts, useDeleteContact, useUpdateContact } from "../queries";
import { type ContactFormValues, MAX_CONTACTS, contactSchema } from "../schemas";
import type { Contact } from "../types";

const defaults = (c?: Contact): ContactFormValues => ({
  name: c?.name ?? "",
  job_title: c?.job_title ?? "",
  email: c?.email ?? "",
  phone: c?.phone ?? "",
  notes: c?.notes ?? "",
  is_primary: c?.is_primary ?? false,
});

interface ContactsPanelProps {
  customerId: string;
}

/** The customer's contact persons: add, edit, delete and choose the primary one (none or one). */
export function ContactsPanel({ customerId }: ContactsPanelProps) {
  const { showToast } = useToast();
  const query = useContacts(customerId);
  const contacts = query.data ?? [];
  const [form, setForm] = useState<{ open: boolean; contact?: Contact; key: number }>({
    open: false,
    key: 0,
  });
  const [toDelete, setToDelete] = useState<Contact | null>(null);

  const openForm = (contact?: Contact) =>
    setForm((f) => ({ open: true, contact, key: f.key + 1 }));
  const closeForm = () => setForm((f) => ({ ...f, open: false }));

  return (
    <Card style={{ marginTop: 20 }}>
      <CardHead>
        <h3>Pessoas de contato ({contacts.length})</h3>
        <Button size="xs" disabled={contacts.length >= MAX_CONTACTS} onClick={() => openForm()}>
          <Icon name="plus" size={12} /> Adicionar contato
        </Button>
      </CardHead>

      <T>
        <thead>
          <tr>
            <th>Nome</th>
            <th>Cargo</th>
            <th>E-mail</th>
            <th>Telefone</th>
            <th>Principal</th>
            <th style={{ width: 90 }} />
          </tr>
        </thead>
        <tbody>
          {contacts.map((c) => (
            <tr key={c.contact_id}>
              <td style={{ fontWeight: 500 }}>{c.name}</td>
              <td style={{ color: "var(--ink-3)" }}>{c.job_title ?? "—"}</td>
              <td>{c.email ?? "—"}</td>
              <td style={{ color: "var(--ink-3)" }}>{c.phone ?? "—"}</td>
              <td>
                {c.is_primary ? (
                  <Status variant="pos">Principal</Status>
                ) : (
                  <span style={{ color: "var(--ink-4)" }}>—</span>
                )}
              </td>
              <td>
                <RowActions label={c.name} onEdit={() => openForm(c)} onDelete={() => setToDelete(c)} />
              </td>
            </tr>
          ))}
        </tbody>
      </T>
      {query.isPending && <Empty>Carregando…</Empty>}
      {query.isError && <Empty>{errorMessage(query.error, "Erro ao carregar os contatos")}</Empty>}
      {!query.isPending && !query.isError && contacts.length === 0 && (
        <Empty>Nenhuma pessoa de contato cadastrada.</Empty>
      )}

      <ResourceFormDialog<Contact, ContactFormValues>
        key={form.key}
        open={form.open}
        item={form.contact}
        noun="contato"
        schema={contactSchema}
        defaults={defaults}
        getId={(c) => c.contact_id}
        useCreate={() => useAddContact(customerId)}
        useUpdate={() => useUpdateContact(customerId)}
        renderFields={({ register, errors }) => (
          <div style={{ display: "grid", gap: 14 }}>
            <TextField
              id="contact-name"
              label="Nome"
              autoFocus
              error={errors.name?.message}
              {...register("name")}
            />
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 14 }}>
              <TextField
                id="contact-job-title"
                label="Cargo"
                placeholder="Compras, Financeiro…"
                error={errors.job_title?.message}
                {...register("job_title")}
              />
              <TextField
                id="contact-phone"
                label="Telefone"
                error={errors.phone?.message}
                {...register("phone")}
              />
            </div>
            <TextField
              id="contact-email"
              label="E-mail"
              error={errors.email?.message}
              {...register("email")}
            />
            <TextField
              id="contact-notes"
              label="Observações"
              error={errors.notes?.message}
              {...register("notes")}
            />
            <label style={{ display: "flex", alignItems: "center", gap: 8, fontSize: 13 }}>
              <input type="checkbox" {...register("is_primary")} />
              Contato principal do cliente
              <span style={{ fontSize: 11, color: "var(--ink-4)" }}>
                (substitui o principal atual, se houver)
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

      <ResourceDeleteDialog<Contact>
        item={toDelete}
        noun="contato"
        getId={(c) => c.contact_id}
        getLabel={(c) => c.name}
        useRemove={() => useDeleteContact(customerId)}
        onClose={() => setToDelete(null)}
      />
    </Card>
  );
}
