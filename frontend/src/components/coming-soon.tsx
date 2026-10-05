import { Page, PageHead, SectionLabel, Card, ProdThumb } from './ui'

interface ComingSoonProps {
  title: string
}

export function ComingSoon({ title }: ComingSoonProps) {
  return (
    <Page>
      <PageHead>
        <div>
          <SectionLabel>EM CONSTRUÇÃO</SectionLabel>
          <h1>{title}</h1>
          <div style={{ fontSize: 13, color: 'var(--ink-3)', marginTop: 4 }}>
            Este módulo ainda não está implementado nesta versão.
          </div>
        </div>
      </PageHead>
      <Card style={{ padding: 60, textAlign: 'center' }}>
        <ProdThumb style={{ width: 48, height: 48, margin: '0 auto 16px' }} />
        <div style={{ color: 'var(--ink-3)', fontSize: 14 }}>
          Foco atual: Dashboard, Produtos, Estoque, Vendas e Financeiro.
        </div>
      </Card>
    </Page>
  )
}
