import { useNavigate } from "@modern-js/runtime/router";
import {
  Button,
  Field,
  FieldDescription,
  FieldLabel,
  Input,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@openlab-ui/react";
import styled from "@xstyled/emotion";
import { useState } from "react";
import { Icon } from "../../../components/icon";
import {
  Card,
  CardBody,
  CardHead,
  Divider,
  FInputStandalone,
  FSelect,
  FTextarea,
  Kv,
  Page,
  PageActions,
  PageHead,
  SectionLabel,
  Subtitle,
  T,
  TwoCol,
} from "../../../components/ui";
import { useToast } from "../../../contexts/toast-context";
import { fmtBRL, PRODUCTS } from "../../../data";

interface OrderItem {
  sku: string;
  qty: number;
}

const CheckIcon = styled.span<{ $ok?: boolean }>`
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: ${({ $ok }) => ($ok ? "var(--pos-soft)" : "var(--warn-soft)")};
  color: ${({ $ok }) => ($ok ? "var(--pos)" : "var(--warn)")};
  display: grid;
  place-items: center;
  font-size: 10px;
  flex-shrink: 0;
`;

const CheckRow = styled.div<{ $last?: boolean }>`
  padding: 10px 18px;
  border-bottom: ${({ $last }) => ($last ? "0" : "1px solid var(--line)")};
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12.5px;
`;

const CheckLabel = styled.span`
  display: inline-flex;
  align-items: center;
  gap: 8px;
`;

const TotalRow = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: baseline;
`;

const SmallFSelect = styled(FSelect)`
  height: 30px;
`;

const NumInput = styled(FInputStandalone)`
  height: 30px;
  text-align: right;
  width: 70px;
`;

export default function NovoPedido() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [customer, setCustomer] = useState("");
  const [channel, setChannel] = useState("Direto");
  const [items, setItems] = useState<OrderItem[]>([
    { sku: "MTR-0420-A", qty: 2 },
  ]);
  const [obs, setObs] = useState("");

  const addItem = () => setItems([...items, { sku: "", qty: 1 }]);
  const updateItem = (i: number, patch: Partial<OrderItem>) =>
    setItems(items.map((it, idx) => (idx === i ? { ...it, ...patch } : it)));
  const removeItem = (i: number) =>
    setItems(items.filter((_, idx) => idx !== i));

  const sellableProducts = PRODUCTS.filter((p) => p.price > 0);
  const total = items.reduce((s, it) => {
    const p = PRODUCTS.find((x) => x.sku === it.sku);
    return s + (p ? p.price * it.qty : 0);
  }, 0);

  return (
    <Page>
      <div style={{ marginBottom: 14 }}>
        <Button
          variant="ghost"
          onClick={() => navigate("/vendas")}
          style={{ marginLeft: -10, color: "var(--ink-3)" }}
        >
          ← Voltar
        </Button>
      </div>

      <PageHead>
        <div>
          <SectionLabel>COMERCIAL · NOVO PEDIDO</SectionLabel>
          <h1>Novo pedido de venda</h1>
          <Subtitle>Rascunho · será atribuído ID ao salvar</Subtitle>
        </div>
        <PageActions>
          <Button variant="ghost" onClick={() => navigate("/vendas")}>
            Descartar
          </Button>
          <Button variant="ghost">Salvar rascunho</Button>
          <Button
            onClick={() => {
              showToast("Pedido confirmado! (mock)");
              navigate("/vendas");
            }}
          >
            <Icon name="check" /> Confirmar pedido
          </Button>
        </PageActions>
      </PageHead>

      <TwoCol>
        <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
          <Card>
            <CardHead>
              <h3>Cliente e canal</h3>
            </CardHead>
            <CardBody
              style={{
                display: "grid",
                gridTemplateColumns: "2fr 1fr",
                gap: 14,
              }}
            >
              <Field>
                <FieldLabel>Cliente *</FieldLabel>
                <Input
                  required
                  placeholder="Buscar por nome, CNPJ ou código…"
                  value={customer}
                  onChange={(e) => setCustomer(e.target.value)}
                />
                <FieldDescription>
                  Comece a digitar para buscar no cadastro
                </FieldDescription>
              </Field>
              <Field>
                <FieldLabel>Canal</FieldLabel>
                <Select
                  value={channel}
                  onValueChange={(value) => {
                    if (!value) return;

                    setChannel(value);
                  }}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Selecionar" />
                  </SelectTrigger>
                  <SelectContent>
                    {["Direto", "Representante", "Online"].map((value) => (
                      <SelectItem key={value} value={value}>
                        {value}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </Field>
              <Field>
                <FieldLabel>Vendedor responsável</FieldLabel>
                <Select>
                  <SelectTrigger>
                    <SelectValue placeholder="Selecionar" />
                  </SelectTrigger>
                  <SelectContent>
                    {["C. Mendes", "P. Lima", "M. Souza"].map((value) => (
                      <SelectItem key={value} value={value}>
                        {value}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </Field>
              <Field>
                <FieldLabel>Previsão de entrega</FieldLabel>
                <Input type="date" defaultValue="2026-05-12" />
              </Field>
            </CardBody>
          </Card>

          <Card>
            <CardHead>
              <h3>Itens do pedido</h3>
              <Button variant="secondary" onClick={addItem}>
                <Icon name="plus" size={12} /> Adicionar item
              </Button>
            </CardHead>
            <T>
              <thead>
                <tr>
                  <th style={{ width: 200 }}>SKU</th>
                  <th>Produto</th>
                  <th className="num" style={{ width: 90 }}>
                    Qtd
                  </th>
                  <th className="num" style={{ width: 120 }}>
                    Preço
                  </th>
                  <th className="num" style={{ width: 120 }}>
                    Subtotal
                  </th>
                  <th style={{ width: 40 }} />
                </tr>
              </thead>
              <tbody>
                {items.map((it, i) => {
                  const p = PRODUCTS.find((x) => x.sku === it.sku);
                  return (
                    <tr key={i}>
                      <td>
                        <Select
                          value={it.sku}
                          onValueChange={(value) => {
                            if (!value) return;

                            updateItem(i, { sku: value });
                          }}
                        >
                          <SelectTrigger>
                            <SelectValue placeholder="Selecionar" />
                          </SelectTrigger>
                          <SelectContent>
                            {sellableProducts.map((p) => (
                              <SelectItem key={p.sku} value={p.sku}>
                                {p.sku}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </td>
                      <td
                        style={{ color: p ? "var(--ink-1)" : "var(--ink-3)" }}
                      >
                        {p ? p.name : "—"}
                      </td>
                      <td className="num">
                        <NumInput
                          className="mono"
                          type="number"
                          min="1"
                          value={it.qty}
                          onChange={(e) =>
                            updateItem(i, {
                              qty: parseInt(e.target.value) || 1,
                            })
                          }
                        />
                      </td>
                      <td className="num">{p ? fmtBRL(p.price) : "—"}</td>
                      <td className="num">
                        <b>{p ? fmtBRL(p.price * it.qty) : "—"}</b>
                      </td>
                      <td>
                        <Button
                          variant="destructive"
                          size="icon"
                          onClick={() => removeItem(i)}
                        >
                          <Icon name="trash" size={12} />
                        </Button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </T>
          </Card>

          <Card>
            <CardHead>
              <h3>Observações</h3>
            </CardHead>
            <CardBody>
              <FTextarea
                placeholder="Notas internas, condição especial, instruções de entrega…"
                value={obs}
                onChange={(e) => setObs(e.target.value)}
              />
            </CardBody>
          </Card>
        </div>

        <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
          <Card>
            <CardHead>
              <h3>Resumo</h3>
            </CardHead>
            <CardBody>
              <Kv style={{ gridTemplateColumns: "1fr auto" }}>
                <dt>Itens</dt>
                <dd className="mono" style={{ textAlign: "right" }}>
                  {items.length}
                </dd>
                <dt>Subtotal</dt>
                <dd className="mono" style={{ textAlign: "right" }}>
                  {fmtBRL(total)}
                </dd>
                <dt>Frete</dt>
                <dd
                  className="mono"
                  style={{ textAlign: "right", color: "var(--ink-3)" }}
                >
                  a calcular
                </dd>
                <dt>Impostos est.</dt>
                <dd className="mono" style={{ textAlign: "right" }}>
                  {fmtBRL(total * 0.18)}
                </dd>
              </Kv>
              <Divider />
              <TotalRow>
                <span style={{ color: "var(--ink-3)" }}>Total</span>
                <span
                  className="mono"
                  style={{ fontSize: 22, fontWeight: 600 }}
                >
                  {fmtBRL(total * 1.18)}
                </span>
              </TotalRow>
            </CardBody>
          </Card>

          <Card>
            <CardHead>
              <h3>Verificações</h3>
            </CardHead>
            <div style={{ padding: 0 }}>
              {(
                [
                  [
                    "Cliente cadastrado",
                    !!customer,
                    customer ? "OK" : "pendente",
                  ],
                  ["Estoque disponível", true, "todos os itens"],
                  ["Crédito aprovado", true, "limite R$ 80k"],
                  ["Margem mínima", true, "26,4%"],
                ] as [string, boolean, string][]
              ).map(([l, ok, info], i) => (
                <CheckRow key={i} $last={i === 3}>
                  <CheckLabel>
                    <CheckIcon $ok={ok}>
                      {ok ? <Icon name="check" size={9} /> : "!"}
                    </CheckIcon>
                    {l}
                  </CheckLabel>
                  <span style={{ color: "var(--ink-3)", fontSize: 11 }}>
                    {info}
                  </span>
                </CheckRow>
              ))}
            </div>
          </Card>
        </div>
      </TwoCol>
    </Page>
  );
}
