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
  Tab,
  TableToolbar,
  TableWrap,
  Tabs,
} from "@/components/ui";
import { Button } from "@openlab-ui/react";
import styled from "@xstyled/emotion";
import { useMemo, useState } from "react";
import { FINANCE, STATUS_LABELS, fmtBRL, fmtDate } from "../../data";

const TypeBadge = styled.span<{ $recv?: boolean }>`
  font-size: 11.5px;
  padding: 2px 8px;
  border-radius: 4px;
  background: ${({ $recv }) =>
    $recv ? "var(--pos-soft)" : "var(--surface-2)"};
  color: ${({ $recv }) => ($recv ? "var(--pos)" : "var(--ink-3)")};
  font-weight: 500;
`;

export default function Financeiro() {
  const [tab, setTab] = useState("todos");
  const [search, setSearch] = useState("");

  const filtered = useMemo(
    () =>
      FINANCE.filter((f) => {
        if (tab === "pagar" && f.type !== "pagar") return false;
        if (tab === "receber" && f.type !== "receber") return false;
        if (
          search &&
          !`${f.id} ${f.party} ${f.doc}`
            .toLowerCase()
            .includes(search.toLowerCase())
        )
          return false;
        return true;
      }),
    [tab, search],
  );

  const aReceber = FINANCE.filter(
    (f) => f.type === "receber" && f.status === "aberto",
  ).reduce((s, f) => s + f.amount, 0);
  const aPagar = FINANCE.filter(
    (f) => f.type === "pagar" && f.status === "aberto",
  ).reduce((s, f) => s + f.amount, 0);
  const atrasado = FINANCE.filter((f) => f.status === "atrasado").reduce(
    (s, f) => s + f.amount,
    0,
  );
  const saldo = aReceber - aPagar;

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>ADMINISTRATIVO · FINANCEIRO</SectionLabel>
          <h1>Contas a pagar &amp; receber</h1>
          <Subtitle>
            Posição em <span className="mono">28/04/2026</span>
          </Subtitle>
        </div>
        <PageActions>
          <Button variant="ghost">
            <Icon name="download" /> Conciliar
          </Button>
          <Button>
            <Icon name="plus" /> Novo lançamento
          </Button>
        </PageActions>
      </PageHead>

      <StatGrid>
        <Stat>
          <StatLabel>A receber (em aberto)</StatLabel>
          <StatValue>{fmtBRL(aReceber)}</StatValue>
          <StatDelta $pos>
            <Icon name="arrow-up" size={11} /> 3 títulos
          </StatDelta>
        </Stat>
        <Stat>
          <StatLabel>A pagar (em aberto)</StatLabel>
          <StatValue>{fmtBRL(aPagar)}</StatValue>
          <StatDelta>3 títulos · próx. 29/04</StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Em atraso</StatLabel>
          <StatValue>{fmtBRL(atrasado)}</StatValue>
          <StatDelta $neg>
            <Icon name="arrow-down" size={11} /> 1 título · 6 dias
          </StatDelta>
        </Stat>
        <Stat>
          <StatLabel>Saldo projetado (30d)</StatLabel>
          <StatValue
            style={{ color: saldo >= 0 ? "var(--ink-1)" : "var(--neg)" }}
          >
            {fmtBRL(saldo)}
          </StatValue>
          <StatDelta>após compromissos firmes</StatDelta>
        </Stat>
      </StatGrid>

      <div style={{ height: 24 }} />

      <Tabs>
        {[
          ["todos", "Todos"],
          ["receber", "A receber"],
          ["pagar", "A pagar"],
        ].map(([id, l]) => (
          <Tab key={id} $active={tab === id} onClick={() => setTab(id)}>
            {l}
          </Tab>
        ))}
      </Tabs>

      <TableWrap>
        <TableToolbar>
          <SearchWrap>
            <Icon name="search" size={13} />
            <FInput
              placeholder="Buscar título, parte ou documento…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{ width: 320 }}
            />
          </SearchWrap>
          <div style={{ flex: 1 }} />
          <Button variant="ghost" size="xs">
            <Icon name="calendar" size={12} /> Abr 2026
          </Button>
          <Button variant="ghost" size="xs">
            <Icon name="filter" size={12} /> Filtros
          </Button>
        </TableToolbar>

        <T>
          <thead>
            <tr>
              <th>ID</th>
              <th>Tipo</th>
              <th>Vencimento</th>
              <th>Parte</th>
              <th>Categoria</th>
              <th>Documento</th>
              <th className="num">Valor</th>
              <th>Status</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {filtered.map((f) => {
              const s = STATUS_LABELS[f.status];
              return (
                <tr key={f.id}>
                  <td className="id">{f.id}</td>
                  <td>
                    <TypeBadge $recv={f.type === "receber"}>
                      {f.type === "receber" ? "Receber" : "Pagar"}
                    </TypeBadge>
                  </td>
                  <td className="mono" style={{ color: "var(--ink-3)" }}>
                    {fmtDate(f.date)}
                  </td>
                  <td>
                    <b>{f.party}</b>
                  </td>
                  <td style={{ color: "var(--ink-3)" }}>{f.category}</td>
                  <td className="id">{f.doc}</td>
                  <td
                    className="num"
                    style={{
                      color:
                        f.type === "receber" ? "var(--pos)" : "var(--ink-1)",
                    }}
                  >
                    <b>
                      {f.type === "receber" ? "+" : "−"} {fmtBRL(f.amount)}
                    </b>
                  </td>
                  <td>
                    <Status variant={s.cls as any}>{s.label}</Status>
                  </td>
                  <td>
                    {f.status !== "pago" && (
                      <Button variant="ghost" size="xs">
                        {f.type === "receber" ? "Baixar" : "Pagar"}
                      </Button>
                    )}
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
