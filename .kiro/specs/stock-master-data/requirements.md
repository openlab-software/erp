# Requirements Document

## Introduction

Este documento especifica a evolução do `stock-service` além do CRUD básico de `Stock` e `Reassignment` (`.kiro/specs/catalog-stock-crud`), cobrindo o que hoje é uma lacuna funcional real do microsserviço: **não existe nenhum histórico de movimentação de estoque**. Uma auditoria do código atual confirma que `StockItem.current_value` só é atribuído uma vez (`0`, na criação reativa do item via `product.created`) e nunca mais é alterado em lugar nenhum — nem `Reassignment` de fato move saldo entre estoques hoje. Também não existe nenhum endpoint de leitura de `StockItem` via REST.

Este spec fecha essas lacunas introduzindo: leitura de itens de estoque, um cadastro de motivos de movimentação, o lançamento de movimentações manuais (entrada/saída/ajuste) com kardex, a correção do `Reassignment` para efetivamente mover saldo entre estoques (gerando movimentações), reserva de estoque, contagem de inventário (cycle count) e relatórios de saldo/alerta. Segue os mesmos padrões já adotados: paginação, eventos de domínio via outbox transacional (CDI `Event<DomainEvent>` + `@Observes`), e respostas de erro padronizadas.

Fora do escopo: rastreamento por lote/validade, localização física dentro do depósito (bin location), e qualquer integração com um futuro serviço de vendas/compras (a reserva aqui é só um contador — quem decide reservar é responsabilidade de quem chama a API).

## Glossary

- **Stock_Item**: saldo de um produto em um `Stock` (`stock_id`, `catalog_product_public_id`, `min_value`, `current_value`, `max_value`, `reserved_value`, `active`). `Available_Value` (calculado, não persistido) = `current_value - reserved_value`.
- **Movement_Reason**: cadastro de motivo de movimentação (`reason_id`, `description` única), usado para qualificar um `Stock_Movement` do tipo `ADJUSTMENT`.
- **Stock_Movement**: registro append-only de uma alteração em `Stock_Item.current_value` (`movement_id`, `stock_id`, `catalog_product_public_id`, `type`, `quantity`, `resulting_balance`, `reason_id` opcional, `reference` opcional, `created_at`). `type` em `{ENTRY, EXIT, ADJUSTMENT, TRANSFER_IN, TRANSFER_OUT}`.
- **Stock_Count**: registro de uma contagem física de inventário (`count_id`, `stock_id`, `catalog_product_public_id`, `system_value` no momento da contagem, `counted_value`, `created_at`).
- **Public_ID**: identificador público no formato `prefix_<ULID>` (ex: `mov_01J...`, `reason_01J...`, `count_01J...`).

## Requirements

### Requirement 1: Leitura de itens de estoque

**User Story:** Como consumidor da API do `Stock_Service`, quero consultar os itens (saldos) de um estoque, para que eu possa exibir o inventário atual — hoje `Stock_Item` é criado reativamente mas não é exposto por nenhum endpoint.

#### Acceptance Criteria

1. WHEN o cliente envia `GET /v1/stocks/{id}/items`, THE Stock_Service SHALL responder `200` com um `Listing_Page` paginado (mesmas regras de `page`/`page_size` já usadas em `Stock`) contendo os `Stock_Item` daquele estoque, cada um com `min_value`, `current_value`, `max_value`, `reserved_value` e `available_value` (calculado).
2. WHERE o parâmetro `product_id` é informado, THE Stock_Service SHALL retornar somente o item daquele produto.
3. IF `{id}` não corresponde a um estoque existente, THEN THE Stock_Service SHALL responder `404`.
4. WHEN o cliente envia `GET /v1/stocks/{id}/items/{productId}`, THE Stock_Service SHALL responder `200` com o `Stock_Item` daquele produto naquele estoque, ou `404` se não existir item para esse produto naquele estoque.

### Requirement 2: Cadastro de Motivo de Movimentação

**User Story:** Como administrador de estoque, quero cadastrar motivos padronizados de ajuste (perda, quebra, doação, etc.), para que os ajustes de inventário sejam auditáveis e consistentes.

#### Acceptance Criteria

1. WHEN o cliente envia `POST /v1/movement-reasons` com payload `{ "description" }` não vazio, THE Stock_Service SHALL criar o `Movement_Reason` e responder `201`.
2. IF já existe um `Movement_Reason` com a mesma `description` (case-insensitive), THEN THE Stock_Service SHALL responder `409`.
3. THE Stock_Service SHALL expor `GET /v1/movement-reasons` (paginado, com `q`), `PUT /v1/movement-reasons/{id}` e `DELETE /v1/movement-reasons/{id}`, seguindo as mesmas regras de validação/paginação/erro já usadas em `Stock`.
4. IF ao menos um `Stock_Movement` referencia o `Movement_Reason`, THEN THE Stock_Service SHALL responder `409` à tentativa de exclusão.
5. WHEN um `Movement_Reason` é criado, atualizado ou removido, THE Stock_Service SHALL publicar `movement_reason.created`/`movement_reason.updated`/`movement_reason.deleted` na mesma transação da escrita.

### Requirement 3: Lançamento manual de movimentação de estoque

**User Story:** Como operador de depósito, quero lançar entradas, saídas e ajustes manuais de estoque, para que o saldo do sistema reflita a realidade física e cada alteração fique registrada.

#### Acceptance Criteria

1. WHEN o cliente envia `POST /v1/stocks/{id}/movements` com payload `{ "product_id", "type", "quantity", "reason_id"?, "reference"? }`, THE Stock_Service SHALL validar `type` em `{ENTRY, EXIT, ADJUSTMENT}` (400 se fora desse conjunto) e responder `404` se `{id}` ou `product_id` não corresponderem a um `Stock`/`Stock_Item` existente.
2. IF `type=ENTRY` ou `type=EXIT`, THEN `quantity` SHALL ser um inteiro estritamente positivo (400 caso contrário); `ENTRY` soma `quantity` ao `current_value`, `EXIT` subtrai.
3. IF `type=ADJUSTMENT`, THEN `quantity` pode ser positivo ou negativo (não pode ser zero — 400 se for) e é somado diretamente ao `current_value` (delta), e `reason_id` SHALL ser obrigatório (400 se ausente ou se não corresponde a um `Movement_Reason` existente).
4. IF a aplicação de `EXIT` ou de um `ADJUSTMENT` negativo resultaria em `current_value` negativo, THEN THE Stock_Service SHALL responder `409` e não alterar nada.
5. WHEN a movimentação é aplicada com sucesso, THE Stock_Service SHALL, na MESMA transação: atualizar `Stock_Item.current_value`, gravar um `Stock_Movement` com o `resulting_balance` pós-aplicação, e publicar o evento `stock_movement.created` com payload `{ "id", "stock_id", "product_id", "type", "quantity", "resulting_balance" }`.

### Requirement 4: Extrato de movimentações (Kardex)

**User Story:** Como auditor/administrador de estoque, quero consultar o histórico de movimentações de um estoque, para que eu possa reconstruir como um saldo chegou ao valor atual.

#### Acceptance Criteria

1. WHEN o cliente envia `GET /v1/stocks/{id}/movements`, THE Stock_Service SHALL responder `200` com um `Listing_Page` paginado dos `Stock_Movement` daquele estoque, ordenado por `created_at` descendente.
2. WHERE os parâmetros `product_id`, `type`, `from` e/ou `to` (datas) são informados, THE Stock_Service SHALL filtrar o extrato de acordo.
3. IF `{id}` não corresponde a um estoque existente, THEN THE Stock_Service SHALL responder `404`.

### Requirement 5: Reassignment passa a mover saldo de fato

**User Story:** Como administrador de estoque, quero que uma transferência entre estoques realmente debite o estoque de origem e credite o de destino, para que o saldo do sistema reflita a movimentação física — hoje `Reassignment` é registrado mas não altera nenhum saldo.

#### Acceptance Criteria

1. WHEN uma `POST /v1/reassignments` é processada, THE Stock_Service SHALL, para cada item, validar que o `Stock_Item` do `from_stock_id` tem `current_value` suficiente para a `quantity` solicitada.
2. IF qualquer item da requisição não tem saldo suficiente no `from_stock_id`, THEN THE Stock_Service SHALL responder `409` e não aplicar NENHUM item da requisição (tudo ou nada).
3. WHEN a validação de todos os itens passa, THE Stock_Service SHALL, na MESMA transação da criação do `Reassignment`: subtrair `quantity` do `Stock_Item` do `from_stock_id` e somar ao `Stock_Item` do `to_stock_id` para cada item (criando o `Stock_Item` de destino se ainda não existir, com `min_value`/`max_value` nulos).
4. WHEN cada item é aplicado, THE Stock_Service SHALL gravar dois `Stock_Movement` (um `TRANSFER_OUT` no `from_stock_id`, um `TRANSFER_IN` no `to_stock_id`), ambos com `reference` apontando para o `reassignment_id`.
5. THE Stock_Service SHALL continuar publicando os eventos de `Reassignment` já existentes (se houver) sem alteração de formato, adicionalmente aos eventos `stock_movement.created` dos itens 3/4.

### Requirement 6: Reserva de estoque

**User Story:** Como operador comercial, quero reservar uma quantidade de um produto em um estoque, para que ela não seja contabilizada como disponível para outras vendas enquanto um pedido está pendente.

#### Acceptance Criteria

1. THE Stock_Service SHALL adicionar o campo `reserved_value` (inteiro, default `0`) a `Stock_Item`.
2. WHEN o cliente envia `POST /v1/stocks/{id}/items/{productId}/reserve` com payload `{ "quantity" }` (inteiro positivo, 400 caso contrário), THE Stock_Service SHALL somar `quantity` a `reserved_value` e responder `200` com o item atualizado.
3. IF `quantity` a reservar é maior que o `available_value` atual (`current_value - reserved_value`), THEN THE Stock_Service SHALL responder `409` e não alterar `reserved_value`.
4. WHEN o cliente envia `POST /v1/stocks/{id}/items/{productId}/release` com payload `{ "quantity" }` (inteiro positivo, 400 caso contrário), THE Stock_Service SHALL subtrair `quantity` de `reserved_value` e responder `200`.
5. IF `quantity` a liberar é maior que o `reserved_value` atual, THEN THE Stock_Service SHALL responder `409` e não alterar `reserved_value`.
6. THE Stock_Service SHALL considerar `reserved_value > 0` como bloqueio adicional de exclusão de `Stock` (junto com a regra já existente de `current_value > 0`, do spec `catalog-stock-crud` Requirement 11).
7. Reservar/liberar NÃO SHALL alterar `current_value` nem gerar `Stock_Movement` — é um contador independente, aplicado por cima do `current_value` físico.

### Requirement 7: Contagem de inventário (cycle count)

**User Story:** Como operador de depósito, quero registrar o resultado de uma contagem física periódica, para que divergências entre o saldo do sistema e a realidade sejam corrigidas e auditadas.

#### Acceptance Criteria

1. WHEN o cliente envia `POST /v1/stocks/{id}/counts` com payload `{ "product_id", "counted_value" }` (inteiro não negativo, 400 caso contrário), THE Stock_Service SHALL registrar um `Stock_Count` com `system_value` = `current_value` do item no momento da contagem, e responder `201`.
2. IF `counted_value` difere de `system_value`, THEN THE Stock_Service SHALL, na MESMA transação, gerar automaticamente um `Stock_Movement` do tipo `ADJUSTMENT` com `quantity = counted_value - system_value` (podendo ser negativo) e `reference` apontando para o `count_id`, seguindo as mesmas regras de aplicação do Requirement 3 (SEM exigir `reason_id` neste caso específico, já que a contagem física É o motivo).
3. IF `counted_value` é igual a `system_value`, THEN THE Stock_Service SHALL gravar o `Stock_Count` sem gerar nenhum `Stock_Movement`.
4. IF `{id}` ou `product_id` não correspondem a um `Stock`/`Stock_Item` existente, THEN THE Stock_Service SHALL responder `404`.
5. WHEN o cliente envia `GET /v1/stocks/{id}/counts`, THE Stock_Service SHALL responder `200` com um `Listing_Page` paginado dos `Stock_Count` daquele estoque, ordenado por `created_at` descendente, aceitando filtro `product_id`.

### Requirement 8: Alertas de saldo fora da faixa

**User Story:** Como administrador de estoque, quero identificar rapidamente itens abaixo do mínimo ou acima do máximo, para que eu saiba o que precisa de reposição ou está com excesso.

#### Acceptance Criteria

1. WHEN o cliente envia `GET /v1/stocks/{id}/items?below_min=true`, THE Stock_Service SHALL retornar somente `Stock_Item` cujo `min_value` não é nulo E `current_value < min_value`.
2. WHEN o cliente envia `GET /v1/stocks/{id}/items?above_max=true`, THE Stock_Service SHALL retornar somente `Stock_Item` cujo `max_value` não é nulo E `current_value > max_value`.
3. IF ambos `below_min=true` e `above_max=true` são informados na mesma requisição, THEN THE Stock_Service SHALL responder `400`.
4. Essa filtragem SHALL compor com a paginação e o filtro `product_id` já definidos no Requirement 1 (não é um endpoint separado).

### Requirement 9: Saldo consolidado de um produto entre estoques

**User Story:** Como consumidor da API (ex: tela de disponibilidade de produto), quero ver o saldo de um produto somado em todos os estoques, para que eu não precise consultar estoque por estoque manualmente.

#### Acceptance Criteria

1. WHEN o cliente envia `GET /v1/products/{productId}/balance`, THE Stock_Service SHALL responder `200` com `{ "product_id", "total_current_value", "total_reserved_value", "total_available_value", "by_stock": [{ "stock_id", "current_value", "reserved_value", "available_value" }, ...] }`, somando apenas `Stock_Item` com `active=true`.
2. IF nenhum `Stock_Item` existe para `productId` em nenhum estoque, THEN THE Stock_Service SHALL responder `200` com todos os totais em `0` e `by_stock` vazio (não é erro).

### Requirement 10: Atomicidade entre escrita e publicação de eventos (novos agregados)

**User Story:** Como operador do sistema, quero que os eventos dos novos recursos sigam a mesma garantia de atomicidade já estabelecida, para que consumidores não recebam eventos órfãos.

#### Acceptance Criteria

1. WHEN qualquer mutação descrita nos Requirements 2, 3, 5, 6 ou 7 é processada, THE Stock_Service SHALL gravar o evento na tabela outbox dentro da mesma transação JTA da escrita do agregado, via `Event<DomainEvent>`/`OutboxEventPublisher` já estabelecidos.
2. THE entrega ao broker (RabbitMQ) desses novos eventos SHALL continuar sendo responsabilidade assíncrona do `OutboxRelay` existente, sem exigir nenhum relay adicional.

### Requirement 11: Padronização de respostas de erro (novos endpoints)

**User Story:** Como cliente da API, quero que os novos endpoints sigam o mesmo formato de erro já usado no restante do `stock-service`, para que eu não precise tratar casos especiais.

#### Acceptance Criteria

1. WHEN qualquer endpoint introduzido neste spec retorna status `400`, `404` ou `409`, THE Stock_Service SHALL responder com corpo JSON `{ "message": <string descritiva> }`, exceto falhas de validação de payload, que SHALL seguir o formato `{ "mensagem": "...", "erros": {...} }` já definido em `catalog-stock-crud`.
