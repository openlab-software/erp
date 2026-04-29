import { Icon } from "@/components/icon";
import {
  FInput,
  Page,
  PageActions,
  PageHead,
  ProdThumb,
  SearchWrap,
  SectionLabel,
  Status,
  Subtitle,
  T,
  TableFooter,
  TableToolbar,
  TableWrap,
} from "@/components/ui";
import { Link, useNavigate } from "@modern-js/runtime/router";
import { Button } from "@openlab-ui/react";
import { useMemo, useState } from "react";
import { PRODUCTS, fmtBRL, fmtNum } from "../../data";

export default function Produtos() {
  const navigate = useNavigate();
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("Todas");
  const [selected, setSelected] = useState<Set<string>>(new Set());

  const categories = useMemo(
    () => ["Todas", ...Array.from(new Set(PRODUCTS.map((p) => p.category)))],
    [],
  );

  const filtered = useMemo(() => {
    return PRODUCTS.filter((p) => {
      if (category !== "Todas" && p.category !== category) return false;
      if (
        search &&
        !`${p.sku} ${p.name} ${p.supplier}`
          .toLowerCase()
          .includes(search.toLowerCase())
      )
        return false;
      return true;
    });
  }, [search, category]);

  const toggleAll = () => {
    if (selected.size === filtered.length) setSelected(new Set());
    else setSelected(new Set(filtered.map((p) => p.sku)));
  };
  const toggle = (sku: string) => {
    const n = new Set(selected);
    n.has(sku) ? n.delete(sku) : n.add(sku);
    setSelected(n);
  };

  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>CADASTRO · PRODUTOS</SectionLabel>
          <h1>Produtos</h1>
          <Subtitle>
            <span className="mono">{PRODUCTS.length}</span> produtos cadastrados
            ·{" "}
            <span className="mono">
              {PRODUCTS.filter((p) => p.stock < p.min).length}
            </span>{" "}
            abaixo do mínimo
          </Subtitle>
        </div>
        <PageActions>
          <Button variant="ghost">
            <Icon name="upload" /> Importar
          </Button>
          <Button variant="ghost">
            <Icon name="download" /> Exportar
          </Button>
          <Button>
            <Icon name="plus" /> Novo produto
          </Button>
        </PageActions>
      </PageHead>

      <TableWrap>
        <TableToolbar>
          <SearchWrap>
            <Icon name="search" size={13} />
            <FInput
              placeholder="Buscar por SKU, nome ou fornecedor…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{ width: 320 }}
            />
          </SearchWrap>
          <div style={{ display: "flex", gap: 6, marginLeft: 8 }}>
            {categories.slice(0, 6).map((c) => (
              <Button
                size="xs"
                key={c}
                variant={c === category ? "default" : "ghost"}
                onClick={() => setCategory(c)}
              >
                {c}
              </Button>
            ))}
          </div>
          <div style={{ flex: 1 }} />
          {selected.size > 0 && (
            <span
              style={{ fontSize: 12, color: "var(--ink-3)", marginRight: 8 }}
            >
              {selected.size} selecionado{selected.size > 1 ? "s" : ""}
            </span>
          )}
          <Button variant="ghost" size="xs">
            <Icon name="filter" size={12} /> Filtros
          </Button>
          <Button variant="ghost" size="xs">
            <Icon name="more" size={14} />
          </Button>
        </TableToolbar>

        <T>
          <thead>
            <tr>
              <th className="ck">
                <input
                  type="checkbox"
                  checked={
                    selected.size === filtered.length && filtered.length > 0
                  }
                  onChange={toggleAll}
                />
              </th>
              <th />
              <th>SKU</th>
              <th>Produto</th>
              <th>Categoria</th>
              <th>Fornecedor</th>
              <th className="num">Custo</th>
              <th className="num">Preço</th>
              <th className="num">Estoque</th>
              <th>Status</th>
              <th style={{ width: 60 }} />
            </tr>
          </thead>
          <tbody>
            {filtered.map((p) => {
              const out = p.stock === 0;
              const low = p.stock < p.min;
              const variant = out ? "neg" : low ? "warn" : "pos";
              const label = out ? "Ruptura" : low ? "Baixo" : "OK";
              return (
                <tr
                  key={p.sku}
                  className={selected.has(p.sku) ? "selected" : ""}
                >
                  <td className="ck">
                    <input
                      type="checkbox"
                      checked={selected.has(p.sku)}
                      onChange={() => toggle(p.sku)}
                    />
                  </td>
                  <td>
                    <ProdThumb />
                  </td>
                  <td className="id">{p.sku}</td>
                  <td>
                    <a
                      onClick={() => navigate(`/produtos/${p.sku}`)}
                      style={{
                        cursor: "pointer",
                        color: "var(--ink-1)",
                        fontWeight: 500,
                      }}
                    >
                      {p.name}
                    </a>
                  </td>
                  <td style={{ color: "var(--ink-3)" }}>{p.category}</td>
                  <td style={{ color: "var(--ink-3)" }}>{p.supplier}</td>
                  <td className="num">{fmtBRL(p.cost)}</td>
                  <td className="num">
                    {p.price ? (
                      fmtBRL(p.price)
                    ) : (
                      <span style={{ color: "var(--ink-3)" }}>—</span>
                    )}
                  </td>
                  <td className="num">
                    <b>{fmtNum(p.stock)}</b>{" "}
                    <span style={{ color: "var(--ink-3)" }}>{p.uom}</span>
                  </td>
                  <td>
                    <Status variant={variant as any}>{label}</Status>
                  </td>
                  <td>
                    <div className="row-actions">
                      <Link to={`/produtos/${p.sku}`}>
                        <Button variant="ghost" size="icon">
                          <Icon name="external" size={13} />
                        </Button>
                      </Link>
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

        <TableFooter>
          <span>
            Mostrando <b style={{ color: "var(--ink-1)" }}>{filtered.length}</b>{" "}
            de {PRODUCTS.length}
          </span>
          <div style={{ display: "flex", gap: 6 }}>
            <Button variant="ghost" size="xs" disabled>
              ‹ Anterior
            </Button>
            <Button>1</Button>
            <Button variant="ghost">2</Button>
            <Button variant="ghost">Próximo ›</Button>
          </div>
        </TableFooter>
      </TableWrap>
    </Page>
  );
}
