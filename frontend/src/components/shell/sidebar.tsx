import { useLocation, Link } from "@modern-js/runtime/router";
import styled from "@xstyled/emotion";
import { Icon } from "../icon";

const NAV = [
  {
    group: "Operação",
    items: [
      { path: "/", label: "Dashboard", icon: "dashboard" },
      { path: "/produtos", label: "Produtos", icon: "box", badge: "14" },
      { path: "/estoque", label: "Estoque", icon: "warehouse", badge: "3" },
    ],
  },
  {
    group: "Comercial",
    items: [
      { path: "/vendas", label: "Vendas", icon: "cart", badge: "8" },
      { path: "/vendas/novo", label: "Novo pedido", icon: "plus" },
    ],
  },
  {
    group: "Administrativo",
    items: [
      { path: "/financeiro", label: "Financeiro", icon: "wallet" },
      { path: "/producao", label: "Produção", icon: "factory" },
      { path: "/relatorios", label: "Relatórios", icon: "chart" },
    ],
  },
  {
    group: "Sistema",
    items: [
      { path: "/configuracoes", label: "Configurações", icon: "settings" },
    ],
  },
];

const Side = styled.aside`
  background: var(--side-bg);
  color: var(--side-ink);
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--side-line);
  overflow: hidden;
`;

const Brand = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 14px 14px;
  border-bottom: 1px solid var(--side-line);
  min-height: 56px;
`;

const Mark = styled.div`
  width: 26px;
  height: 26px;
  border-radius: 6px;
  background: var(--accent);
  position: relative;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  &::before {
    content: "";
    position: absolute;
    width: 14px;
    height: 4px;
    background: var(--accent);
    border-radius: 2px;
    top: -3px;
    left: 50%;
    transform: translateX(-50%);
  }
`;

const BrandText = styled.div`
  display: flex;
  flex-direction: column;
  line-height: 1.1;
  white-space: nowrap;
`;

const BrandName = styled.span`
  font-weight: 600;
  font-size: 14px;
  color: #f5f5f4;
  b {
    color: var(--accent-mid);
    font-weight: 700;
  }
`;

const BrandSub = styled.span`
  font-family: var(--font-mono);
  font-size: 9px;
  letter-spacing: 0.18em;
  color: var(--side-ink-dim);
  margin-top: 2px;
`;

const SearchWrap = styled.div`
  padding: 12px 12px 6px;
  position: relative;
  &::before {
    content: "";
    position: absolute;
    left: 22px;
    top: 50%;
    transform: translateY(-50%);
    width: 12px;
    height: 12px;
    background: no-repeat center / contain
      url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16' fill='none' stroke='%238a8784' stroke-width='1.5'><circle cx='7' cy='7' r='5'/><path d='M14 14l-3-3'/></svg>");
    pointer-events: none;
  }
`;

const SearchInput = styled.input`
  width: 100%;
  height: 30px;
  padding: 0 10px 0 30px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--side-line);
  border-radius: 6px;
  color: var(--side-ink);
  font-size: 12px;
  outline: none;
  transition: border-color 120ms;
  &::placeholder {
    color: var(--side-ink-dim);
  }
  &:focus {
    border-color: rgba(93, 202, 165, 0.4);
  }
`;

const Section = styled.div`
  font-family: var(--font-mono);
  font-size: 9px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--side-ink-dim);
  padding: 14px 16px 6px;
  white-space: nowrap;
`;

const Nav = styled.nav`
  padding: 4px 8px;
  flex: 1;
  overflow-y: auto;
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--side-line);
    border-radius: 3px;
  }
`;

const NavItem = styled.div<{ $active?: boolean; $collapsed?: boolean }>`
  display: flex;
  align-items: center;
  gap: 10px;
  padding: ${({ $collapsed }) => ($collapsed ? "8px" : "7px 10px")};
  justify-content: ${({ $collapsed }) =>
    $collapsed ? "center" : "flex-start"};
  border-radius: 6px;
  color: ${({ $active }) => ($active ? "#fff" : "var(--side-ink)")};
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  position: relative;
  transition:
    background 120ms,
    color 120ms;
  background: ${({ $active }) =>
    $active ? "rgba(255,255,255,0.07)" : "transparent"};
  &:hover {
    background: rgba(255, 255, 255, 0.05);
  }
  ${({ $active }) =>
    $active &&
    `
    &::before {
      content: "";
      position: absolute;
      left: -8px; top: 50%; transform: translateY(-50%);
      width: 3px; height: 16px;
      background: var(--accent-mid);
      border-radius: 0 2px 2px 0;
    }
  `}
`;

const NavIcon = styled.span<{ $active?: boolean }>`
  width: 16px;
  height: 16px;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  color: ${({ $active }) =>
    $active ? "var(--accent-mid)" : "var(--side-ink-dim)"};
`;

const NavBadge = styled.span`
  margin-left: auto;
  font-family: var(--font-mono);
  font-size: 10px;
  background: rgba(255, 255, 255, 0.06);
  color: var(--side-ink);
  padding: 1px 6px;
  border-radius: 8px;
  min-width: 18px;
  text-align: center;
`;

const Foot = styled.div<{ $collapsed?: boolean }>`
  border-top: 1px solid var(--side-line);
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: ${({ $collapsed }) =>
    $collapsed ? "center" : "flex-start"};
`;

const Avatar = styled.div`
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4a4845, #2a2826);
  display: grid;
  place-items: center;
  font-size: 11px;
  color: #ededeb;
  font-weight: 600;
  flex-shrink: 0;
`;

const FootWho = styled.div`
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
`;

const FootName = styled.span`
  font-size: 12px;
  color: #f5f5f4;
`;

const FootRole = styled.span`
  font-size: 10px;
  color: var(--side-ink-dim);
`;

interface SidebarProps {
  collapsed: boolean;
}

export function Sidebar({ collapsed }: SidebarProps) {
  const location = useLocation();

  const isActive = (path: string) => {
    if (path === "/") return location.pathname === "/";
    return location.pathname.startsWith(path);
  };

  return (
    <Side>
      <Brand>
        <Mark aria-hidden="true">
          <span
            style={{
              position: "absolute",
              inset: 0,
              display: "grid",
              placeItems: "center",
            }}
          >
            <svg width="14" height="16" viewBox="0 0 14 16" fill="none">
              <rect x="3" y="0" width="8" height="2" rx="0.5" fill="#0F6E56" />
              <rect
                x="0"
                y="3"
                width="14"
                height="13"
                rx="1.5"
                fill="#0F6E56"
              />
              <rect
                x="1.5"
                y="4.5"
                width="11"
                height="10"
                rx="0.8"
                fill="#E1F5EE"
              />
              <circle cx="4.5" cy="8" r="0.9" fill="#5DCAA5" />
              <circle cx="8" cy="6.8" r="0.6" fill="#5DCAA5" opacity="0.7" />
              <rect
                x="2.5"
                y="10"
                width="9"
                height="3"
                rx="0.4"
                fill="#5DCAA5"
                opacity="0.45"
              />
            </svg>
          </span>
        </Mark>
        {!collapsed && (
          <BrandText>
            <BrandName>
              Open<b>Lab</b> ERP
            </BrandName>
            <BrandSub>INDÚSTRIA · 04.26</BrandSub>
          </BrandText>
        )}
      </Brand>

      {!collapsed && (
        <SearchWrap>
          <SearchInput placeholder="Buscar… ⌘K" aria-label="Buscar" />
        </SearchWrap>
      )}

      <Nav>
        {NAV.map(({ group, items }) => (
          <div key={group}>
            {!collapsed && <Section>{group}</Section>}
            {items.map((item) => {
              const active = isActive(item.path);
              return (
                <Link to={item.path} key={item.path}>
                  <NavItem
                    $active={active}
                    $collapsed={collapsed}
                    title={collapsed ? item.label : undefined}
                  >
                    <NavIcon $active={active}>
                      <Icon name={item.icon} />
                    </NavIcon>
                    {!collapsed && <span>{item.label}</span>}
                    {!collapsed && item.badge && (
                      <NavBadge>{item.badge}</NavBadge>
                    )}
                  </NavItem>
                </Link>
              );
            })}
          </div>
        ))}
      </Nav>

      <Foot $collapsed={collapsed}>
        <Avatar>RC</Avatar>
        {!collapsed && (
          <FootWho>
            <FootName>Renato Costa</FootName>
            <FootRole>Coord. Operações</FootRole>
          </FootWho>
        )}
      </Foot>
    </Side>
  );
}
