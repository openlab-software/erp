import { Icon } from "@/components/icon";
import { MoveBadge } from "@/components/status-badge";
import {
  BarFill,
  BarTrack,
  Card,
  CardBody,
  CardHead,
  CardHeadSub,
  FInput,
  Page,
  PageActions,
  PageHead,
  SearchWrap,
  SectionLabel,
  Stat,
  StatDelta,
  StatGrid,
  StatLabel,
  StatValue,
  Status,
  Subtitle,
  T,
  Tab,
  TableToolbar,
  TableWrap,
  Tabs,
  ThreeCol,
} from "@/components/ui";
import { useNavigate } from "@modern-js/runtime/router";
import { Button } from "@openlab-ui/react";
import styled from "@xstyled/emotion";
import { useMemo, useState } from "react";
import { MOVEMENTS, PRODUCTS, fmtBRL, fmtDateTime, fmtNum } from "../../data";

const MinLine = styled.div`
  position: absolute;
  top: -2px;
  bottom: -2px;
  width: 1px;
  background: var(--ink-3);
  opacity: 0.6;
`;

const CapacityCell = styled.div`
  position: relative;
  min-width: 160px;
`;

const ProdRow = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
`;

const ProdMini = styled.div`
  width: 24px;
  height: 24px;
  border-radius: 4px;
  background: repeating-linear-gradient(
    135deg,
    var(--surface-sunken) 0 3px,
    var(--surface-2) 3px 6px
  );
  border: 1px solid var(--line);
  flex-shrink: 0;
`;

const InvRow = styled.div<{ $last?: boolean }>`
  padding: 12px 18px;
  border-bottom: ${({ $last }) => ($last ? "0" : "1px solid var(--line)")};
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

const DateBlock = styled.div`
  width: 42px;
  text-align: center;
  padding: 4px 0;
  background: var(--surface-2);
  border-radius: 6px;
`;

const ScheduleRow = styled.div<{ $last?: boolean }>`
  padding: 14px 18px;
  border-bottom: ${({ $last }) => ($last ? "0" : "1px solid var(--line)")};
  display: flex;
  gap: 14px;
  align-items: center;
`;

export default function Estoque() {
  const navigate = useNavigate();
  const [tab, setTab] = useState("posicao");
  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState("todos");
  const [warehouse, setWarehouse] = useState("todos");

  const lowItems = PRODUCTS.filter((p) => p.stock < p.min);
  const totalValue = PRODUCTS.reduce((s, p) => s + p.cost * p.stock, 0);
  const totalUnits = PRODUCTS.reduce((s, p) => s + p.stock, 0);

  const filteredProducts = useMemo(() => {
    return PRODUCTS.filter((p) => {
      if (
        search &&
        !`${p.sku} ${p.name}`.toLowerCase().includes(search.toLowerCase())
      )
        return false;
      if (warehouse !== "todos" && !p.location.startsWith(warehouse))
        return false;
      return true;
    });
  }, [search, warehouse]);

  const filteredMoves = useMemo(() => {
    return MOVEMENTS.filter((m) => {
      if (typeFilter !== "todos" && m.type !== typeFilter) return false;
      if (
        search &&
        !`${m.sku} ${m.product} ${m.doc}`
          .toLowerCase()
          .includes(search.toLowerCase())
      )
        return false;
      return true;
    });
  }, [typeFilter, search]);

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>OPERAÇÃO · ESTOQUE</SectionLabel>
          <h1>Estoque</h1>
          <Subtitle>
            Posição em tempo real · última sincronização{" "}
            <span className="mono">há 4 min</span>
          </Subtitle>
        </div>
        <PageActions>
          <Button variant="ghost">
            <Icon name="refresh" size={13} /> Sincronizar
          </Button>
          <Button variant="ghost">
            <Icon name="transfer" size={13} /> Transferir
          </Button>
          <Button>
            <Icon name="plus" /> Nova movimentação
          </Button>
        </PageActions>
      </PageHead>

      <StatGrid>
        <Stat>
          <StatLabel>Valor em estoque</StatLabel>
          <StatValue>{fmtBRL(totalValue)}</StatValue>
          <StatDelta $pos>
            <Icon name="arrow-up" size={11} /> +R$ 14.2k esta semana
          </StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Unidades totais</StatLabel>
          <StatValue>
            {fmtNum(totalUnits)}
            <span className="unit">un</span>
          </StatValue>
          <StatDelta>{PRODUCTS.length} SKUs ativos</StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Itens em ruptura</StatLabel>
          <StatValue>{PRODUCTS.filter((p) => p.stock === 0).length}</StatValue>
          <StatDelta $neg>
            <Icon name="arrow-down" size={11} /> requer compra
          </StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Acuracidade (90d)</StatLabel>
          <StatValue>
            98,7<span className="unit">%</span>
          </StatValue>
          <StatDelta $pos>+0,3pp vs. trim. anterior</StatDelta>
        </Stat>
      </StatGrid>

      <div style={{ height: 24 }} />

      <Tabs>
        {[
          ["posicao", "Posição"],
          ["movimentacoes", "Movimentações"],
          ["alertas", `Alertas (${lowItems.length})`],
          ["inventario", "Inventário"],
        ].map(([id, label]) => (
          <Tab key={id} $active={tab === id} onClick={() => setTab(id)}>
            {label}
          </Tab>
        ))}
      </Tabs>

      {tab === "posicao" && (
        <TableWrap>
          <TableToolbar>
            <SearchWrap>
              <Icon name="search" size={13} />
              <FInput
                placeholder="Buscar SKU ou produto…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                style={{ width: 280 }}
              />
            </SearchWrap>
            <div style={{ display: "flex", gap: 6, marginLeft: 8 }}>
              {[
                ["todos", "Todos"],
                ["A", "Armazém A"],
                ["B", "Armazém B"],
                ["C", "Armazém C"],
                ["D", "Armazém D"],
                ["EXT", "Externo"],
              ].map(([id, l]) => (
                <Button
                  size="xs"
                  variant={warehouse === id ? "default" : "ghost"}
                  onClick={() => setWarehouse(id)}
                >
                  {l}
                </Button>
              ))}
            </div>
          </TableToolbar>
          <T>
            <thead>
              <tr>
                <th>SKU</th>
                <th>Produto</th>
                <th>Local</th>
                <th>Capacidade</th>
                <th className="num">Estoque</th>
                <th className="num">Mínimo</th>
                <th className="num">Cobertura</th>
                <th className="num">Valor</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {filteredProducts.map((p) => {
                const pct = Math.min(100, (p.stock / p.max) * 100);
                const minPct = (p.min / p.max) * 100;
                const variant =
                  p.stock === 0 ? "neg" : p.stock < p.min ? "warn" : "pos";
                const dailyConsumption = Math.max(1, Math.round(p.max / 30));
                const coverage = Math.round(p.stock / dailyConsumption);
                return (
                  <tr key={p.sku}>
                    <td className="id">{p.sku}</td>
                    <td>
                      <ProdRow>
                        <ProdMini />
                        <span>{p.name}</span>
                      </ProdRow>
                    </td>
                    <td className="mono" style={{ color: "var(--ink-3)" }}>
                      {p.location}
                    </td>
                    <td>
                      <CapacityCell>
                        <BarTrack>
                          <BarFill
                            variant={variant as any}
                            style={{ width: pct + "%" }}
                          />
                          <MinLine style={{ left: minPct + "%" }} />
                        </BarTrack>
                      </CapacityCell>
                    </td>
                    <td className="num">
                      <b>{fmtNum(p.stock)}</b>{" "}
                      <span style={{ color: "var(--ink-3)" }}>{p.uom}</span>
                    </td>
                    <td className="num" style={{ color: "var(--ink-3)" }}>
                      {p.min}
                    </td>
                    <td className="num">
                      {p.stock === 0 ? (
                        <span style={{ color: "var(--neg)" }}>—</span>
                      ) : (
                        <span>{coverage}d</span>
                      )}
                    </td>
                    <td className="num">{fmtBRL(p.cost * p.stock)}</td>
                    <td>
                      <Status variant={variant as any}>
                        {p.stock === 0
                          ? "Ruptura"
                          : p.stock < p.min
                            ? "Baixo"
                            : "OK"}
                      </Status>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </T>
        </TableWrap>
      )}

      {tab === "movimentacoes" && (
        <TableWrap>
          <TableToolbar>
            <SearchWrap>
              <Icon name="search" size={13} />
              <FInput
                placeholder="Buscar movimentação, SKU, documento…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                style={{ width: 320 }}
              />
            </SearchWrap>
            <div style={{ display: "flex", gap: 6, marginLeft: 8 }}>
              {[
                ["todos", "Todos"],
                ["entrada", "Entradas"],
                ["saida", "Saídas"],
                ["ajuste", "Ajustes"],
                ["transferencia", "Transf."],
              ].map(([id, l]) => (
                <Button
                  key={id}
                  variant={typeFilter === id ? "default" : "ghost"}
                  size="xs"
                  onClick={() => setTypeFilter(id)}
                >
                  {l}
                </Button>
              ))}
            </div>
            <div style={{ flex: 1 }} />
            <Button variant="ghost" size="xs">
              <Icon name="calendar" size={12} /> Abr 2026
            </Button>
            <Button variant="ghost" size="xs">
              <Icon name="download" size={12} /> Exportar
            </Button>
          </TableToolbar>
          <T>
            <thead>
              <tr>
                <th>ID</th>
                <th>Data</th>
                <th>Tipo</th>
                <th>SKU</th>
                <th>Produto</th>
                <th>Documento</th>
                <th>Origem / Destino</th>
                <th className="num">Qtd</th>
                <th className="num">Valor unit.</th>
                <th>Usuário</th>
              </tr>
            </thead>
            <tbody>
              {filteredMoves.map((m) => (
                <tr key={m.id}>
                  <td className="id">{m.id}</td>
                  <td className="mono" style={{ color: "var(--ink-3)" }}>
                    {fmtDateTime(m.date)}
                  </td>
                  <td>
                    <MoveBadge type={m.type} />
                  </td>
                  <td className="id">{m.sku}</td>
                  <td>{m.product}</td>
                  <td className="id">{m.doc}</td>
                  <td style={{ color: "var(--ink-3)" }}>{m.origin}</td>
                  <td
                    className="num"
                    style={{ color: m.qty < 0 ? "var(--neg)" : "var(--ink-1)" }}
                  >
                    <b>
                      {m.qty > 0 ? "+" : ""}
                      {m.qty}
                    </b>
                  </td>
                  <td className="num">{fmtBRL(m.unit)}</td>
                  <td style={{ color: "var(--ink-3)" }}>{m.user}</td>
                </tr>
              ))}
            </tbody>
          </T>
        </TableWrap>
      )}

      {tab === "alertas" && (
        <Card>
          <CardHead>
            <div>
              <h3>Itens que requerem atenção</h3>
              <CardHeadSub>
                Abaixo do estoque mínimo configurado · ordenado por urgência
              </CardHeadSub>
            </div>
            <Button variant="ghost" size="xs">
              Sugerir compra
            </Button>
          </CardHead>
          <T>
            <thead>
              <tr>
                <th>Severidade</th>
                <th>SKU</th>
                <th>Produto</th>
                <th>Fornecedor</th>
                <th className="num">Estoque</th>
                <th className="num">Mínimo</th>
                <th className="num">Sugestão</th>
                <th className="num">Lead time</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {[...lowItems]
                .sort((a, b) => a.stock - b.stock)
                .map((p) => (
                  <tr key={p.sku}>
                    <td>
                      <Status variant={p.stock === 0 ? "neg" : "warn"}>
                        {p.stock === 0 ? "Crítico" : "Atenção"}
                      </Status>
                    </td>
                    <td className="id">{p.sku}</td>
                    <td>{p.name}</td>
                    <td style={{ color: "var(--ink-3)" }}>{p.supplier}</td>
                    <td className="num">
                      <b>{p.stock}</b>
                    </td>
                    <td className="num" style={{ color: "var(--ink-3)" }}>
                      {p.min}
                    </td>
                    <td className="num">
                      {p.max - p.stock} {p.uom}
                    </td>
                    <td className="num">7–14d</td>
                    <td>
                      <Button variant="ghost">Pedir compra</Button>
                    </td>
                  </tr>
                ))}
            </tbody>
          </T>
        </Card>
      )}

      {tab === "inventario" && (
        <ThreeCol>
          <Card>
            <CardHead>
              <h3>Inventário em andamento</h3>
            </CardHead>
            <CardBody>
              <div
                className="mono"
                style={{
                  fontSize: 10,
                  letterSpacing: "0.18em",
                  textTransform: "uppercase",
                  color: "var(--ink-3)",
                }}
              >
                INV-Q2-26
              </div>
              <h2
                style={{ margin: "4px 0 12px", fontSize: 18, fontWeight: 600 }}
              >
                Inventário cíclico — Armazém A
              </h2>
              <div
                style={{
                  fontSize: 12,
                  color: "var(--ink-3)",
                  marginBottom: 14,
                }}
              >
                Iniciado em 26/04 · responsável M. Almeida
              </div>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  fontSize: 12,
                  marginBottom: 6,
                }}
              >
                <span>Progresso</span>
                <span className="mono">
                  <b>184</b> / 240 SKUs
                </span>
              </div>
              <BarTrack>
                <BarFill variant="pos" style={{ width: "76%" }} />
              </BarTrack>
              <Button
                style={{
                  marginTop: 18,
                  width: "100%",
                  justifyContent: "center",
                }}
              >
                Continuar contagem
              </Button>
            </CardBody>
          </Card>

          <Card>
            <CardHead>
              <h3>Divergências encontradas</h3>
            </CardHead>
            <div style={{ padding: 0 }}>
              {[
                {
                  sku: "PFC-SOL-M8",
                  name: "Parafuso M8x30 inox",
                  sys: 88,
                  real: 86,
                },
                {
                  sku: "BRG-6204-Z",
                  name: "Rolamento 6204-2Z",
                  sys: 842,
                  real: 842,
                },
                {
                  sku: "CRR-TRP-9X",
                  name: "Correia A-90",
                  sys: 312,
                  real: 309,
                },
              ].map((d, i) => {
                const diff = d.real - d.sys;
                return (
                  <InvRow key={d.sku} $last={i === 2}>
                    <div>
                      <div
                        className="mono"
                        style={{ fontSize: 11, color: "var(--ink-3)" }}
                      >
                        {d.sku}
                      </div>
                      <div style={{ fontSize: 13 }}>{d.name}</div>
                    </div>
                    <div style={{ textAlign: "right" }}>
                      <div
                        className="mono"
                        style={{ fontSize: 11, color: "var(--ink-3)" }}
                      >
                        sis. {d.sys} · real {d.real}
                      </div>
                      <div
                        className="mono"
                        style={{
                          fontWeight: 600,
                          fontSize: 13,
                          color:
                            diff === 0
                              ? "var(--ink-3)"
                              : diff > 0
                                ? "var(--pos)"
                                : "var(--neg)",
                        }}
                      >
                        {diff === 0 ? "OK" : (diff > 0 ? "+" : "") + diff}
                      </div>
                    </div>
                  </InvRow>
                );
              })}
            </div>
          </Card>

          <Card>
            <CardHead>
              <h3>Próximas contagens</h3>
            </CardHead>
            <div style={{ padding: 0 }}>
              {[
                { d: "03/05", t: "Armazém B (rolamentos)", c: "Mensal" },
                { d: "10/05", t: "Externo EXT-01..04", c: "Trimestral" },
                { d: "15/05", t: "Armazém D (eletrônica)", c: "Mensal" },
              ].map((c, i) => (
                <ScheduleRow key={i} $last={i === 2}>
                  <DateBlock>
                    <div
                      className="mono"
                      style={{ fontSize: 14, fontWeight: 600 }}
                    >
                      {c.d.split("/")[0]}
                    </div>
                    <div
                      className="mono"
                      style={{
                        fontSize: 9,
                        color: "var(--ink-3)",
                        letterSpacing: "0.1em",
                      }}
                    >
                      MAI
                    </div>
                  </DateBlock>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 13 }}>{c.t}</div>
                    <div
                      style={{
                        fontSize: 11,
                        color: "var(--ink-3)",
                        marginTop: 2,
                      }}
                    >
                      {c.c}
                    </div>
                  </div>
                  <Icon name="chevron-right" size={12} />
                </ScheduleRow>
              ))}
            </div>
          </Card>
        </ThreeCol>
      )}
    </Page>
  );
}
