# Módulos do ERP

O ERP é entregue em **módulos** (os "aplicativos" do Odoo). Um módulo agrupa os microserviços que dependem entre
si, declara do que depende e o que aparece no menu. Dá para instalar todos juntos ou só os que o cliente contratou.

```
                         ┌──────────┐   ┌──────────┐
                         │ contacts │   │ products │      base (sem dependências)
                         └────▲─────┘   └────▲─────┘
              ┌───────────────┼──────────────┤
         ┌────┴───┐      ┌────┴────┐    ┌────┴─────┐
         │  crm   │      │  sales  │    │ purchase │      depends (obrigatório)
         └────────┘      └─────────┘    └──────────┘
   ┌───────────┐   ┌────────────┐
   │ inventory │   │ accounting │      inventory depends products · accounting depends contacts
   └───────────┘   └────────────┘
```

| Módulo       | Serviço              | Odoo        | Depende de          | Integra (opcional) com        |
| ------------ | -------------------- | ----------- | ------------------- | ----------------------------- |
| `contacts`   | `customer-service`   | Contacts    | –                   | –                             |
| `products`   | `catalog-service`    | Products    | –                   | –                             |
| `inventory`  | `stock-service`      | Inventory   | `products`          | `sales`, `purchase`           |
| `crm`        | `crm-service`        | CRM         | `contacts`          | `sales`                       |
| `sales`      | `sales-service`      | Sales       | `contacts`, `products` | `crm`, `inventory`, `accounting` |
| `purchase`   | `purchase-service`   | Purchase    | `products`          | `inventory`, `accounting`     |
| `accounting` | `accounting-service` | Accounting  | `contacts`          | `sales`, `purchase`           |

## `depends` x `integrates_with`

- **`depends`** é obrigatório: o módulo não funciona sem o outro (vendas precisa de clientes e produtos). Ao
  instalar `sales`, o chart liga `contacts` e `products` sozinho.
- **`integrates_with`** é opcional: os módulos conversam por **eventos** (RabbitMQ) quando os dois estão
  instalados, e cada um funciona sozinho quando o outro não está. Nenhum serviço chama outro por HTTP.

| Evento                       | Quem publica | O que acontece em quem escuta (se o módulo existir)                                    |
| ---------------------------- | ------------ | -------------------------------------------------------------------------------------- |
| `sales_order.confirmed`      | sales        | inventory cria a **entrega** (`DELIVERY`) · accounting cria a **fatura** em rascunho   |
| `sales_order.cancelled`      | sales        | inventory cancela as entregas ainda não validadas                                       |
| `purchase_order.confirmed`   | purchase     | inventory cria o **recebimento** (`RECEIPT`) · accounting cria a **fatura do fornecedor** |
| `purchase_order.cancelled`   | purchase     | inventory cancela os recebimentos ainda não validados                                   |
| `picking.done`               | inventory    | sales atualiza o status de entrega · purchase atualiza o status de recebimento          |
| `invoice.posted`             | accounting   | sales marca o pedido como faturado · purchase marca como faturado pelo fornecedor       |

Por que funciona sem o outro lado: todo consumidor declara o *exchange* do produtor (idempotente) e cria a sua
própria fila, então o serviço sobe igual e a fila só fica vazia. Os consumidores são idempotentes (tabela
`processed_events` por serviço), porque o broker entrega "pelo menos uma vez". Já quem publica usa o *outbox*
transacional: se o broker estiver fora do ar, o evento fica `pending` (com o último erro) e sai quando ele voltar.

Detalhes que valem a pena conhecer: as linhas de pedido carregam `product_type` (`GOOD` ou `SERVICE`) — serviço
nunca espera entrega nem recebimento e o `inventory` o ignora; o estoque conta unidades inteiras (quantidade
fracionária sobe para o inteiro seguinte); e a entrega/recebimento automático usa o armazém configurado em
`STOCK_DEFAULT_STOCK` ou, na falta dele, o primeiro armazém criado.

## Manifesto (`modules/<nome>/module.json`)

```json
{
  "name": "sales",
  "display_name": "Vendas",
  "summary": "…",
  "category": "Sales",
  "version": "1.0.0",
  "depends": ["contacts", "products"],
  "integrates_with": ["crm", "inventory", "accounting"],
  "services": [{ "name": "sales-service", "port": 8084, "bff_prefix": "sales" }],
  "menu": [{ "group": "Comercial", "label": "Pedidos de venda", "path": "/vendas", "icon": "cart" }]
}
```

`bff_prefix` define a variável `<PREFIXO>_SERVICE_URL` que o BFF do frontend usa para chegar no serviço.

O manifesto é lido em quatro lugares: a CI (matriz de serviços a construir), o chart do Helm (o que implantar), o
BFF (`GET /api/modules`) e o menu do frontend (só mostra o que está instalado).

## Ferramentas

```bash
node scripts/modules.mjs list                  # módulos, dependências e serviços
node scripts/modules.mjs validate              # dependências, ciclos, portas repetidas, serviço sem módulo
node scripts/modules.mjs resolve sales         # fecho de dependências, na ordem de instalação
node scripts/modules.mjs services crm          # serviços de crm + dependências (JSON)
node scripts/modules.mjs stage                 # copia os manifestos para o chart (.devops/helm/files/modules)

node scripts/new-service.mjs <nome>-service --package <pkg> --port <porta> --title "Descrição"
```

## Instalar só alguns módulos (Helm)

```yaml
# values.yaml
modules:
  contacts:   { enabled: false }
  products:   { enabled: false }
  inventory:  { enabled: false }
  crm:        { enabled: true }    # liga também contacts (dependência)
  sales:      { enabled: false }
  purchase:   { enabled: false }
  accounting: { enabled: false }
```

Para renderizar localmente: `node scripts/modules.mjs stage`, colocar os manifestos dos serviços em
`.devops/helm/files/<serviço>.yaml` (saída de `mvn package`) e `helm template erp .devops/helm`.
Um módulo desconhecido em `modules` falha o render com a lista dos existentes.

## Como criar um módulo novo

1. `node scripts/new-service.mjs <nome>-service --package <pkg> --port <porta> --title "…"` cria a base do serviço
   (erros padronizados, i18n, outbox transacional, RabbitMQ, Flyway, OpenAPI, métricas e traces).
2. Escreva o domínio em `domain/`, `application/usecase/` e `infra/` e as migrations `V2+` (o schema é só do serviço).
3. Crie `modules/<nome>/module.json` (porta única; `depends` só para o que é indispensável).
4. Se reagir a eventos de outro módulo, **não** o coloque em `depends`: declare em `integrates_with`, consuma com
   fila própria e idempotência (veja `sales-service/.../IntegrationEventsConsumer`).
5. Se tiver tela, crie as rotas do BFF em `frontend/api/lambda/<prefixo>/` e a feature em `frontend/src/features/`;
   o menu vem do manifesto.
6. `node scripts/modules.mjs validate` — a CI roda o mesmo e passa a construir e publicar o serviço.

## Roteiro (Odoo ainda não coberto)

`manufacturing` (MRP: listas de materiais e ordens de produção), `website`/`ecommerce` (vitrine e carrinho sobre
`products` + `sales`), `pos`, `project`, `hr`. Cada um entraria como módulo novo, sem tocar nos existentes.
