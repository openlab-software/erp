import { Stats } from "@/components/dashboard/stats";
import { Link, useNavigate } from "@modern-js/runtime/router";
import { Button } from "@openlab-ui/react";
import styled from "@xstyled/emotion";
import { Icon } from "../components/icon";
import {
  Barchart,
  Card,
  CardBody,
  CardHead,
  CardHeadSub,
  Donut,
  DonutVal,
  Page,
  PageActions,
  PageHead,
  SectionLabel,
  Status,
  Subtitle,
  T,
  TwoCol,
} from "../components/ui";

const days = ["22", "23", "24", "25", "26", "27", "28"];
const bars = [
  [42, 28],
  [56, 31],
  [38, 24],
  [62, 40],
  [71, 33],
  [45, 22],
  [88, 51],
];

const activity = [
  {
    t: "09:14",
    txt: "Entrada de 500 un BRG-6204-Z (NF-1842)",
    u: "L. Pereira",
  },
  { t: "08:42", txt: "Consumo de 2 un MTR-0420-A na OP-0915", u: "R. Costa" },
  { t: "08:10", txt: "Pedido PED-3014 entrou em produção", u: "Sistema" },
  {
    t: "ontem",
    txt: "Inventário cíclico ajustou -2 un PFC-SOL-M8",
    u: "M. Almeida",
  },
  {
    t: "ontem",
    txt: "Transferência 20 un SEN-IND-M18 → Filial Sul",
    u: "J. Tavares",
  },
];

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

const LegendRowLabel = styled.span`
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--ink-3);
`;

const LegendRowDot = styled.span<{ $color: string }>`
  width: 8px;
  height: 8px;
  background: ${({ $color }) => $color};
  border-radius: 2px;
  display: inline-block;
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
  min-width: 42px;
  padding-top: 2px;
`;

const ActivityText = styled.div`
  flex: 1;
  font-size: 13px;
`;

const ActivityUser = styled.div`
  font-size: 11.5px;
  color: var(--ink-3);
  margin-top: 2px;
`;

export default function Dashboard() {
  const navigate = useNavigate();

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>RESUMO OPERACIONAL · 28 ABR 2026</SectionLabel>
          <h1>Bom dia, Renato.</h1>
          <Subtitle>
            Você tem <span className="mono">3 itens</span> em ruptura,{" "}
            <span className="mono">8 pedidos</span> aguardando expedição e{" "}
            <span className="mono">R$ 9.888,00</span> em contas a pagar até
            amanhã.
          </Subtitle>
        </div>
        <PageActions>
          <Button variant="ghost">
            <Icon name="download" /> Exportar
          </Button>
          <Link to="/vendas/novo">
            <Button>
              <Icon name="plus" /> Novo pedido
            </Button>
          </Link>
        </PageActions>
      </PageHead>

      <Stats />

      <div style={{ height: 20 }} />

      <TwoCol>
        <Card>
          <CardHead>
            <div>
              <h3>Movimentações de estoque · últimos 7 dias</h3>
              <CardHeadSub>
                Entradas em cinza, saídas em verde · barras em unidades
              </CardHeadSub>
            </div>
            <div style={{ display: "flex", gap: 8 }}>
              <Button size="xs">7d</Button>
              <Button size="xs" variant="ghost">
                30d
              </Button>
              <Button size="xs" variant="ghost">
                90d
              </Button>
            </div>
          </CardHead>
          <CardBody>
            <Barchart>
              {bars.map(([a, b], i) => (
                <div className="bar-col" key={i}>
                  <div
                    className="b"
                    style={{ height: (a / 100) * 100 + "%" }}
                  />
                  <div
                    className="b alt"
                    style={{ height: (b / 100) * 100 + "%", marginTop: -2 }}
                  />
                  <div className="lbl">{days[i]}</div>
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
                Saídas / consumo produção
              </LegendItem>
            </LegendWrap>
          </CardBody>
        </Card>

        <Card>
          <CardHead>
            <h3>Status dos pedidos</h3>
          </CardHead>
          <CardBody style={{ display: "flex", alignItems: "center", gap: 24 }}>
            <Donut
              style={
                { "--p": 64, "--c": "var(--accent-mid)" } as React.CSSProperties
              }
            >
              <DonutVal className="mono">
                64<span style={{ fontSize: 13, color: "var(--ink-3)" }}>%</span>
              </DonutVal>
            </Donut>
            <div style={{ flex: 1, fontSize: 12.5 }}>
              {[
                { color: "var(--accent-mid)", label: "Concluídos", val: "32" },
                { color: "var(--ink-3)", label: "Em produção", val: "12" },
                {
                  color: "var(--line-strong)",
                  label: "Em expedição",
                  val: "8",
                },
                {
                  color: "var(--line-strong)",
                  label: "Cancelados",
                  val: "2",
                  last: true,
                },
              ].map((leg, i) => (
                <LegendRow key={i} $last={leg.last}>
                  <LegendRowLabel>
                    <LegendRowDot $color={leg.color} />
                    {leg.label}
                  </LegendRowLabel>
                  <span className="mono">{leg.val}</span>
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
                <th>SKU</th>
                <th>Produto</th>
                <th className="num">Estoque</th>
                <th className="num">Mínimo</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td className="id">PRS-HID-001</td>
                <td>Cilindro hidráulico 80x250mm</td>
                <td className="num">
                  <b>0</b>
                </td>
                <td className="num">4</td>
                <td>
                  <Status variant="neg">Ruptura</Status>
                </td>
              </tr>
              <tr>
                <td className="id">DSP-PROG-V2</td>
                <td>Display programável 7"</td>
                <td className="num">
                  <b>3</b>
                </td>
                <td className="num">6</td>
                <td>
                  <Status variant="warn">Abaixo do mín.</Status>
                </td>
              </tr>
              <tr>
                <td className="id">VLV-PNEU-12</td>
                <td>Válvula pneumática 5/2 vias</td>
                <td className="num">
                  <b>8</b>
                </td>
                <td className="num">12</td>
                <td>
                  <Status variant="warn">Abaixo do mín.</Status>
                </td>
              </tr>
            </tbody>
          </T>
        </Card>

        <Card>
          <CardHead>
            <h3>Atividade recente</h3>
          </CardHead>
          <div style={{ padding: 0 }}>
            {activity.map((a, i) => (
              <ActivityRow key={i} $last={i === activity.length - 1}>
                <ActivityTime>{a.t}</ActivityTime>
                <ActivityText>
                  {a.txt}
                  <ActivityUser>{a.u}</ActivityUser>
                </ActivityText>
              </ActivityRow>
            ))}
          </div>
        </Card>
      </TwoCol>
    </Page>
  );
}
