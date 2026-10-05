import { useState } from "react";
import { useNavigate, useParams } from "@modern-js/runtime/router";
import styled from "@xstyled/emotion";
import { Icon } from "../../../components/icon";
import { MoveBadge } from "../../../components/status-badge";
import { Button } from "@openlab-ui/react";
import { PRODUCTS, MOVEMENTS, fmtBRL, fmtDateTime } from "../../../data";
import {
  Page,
  PageHead,
  PageActions,
  SectionLabel,
  Subtitle,
  Btn,
  BtnPrimary,
  BtnGhost,
  Card,
  CardHead,
  CardBody,
  Status,
  T,
  Kv,
  Tabs,
  Tab,
  BarTrack,
  BarFill,
  Empty,
  ProdThumbLg,
} from "../../../components/ui";

const TABS = [
  ["geral", "Geral"],
  ["estoque", "Estoque & locais"],
  ["movimentacoes", "Movimentações"],
  ["fiscal", "Fiscal"],
  ["historico", "Histórico"],
] as const;

const DetailGrid = styled.div`
  display: grid;
  grid-template-columns: 260px 1fr 1fr;
  gap: 20px;
`;

const ThumbCaption = styled.div`
  margin-top: 10px;
  font-size: 11px;
  color: var(--ink-3);
  text-align: center;
`;

const StockRow = styled.div`
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 12px;
`;

const StockMinMax = styled.div`
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 11px;
  color: var(--ink-3);
`;

const MinMarker = styled.div`
  position: absolute;
  top: -2px;
  bottom: -2px;
  width: 1px;
  background: var(--ink-3);
`;

const TrackWrap = styled.div`
  position: relative;
`;

export default function ProdutoDetalhe() {
  const navigate = useNavigate();
  const { sku } = useParams<{ sku: string }>();
  const p = PRODUCTS.find((x) => x.sku === sku) ?? PRODUCTS[0];
  const [tab, setTab] = useState<string>("geral");

  const recentMoves = MOVEMENTS.filter((m) => m.sku === p.sku).slice(0, 5);
  const stockPct = Math.min(100, (p.stock / p.max) * 100);
  const minPct = (p.min / p.max) * 100;

  return (
    <Page>
      <div style={{ marginBottom: 14 }}>
        <Button
          variant="ghost"
          onClick={() => navigate("/produtos")}
          style={{ marginLeft: -10, color: "var(--ink-3)" }}
        >
          ← Voltar para Produtos
        </Button>
      </div>

      <PageHead>
        <div>
          <SectionLabel style={{ fontFamily: "var(--font-mono)" }}>
            {p.sku}
          </SectionLabel>
          <h1>{p.name}</h1>
          <Subtitle>
            {p.category} · {p.supplier} · armazém{" "}
            <span className="mono">{p.location}</span>
          </Subtitle>
        </div>
        <PageActions>
          <Button variant="ghost">
            <Icon name="edit" size={13} /> Editar
          </Button>
          <Button>
            <Icon name="plus" /> Movimentar
          </Button>
        </PageActions>
      </PageHead>

      <Tabs>
        {TABS.map(([id, label]) => (
          <Tab key={id} $active={tab === id} onClick={() => setTab(id)}>
            {label}
          </Tab>
        ))}
      </Tabs>

      {tab === "geral" && (
        <DetailGrid>
          <div>
            <ProdThumbLg />
            <ThumbCaption>Sem imagem cadastrada</ThumbCaption>
          </div>

          <Card>
            <CardHead>
              <h3>Identificação</h3>
            </CardHead>
            <CardBody>
              <Kv>
                <dt>SKU</dt>
                <dd className="mono">{p.sku}</dd>
                <dt>Nome</dt>
                <dd>{p.name}</dd>
                <dt>Categoria</dt>
                <dd>{p.category}</dd>
                <dt>Unidade</dt>
                <dd className="mono">{p.uom}</dd>
                <dt>Peso unit.</dt>
                <dd className="mono">{p.weight} kg</dd>
                <dt>Fornecedor</dt>
                <dd>{p.supplier}</dd>
                <dt>Status</dt>
                <dd>
                  <Status variant="pos">Ativo</Status>
                </dd>
              </Kv>
            </CardBody>
          </Card>

          <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
            <Card>
              <CardHead>
                <h3>Preços e custos</h3>
              </CardHead>
              <CardBody>
                <Kv>
                  <dt>Custo médio</dt>
                  <dd className="mono">{fmtBRL(p.cost)}</dd>
                  <dt>Preço de venda</dt>
                  <dd className="mono">{p.price ? fmtBRL(p.price) : "—"}</dd>
                  <dt>Margem</dt>
                  <dd className="mono" style={{ color: "var(--pos)" }}>
                    {p.price
                      ? (((p.price - p.cost) / p.price) * 100).toFixed(1) + "%"
                      : "—"}
                  </dd>
                </Kv>
              </CardBody>
            </Card>

            <Card>
              <CardHead>
                <h3>Posição de estoque</h3>
              </CardHead>
              <CardBody>
                <StockRow>
                  <span style={{ color: "var(--ink-3)" }}>Atual</span>
                  <span className="mono">
                    <b>{p.stock}</b> / {p.max} {p.uom}
                  </span>
                </StockRow>
                <TrackWrap>
                  <BarTrack style={{ height: 8 }}>
                    <BarFill
                      variant={
                        p.stock === 0 ? "neg" : p.stock < p.min ? "warn" : "pos"
                      }
                      style={{ width: stockPct + "%" }}
                    />
                    <MinMarker style={{ left: minPct + "%" }} />
                  </BarTrack>
                </TrackWrap>
                <StockMinMax>
                  <span>
                    Mín: <span className="mono">{p.min}</span>
                  </span>
                  <span>
                    Máx: <span className="mono">{p.max}</span>
                  </span>
                </StockMinMax>
              </CardBody>
            </Card>
          </div>
        </DetailGrid>
      )}

      {tab !== "geral" && (
        <Card>
          <CardHead>
            <h3>
              {tab === "estoque"
                ? "Locais de armazenamento"
                : tab === "movimentacoes"
                  ? "Movimentações recentes"
                  : tab === "fiscal"
                    ? "Dados fiscais"
                    : "Histórico"}
            </h3>
          </CardHead>
          {tab === "movimentacoes" ? (
            <T>
              <thead>
                <tr>
                  <th>Data</th>
                  <th>Tipo</th>
                  <th>Documento</th>
                  <th>Origem/Destino</th>
                  <th className="num">Qtd</th>
                  <th>Usuário</th>
                </tr>
              </thead>
              <tbody>
                {recentMoves.length === 0 && (
                  <tr>
                    <td colSpan={6} className="empty">
                      Sem movimentações recentes para este SKU.
                    </td>
                  </tr>
                )}
                {recentMoves.map((m) => (
                  <tr key={m.id}>
                    <td className="mono">{fmtDateTime(m.date)}</td>
                    <td>
                      <MoveBadge type={m.type} />
                    </td>
                    <td className="id">{m.doc}</td>
                    <td>{m.origin}</td>
                    <td className="num">
                      {m.qty > 0 ? "+" : ""}
                      {m.qty}
                    </td>
                    <td>{m.user}</td>
                  </tr>
                ))}
              </tbody>
            </T>
          ) : (
            <Empty>
              <Icon name="box" size={32} />
              <div style={{ marginTop: 12 }}>
                Conteúdo da aba <b>{tab}</b> em mock.
              </div>
            </Empty>
          )}
        </Card>
      )}
    </Page>
  );
}
