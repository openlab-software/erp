import { Card, Empty, Page, PageHead, SectionLabel } from "@/components/ui";
import type { ReactNode } from "react";
import { useModules } from "../queries";

interface ModuleGateProps {
  /** Name in modules/<name>/module.json. */
  module: string;
  children: ReactNode;
}

/**
 * Renders the page only when its module is installed. A deployment can ship a subset of the modules, and a
 * bookmarked URL of a module that is not there should explain it instead of failing on every request.
 */
export function ModuleGate({ module, children }: ModuleGateProps) {
  const modules = useModules();

  if (modules.isPending) {
    return (
      <Page>
        <Empty>Carregando…</Empty>
      </Page>
    );
  }
  if (modules.data?.some((m) => m.name === module)) {
    return <>{children}</>;
  }
  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>MÓDULO NÃO INSTALADO</SectionLabel>
          <h1>Este módulo não faz parte desta instalação</h1>
        </div>
      </PageHead>
      <Card style={{ padding: 32, color: "var(--ink-3)", fontSize: 14 }}>
        O módulo <b className="mono">{module}</b> não está habilitado neste ambiente. Peça a quem administra a
        instalação para ligá-lo em <span className="mono">modules.{module}.enabled</span>.
      </Card>
    </Page>
  );
}
