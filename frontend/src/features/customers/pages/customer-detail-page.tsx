import { Icon } from "@/components/icon";
import { ResourceDeleteDialog } from "@/components/resource/resource-delete-dialog";
import {
  Card,
  CardBody,
  CardHead,
  Empty,
  Kv,
  Page,
  PageActions,
  PageHead,
  SectionLabel,
  Status,
  Subtitle,
} from "@/components/ui";
import { useToast } from "@/contexts/toast-context";
import { formatDateTime, formatDocument } from "@/lib/format";
import { errorMessage } from "@/lib/query";
import { useNavigate } from "@modern-js/runtime/router";
import { Button } from "@openlab-ui/react";
import styled from "@xstyled/emotion";
import type { ReactNode } from "react";
import { useState } from "react";
import { CustomerFormDialog } from "../components/customer-form-dialog";
import { DOCUMENT_LABEL, STATUS_LABEL, TYPE_LABEL } from "../labels";
import { useChangeCustomerStatus, useCustomer, useDeleteCustomer } from "../queries";
import type { Customer } from "../types";

const Grid = styled.div`
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
`;

interface CustomerDetailPageProps {
  customerId: string;
  /** The customer's addresses (owned by the addresses feature), rendered under the data cards. */
  addresses?: ReactNode;
}

export function CustomerDetailPage({ customerId, addresses }: CustomerDetailPageProps) {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const query = useCustomer(customerId);
  const changeStatus = useChangeCustomerStatus();
  const [form, setForm] = useState({ open: false, key: 0 });
  const [deleting, setDeleting] = useState<Customer | null>(null);

  const customer = query.data;

  const back = (
    <div style={{ marginBottom: 14 }}>
      <Button
        variant="ghost"
        onClick={() => navigate("/clientes")}
        style={{ marginLeft: -10, color: "var(--ink-3)" }}
      >
        ← Voltar para Clientes
      </Button>
    </div>
  );

  if (!customer) {
    return (
      <Page>
        {back}
        <Empty>
          {query.isError
            ? errorMessage(query.error, "Erro ao carregar o cliente")
            : "Carregando…"}
        </Empty>
      </Page>
    );
  }

  const status = STATUS_LABEL[customer.status];
  const company = customer.type === "COMPANY";
  const nextStatus = customer.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";

  return (
    <Page>
      {back}

      <PageHead>
        <div>
          <SectionLabel style={{ fontFamily: "var(--font-mono)" }}>
            {customer.customer_id}
          </SectionLabel>
          <h1>{customer.name}</h1>
          <Subtitle>
            {TYPE_LABEL[customer.type]} ·{" "}
            <Status variant={status.variant || undefined}>{status.label}</Status>
          </Subtitle>
        </div>
        <PageActions>
          <Button
            variant="ghost"
            disabled={changeStatus.isPending}
            onClick={() =>
              changeStatus.mutate(
                { id: customer.customer_id, status: nextStatus },
                {
                  onSuccess: () =>
                    showToast(nextStatus === "ACTIVE" ? "Cliente ativado" : "Cliente inativado"),
                  onError: (e) => showToast(errorMessage(e, "Erro ao alterar o status")),
                },
              )
            }
          >
            {customer.status === "ACTIVE" ? "Inativar" : "Ativar"}
          </Button>
          <Button variant="ghost" onClick={() => setForm((f) => ({ open: true, key: f.key + 1 }))}>
            <Icon name="edit" size={13} /> Editar
          </Button>
          <Button variant="ghost" onClick={() => setDeleting(customer)}>
            <Icon name="trash" size={13} /> Excluir
          </Button>
        </PageActions>
      </PageHead>

      <Grid>
        <Card>
          <CardHead>
            <h3>Identificação</h3>
          </CardHead>
          <CardBody>
            <Kv>
              <dt>{company ? "Razão social" : "Nome"}</dt>
              <dd>{customer.name}</dd>
              {company && (
                <>
                  <dt>Nome fantasia</dt>
                  <dd>{customer.trade_name ?? "—"}</dd>
                </>
              )}
              <dt>{DOCUMENT_LABEL[customer.type]}</dt>
              <dd className="mono">{formatDocument(customer.document)}</dd>
              {company ? (
                <>
                  <dt>Inscrição estadual</dt>
                  <dd>{customer.state_registration ?? "—"}</dd>
                </>
              ) : (
                <>
                  <dt>Nascimento</dt>
                  <dd className="mono">
                    {customer.birth_date
                      ? new Date(`${customer.birth_date}T00:00:00`).toLocaleDateString("pt-BR")
                      : "—"}
                  </dd>
                </>
              )}
            </Kv>
          </CardBody>
        </Card>

        <Card>
          <CardHead>
            <h3>Contato e registro</h3>
          </CardHead>
          <CardBody>
            <Kv>
              <dt>E-mail</dt>
              <dd>{customer.email ?? "—"}</dd>
              <dt>Telefone</dt>
              <dd>{customer.phone ?? "—"}</dd>
              <dt>Cadastrado em</dt>
              <dd className="mono">{formatDateTime(customer.created_at)}</dd>
              <dt>Atualizado em</dt>
              <dd className="mono">{formatDateTime(customer.updated_at)}</dd>
            </Kv>
          </CardBody>
        </Card>
      </Grid>

      {addresses}

      <CustomerFormDialog
        key={form.key}
        open={form.open}
        customer={customer}
        onClose={() => setForm((f) => ({ ...f, open: false }))}
        onSaved={(message) => {
          setForm((f) => ({ ...f, open: false }));
          showToast(message);
        }}
      />

      <ResourceDeleteDialog<Customer>
        item={deleting}
        noun="cliente"
        getId={(c) => c.customer_id}
        getLabel={(c) => c.name}
        hint="Os endereços do cliente também serão excluídos. Para manter o histórico, prefira inativar."
        useRemove={useDeleteCustomer}
        onClose={() => setDeleting(null)}
        onDeleted={() => navigate("/clientes")}
      />
    </Page>
  );
}
