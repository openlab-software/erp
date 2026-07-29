# Requirements Document

## Introduction

Este documento especifica a conclusão dos CRUDs básicos de duas entidades do `catalog-service` (Category e Product) e uma do `stock-service` (Stock), seguindo os padrões DDD + Outbox + REST + GORM já adotados no monorepo. O objetivo é fechar as operações de leitura, atualização e remoção que hoje estão parciais ou ausentes, padronizar paginação nas listagens, formalizar a emissão de eventos de domínio (`created`, `updated`, `deleted`) e definir as regras de exclusão considerando integridade referencial entre os dois serviços.

StockItem e Reassignment estão fora do escopo deste spec — StockItem segue sendo criado de forma reativa pelo evento `product.created`, e Reassignment não é alterado.

## Glossary

- **Catalog_Service**: microsserviço Go que gerencia o catálogo (categorias e produtos), exposto na porta 8080 sob `/catalog-service/v1`.
- **Stock_Service**: microsserviço Go que gerencia estoques, exposto na porta 8081 sob `/v1`.
- **Category**: agregado do `Catalog_Service` que representa uma categoria de produtos (campos: `category_id`, `description`).
- **Product**: agregado do `Catalog_Service` que representa um produto (campos: `product_id`, `description`, `short_description`, `unit_of_measure`, `status`, `category`).
- **Stock**: agregado do `Stock_Service` que representa um depósito ou local de armazenagem (campos: `stock_id`, `description`, `items`).
- **StockItem**: entidade filha do `Stock` que representa o saldo de um produto em um estoque (campos: `product_id`, `min_value`, `current_value`, `max_value`).
- **Reassignment**: entidade do `Stock_Service` que representa uma transferência de itens entre estoques.
- **Outbox_Publisher**: implementação de `event.Publisher` baseada na tabela outbox que garante atomicidade entre persistência e publicação de eventos.
- **Public_ID**: identificador público no formato `prefix_<random>` (ex: `cat_2a1b3c4d5e`, `prod_xyz789`, `stock_abc123`).
- **Domain_Event**: mensagem nomeada (ex: `category.created`) publicada pelo serviço produtor após uma transição de estado.
- **Product_Status**: enumeração com os valores `DRAFT`, `PUBLISHED`, `ACTIVE`, `INACTIVE` que controla o ciclo de vida do produto.
- **Hard_Delete**: remoção física do registro no banco de dados.
- **Soft_Delete**: marcação lógica de inatividade (no caso de Product, mudança para `status=INACTIVE`) preservando o registro.
- **Listing_Page**: resposta de listagem paginada no formato `{ "data": [...], "page": <int>, "page_size": <int>, "total": <int> }`.

## Requirements

### Requirement 1: Listagem paginada de Categorias

**User Story:** Como consumidor da API do `Catalog_Service`, quero listar categorias de forma paginada e filtrada por descrição, para que eu possa navegar grandes catálogos sem carregar todos os registros de uma vez.

#### Acceptance Criteria

1. WHEN o cliente envia `GET /catalog-service/v1/categories`, THE Catalog_Service SHALL responder com status `200` e um `Listing_Page` contendo categorias.
2. WHERE os parâmetros de query `page` e `page_size` são informados, THE Catalog_Service SHALL retornar a página solicitada com no máximo `page_size` itens.
3. IF `page` não é informado ou é menor que 1, THEN THE Catalog_Service SHALL usar `page=1`.
4. IF `page_size` não é informado, THEN THE Catalog_Service SHALL usar `page_size=20`.
5. IF `page_size` informado é maior que 100, THEN THE Catalog_Service SHALL responder com status `400`.
6. WHERE o parâmetro de query `q` é informado, THE Catalog_Service SHALL retornar somente categorias cuja `description` contenha `q` (busca case-insensitive).
7. THE Catalog_Service SHALL ordenar a listagem por `created_at` descendente.
8. WHERE somente um dos parâmetros `page` ou `page_size` é informado, THE Catalog_Service SHALL aplicar paginação usando o valor informado e o default para o ausente.

### Requirement 2: Atualização de Categoria

**User Story:** Como administrador do catálogo, quero atualizar a descrição de uma categoria existente, para que eu possa corrigir erros sem recriar registros.

#### Acceptance Criteria

1. WHEN o cliente envia `PUT /catalog-service/v1/categories/{id}` com payload `{ "description": <string não vazia> }` e todas as validações dos itens 2, 3, 4 e 5 passam, THE Catalog_Service SHALL substituir a `description` da categoria identificada por `{id}` e responder com status `200` e a categoria atualizada.
2. IF `{id}` não corresponde a uma categoria existente, THEN THE Catalog_Service SHALL responder com status `404`.
3. IF `{id}` não tem o formato `cat_*`, THEN THE Catalog_Service SHALL responder com status `400`.
4. IF a `description` informada está em branco após `trim`, THEN THE Catalog_Service SHALL responder com status `400`.
5. IF a `description` informada já existe em outra categoria (comparação case-insensitive), THEN THE Catalog_Service SHALL responder com status `409` e não modificar nenhum registro.
6. WHEN a atualização é concluída com sucesso, THE Catalog_Service SHALL publicar o evento `category.updated` com payload `{ "id": <category_id>, "description": <nova description> }` na mesma transação da escrita.

### Requirement 3: Exclusão de Categoria com proteção referencial

**User Story:** Como administrador do catálogo, quero excluir uma categoria sem produtos vinculados, para que eu possa limpar entradas obsoletas sem quebrar referências.

#### Acceptance Criteria

1. WHEN o cliente envia `DELETE /catalog-service/v1/categories/{id}` e não existe nenhum `Product` associado a essa categoria, THE Catalog_Service SHALL realizar `Hard_Delete` da categoria e responder com status `204`.
2. IF existe ao menos um `Product` associado à categoria identificada por `{id}`, THEN THE Catalog_Service SHALL responder com status `409` e não remover a categoria.
3. IF `{id}` não corresponde a uma categoria existente, THEN THE Catalog_Service SHALL responder com status `404`.
4. IF `{id}` não tem o formato `cat_*`, THEN THE Catalog_Service SHALL responder com status `400`.
5. WHEN uma categoria é removida com sucesso, THE Catalog_Service SHALL publicar o evento `category.deleted` com payload `{ "id": <category_id> }` na mesma transação da escrita.

### Requirement 4: Listagem paginada de Produtos

**User Story:** Como consumidor da API do `Catalog_Service`, quero listar produtos de forma paginada e filtrada, para que eu possa exibir catálogos grandes em uma UI sem degradar a performance.

#### Acceptance Criteria

1. WHEN o cliente envia `GET /catalog-service/v1/products`, THE Catalog_Service SHALL responder com status `200` e um `Listing_Page` contendo produtos.
2. WHERE os parâmetros `page` e `page_size` são informados, THE Catalog_Service SHALL aplicar a mesma regra de paginação descrita em Requirement 1 (defaults `page=1`, `page_size=20`).
3. IF `page_size` informado é maior que 100, THEN THE Catalog_Service SHALL responder com status `400`.
4. WHERE o parâmetro `q` é informado, THE Catalog_Service SHALL retornar produtos cuja `description` ou `short_description` contenha `q` (case-insensitive).
5. WHERE o parâmetro `category_id` é informado, THE Catalog_Service SHALL retornar somente produtos vinculados à categoria informada.
6. WHERE o parâmetro `status` é informado, THE Catalog_Service SHALL retornar somente produtos com o `Product_Status` informado.
7. THE Catalog_Service SHALL ordenar a listagem por `created_at` descendente.

### Requirement 5: Atualização de Produto

**User Story:** Como administrador do catálogo, quero atualizar dados de um produto existente, para que eu possa manter as informações sincronizadas com mudanças de fornecedor ou descrição.

#### Acceptance Criteria

1. WHEN o cliente envia `PUT /catalog-service/v1/products/{id}` com payload `{ "description", "short_description", "unit_of_measure", "category_id" }`, THE Catalog_Service SHALL substituir esses campos do produto identificado por `{id}` e responder com status `200` e o produto atualizado.
2. IF `{id}` não corresponde a um produto existente, THEN THE Catalog_Service SHALL responder com status `404`.
3. IF `{id}` não tem o formato `prod_*`, THEN THE Catalog_Service SHALL responder com status `400`.
4. IF qualquer um dos campos `description`, `short_description`, `unit_of_measure` ou `category_id` está em branco após `trim`, THEN THE Catalog_Service SHALL responder com status `400`.
5. IF o `category_id` informado não corresponde a uma categoria existente, THEN THE Catalog_Service SHALL responder com status `400`.
6. THE Catalog_Service SHALL preservar o campo `status` do produto durante a atualização (status só é alterado pelo endpoint dedicado em Requirement 6).
7. WHEN a atualização é concluída com sucesso, THE Catalog_Service SHALL publicar o evento `product.updated` com payload `{ "id", "description", "short_description", "unit_of_measure", "category_id", "status" }` na mesma transação da escrita.

### Requirement 6: Mudança de status do Produto

**User Story:** Como administrador do catálogo, quero alterar o status de um produto pelo seu ciclo de vida (`DRAFT` → `PUBLISHED` → `INACTIVE`), para que eu possa controlar quando o produto fica disponível para uso pelos demais serviços.

#### Acceptance Criteria

1. WHEN o cliente envia `PATCH /catalog-service/v1/products/{id}/status` com payload `{ "status": <novo status> }`, THE Catalog_Service SHALL atualizar o status do produto e responder com status `200` e o produto atualizado.
2. IF o novo status não pertence ao conjunto `{DRAFT, PUBLISHED, ACTIVE, INACTIVE}`, THEN THE Catalog_Service SHALL responder com status `400`.
3. IF `{id}` não corresponde a um produto existente, THEN THE Catalog_Service SHALL responder com status `404`.
4. IF a transição de status não é permitida pelas regras `DRAFT → PUBLISHED`, `PUBLISHED → INACTIVE`, `INACTIVE → PUBLISHED`, THEN THE Catalog_Service SHALL responder com status `409`.
5. WHEN o status é alterado com sucesso, THE Catalog_Service SHALL publicar o evento `product.updated` com payload contendo o novo `status` na mesma transação da escrita.

### Requirement 7: Exclusão de Produto com regra de ciclo de vida

**User Story:** Como administrador do catálogo, quero excluir produtos respeitando seu ciclo de vida, para que produtos já publicados não desapareçam dos sistemas downstream que dependem deles.

#### Acceptance Criteria

1. WHEN o cliente envia `DELETE /catalog-service/v1/products/{id}` e o produto está com `status=DRAFT`, THE Catalog_Service SHALL realizar `Hard_Delete` do produto e responder com status `204`.
2. WHEN o cliente envia `DELETE /catalog-service/v1/products/{id}` e o produto está com `status=PUBLISHED` ou `status=ACTIVE`, THE Catalog_Service SHALL alterar o status do produto para `INACTIVE` (`Soft_Delete`) e responder com status `204`.
3. IF o produto já está com `status=INACTIVE`, THEN THE Catalog_Service SHALL responder com status `204` sem realizar nenhuma alteração adicional.
4. IF `{id}` não corresponde a um produto existente, THEN THE Catalog_Service SHALL responder com status `404`.
5. IF `{id}` não tem o formato `prod_*`, THEN THE Catalog_Service SHALL responder com status `400`.
6. WHEN ocorre `Hard_Delete` (Acceptance Criteria 1), THE Catalog_Service SHALL publicar o evento `product.deleted` com payload `{ "id": <product_id> }` na mesma transação da escrita.
7. WHEN ocorre `Soft_Delete` (Acceptance Criteria 2), THE Catalog_Service SHALL publicar o evento `product.updated` com payload contendo `status=INACTIVE` na mesma transação da escrita.

### Requirement 8: Listagem e leitura de Stocks

**User Story:** Como consumidor da API do `Stock_Service`, quero listar e consultar estoques individualmente, para que eu possa apresentar a relação de depósitos disponíveis em uma UI.

#### Acceptance Criteria

1. WHEN o cliente envia `GET /v1/stocks`, THE Stock_Service SHALL responder com status `200` e um `Listing_Page` contendo estoques.
2. WHERE os parâmetros `page` e `page_size` são informados, THE Stock_Service SHALL aplicar a mesma regra de paginação descrita em Requirement 1 (defaults `page=1`, `page_size=20`).
3. IF `page_size` informado é maior que 100, THEN THE Stock_Service SHALL responder com status `400`.
4. WHERE o parâmetro `q` é informado, THE Stock_Service SHALL retornar somente estoques cuja `description` contenha `q` (case-insensitive).
5. THE Stock_Service SHALL ordenar a listagem por `created_at` descendente.
6. WHEN o cliente envia `GET /v1/stocks/{id}`, THE Stock_Service SHALL responder com status `200` e o estoque identificado por `{id}`.
7. IF `{id}` não corresponde a um estoque existente, THEN THE Stock_Service SHALL responder com status `404`.
8. IF `{id}` não tem o formato `stock_*`, THEN THE Stock_Service SHALL responder com status `400`.

### Requirement 9: Criação de Stock com unicidade de description

**User Story:** Como administrador de estoque, quero criar estoques com descrições únicas, para que cada depósito seja identificável sem ambiguidade.

#### Acceptance Criteria

1. WHEN o cliente envia `POST /v1/stocks` com payload `{ "description": <string não vazia> }`, THE Stock_Service SHALL criar um novo `Stock` e responder com status `201` e o estoque criado.
2. IF a `description` informada está em branco após `trim`, THEN THE Stock_Service SHALL responder com status `400`.
3. IF já existe um `Stock` com a mesma `description` (comparação case-insensitive), THEN THE Stock_Service SHALL responder com status `409` e não criar o estoque.
4. WHEN um estoque é criado com sucesso, THE Stock_Service SHALL publicar o evento `stock.created` com payload `{ "id": <stock_id>, "description": <description> }` na mesma transação da escrita.

### Requirement 10: Atualização de Stock

**User Story:** Como administrador de estoque, quero atualizar a descrição de um estoque existente, para que eu possa renomear depósitos quando reorganizo a operação.

#### Acceptance Criteria

1. WHEN o cliente envia `PUT /v1/stocks/{id}` com payload `{ "description": <string não vazia> }`, THE Stock_Service SHALL substituir a `description` do estoque identificado por `{id}` e responder com status `200` e o estoque atualizado.
2. IF `{id}` não corresponde a um estoque existente, THEN THE Stock_Service SHALL responder com status `404`.
3. IF `{id}` não tem o formato `stock_*`, THEN THE Stock_Service SHALL responder com status `400`.
4. IF a `description` informada está em branco após `trim`, THEN THE Stock_Service SHALL responder com status `400`.
5. IF a `description` informada já existe em outro estoque (case-insensitive), THEN THE Stock_Service SHALL responder com status `409` e não modificar nenhum registro.
6. WHEN a atualização é concluída com sucesso, THE Stock_Service SHALL publicar o evento `stock.updated` com payload `{ "id": <stock_id>, "description": <nova description> }` na mesma transação da escrita.

### Requirement 11: Exclusão de Stock com proteção referencial

**User Story:** Como administrador de estoque, quero excluir estoques vazios sem histórico, para que eu possa remover depósitos descomissionados sem perder rastreabilidade dos estoques que ainda contêm dados.

#### Acceptance Criteria

1. WHEN o cliente envia `DELETE /v1/stocks/{id}` e o estoque não possui nenhum `StockItem` com `current_value > 0` e não é referenciado em nenhum `Reassignment`, THE Stock_Service SHALL realizar `Hard_Delete` do estoque e responder com status `204`.
2. IF existe ao menos um `StockItem` com `current_value > 0` no estoque identificado por `{id}`, THEN THE Stock_Service SHALL responder com status `409` e não remover o estoque.
3. IF o estoque identificado por `{id}` é referenciado como `from_stock_id` ou `to_stock_id` em algum `Reassignment`, THEN THE Stock_Service SHALL responder com status `409` e não remover o estoque.
4. IF `{id}` não corresponde a um estoque existente, THEN THE Stock_Service SHALL responder com status `404`.
5. IF `{id}` não tem o formato `stock_*`, THEN THE Stock_Service SHALL responder com status `400`.
6. WHEN um estoque é removido com sucesso, THE Stock_Service SHALL publicar o evento `stock.deleted` com payload `{ "id": <stock_id> }` na mesma transação da escrita.
7. WHEN um estoque é removido com sucesso, THE Stock_Service SHALL remover em cascata todos os `StockItem` associados a esse estoque na mesma transação.

### Requirement 12: Reação do Stock_Service ao soft-delete e hard-delete de Product

**User Story:** Como operador do `Stock_Service`, quero que os `StockItem` reflitam mudanças de ciclo de vida do produto no `Catalog_Service`, para que o saldo histórico seja preservado quando apropriado e o produto removido não fique pendurado em estoques.

#### Acceptance Criteria

1. WHEN o `Stock_Service` recebe um evento `product.updated` com `status=INACTIVE`, THE Stock_Service SHALL marcar todos os `StockItem` desse produto como inativos preservando o saldo atual e o histórico.
2. WHEN o `Stock_Service` recebe um evento `product.deleted`, THE Stock_Service SHALL remover imediatamente todos os `StockItem` associados ao produto removido, sem etapa intermediária de inativação.
3. IF o evento recebido se refere a um produto que não existe no `Stock_Service`, THEN THE Stock_Service SHALL ignorar o evento sem retornar erro ao broker e sem bloquear operações futuras de `StockItem` para outros produtos.
4. WHEN qualquer um dos eventos descritos em 12.1 ou 12.2 é processado com sucesso, THE Stock_Service SHALL confirmar (ack) a mensagem ao broker.

### Requirement 13: Padronização de respostas de erro

**User Story:** Como cliente da API, quero receber respostas de erro padronizadas, para que eu possa tratar falhas de forma consistente em todos os endpoints.

#### Acceptance Criteria

1. WHEN um endpoint retorna status `400`, `404`, `409` ou `422`, THE Catalog_Service SHALL responder com corpo JSON `{ "message": <string descritiva> }`.
2. WHEN um endpoint retorna status `400`, `404`, `409` ou `422`, THE Stock_Service SHALL responder com corpo JSON `{ "message": <string descritiva> }`.
3. WHEN ocorre falha de validação de payload (ex: campo obrigatório ausente), THE Catalog_Service SHALL responder com status `400` e corpo `{ "mensagem": "Um ou mais campos são inválidos", "erros": { <campo>: <descrição> } }`.
4. WHEN ocorre falha de validação de payload (ex: campo obrigatório ausente), THE Stock_Service SHALL responder com status `400` e corpo `{ "mensagem": "Um ou mais campos são inválidos", "erros": { <campo>: <descrição> } }`.

### Requirement 14: Atomicidade entre escrita e publicação de eventos

**User Story:** Como operador do sistema, quero que eventos de domínio só sejam publicados quando a escrita correspondente for confirmada no banco, para que consumidores não recebam eventos órfãos em caso de falha.

#### Acceptance Criteria

1. WHEN qualquer mutação descrita nos requirements 2, 3, 5, 6, 7, 9, 10 ou 11 é processada, THE Catalog_Service SHALL gravar o evento na tabela outbox dentro da mesma transação SQL da escrita do agregado, utilizando o `Outbox_Publisher` já existente no `libs/go-common/outbox`.
2. WHEN qualquer mutação descrita nos requirements 9, 10 ou 11 é processada, THE Stock_Service SHALL gravar o evento na tabela outbox dentro da mesma transação SQL da escrita do agregado, utilizando o `Outbox_Publisher` já existente no `libs/go-common/outbox`.
3. IF a transação SQL é revertida por qualquer motivo, THEN nem a escrita do agregado nem o registro outbox SHALL ser efetivados.
4. THE entrega do evento ao broker (RabbitMQ) é responsabilidade do processo `relay` e ocorre de forma assíncrona após o commit da transação, não bloqueando a resposta HTTP.
