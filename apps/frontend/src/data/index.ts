export interface Product {
  sku: string
  name: string
  category: string
  uom: string
  cost: number
  price: number
  stock: number
  min: number
  max: number
  status: string
  supplier: string
  weight: number
  location: string
}

export interface Movement {
  id: string
  date: string
  type: 'entrada' | 'saida' | 'ajuste' | 'transferencia'
  sku: string
  product: string
  qty: number
  unit: number
  doc: string
  origin: string
  user: string
}

export interface Order {
  id: string
  date: string
  customer: string
  items: number
  total: number
  status: string
  channel: string
  seller: string
}

export interface FinanceItem {
  id: string
  type: 'pagar' | 'receber'
  date: string
  party: string
  doc: string
  amount: number
  status: string
  category: string
}

export interface StatusInfo {
  label: string
  cls: string
}

export const PRODUCTS: Product[] = [
  { sku: 'MTR-0420-A', name: 'Motor de indução trifásico 5cv', category: 'Motores', uom: 'un', cost: 1284.50, price: 1899.00, stock: 24, min: 10, max: 60, status: 'ativo', supplier: 'WEG Componentes', weight: 38.5, location: 'A-12-03' },
  { sku: 'BRG-6204-Z', name: 'Rolamento blindado 6204-2Z', category: 'Rolamentos', uom: 'un', cost: 12.40, price: 24.90, stock: 842, min: 200, max: 2000, status: 'ativo', supplier: 'SKF do Brasil', weight: 0.105, location: 'B-04-12' },
  { sku: 'VLV-PNEU-12', name: 'Válvula pneumática 5/2 vias 1/2"', category: 'Pneumática', uom: 'un', cost: 186.20, price: 312.00, stock: 8, min: 12, max: 40, status: 'ativo', supplier: 'Festo Brasil', weight: 0.42, location: 'C-07-01' },
  { sku: 'CHP-INX-304', name: 'Chapa Inox 304 1.2mm 1000x2000', category: 'Matéria-prima', uom: 'chapa', cost: 412.00, price: 0, stock: 64, min: 20, max: 120, status: 'ativo', supplier: 'Aperam', weight: 19.2, location: 'EXT-01' },
  { sku: 'PRS-HID-001', name: 'Cilindro hidráulico 80x250mm', category: 'Hidráulica', uom: 'un', cost: 645.00, price: 1120.00, stock: 0, min: 4, max: 18, status: 'ativo', supplier: 'Parker Hannifin', weight: 12.8, location: 'C-02-04' },
  { sku: 'SEN-IND-M18', name: 'Sensor indutivo M18 PNP NA', category: 'Eletrônica', uom: 'un', cost: 78.30, price: 145.00, stock: 156, min: 30, max: 200, status: 'ativo', supplier: 'Sick Sensors', weight: 0.085, location: 'D-01-08' },
  { sku: 'CNT-CIR-25A', name: 'Contator tripolar 25A 220V', category: 'Elétrica', uom: 'un', cost: 92.10, price: 168.00, stock: 47, min: 20, max: 100, status: 'ativo', supplier: 'Schneider', weight: 0.62, location: 'D-03-15' },
  { sku: 'CRR-TRP-9X', name: 'Correia trapezoidal A-90', category: 'Transmissão', uom: 'un', cost: 18.40, price: 36.00, stock: 312, min: 80, max: 500, status: 'ativo', supplier: 'Optibelt', weight: 0.18, location: 'B-08-22' },
  { sku: 'RDC-COX-30', name: 'Redutor coaxial 1:30 0,5cv', category: 'Transmissão', uom: 'un', cost: 1850.00, price: 2780.00, stock: 6, min: 4, max: 16, status: 'ativo', supplier: 'SEW Eurodrive', weight: 22.0, location: 'A-06-02' },
  { sku: 'CBL-FLX-25', name: 'Cabo flexível 2,5mm² 1kV (rolo)', category: 'Elétrica', uom: 'rolo', cost: 348.00, price: 512.00, stock: 18, min: 8, max: 40, status: 'ativo', supplier: 'Prysmian', weight: 24.5, location: 'EXT-04' },
  { sku: 'PRF-AL-40X40', name: 'Perfil alumínio 40x40 (6m)', category: 'Matéria-prima', uom: 'barra', cost: 96.00, price: 0, stock: 144, min: 40, max: 220, status: 'ativo', supplier: 'Item Brasil', weight: 9.6, location: 'EXT-02' },
  { sku: 'PFC-SOL-M8', name: 'Parafuso sextavado M8x30 inox', category: 'Fixação', uom: 'kg', cost: 28.00, price: 0, stock: 86, min: 30, max: 200, status: 'ativo', supplier: 'Ciser', weight: 1.0, location: 'B-12-30' },
  { sku: 'DSP-PROG-V2', name: 'Display programável 7" colorido', category: 'Eletrônica', uom: 'un', cost: 1240.00, price: 2150.00, stock: 3, min: 6, max: 20, status: 'baixo', supplier: 'Weintek', weight: 1.4, location: 'D-02-01' },
  { sku: 'MNG-FLG-50', name: 'Mangueira flexível DN50 1m', category: 'Hidráulica', uom: 'm', cost: 64.00, price: 112.00, stock: 280, min: 60, max: 400, status: 'ativo', supplier: 'Kanaflex', weight: 1.8, location: 'C-05-10' },
]

export const MOVEMENTS: Movement[] = [
  { id: 'MV-2826', date: '2026-04-28T09:14', type: 'entrada', sku: 'BRG-6204-Z', product: 'Rolamento blindado 6204-2Z', qty: 500, unit: 12.40, doc: 'NF-1842', origin: 'SKF do Brasil', user: 'L. Pereira' },
  { id: 'MV-2825', date: '2026-04-28T08:42', type: 'saida', sku: 'MTR-0420-A', product: 'Motor de indução trifásico 5cv', qty: 2, unit: 1284.50, doc: 'OP-0915', origin: 'Linha Montagem 02', user: 'R. Costa' },
  { id: 'MV-2824', date: '2026-04-28T08:10', type: 'saida', sku: 'CRR-TRP-9X', product: 'Correia trapezoidal A-90', qty: 12, unit: 18.40, doc: 'OP-0914', origin: 'Linha Montagem 01', user: 'R. Costa' },
  { id: 'MV-2823', date: '2026-04-27T17:38', type: 'ajuste', sku: 'PFC-SOL-M8', product: 'Parafuso sextavado M8x30 inox', qty: -2, unit: 28.00, doc: 'INV-Q1-A', origin: 'Inventário cíclico', user: 'M. Almeida' },
  { id: 'MV-2822', date: '2026-04-27T16:02', type: 'entrada', sku: 'CHP-INX-304', product: 'Chapa Inox 304 1.2mm 1000x2000', qty: 24, unit: 412.00, doc: 'NF-1841', origin: 'Aperam', user: 'L. Pereira' },
  { id: 'MV-2821', date: '2026-04-27T14:51', type: 'saida', sku: 'VLV-PNEU-12', product: 'Válvula pneumática 5/2 vias 1/2"', qty: 4, unit: 186.20, doc: 'OP-0913', origin: 'Linha Pneumática', user: 'R. Costa' },
  { id: 'MV-2820', date: '2026-04-27T11:20', type: 'transferencia', sku: 'SEN-IND-M18', product: 'Sensor indutivo M18 PNP NA', qty: 20, unit: 78.30, doc: 'TR-0142', origin: 'CD Central → Filial Sul', user: 'J. Tavares' },
  { id: 'MV-2819', date: '2026-04-27T10:08', type: 'saida', sku: 'PRS-HID-001', product: 'Cilindro hidráulico 80x250mm', qty: 4, unit: 645.00, doc: 'OP-0912', origin: 'Linha Hidráulica', user: 'R. Costa' },
  { id: 'MV-2818', date: '2026-04-26T15:45', type: 'entrada', sku: 'RDC-COX-30', product: 'Redutor coaxial 1:30 0,5cv', qty: 6, unit: 1850.00, doc: 'NF-1840', origin: 'SEW Eurodrive', user: 'L. Pereira' },
  { id: 'MV-2817', date: '2026-04-26T13:12', type: 'saida', sku: 'CNT-CIR-25A', product: 'Contator tripolar 25A 220V', qty: 8, unit: 92.10, doc: 'OP-0911', origin: 'Linha Elétrica', user: 'R. Costa' },
]

export const ORDERS: Order[] = [
  { id: 'PED-3014', date: '2026-04-28', customer: 'Metalúrgica Andrade Ltda', items: 4, total: 18420.00, status: 'producao', channel: 'Direto', seller: 'C. Mendes' },
  { id: 'PED-3013', date: '2026-04-28', customer: 'Engenharia Sul SA', items: 2, total: 5598.00, status: 'faturado', channel: 'Representante', seller: 'P. Lima' },
  { id: 'PED-3012', date: '2026-04-27', customer: 'Indústria Beta Componentes', items: 7, total: 32104.50, status: 'expedicao', channel: 'Direto', seller: 'C. Mendes' },
  { id: 'PED-3011', date: '2026-04-27', customer: 'Automação Norte Eireli', items: 1, total: 2780.00, status: 'concluido', channel: 'Online', seller: '—' },
  { id: 'PED-3010', date: '2026-04-26', customer: 'Refinaria Oeste SA', items: 12, total: 84992.00, status: 'concluido', channel: 'Direto', seller: 'P. Lima' },
  { id: 'PED-3009', date: '2026-04-26', customer: 'Fábrica Aurora Equipamentos', items: 3, total: 7340.00, status: 'cancelado', channel: 'Representante', seller: 'M. Souza' },
  { id: 'PED-3008', date: '2026-04-25', customer: 'Mineradora Caeté', items: 5, total: 14580.00, status: 'faturado', channel: 'Direto', seller: 'C. Mendes' },
  { id: 'PED-3007', date: '2026-04-24', customer: 'Metalúrgica Andrade Ltda', items: 2, total: 3798.00, status: 'concluido', channel: 'Direto', seller: 'C. Mendes' },
]

export const FINANCE: FinanceItem[] = [
  { id: 'FIN-9081', type: 'receber', date: '2026-04-30', party: 'Refinaria Oeste SA', doc: 'NF-1842', amount: 84992.00, status: 'aberto', category: 'Vendas' },
  { id: 'FIN-9080', type: 'pagar', date: '2026-04-29', party: 'Aperam Inox', doc: 'BOL-2204', amount: 9888.00, status: 'aberto', category: 'Matéria-prima' },
  { id: 'FIN-9079', type: 'receber', date: '2026-04-28', party: 'Engenharia Sul SA', doc: 'NF-1841', amount: 5598.00, status: 'aberto', category: 'Vendas' },
  { id: 'FIN-9078', type: 'pagar', date: '2026-04-27', party: 'Schneider Electric', doc: 'BOL-2203', amount: 4321.00, status: 'pago', category: 'Componentes' },
  { id: 'FIN-9077', type: 'receber', date: '2026-04-26', party: 'Mineradora Caeté', doc: 'NF-1840', amount: 14580.00, status: 'pago', category: 'Vendas' },
  { id: 'FIN-9076', type: 'pagar', date: '2026-04-25', party: 'CEMIG Energia', doc: 'FAT-04-26', amount: 18420.00, status: 'pago', category: 'Utilities' },
  { id: 'FIN-9075', type: 'pagar', date: '2026-05-02', party: 'Folha Abril/26', doc: 'FOL-0426', amount: 142880.00, status: 'aberto', category: 'Pessoal' },
  { id: 'FIN-9074', type: 'receber', date: '2026-05-05', party: 'Indústria Beta Componentes', doc: 'NF-1843', amount: 32104.50, status: 'aberto', category: 'Vendas' },
  { id: 'FIN-9073', type: 'pagar', date: '2026-04-22', party: 'Festo Brasil', doc: 'BOL-2202', amount: 2980.00, status: 'atrasado', category: 'Componentes' },
]

export const STATUS_LABELS: Record<string, StatusInfo> = {
  producao: { label: 'Em produção', cls: 'warn' },
  faturado: { label: 'Faturado', cls: 'info' },
  expedicao: { label: 'Em expedição', cls: 'warn' },
  concluido: { label: 'Concluído', cls: 'pos' },
  cancelado: { label: 'Cancelado', cls: 'neg' },
  aberto: { label: 'Em aberto', cls: '' },
  pago: { label: 'Pago', cls: 'pos' },
  atrasado: { label: 'Atrasado', cls: 'neg' },
  ativo: { label: 'Ativo', cls: 'pos' },
  baixo: { label: 'Estoque baixo', cls: 'warn' },
  inativo: { label: 'Inativo', cls: '' },
}

export const fmtBRL = (n: number): string =>
  'R$ ' + n.toLocaleString('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })

export const fmtNum = (n: number): string => n.toLocaleString('pt-BR')

export const fmtDate = (s: string): string => {
  const d = new Date(s)
  return d.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: '2-digit',
  })
}

export const fmtDateTime = (s: string): string => {
  const d = new Date(s)
  return (
    d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' }) +
    ' ' +
    d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
  )
}
