import { Icon } from "@/components/icon";
import {
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
  TableToolbar,
  TableWrap,
} from "@/components/ui";
import { Link, useNavigate } from "@modern-js/runtime/router";
import { Button } from "@openlab-ui/react";
import { useMemo, useState } from "react";
import { ORDERS, STATUS_LABELS, fmtBRL, fmtDate } from "../../data";

export default function Vendas() {
  const navigate = useNavigate();
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("todos");

  const filtered = useMemo(() => {
    return ORDERS.filter((o) => {
      if (status !== "todos" && o.status !== status) return false;
      if (
        search &&
        !`${o.id} ${o.customer} ${o.seller}`
          .toLowerCase()
          .includes(search.toLowerCase())
      )
        return false;
      return true;
    });
  }, [search, status]);

  const totalMonth = ORDERS.filter((o) => o.status !== "cancelado").reduce(
    (s, o) => s + o.total,
    0,
  );

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>COMERCIAL · VENDAS</SectionLabel>
          <h1>Pedidos</h1>
          <Subtitle>
            <span className="mono">{ORDERS.length}</span> pedidos em abril ·{" "}
            <span className="mono">{fmtBRL(totalMonth)}</span> faturados
          </Subtitle>
        </div>
        <PageActions>
          <Button variant="ghost">
            <Icon name="download" /> Exportar
          </Button>
          <Link to={"/vendas/novo"}>
            <Button>
              <Icon name="plus" /> Novo pedido
            </Button>
          </Link>
        </PageActions>
      </PageHead>

      <StatGrid>
        <Stat>
          <StatLabel>Pedidos abertos</StatLabel>
          <StatValue>
            {
              ORDERS.filter((o) =>
                ["producao", "faturado", "expedicao"].includes(o.status),
              ).length
            }
          </StatValue>
          <StatDelta>de {ORDERS.length} no mês</StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Ticket médio</StatLabel>
          <StatValue>
            {fmtBRL(
              totalMonth /
                ORDERS.filter((o) => o.status !== "cancelado").length,
            )}
          </StatValue>
          <StatDelta $pos>
            <Icon name="arrow-up" size={11} /> +4,8%
          </StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Lead time médio</StatLabel>
          <StatValue>
            8,4<span className="unit">dias</span>
          </StatValue>
          <StatDelta>cotação → expedição</StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Taxa de cancelamento</StatLabel>
          <StatValue>
            2,1<span className="unit">%</span>
          </StatValue>
          <StatDelta>1 cancelado em abril</StatDelta>
        </Stat>
      </StatGrid>

      <div style={{ height: 24 }} />

      <TableWrap>
        <TableToolbar>
          <SearchWrap>
            <Icon name="search" size={13} />
            <FInput
              placeholder="Buscar pedido, cliente ou vendedor…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{ width: 320 }}
            />
          </SearchWrap>
          <div style={{ display: "flex", gap: 6, marginLeft: 8 }}>
            {[
              ["todos", "Todos"],
              ["producao", "Produção"],
              ["expedicao", "Expedição"],
              ["faturado", "Faturado"],
              ["concluido", "Concluídos"],
              ["cancelado", "Cancelados"],
            ].map(([id, l]) => (
              <Button
                size="xs"
                variant={status === id ? "default" : "ghost"}
                key={id}
                onClick={() => setStatus(id)}
              >
                {l}
              </Button>
            ))}
          </div>
        </TableToolbar>

        <T>
          <thead>
            <tr>
              <th>Pedido</th>
              <th>Data</th>
              <th>Cliente</th>
              <th>Canal</th>
              <th>Vendedor</th>
              <th className="num">Itens</th>
              <th className="num">Total</th>
              <th>Status</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {filtered.map((o) => {
              const s = STATUS_LABELS[o.status];
              return (
                <tr key={o.id}>
                  <td className="id">{o.id}</td>
                  <td className="mono" style={{ color: "var(--ink-3)" }}>
                    {fmtDate(o.date)}
                  </td>
                  <td>
                    <b>{o.customer}</b>
                  </td>
                  <td style={{ color: "var(--ink-3)" }}>{o.channel}</td>
                  <td style={{ color: "var(--ink-3)" }}>{o.seller}</td>
                  <td className="num">{o.items}</td>
                  <td className="num">
                    <b>{fmtBRL(o.total)}</b>
                  </td>
                  <td>
                    <Status variant={s.cls as any}>{s.label}</Status>
                  </td>
                  <td>
                    <div className="row-actions">
                      <Button variant="ghost" size="icon">
                        <Icon name="more" size={14} />
                      </Button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </T>
      </TableWrap>
    </Page>
  );
}
