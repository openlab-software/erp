# Requirements Document

## Introduction

Este documento especifica a evolução do `catalog-service` de um cadastro mínimo de Category/Product (`.kiro/specs/catalog-stock-crud`) para um módulo de cadastros mais completo do ERP, cobrindo estrutura de produto (unidade de medida, marca, categorias hierárquicas, atributos livres), dados comerciais (preço, fornecedor padrão) e recursos operacionais (código de barras, imagens, importação/exportação em massa, busca rápida). Segue os mesmos padrões já adotados: paginação, eventos de domínio via outbox transacional, e respostas de erro padronizadas.

Uma decisão central deste spec é que `Product` deixa de representar exclusivamente bens físicos: um produto pode ser um `GOOD` (bem físico, controlado em estoque) ou um `SERVICE` (serviço comercializável, não controlado em estoque). Isso tem impacto direto no `Stock_Service`, que não deve mais criar `StockItem` reativamente para produtos do tipo `SERVICE`.

Fora do escopo deste spec: classificação fiscal (NCM/CFOP/CST), matriz completa de variações de SKU (cada combinação de atributo virando um produto vendável próprio) e upload de arquivo binário para imagens (imagens são referenciadas por URL).

## Glossary

- **Unit_Of_Measure**: cadastro de unidade de medida (`unit_of_measure_id`, `code` único ex. `UN`, `KG`, `HR`, `description`), referenciado por `Product`.
- **Brand**: cadastro de marca (`brand_id`, `description` única), referenciado opcionalmente por `Product`.
- **Supplier**: cadastro de fornecedor (`supplier_id`, `name`, `document` único — CNPJ/CPF), referenciado opcionalmente por `Product` como fornecedor padrão.
- **Product_Type**: enumeração `{GOOD, SERVICE}` que define se o produto é um bem físico controlado em estoque ou um serviço não controlado em estoque. Definido na criação do produto e imutável após a criação.
- **Product_Attribute**: par livre `{name, value}` associado a um `Product` (ex: `{"cor": "azul"}`), sem ciclo de vida próprio — substituído em bloco a cada atualização.
- **Product_Barcode**: código de barras/SKU associado a um `Product` (`barcode_id`, `code` único globalmente, `type` em `{EAN13, EAN8, UPC, INTERNAL}`). Um produto pode ter múltiplos.
- **Product_Image**: imagem associada a um `Product` por URL (`image_id`, `url`, `is_primary`). Um produto pode ter múltiplas; no máximo uma marcada como `is_primary`.
- **Price_History**: registro append-only (`price_history_id`, `product_id`, `sale_price`, `cost_price`, `changed_at`) criado a cada alteração de preço de um `Product`.
- **Public_ID**: identificador público no formato `prefix_<ULID>` (ex: `uom_01J...`, `brand_01J...`, `supplier_01J...`, `barcode_01J...`, `image_01J...`).

## Requirements

### Requirement 1: Cadastro de Unidade de Medida

**User Story:** Como administrador do catálogo, quero cadastrar unidades de medida reutilizáveis, para que os produtos referenciem uma unidade consistente em vez de texto livre.

#### Acceptance Criteria

1. WHEN o cliente envia `POST /catalog-service/v1/units-of-measure` com payload `{ "code", "description" }` não vazios, THE Catalog_Service SHALL criar a `Unit_Of_Measure` e responder com status `201`.
2. IF já existe uma `Unit_Of_Measure` com o mesmo `code` (case-insensitive), THEN THE Catalog_Service SHALL responder com status `409`.
3. WHEN o cliente envia `GET /catalog-service/v1/units-of-measure`, THE Catalog_Service SHALL responder com um `Listing_Page` paginado (mesmas regras de `page`/`page_size`/`q` do Requirement 1 de `catalog-stock-crud`).
4. WHEN o cliente envia `PUT /catalog-service/v1/units-of-measure/{id}`, THE Catalog_Service SHALL atualizar `code`/`description` respeitando a mesma regra de unicidade de `code`.
5. WHEN o cliente envia `DELETE /catalog-service/v1/units-of-measure/{id}` e nenhum `Product` a referencia, THE Catalog_Service SHALL remover o registro e responder `204`.
6. IF ao menos um `Product` referencia a `Unit_Of_Measure`, THEN THE Catalog_Service SHALL responder com status `409` à tentativa de exclusão.
7. WHEN uma `Unit_Of_Measure` é criada, atualizada ou removida, THE Catalog_Service SHALL publicar `uom.created`/`uom.updated`/`uom.deleted` respectivamente, na mesma transação da escrita.

### Requirement 2: Produto referencia Unidade de Medida por ID

**User Story:** Como consumidor da API, quero que o produto referencie uma unidade de medida cadastrada, para que os valores fiquem padronizados entre todos os produtos.

#### Acceptance Criteria

1. THE Catalog_Service SHALL substituir o campo texto-livre `unit_of_measure` de `Product` por `unit_of_measure_id`, uma referência a `Unit_Of_Measure`.
2. WHEN o `Catalog_Service` sobe a migração deste requirement, THE Catalog_Service SHALL criar uma `Unit_Of_Measure` por cada valor distinto hoje presente em `Product.unit_of_measure` e reapontar os produtos existentes para o registro correspondente, preservando os dados.
3. IF o `unit_of_measure_id` informado em criação ou atualização de `Product` não corresponde a uma `Unit_Of_Measure` existente, THEN THE Catalog_Service SHALL responder com status `400`.
4. THE Catalog_Service SHALL incluir `unit_of_measure_id` nos payloads dos eventos `product.created`/`product.updated`, no lugar do antigo campo texto.

### Requirement 3: Cadastro de Marca

**User Story:** Como administrador do catálogo, quero cadastrar marcas, para que eu possa agrupar e filtrar produtos por fabricante/marca comercial.

#### Acceptance Criteria

1. WHEN o cliente envia `POST /catalog-service/v1/brands` com payload `{ "description" }` não vazio, THE Catalog_Service SHALL criar a `Brand` e responder `201`.
2. IF já existe uma `Brand` com a mesma `description` (case-insensitive), THEN THE Catalog_Service SHALL responder `409`.
3. THE Catalog_Service SHALL expor `GET /catalog-service/v1/brands` (paginado, com `q`), `PUT /catalog-service/v1/brands/{id}` e `DELETE /catalog-service/v1/brands/{id}`, seguindo as mesmas regras de validação, paginação e erro já usadas em `Category`.
4. IF ao menos um `Product` referencia a `Brand`, THEN THE Catalog_Service SHALL responder `409` à tentativa de exclusão.
5. WHEN uma `Brand` é criada, atualizada ou removida, THE Catalog_Service SHALL publicar `brand.created`/`brand.updated`/`brand.deleted` na mesma transação da escrita.

### Requirement 4: Produto referencia Marca (opcional)

**User Story:** Como administrador do catálogo, quero associar uma marca a um produto quando aplicável, para que produtos de fabricantes diferentes sejam distinguíveis.

#### Acceptance Criteria

1. THE Catalog_Service SHALL adicionar o campo opcional `brand_id` a `Product`, aceito em `POST /catalog-service/v1/products` e `PUT /catalog-service/v1/products/{id}`.
2. IF `brand_id` é informado e não corresponde a uma `Brand` existente, THEN THE Catalog_Service SHALL responder `400`.
3. WHERE o parâmetro de query `brand_id` é informado em `GET /catalog-service/v1/products`, THE Catalog_Service SHALL retornar somente produtos vinculados àquela marca.
4. THE Catalog_Service SHALL incluir `brand_id` (podendo ser `null`) nos payloads de `product.created`/`product.updated`.

### Requirement 5: Hierarquia de Categorias

**User Story:** Como administrador do catálogo, quero organizar categorias em subcategorias, para que o catálogo reflita uma árvore de departamentos/seções em vez de uma lista plana.

#### Acceptance Criteria

1. THE Catalog_Service SHALL adicionar o campo opcional `parent_category_id` a `Category`, aceito em `POST` e `PUT /catalog-service/v1/categories/{id}`.
2. IF `parent_category_id` é informado e não corresponde a uma `Category` existente, THEN THE Catalog_Service SHALL responder `400`.
3. IF `parent_category_id` informado é igual ao próprio `{id}` da categoria (auto-referência) OU introduz um ciclo na árvore (um ancestral apontando para um descendente), THEN THE Catalog_Service SHALL responder `409`.
4. WHEN o cliente envia `GET /catalog-service/v1/categories`, THE Catalog_Service SHALL incluir `parent_category_id` em cada item retornado.
5. IF a categoria identificada por `{id}` possui ao menos uma subcategoria (`parent_category_id` apontando para ela), THEN THE Catalog_Service SHALL responder `409` à tentativa de exclusão, além da regra já existente de proteção contra `Product` vinculado.
6. WHEN `parent_category_id` é alterado com sucesso, THE Catalog_Service SHALL incluir o novo valor no payload de `category.updated`.

### Requirement 6: Produto pode ser bem físico ou serviço

**User Story:** Como administrador comercial, quero cadastrar tanto produtos físicos quanto serviços no mesmo catálogo, para que o ERP possa comercializar ambos de forma unificada.

#### Acceptance Criteria

1. THE Catalog_Service SHALL adicionar o campo obrigatório `type` a `Product`, com valores permitidos `{GOOD, SERVICE}`.
2. WHEN o cliente envia `POST /catalog-service/v1/products` sem `type` ou com um valor fora do conjunto permitido, THE Catalog_Service SHALL responder `400`.
3. THE Catalog_Service SHALL considerar `type` imutável: `PUT /catalog-service/v1/products/{id}` NÃO SHALL alterar o `type` de um produto existente, mesmo que informado no payload (o valor existente é preservado).
4. WHERE o parâmetro de query `type` é informado em `GET /catalog-service/v1/products`, THE Catalog_Service SHALL retornar somente produtos daquele tipo.
5. THE Catalog_Service SHALL incluir `type` nos payloads de `product.created`/`product.updated`.

### Requirement 7: Stock_Service não controla estoque de Serviços

**User Story:** Como operador do `Stock_Service`, quero que produtos do tipo `SERVICE` nunca gerem `StockItem`, para que o controle de estoque reflita apenas bens físicos.

#### Acceptance Criteria

1. WHEN o `Stock_Service` recebe um evento `product.created` com `type=SERVICE`, THE Stock_Service SHALL ignorar a criação reativa de `StockItem` para esse produto e confirmar (ack) a mensagem normalmente.
2. WHEN o `Stock_Service` recebe um evento `product.created` com `type=GOOD`, THE Stock_Service SHALL manter o comportamento reativo de criação de `StockItem` já existente.
3. IF um evento `product.created` anterior à introdução deste campo não contém `type` (mensagem legada), THEN THE Stock_Service SHALL tratar como `type=GOOD` (comportamento atual preservado).

### Requirement 8: Atributos livres do Produto

**User Story:** Como administrador do catálogo, quero anexar pares chave/valor livres a um produto, para que eu possa registrar características (cor, voltagem, duração de serviço etc.) sem modelar uma coluna nova para cada uma.

#### Acceptance Criteria

1. WHEN o cliente envia `PUT /catalog-service/v1/products/{id}/attributes` com payload `{ "attributes": [{ "name", "value" }, ...] }`, THE Catalog_Service SHALL substituir integralmente o conjunto de `Product_Attribute` do produto e responder `200` com a lista atual.
2. IF algum item do payload tem `name` ou `value` em branco após `trim`, THEN THE Catalog_Service SHALL responder `400` e não alterar nenhum atributo existente.
3. IF o payload contém `name` duplicado (case-insensitive) na mesma requisição, THEN THE Catalog_Service SHALL responder `400`.
4. IF `{id}` não corresponde a um produto existente, THEN THE Catalog_Service SHALL responder `404`.
5. THE Catalog_Service SHALL incluir a lista de `Product_Attribute` na resposta de leitura de um produto (`GET /catalog-service/v1/products/{id}`).
6. WHEN os atributos de um produto são substituídos com sucesso, THE Catalog_Service SHALL publicar `product.updated` incluindo a nova lista de atributos, na mesma transação da escrita.

### Requirement 9: Cadastro de Fornecedor

**User Story:** Como administrador de compras, quero cadastrar fornecedores, para que eu possa vincular produtos ao fornecedor padrão de origem.

#### Acceptance Criteria

1. WHEN o cliente envia `POST /catalog-service/v1/suppliers` com payload `{ "name", "document" }` não vazios, THE Catalog_Service SHALL criar o `Supplier` e responder `201`.
2. IF já existe um `Supplier` com o mesmo `document`, THEN THE Catalog_Service SHALL responder `409`.
3. THE Catalog_Service SHALL expor `GET /catalog-service/v1/suppliers` (paginado, com `q` sobre `name`), `PUT /catalog-service/v1/suppliers/{id}` e `DELETE /catalog-service/v1/suppliers/{id}`, seguindo as mesmas regras de validação, paginação e erro já usadas em `Category`.
4. IF ao menos um `Product` referencia o `Supplier` como fornecedor padrão, THEN THE Catalog_Service SHALL responder `409` à tentativa de exclusão.
5. WHEN um `Supplier` é criado, atualizado ou removido, THE Catalog_Service SHALL publicar `supplier.created`/`supplier.updated`/`supplier.deleted` na mesma transação da escrita.

### Requirement 10: Produto referencia Fornecedor padrão (opcional)

**User Story:** Como administrador de compras, quero saber qual fornecedor padrão originou um produto, para que o processo de reposição saiba a quem comprar.

#### Acceptance Criteria

1. THE Catalog_Service SHALL adicionar o campo opcional `default_supplier_id` a `Product`, aceito em `POST` e `PUT /catalog-service/v1/products/{id}`.
2. IF `default_supplier_id` é informado e não corresponde a um `Supplier` existente, THEN THE Catalog_Service SHALL responder `400`.
3. THE Catalog_Service SHALL incluir `default_supplier_id` (podendo ser `null`) nos payloads de `product.created`/`product.updated`.

### Requirement 11: Preço de venda e custo do Produto

**User Story:** Como administrador comercial, quero definir e alterar o preço de venda e custo de um produto, para que os demais serviços do ERP consultem um preço vigente confiável, e quero visualizar o histórico dessas mudanças.

#### Acceptance Criteria

1. THE Catalog_Service SHALL adicionar os campos `sale_price` e `cost_price` (decimais, default `0`) a `Product`.
2. WHEN o cliente envia `PATCH /catalog-service/v1/products/{id}/price` com payload `{ "sale_price", "cost_price" }`, THE Catalog_Service SHALL atualizar os dois campos e responder `200` com o produto atualizado.
3. IF `sale_price` ou `cost_price` informado é negativo, THEN THE Catalog_Service SHALL responder `400` e não alterar o preço.
4. IF `{id}` não corresponde a um produto existente, THEN THE Catalog_Service SHALL responder `404`.
5. WHEN o preço é alterado com sucesso, THE Catalog_Service SHALL gravar uma entrada em `Price_History` com os novos valores e o timestamp da alteração, na mesma transação da escrita.
6. WHEN o cliente envia `GET /catalog-service/v1/products/{id}/price-history`, THE Catalog_Service SHALL responder `200` com a lista de `Price_History` do produto ordenada por `changed_at` descendente.
7. WHEN o preço é alterado com sucesso, THE Catalog_Service SHALL publicar o evento `product.price_changed` com payload `{ "id", "sale_price", "cost_price" }` na mesma transação da escrita.

### Requirement 12: Códigos de barra do Produto

**User Story:** Como operador de PDV/depósito, quero associar múltiplos códigos de barra a um produto, para que a leitura por diferentes formatos (EAN, código interno) identifique o mesmo produto.

#### Acceptance Criteria

1. WHEN o cliente envia `POST /catalog-service/v1/products/{id}/barcodes` com payload `{ "code", "type" }`, THE Catalog_Service SHALL criar o `Product_Barcode` vinculado ao produto e responder `201`.
2. IF `type` informado não pertence a `{EAN13, EAN8, UPC, INTERNAL}`, THEN THE Catalog_Service SHALL responder `400`.
3. IF já existe um `Product_Barcode` com o mesmo `code` (em qualquer produto), THEN THE Catalog_Service SHALL responder `409`.
4. IF `{id}` não corresponde a um produto existente, THEN THE Catalog_Service SHALL responder `404`.
5. WHEN o cliente envia `DELETE /catalog-service/v1/products/{id}/barcodes/{barcodeId}`, THE Catalog_Service SHALL remover o `Product_Barcode` e responder `204`.
6. THE Catalog_Service SHALL incluir a lista de `Product_Barcode` na resposta de leitura de um produto.
7. WHEN um `Product_Barcode` é adicionado ou removido, THE Catalog_Service SHALL publicar `product.updated` incluindo a lista atual de códigos de barra, na mesma transação da escrita.

### Requirement 13: Imagens do Produto

**User Story:** Como administrador do catálogo, quero associar imagens a um produto, para que o produto seja exibido visualmente nas telas que consomem o catálogo.

#### Acceptance Criteria

1. WHEN o cliente envia `POST /catalog-service/v1/products/{id}/images` com payload `{ "url" }`, THE Catalog_Service SHALL criar o `Product_Image` vinculado ao produto e responder `201`. Se for a primeira imagem do produto, THE Catalog_Service SHALL marcá-la como `is_primary=true` automaticamente.
2. IF `url` está em branco após `trim` ou não é uma URL bem formada, THEN THE Catalog_Service SHALL responder `400`.
3. WHEN o cliente envia `PATCH /catalog-service/v1/products/{id}/images/{imageId}/primary`, THE Catalog_Service SHALL marcar essa imagem como `is_primary=true` e desmarcar qualquer outra imagem do mesmo produto que estivesse marcada, na mesma transação.
4. WHEN o cliente envia `DELETE /catalog-service/v1/products/{id}/images/{imageId}`, THE Catalog_Service SHALL remover a imagem e responder `204`. IF a imagem removida era `is_primary` e existem outras imagens restantes, THEN THE Catalog_Service SHALL marcar a imagem mais antiga restante como `is_primary=true`.
5. THE Catalog_Service SHALL incluir a lista de `Product_Image` (com a flag `is_primary`) na resposta de leitura de um produto.

### Requirement 14: Importação em massa de Produtos via CSV

**User Story:** Como administrador do catálogo, quero importar produtos em lote a partir de um CSV, para que eu não precise cadastrar centenas de itens manualmente ao migrar de outro sistema.

#### Acceptance Criteria

1. WHEN o cliente envia `POST /catalog-service/v1/products/import` (multipart, arquivo CSV) com colunas `description, short_description, type, unit_of_measure_code, category_id, brand_id, sale_price, cost_price`, THE Catalog_Service SHALL processar cada linha aplicando as mesmas validações de `POST /catalog-service/v1/products`.
2. THE Catalog_Service SHALL processar o arquivo linha a linha de forma independente: uma linha inválida NÃO SHALL impedir o processamento das demais linhas válidas (sem rollback do lote inteiro).
3. WHEN o processamento termina, THE Catalog_Service SHALL responder `200` com um relatório `{ "total", "created", "failed": [{ "line", "error" }, ...] }`.
4. IF o arquivo enviado não é um CSV válido ou não contém a coluna `description`, THEN THE Catalog_Service SHALL responder `400` sem processar nenhuma linha.
5. WHEN uma linha é importada com sucesso, THE Catalog_Service SHALL publicar `product.created` para o produto resultante, seguindo a mesma regra transacional por linha (uma transação por linha, não uma transação para o arquivo inteiro).

### Requirement 15: Exportação de Produtos via CSV

**User Story:** Como administrador do catálogo, quero exportar o catálogo de produtos em CSV, para que eu possa analisar ou editar em planilha e para dar suporte a integrações externas.

#### Acceptance Criteria

1. WHEN o cliente envia `GET /catalog-service/v1/products/export`, THE Catalog_Service SHALL responder `200` com corpo CSV (`Content-Type: text/csv`) contendo todos os produtos, respeitando os mesmos filtros de query (`q`, `category_id`, `status`, `type`, `brand_id`) aceitos por `GET /catalog-service/v1/products`.
2. THE Catalog_Service SHALL incluir no CSV exportado, no mínimo, as colunas `id, description, short_description, type, status, unit_of_measure_code, category_id, brand_id, sale_price, cost_price`.
3. THE Catalog_Service SHALL ignorar `page`/`page_size` na exportação, retornando o conjunto completo que satisfaz os filtros.

### Requirement 16: Busca rápida (autocomplete) de Produtos

**User Story:** Como consumidor da API (ex: tela de PDV ou pedido de venda), quero buscar produtos por digitação parcial com resposta rápida e leve, para que eu monte uma lista de sugestões sem pagar o custo de uma listagem paginada completa.

#### Acceptance Criteria

1. WHEN o cliente envia `GET /catalog-service/v1/products/autocomplete?q=<termo>`, THE Catalog_Service SHALL responder `200` com uma lista (sem metadados de paginação) de produtos cuja `description` comece com ou contenha `q` (case-insensitive), priorizando correspondências por prefixo.
2. THE Catalog_Service SHALL retornar em cada item somente os campos `id, description, type, sale_price` (payload reduzido em relação à listagem completa).
3. THE Catalog_Service SHALL limitar o resultado a `limit` itens, aceito como parâmetro de query com default `10` e máximo `20`.
4. IF `q` não é informado ou está em branco após `trim`, THEN THE Catalog_Service SHALL responder `400`.
5. THE Catalog_Service SHALL retornar somente produtos com `status` diferente de `INACTIVE` no autocomplete.

### Requirement 17: Atomicidade entre escrita e publicação de eventos (novos agregados)

**User Story:** Como operador do sistema, quero que os eventos dos novos cadastros sigam a mesma garantia de atomicidade já estabelecida, para que consumidores não recebam eventos órfãos.

#### Acceptance Criteria

1. WHEN qualquer mutação descrita nos Requirements 1, 3, 8, 9, 11, 12, 13 ou 14 é processada, THE Catalog_Service SHALL gravar o evento na tabela outbox dentro da mesma transação JTA da escrita do agregado, via `Event<DomainEvent>`/`OutboxEventPublisher` já estabelecidos.
2. THE entrega ao broker (RabbitMQ) desses novos eventos SHALL continuar sendo responsabilidade assíncrona do `OutboxRelayJob` existente, sem exigir nenhum relay adicional.

### Requirement 18: Padronização de respostas de erro (novos endpoints)

**User Story:** Como cliente da API, quero que os novos endpoints sigam o mesmo formato de erro já usado no restante do `catalog-service`, para que eu não precise tratar casos especiais.

#### Acceptance Criteria

1. WHEN qualquer endpoint introduzido neste spec retorna status `400`, `404` ou `409`, THE Catalog_Service SHALL responder com corpo JSON `{ "message": <string descritiva> }`, exceto falhas de validação de payload, que SHALL seguir o formato `{ "mensagem": "...", "erros": {...} }` já definido em `catalog-stock-crud`.
