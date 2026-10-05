import { Icon } from "../icon";
import { Stat, StatDelta, StatGrid, StatLabel, StatValue } from "../ui";

const stats = [
  {
    label: "Receita do mês",
    value: "R$ 1.284.920",
    delta: "+8,2% vs. mar/26",
    pos: true,
  },
  {
    label: "Pedidos abertos",
    value: "47",
    delta: "12 em produção · 8 expedição",
    pos: null,
  },
  {
    label: "Itens em estoque baixo",
    value: "3",
    delta: "ação requerida",
    pos: false,
  },
  {
    label: "Contas a receber (30d)",
    value: "R$ 348.7k",
    delta: "5 vencendo esta semana",
    pos: null,
  },
];

export function Stats() {
  return (
    <StatGrid>
      {stats.map((s, i) => (
        <Stat key={i}>
          <StatLabel>{s.label}</StatLabel>
          <StatValue>{s.value}</StatValue>
          <StatDelta $pos={s.pos === true} $neg={s.pos === false}>
            {s.pos === true && <Icon name="arrow-up" size={11} />}
            {s.pos === false && <Icon name="arrow-down" size={11} />}
            {s.delta}
          </StatDelta>
        </Stat>
      ))}
    </StatGrid>
  );
}
