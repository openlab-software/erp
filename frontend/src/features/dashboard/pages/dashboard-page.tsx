import { Icon } from "@/components/icon";
import {
  Barchart,
  Card,
  CardBody,
  CardHead,
  CardHeadSub,
  Donut,
  DonutVal,
  Empty,
  Page,
  PageActions,
  PageHead,
  SectionLabel,
  Stat,
  StatDelta,
  StatGrid,
  StatLabel,
  StatValue,
  Status,
  Subtitle,
  T,
  TwoCol,
} from "@/components/ui";
import { STATUS_LABEL } from "@/features/products";
import {
  HEALTH_LABEL,
  MOVEMENT_LABEL,
  itemHealth,
  signedQuantity,
} from "@/features/stock";
import { useProductsById } from "@/features/products";
import { formatDateTime } from "@/lib/format";
import { Link, useNavigate } from "@modern-js/runtime/router";
import { Button } from "@openlab-ui/react";
import styled from "@xstyled/emotion";
import { useDashboard } from "../queries";

const LegendWrap = styled.div`
  display: flex;
  gap: 18px;
  margin-top: 24px;
  font-size: 12px;
  color: var(--ink-3);
`;

const LegendItem = styled.span`
  display: inline-flex;
  align-items: center;
  gap: 6px;
`;

const LegendDot = styled.span<{ $color: string }>`
  width: 10px;
  height: 10px;
  background: ${({ $color }) => $color};
  border-radius: 2px;
  display: inline-block;
`;

const LegendRow = styled.div<{ $last?: boolean }>`
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: ${({ $last }) => ($last ? "0" : "1px solid var(--line)")};
`;

const ActivityRow = styled.div<{ $last?: boolean }>`
  display: flex;
  gap: 12px;
  padding: 12px 18px;
  border-bottom: ${({ $last }) => ($last ? "0" : "1px solid var(--line)")};
`;

const ActivityTime = styled.div`
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--ink-3);
  min-width: 96px;
  padding-top: 2px;
`;

const ActivityText = styled.div`
  flex: 1;
  font-size: 13px;
`;

const ActivitySub = styled.div`
  font-size: 11.5px;
  color: var(--ink-3);
  margin-top: 2px;
`;

const STATUS_COLOR = {
  DRAFT: "var(--line-strong)",
  PUBLISHED: "var(--accent-mid)",
  INACTIVE: "var(--ink-3)",
} as const;

const today = () =>
  new Date()
    .toLocaleDateString("pt-BR", { day: "2-digit", month: "short", year: "numeric" })
    .replace(/\./g, "")
    .toUpperCase();

export function DashboardPage() {
  const navigate = useNavigate();
  const data = useDashboard();
  const names = useProductsById([
    ...data.critical.map((i) => i.product_id),
    ...data.recent.map((m) => m.product_id),
  ]);
  const productName = (id: string) => names.get(id)?.description ?? id;

  const peak = Math.max(1, ...data.week.flatMap((d) => [d.inbound, d.outbound]));
  const statusTotal = data.productsByStatus.reduce((n, s) => n + (s.total ?? 0), 0);
  const published = data.productsByStatus.find((s) => s.status === "PUBLISHED")?.total ?? 0;
  const publishedPct = statusTotal ? Math.round((published / statusTotal) * 100) : 0;
  const fmt = (n: number | undefined) => (n === undefined ? "—" : n.toLocaleString("pt-BR"));

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>RESUMO OPERACIONAL · {today()}</SectionLabel>
          <h1>Visão geral</h1>
          <Subtitle>
            <span className="mono">{fmt(data.belowMin)}</span> item(ns) abaixo do mínimo em{" "}
            <span className="mono">{fmt(data.warehouses)}</span> armazém(ns) ·{" "}
            <span className="mono">{fmt(data.products)}</span> produtos no catálogo.
          </Subtitle>
        </div>
        <PageActions>
          <Link to="/estoque">
            <Button variant="ghost">
              <Icon name="warehouse" /> Estoque
            </Button>
          </Link>
          <Link to="/produtos">
            <Button>
              <Icon name="plus" /> Novo produto
            </Button>
          </Link>
        </PageActions>
      </PageHead>

      <StatGrid>
        <Stat>
          <StatLabel>Produtos cadastrados</StatLabel>
          <StatValue>{fmt(data.products)}</StatValue>
          <StatDelta>
            {fmt(published)} publicado(s)
          </StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Itens abaixo do mínimo</StatLabel>
          <StatValue>{fmt(data.belowMin)}</StatValue>
          <StatDelta $neg={!!data.belowMin}>
            {data.belowMin ? "ação requerida" : "tudo em dia"}
          </StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Armazéns</StatLabel>
          <StatValue>{fmt(data.warehouses)}</StatValue>
          <StatDelta>locais de estoque</StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Fornecedores</StatLabel>
          <StatValue>{fmt(data.suppliers)}</StatValue>
          <StatDelta>cadastrados no catálogo</StatDelta>
        </Stat>
      </StatGrid>

      <div style={{ height: 20 }} />

      <TwoCol>
        <Card>
          <CardHead>
            <div>
              <h3>Movimentações de estoque · últimos 7 dias</h3>
              <CardHeadSub>
                Entradas em cinza, saídas em verde · unidades · últimas 100 movimentações por armazém
              </CardHeadSub>
            </div>
          </CardHead>
          <CardBody>
            <Barchart>
              {data.week.map((d) => (
                <div className="bar-col" key={d.label}>
                  <div className="b" style={{ height: `${(d.inbound / peak) * 100}%` }} title={`Entradas: ${d.inbound}`} />
                  <div
                    className="b alt"
                    style={{ height: `${(d.outbound / peak) * 100}%`, marginTop: -2 }}
                    title={`Saídas: ${d.outbound}`}
                  />
                  <div className="lbl">{d.label}</div>
                </div>
              ))}
            </Barchart>
            <LegendWrap>
              <LegendItem>
                <LegendDot $color="var(--ink-2)" />
                Entradas
              </LegendItem>
              <LegendItem>
                <LegendDot $color="var(--accent)" />
                Saídas
              </LegendItem>
            </LegendWrap>
          </CardBody>
        </Card>

        <Card>
          <CardHead>
            <h3>Produtos por status</h3>
          </CardHead>
          <CardBody style={{ display: "flex", alignItems: "center", gap: 24 }}>
            <Donut style={{ "--p": publishedPct, "--c": "var(--accent-mid)" } as React.CSSProperties}>
              <DonutVal className="mono">
                {publishedPct}
                <span style={{ fontSize: 13, color: "var(--ink-3)" }}>%</span>
              </DonutVal>
            </Donut>
            <div style={{ flex: 1, fontSize: 12.5 }}>
              {data.productsByStatus.map((s, i) => (
                <LegendRow key={s.status} $last={i === data.productsByStatus.length - 1}>
                  <LegendItem style={{ color: "var(--ink-3)", gap: 8 }}>
                    <LegendDot $color={STATUS_COLOR[s.status]} />
                    {STATUS_LABEL[s.status].label}
                  </LegendItem>
                  <span className="mono">{fmt(s.total)}</span>
                </LegendRow>
              ))}
            </div>
          </CardBody>
        </Card>
      </TwoCol>

      <div style={{ height: 20 }} />

      <TwoCol>
        <Card>
          <CardHead>
            <h3>Itens críticos</h3>
            <Button
              variant="ghost"
              onClick={() => navigate("/estoque")}
              style={{ height: 30, padding: "0 10px", fontSize: 12.5 }}
            >
              Ver todos <Icon name="arrow-right" size={11} />
            </Button>
          </CardHead>
          <T>
            <thead>
              <tr>
                <th>Produto</th>
                <th>Armazém</th>
                <th className="num">Atual</th>
                <th className="num">Mínimo</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {data.critical.map((item) => {
                const health = HEALTH_LABEL[itemHealth(item)];
                return (
                  <tr key={`${item.stock_id}-${item.product_id}`}>
                    <td>
                      <Link to={`/produtos/${item.product_id}`} style={{ color: "var(--ink-1)" }}>
                        {productName(item.product_id)}
                      </Link>
                    </td>
                    <td style={{ color: "var(--ink-3)" }}>{item.stockName}</td>
                    <td className="num"><b>{item.current_value}</b></td>
                    <td className="num">{item.min_value ?? "—"}</td>
                    <td><Status variant={health.variant}>{health.label}</Status></td>
                  </tr>
                );
              })}
            </tbody>
          </T>
          {data.critical.length === 0 && (
            <Empty>{data.loading ? "Carregando…" : "Nenhum item abaixo do mínimo."}</Empty>
          )}
        </Card>

        <Card>
          <CardHead>
            <h3>Atividade recente</h3>
          </CardHead>
          <div style={{ padding: 0 }}>
            {data.recent.map((m, i) => {
              const qty = signedQuantity(m.type, m.quantity);
              return (
                <ActivityRow key={m.movement_id} $last={i === data.recent.length - 1}>
                  <ActivityTime>{formatDateTime(m.created_at)}</ActivityTime>
                  <ActivityText>
                    {MOVEMENT_LABEL[m.type].label} de{" "}
                    <b>{qty > 0 ? `+${qty}` : qty}</b> · {productName(m.product_id)}
                    <ActivitySub>
                      {m.stockName}
                      {m.reference && ` · ${m.reference}`}
                    </ActivitySub>
                  </ActivityText>
                </ActivityRow>
              );
            })}
            {data.recent.length === 0 && (
              <Empty>{data.loading ? "Carregando…" : "Nenhuma movimentação registrada."}</Empty>
            )}
          </div>
        </Card>
      </TwoCol>
    </Page>
  );
}
