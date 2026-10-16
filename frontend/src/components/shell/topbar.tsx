import { useLocation } from "@modern-js/runtime/router";
import styled from "@xstyled/emotion";
import { Icon } from "../icon";
import { useToast } from "@/contexts/toast-context";
import { useModules } from "@/features/modules";
import type { ErpModule } from "@/features/modules";

/** Pages that are not in a module's menu. */
const STATIC_CRUMBS: Record<string, string[]> = {
  "/": ["Operação", "Dashboard"],
  "/configuracoes": ["Sistema", "Configurações"],
};

/**
 * "Group > Page" of the current URL: the menu item (from the installed modules' manifests) whose path is the
 * longest prefix of it, plus "Detalhe" / "Novo" for pages below that item.
 */
function crumbsFor(pathname: string, modules: ErpModule[]): string[] {
  if (STATIC_CRUMBS[pathname]) return STATIC_CRUMBS[pathname];
  const items = modules.flatMap((m) => m.menu).sort((a, b) => b.path.length - a.path.length);
  const item = items.find((i) => pathname === i.path || pathname.startsWith(`${i.path}/`));
  if (!item) return ["—"];
  if (pathname === item.path) return [item.group, item.label];
  return [item.group, item.label, pathname.endsWith("/novo") ? "Novo" : "Detalhe"];
}

const TopbarWrap = styled.header`
  height: 56px;
  border-bottom: 1px solid var(--line);
  background: var(--surface);
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 16px;
  flex-shrink: 0;
`;

const Toggle = styled.button`
  width: 30px;
  height: 30px;
  border: 1px solid var(--line);
  background: var(--surface);
  border-radius: var(--radius);
  display: grid;
  place-items: center;
  color: var(--ink-2);
  &:hover {
    background: var(--surface-sunken);
  }
`;

const Crumbs = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--ink-3);
`;

const Sep = styled.span`
  color: var(--ink-4);
`;

const Here = styled.span`
  color: var(--ink-1);
  font-weight: 500;
`;

const Spacer = styled.div`
  flex: 1;
`;

const EnvPill = styled.span`
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 0.1em;
  color: var(--ink-3);
  padding: 3px 8px;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: var(--surface-2);
`;

const TbAction = styled.button`
  height: 30px;
  padding: 0 10px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius);
  color: var(--ink-2);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  position: relative;
  &:hover {
    background: var(--surface-sunken);
    border-color: var(--line);
  }
`;

const NotifDot = styled.span`
  position: absolute;
  top: 6px;
  right: 8px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
`;

interface TopbarProps {
  onToggleSide: () => void;
}

export function Topbar({ onToggleSide }: TopbarProps) {
  const location = useLocation();
  const { showToast } = useToast();

  const pathname = location.pathname;

  const modules = useModules();
  const crumbs = crumbsFor(pathname, modules.data ?? []);

  return (
    <TopbarWrap>
      <Toggle onClick={onToggleSide} aria-label="Alternar menu">
        <Icon name="menu" />
      </Toggle>

      <Crumbs>
        {crumbs.map((c, i) => (
          <span key={i} style={{ display: "contents" }}>
            {i > 0 && (
              <Sep>
                <Icon name="chevron-right" size={11} />
              </Sep>
            )}
            {i === crumbs.length - 1 ? <Here>{c}</Here> : <span>{c}</span>}
          </span>
        ))}
      </Crumbs>

      <Spacer />

      <EnvPill>PROD · UF-MG · 28 ABR</EnvPill>

      <TbAction onClick={() => showToast("Busca rápida (⌘K) — em mock")}>
        <Icon name="search" />
        <span style={{ color: "var(--ink-4)" }}>⌘K</span>
      </TbAction>

      <TbAction aria-label="Notificações">
        <Icon name="bell" />
        <NotifDot />
      </TbAction>
    </TopbarWrap>
  );
}
