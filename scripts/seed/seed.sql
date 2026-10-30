-- Dados de demonstração do ERP (todos os módulos), coerentes entre os serviços:
-- pedidos confirmados têm a transferência (picking) e a fatura/conta correspondentes,
-- os saldos de estoque batem com o histórico de movimentações e os totais das faturas
-- são recalculados a partir das linhas, como o accounting-service faz.
--
-- Requer as migrations Flyway já aplicadas (os serviços sobem antes). Roda com psql:
--   psql -v ON_ERROR_STOP=1 -d erp -f scripts/seed/seed.sql   (ou scripts/seed/seed.sh)
-- É idempotente: se o seed já foi aplicado, não faz nada.
--
-- Os ids públicos seguem o formato <prefixo>_<ULID> (26 caracteres Crockford base32):
-- <prefixo>_00000000SEED<n com 14 dígitos>. Os números (S00001, IN/00001, INV/00001, ...)
-- vêm das sequences dos serviços, então não colidem com documentos já existentes.
-- Nenhum evento é gravado no outbox: os dados já entram no estado final de cada serviço.

\set ON_ERROR_STOP on

SELECT EXISTS (SELECT 1 FROM catalog.products WHERE public_id = 'prod_00000000SEED00000000000001') AS seeded \gset
\if :seeded
\echo 'Seed já aplicado; nada a fazer.'
\quit
\endif

BEGIN;

CREATE FUNCTION pg_temp.sid(prefix TEXT, n INT) RETURNS TEXT LANGUAGE sql IMMUTABLE AS
$$ SELECT prefix || '_00000000SEED' || lpad(n::text, 14, '0') $$;

-- chaves internas a partir do id público
CREATE FUNCTION pg_temp.uom(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT unit_of_measure_id FROM catalog.units_of_measure WHERE public_id = pg_temp.sid('uom', n) $$;
CREATE FUNCTION pg_temp.brand(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT brand_id FROM catalog.brands WHERE public_id = pg_temp.sid('brand', n) $$;
CREATE FUNCTION pg_temp.supplier(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT supplier_id FROM catalog.suppliers WHERE public_id = pg_temp.sid('supplier', n) $$;
CREATE FUNCTION pg_temp.category(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT category_id FROM catalog.categories WHERE public_id = pg_temp.sid('cat', n) $$;
CREATE FUNCTION pg_temp.product(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT product_id FROM catalog.products WHERE public_id = pg_temp.sid('prod', n) $$;
CREATE FUNCTION pg_temp.stock(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT stock_id FROM stock.stocks WHERE public_id = pg_temp.sid('stock', n) $$;
CREATE FUNCTION pg_temp.reason(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT movement_reason_id FROM stock.movement_reasons WHERE public_id = pg_temp.sid('reason', n) $$;
CREATE FUNCTION pg_temp.picking(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT picking_id FROM stock.pickings WHERE public_id = pg_temp.sid('picking', n) $$;
CREATE FUNCTION pg_temp.picking_number(n INT) RETURNS TEXT LANGUAGE sql AS
$$ SELECT number FROM stock.pickings WHERE public_id = pg_temp.sid('picking', n) $$;
CREATE FUNCTION pg_temp.customer(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT customer_id FROM customer.customers WHERE public_id = pg_temp.sid('customer', n) $$;
CREATE FUNCTION pg_temp.stage(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT stage_id FROM crm.stages WHERE public_id = 'stage_' || lpad(n::text, 26, '0') $$;
CREATE FUNCTION pg_temp.lead(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT lead_id FROM crm.leads WHERE public_id = pg_temp.sid('lead', n) $$;
CREATE FUNCTION pg_temp.sales_order(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT sales_order_id FROM sales.sales_orders WHERE public_id = pg_temp.sid('sales_order', n) $$;
CREATE FUNCTION pg_temp.so_number(n INT) RETURNS TEXT LANGUAGE sql AS
$$ SELECT number FROM sales.sales_orders WHERE public_id = pg_temp.sid('sales_order', n) $$;
CREATE FUNCTION pg_temp.purchase_order(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT purchase_order_id FROM purchase.purchase_orders WHERE public_id = pg_temp.sid('purchase_order', n) $$;
CREATE FUNCTION pg_temp.po_number(n INT) RETURNS TEXT LANGUAGE sql AS
$$ SELECT number FROM purchase.purchase_orders WHERE public_id = pg_temp.sid('purchase_order', n) $$;
CREATE FUNCTION pg_temp.invoice(n INT) RETURNS BIGINT LANGUAGE sql AS
$$ SELECT invoice_id FROM accounting.invoices WHERE public_id = pg_temp.sid('invoice', n) $$;

-- =====================================================================================
-- Produtos (catalog)
-- =====================================================================================

INSERT INTO catalog.units_of_measure (public_id, code, description) VALUES
    (pg_temp.sid('uom', 1), 'UN', 'Unidade'),
    (pg_temp.sid('uom', 2), 'KG', 'Quilograma'),
    (pg_temp.sid('uom', 3), 'CX', 'Caixa'),
    (pg_temp.sid('uom', 4), 'H',  'Hora'),
    (pg_temp.sid('uom', 5), 'M',  'Metro')
ON CONFLICT DO NOTHING;

INSERT INTO catalog.brands (public_id, description) VALUES
    (pg_temp.sid('brand', 1), 'Dell'),
    (pg_temp.sid('brand', 2), 'Logitech'),
    (pg_temp.sid('brand', 3), 'Samsung'),
    (pg_temp.sid('brand', 4), 'Intelbras')
ON CONFLICT DO NOTHING;

INSERT INTO catalog.suppliers (public_id, name, document) VALUES
    (pg_temp.sid('supplier', 1), 'Distribuidora Tech Brasil Ltda',      '11222333000181'),
    (pg_temp.sid('supplier', 2), 'Alfa Componentes Eletrônicos Ltda',   '45678912000155'),
    (pg_temp.sid('supplier', 3), 'Papelaria Central Atacado Ltda',      '98765432000198')
ON CONFLICT DO NOTHING;

INSERT INTO catalog.categories (public_id, description, parent_category_id) VALUES
    (pg_temp.sid('cat', 1), 'Informática', NULL),
    (pg_temp.sid('cat', 5), 'Redes',       NULL),
    (pg_temp.sid('cat', 6), 'Escritório',  NULL),
    (pg_temp.sid('cat', 7), 'Serviços',    NULL);
INSERT INTO catalog.categories (public_id, description, parent_category_id) VALUES
    (pg_temp.sid('cat', 2), 'Notebooks',   pg_temp.category(1)),
    (pg_temp.sid('cat', 3), 'Periféricos', pg_temp.category(1)),
    (pg_temp.sid('cat', 4), 'Monitores',   pg_temp.category(1));

INSERT INTO catalog.products
    (public_id, description, short_description, status, type, category_id, unit_of_measure_id,
     brand_id, default_supplier_id, sale_price, cost_price)
SELECT pg_temp.sid('prod', p.n), p.description, p.short_description, p.status, p.type,
       pg_temp.category(p.cat), pg_temp.uom(p.uom), pg_temp.brand(p.brand), pg_temp.supplier(p.sup),
       p.sale, p.cost
FROM (VALUES
    (1,  'Notebook Dell Inspiron 15 i5 16GB 512GB SSD', 'Notebook Inspiron 15',  'ACTIVE', 'GOOD',    2, 1, 1,    1,    4299.00, 3200.00),
    (2,  'Mouse sem fio Logitech M280',                  'Mouse M280',            'ACTIVE', 'GOOD',    3, 1, 2,    2,      89.90,   45.00),
    (3,  'Teclado Logitech K120 USB ABNT2',              'Teclado K120',          'ACTIVE', 'GOOD',    3, 1, 2,    2,      79.90,   38.00),
    (4,  'Monitor Samsung 24" Full HD IPS',              'Monitor 24"',           'ACTIVE', 'GOOD',    4, 1, 3,    1,     899.00,  620.00),
    (5,  'Roteador Intelbras Wi-Fi 6 AX1500',            'Roteador Wi-Fi 6',      'ACTIVE', 'GOOD',    5, 1, 4,    2,     349.00,  210.00),
    (6,  'Cabo de rede Cat6 (metro)',                    'Cabo Cat6',             'ACTIVE', 'GOOD',    5, 5, 4,    2,       3.50,    1.20),
    (7,  'Papel A4 75g - caixa com 10 resmas',           'Papel A4 cx 10',        'ACTIVE', 'GOOD',    6, 3, NULL, 3,     289.00,  210.00),
    (8,  'Headset Logitech H390 USB',                    'Headset H390',          'ACTIVE', 'GOOD',    3, 1, 2,    2,     249.00,  150.00),
    (9,  'Instalação e configuração (hora técnica)',     'Hora técnica',          'ACTIVE', 'SERVICE', 7, 4, NULL, NULL,  150.00,    0.00),
    (10, 'Suporte técnico mensal',                       'Suporte mensal',        'ACTIVE', 'SERVICE', 7, 1, NULL, NULL,  490.00,    0.00),
    (11, 'Webcam Logitech C920 Full HD',                 'Webcam C920',           'DRAFT',  'GOOD',    3, 1, 2,    2,     499.00,  320.00)
) AS p(n, description, short_description, status, type, cat, uom, brand, sup, sale, cost);

INSERT INTO catalog.product_attributes (product_id, name, value) VALUES
    (pg_temp.product(1), 'Processador', 'Intel Core i5 13ª geração'),
    (pg_temp.product(1), 'Memória',     '16 GB'),
    (pg_temp.product(1), 'Armazenamento', 'SSD 512 GB'),
    (pg_temp.product(4), 'Tamanho',     '24 polegadas'),
    (pg_temp.product(4), 'Resolução',   '1920x1080'),
    (pg_temp.product(5), 'Padrão',      'Wi-Fi 6 (802.11ax)');

INSERT INTO catalog.product_barcodes (public_id, product_id, code, type)
SELECT pg_temp.sid('barcode', n), pg_temp.product(n), '78910000000' || lpad(n::text, 2, '0'), 'UPC'
FROM generate_series(1, 8) AS n;

INSERT INTO catalog.price_history (public_id, product_id, sale_price, cost_price, changed_at)
SELECT pg_temp.sid('pricehist', p.product_id::int), p.product_id, p.sale_price, p.cost_price, now() - interval '60 days'
FROM catalog.products p
WHERE p.public_id LIKE 'prod\_00000000SEED%';

-- =====================================================================================
-- Contatos (customer)
-- =====================================================================================

INSERT INTO customer.customers
    (public_id, type, status, name, trade_name, document, state_registration, birth_date, email, phone, created_at)
VALUES
    (pg_temp.sid('customer', 1), 'COMPANY',    'ACTIVE',   'Mercado Bom Preço Ltda',     'Bom Preço',         '33444555000181', '110042490114', NULL,         'compras@bompreco.com.br',  '(11) 3456-7890', now() - interval '90 days'),
    (pg_temp.sid('customer', 2), 'COMPANY',    'ACTIVE',   'Construtora Horizonte S.A.', 'Horizonte',         '77888999000181', NULL,           NULL,         'ti@horizonte.com.br',      '(21) 2233-4455', now() - interval '80 days'),
    (pg_temp.sid('customer', 3), 'COMPANY',    'ACTIVE',   'Escola Saber Mais Ltda',     'Colégio Saber Mais', '22333444000181', NULL,          NULL,         'secretaria@sabermais.edu.br', '(31) 3344-5566', now() - interval '45 days'),
    (pg_temp.sid('customer', 4), 'INDIVIDUAL', 'ACTIVE',   'Ana Paula Souza',            NULL,                '52998224725',    NULL,           '1988-04-12', 'ana.souza@email.com',      '(11) 98765-4321', now() - interval '30 days'),
    (pg_temp.sid('customer', 5), 'INDIVIDUAL', 'ACTIVE',   'Carlos Eduardo Lima',        NULL,                '11144477735',    NULL,           '1979-11-03', 'carlos.lima@email.com',    '(41) 99876-5432', now() - interval '20 days'),
    (pg_temp.sid('customer', 6), 'INDIVIDUAL', 'INACTIVE', 'Mariana Oliveira',           NULL,                '93541134780',    NULL,           '1995-07-21', 'mariana.o@email.com',      NULL,              now() - interval '120 days');

INSERT INTO customer.customer_addresses
    (public_id, customer_id, position, label, zip_code, street, street_number, complement, neighborhood, city, state, is_default)
VALUES
    (pg_temp.sid('address', 1), pg_temp.customer(1), 0, 'Matriz',    '01310100', 'Avenida Paulista',          '1000', 'Loja 2',  'Bela Vista',  'São Paulo',      'SP', TRUE),
    (pg_temp.sid('address', 2), pg_temp.customer(1), 1, 'Depósito',  '03102000', 'Rua do Gasômetro',          '350',  NULL,      'Brás',        'São Paulo',      'SP', FALSE),
    (pg_temp.sid('address', 3), pg_temp.customer(2), 0, 'Sede',      '20040020', 'Avenida Rio Branco',        '156',  'Sala 801', 'Centro',     'Rio de Janeiro', 'RJ', TRUE),
    (pg_temp.sid('address', 4), pg_temp.customer(3), 0, 'Escola',    '30130010', 'Avenida Afonso Pena',       '1500', NULL,      'Centro',      'Belo Horizonte', 'MG', TRUE),
    (pg_temp.sid('address', 5), pg_temp.customer(4), 0, 'Casa',      '04538133', 'Rua Joaquim Floriano',      '72',   'Apto 51', 'Itaim Bibi',  'São Paulo',      'SP', TRUE),
    (pg_temp.sid('address', 6), pg_temp.customer(5), 0, 'Casa',      '80010010', 'Rua XV de Novembro',        '700',  NULL,      'Centro',      'Curitiba',       'PR', TRUE);

INSERT INTO customer.customer_contacts
    (public_id, customer_id, position, name, job_title, email, phone, notes, is_primary)
VALUES
    (pg_temp.sid('contact', 1), pg_temp.customer(1), 0, 'Roberto Almeida', 'Comprador',            'roberto@bompreco.com.br',  '(11) 3456-7891', NULL,                           TRUE),
    (pg_temp.sid('contact', 2), pg_temp.customer(1), 1, 'Juliana Prado',   'Financeiro',           'financeiro@bompreco.com.br', '(11) 3456-7892', 'Enviar boletos para este e-mail', FALSE),
    (pg_temp.sid('contact', 3), pg_temp.customer(2), 0, 'Fernando Costa',  'Gerente de TI',        'fernando@horizonte.com.br', '(21) 2233-4456', NULL,                          TRUE),
    (pg_temp.sid('contact', 4), pg_temp.customer(3), 0, 'Patrícia Mendes', 'Diretora',             'patricia@sabermais.edu.br', '(31) 3344-5567', 'Decide compras acima de R$ 5 mil', TRUE);

-- =====================================================================================
-- Estoque (stock): depósitos, itens (regras de reposição min/max) e saldo inicial
-- =====================================================================================

INSERT INTO stock.stocks (public_id, description, created_at) VALUES
    (pg_temp.sid('stock', 1), 'Depósito Central', now() - interval '60 days'),
    (pg_temp.sid('stock', 2), 'Loja Centro',      now() - interval '60 days');

INSERT INTO stock.movement_reasons (public_id, description) VALUES
    (pg_temp.sid('reason', 1), 'Saldo inicial'),
    (pg_temp.sid('reason', 2), 'Ajuste de inventário'),
    (pg_temp.sid('reason', 3), 'Avaria')
ON CONFLICT DO NOTHING;

-- current_value é recalculado no fim a partir das movimentações
INSERT INTO stock.items (stock_id, catalog_product_id, catalog_product_public_id, min_value, max_value, created_at)
SELECT pg_temp.stock(i.stock), pg_temp.product(i.prod), pg_temp.sid('prod', i.prod), i.min_value, i.max_value,
       now() - interval '60 days'
FROM (VALUES
    (1, 1, 5, 30), (1, 2, 20, 100), (1, 3, 15, 60), (1, 4, 5, 40),
    (1, 5, 5, 20), (1, 6, 200, 1000), (1, 7, 10, 40), (1, 8, 5, 30),
    (2, 2, 10, 40), (2, 3, 5, 20), (2, 8, 4, 15)
) AS i(stock, prod, min_value, max_value);

-- saldo inicial (resulting_balance é recalculado no fim)
INSERT INTO stock.movements
    (public_id, stock_id, catalog_product_public_id, type, quantity, resulting_balance, reason_id, reference, created_at)
SELECT pg_temp.sid('mov', m.n), pg_temp.stock(m.stock), pg_temp.sid('prod', m.prod), 'ENTRY', m.qty, 0,
       pg_temp.reason(1), 'Saldo inicial', now() - interval '30 days' + m.n * interval '1 minute'
FROM (VALUES
    (1, 1, 1, 5), (2, 1, 2, 40), (3, 1, 3, 8), (4, 1, 4, 3), (5, 1, 5, 6),
    (6, 1, 6, 500), (7, 1, 7, 4), (8, 1, 8, 12),
    (9, 2, 2, 15), (10, 2, 3, 10), (11, 2, 8, 3)
) AS m(n, stock, prod, qty);

-- =====================================================================================
-- CRM (os estágios e motivos de perda vêm da migration V2 do crm-service)
-- =====================================================================================

INSERT INTO crm.leads
    (public_id, name, type, status, stage_id, customer_id, contact_name, company_name, email, phone,
     expected_revenue, probability, priority, expected_closing, description, assigned_to, lost_reason_id,
     created_at, closed_at)
VALUES
    (pg_temp.sid('lead', 1), 'Notebooks para a equipe da clínica', 'LEAD', 'OPEN', pg_temp.stage(1), NULL,
     'Dr. Marcelo Reis', 'Clínica Vida Plena', 'marcelo@vidaplena.med.br', '(11) 4002-8922',
     25000.00, 10, 1, current_date + 45, 'Contato pelo site, quer 6 notebooks.', 'Vendedor Demo', NULL,
     now() - interval '3 days', NULL),
    (pg_temp.sid('lead', 2), 'Rede Wi-Fi do Colégio Saber Mais', 'OPPORTUNITY', 'OPEN', pg_temp.stage(2), pg_temp.sid('customer', 3),
     'Patrícia Mendes', 'Escola Saber Mais Ltda', 'patricia@sabermais.edu.br', '(31) 3344-5567',
     18000.00, 30, 2, current_date + 20, 'Cobertura Wi-Fi em 12 salas.', 'Vendedor Demo', NULL,
     now() - interval '15 days', NULL),
    (pg_temp.sid('lead', 3), 'Renovação do parque de monitores', 'OPPORTUNITY', 'OPEN', pg_temp.stage(3), pg_temp.sid('customer', 2),
     'Fernando Costa', 'Construtora Horizonte S.A.', 'fernando@horizonte.com.br', '(21) 2233-4456',
     45000.00, 60, 3, current_date + 10, '50 monitores 24" para o escritório.', 'Vendedor Demo', NULL,
     now() - interval '25 days', NULL),
    (pg_temp.sid('lead', 4), 'Equipamentos para os caixas', 'OPPORTUNITY', 'WON', pg_temp.stage(4), pg_temp.sid('customer', 1),
     'Roberto Almeida', 'Mercado Bom Preço Ltda', 'roberto@bompreco.com.br', '(11) 3456-7891',
     5119.25, 100, 2, current_date - 5, 'Monitores, teclados e mouses para 5 caixas.', 'Vendedor Demo', NULL,
     now() - interval '40 days', now() - interval '5 days'),
    (pg_temp.sid('lead', 5), 'Contrato de suporte residencial', 'OPPORTUNITY', 'LOST', pg_temp.stage(3), pg_temp.sid('customer', 4),
     'Ana Paula Souza', NULL, 'ana.souza@email.com', '(11) 98765-4321',
     5880.00, 0, 0, current_date - 10, 'Suporte mensal por 12 meses.', 'Vendedor Demo',
     (SELECT lost_reason_id FROM crm.lost_reasons WHERE public_id = 'lost_reason_' || lpad('1', 26, '0')),
     now() - interval '35 days', now() - interval '10 days');

INSERT INTO crm.lead_tags (lead_id, tag) VALUES
    (pg_temp.lead(1), 'site'), (pg_temp.lead(1), 'saúde'),
    (pg_temp.lead(2), 'educação'), (pg_temp.lead(2), 'redes'),
    (pg_temp.lead(3), 'grande conta'),
    (pg_temp.lead(4), 'varejo');

INSERT INTO crm.activities (public_id, lead_id, type, summary, note, due_date, assigned_to, done, done_at, created_at) VALUES
    (pg_temp.sid('activity', 1), pg_temp.lead(1), 'CALL',    'Ligar para entender a necessidade', NULL,                       current_date + 1,  'Vendedor Demo', FALSE, NULL, now() - interval '3 days'),
    (pg_temp.sid('activity', 2), pg_temp.lead(2), 'MEETING', 'Visita técnica ao colégio',          'Levar o técnico de redes', current_date + 3,  'Vendedor Demo', FALSE, NULL, now() - interval '10 days'),
    (pg_temp.sid('activity', 3), pg_temp.lead(3), 'EMAIL',   'Enviar proposta revisada',           NULL,                       current_date - 2,  'Vendedor Demo', FALSE, NULL, now() - interval '7 days'),
    (pg_temp.sid('activity', 4), pg_temp.lead(3), 'TODO',    'Cotar 50 monitores com o fornecedor', NULL,                      current_date - 6,  'Vendedor Demo', TRUE,  now() - interval '6 days', now() - interval '20 days'),
    (pg_temp.sid('activity', 5), pg_temp.lead(4), 'MEETING', 'Fechamento com o comprador',          NULL,                      current_date - 5,  'Vendedor Demo', TRUE,  now() - interval '5 days', now() - interval '12 days');

-- =====================================================================================
-- Vendas (sales)
--   1 DRAFT · 2 SENT · 3 CONFIRMED (a entregar / a faturar) · 4 DONE (entregue e faturado) · 5 CANCELLED
-- =====================================================================================

INSERT INTO sales.sales_orders
    (public_id, number, state, customer_id, customer_name, lead_id, valid_until, delivery_address, salesperson, note,
     delivery_status, invoice_status, confirmed_at, created_at)
SELECT pg_temp.sid('sales_order', o.n), 'S' || lpad(nextval('sales.sales_order_number_seq')::text, 5, '0'), o.state,
       pg_temp.sid('customer', o.cust), c.name, o.lead, current_date + o.valid_days, o.address, 'Vendedor Demo', o.note,
       o.delivery, o.invoice, now() - o.confirmed_ago, now() - o.created_ago
FROM (VALUES
    (1, 'DRAFT',     5, NULL,                  30, 'Rua XV de Novembro, 700 - Curitiba/PR',      NULL,                          'NONE',    'NONE',       NULL::interval,      interval '1 day'),
    (2, 'SENT',      3, pg_temp.sid('lead', 2), 15, 'Av. Afonso Pena, 1500 - Belo Horizonte/MG', 'Instalação inclusa (6 h).',   'NONE',    'NONE',       NULL,                interval '4 days'),
    (3, 'CONFIRMED', 1, pg_temp.sid('lead', 4), 30, 'Av. Paulista, 1000 - São Paulo/SP',          NULL,                          'PENDING', 'TO_INVOICE', interval '5 days',   interval '9 days'),
    (4, 'DONE',      2, NULL,                  30, 'Av. Rio Branco, 156 - Rio de Janeiro/RJ',    NULL,                          'DONE',    'INVOICED',   interval '22 days',  interval '25 days'),
    (5, 'CANCELLED', 4, NULL,                  30, 'Rua Joaquim Floriano, 72 - São Paulo/SP',    'Cliente desistiu da compra.', 'NONE',    'NONE',       NULL,                interval '12 days')
) AS o(n, state, cust, lead, valid_days, address, note, delivery, invoice, confirmed_ago, created_ago)
JOIN customer.customers c ON c.public_id = pg_temp.sid('customer', o.cust)
ORDER BY o.n;

INSERT INTO sales.sales_order_lines
    (sales_order_id, position, product_id, description, product_type, quantity, unit_price, discount_percent, tax_percent, delivered_quantity)
SELECT pg_temp.sales_order(l.so), l.pos, pg_temp.sid('prod', l.prod), p.description, p.type, l.qty, l.price, l.disc, l.tax, l.delivered
FROM (VALUES
    (1, 0, 1, 1,   4299.00, 0, 0, 0),
    (1, 1, 2, 1,     89.90, 0, 0, 0),
    (2, 0, 5, 4,    349.00, 0, 0, 0),
    (2, 1, 6, 100,    3.50, 0, 0, 0),
    (2, 2, 9, 6,    150.00, 0, 5, 0),
    (3, 0, 4, 5,    899.00, 5, 0, 0),
    (3, 1, 3, 5,     79.90, 0, 0, 0),
    (3, 2, 2, 5,     89.90, 0, 0, 0),
    (4, 0, 1, 2,   4299.00, 0, 0, 2),
    (4, 1, 8, 2,    249.00, 0, 0, 2),
    (4, 2, 10, 1,   490.00, 0, 5, 0),
    (5, 0, 8, 1,    249.00, 0, 0, 0)
) AS l(so, pos, prod, qty, price, disc, tax, delivered)
JOIN catalog.products p ON p.public_id = pg_temp.sid('prod', l.prod);

-- =====================================================================================
-- Compras (purchase)
--   1 DRAFT (RFQ) · 2 DONE (recebido e com conta lançada) · 3 CONFIRMED (a receber / a lançar)
-- =====================================================================================

INSERT INTO purchase.purchase_orders
    (public_id, number, state, supplier_id, supplier_name, origin, expected_date, ship_to_address, buyer, note,
     receipt_status, bill_status, confirmed_at, created_at)
SELECT pg_temp.sid('purchase_order', o.n), 'P' || lpad(nextval('purchase.purchase_order_number_seq')::text, 5, '0'), o.state,
       pg_temp.sid('supplier', o.sup), s.name, o.origin, current_date + o.expected_days,
       'Depósito Central - Rua das Indústrias, 200 - Guarulhos/SP', 'Comprador Demo', NULL,
       o.receipt, o.bill, now() - o.confirmed_ago, now() - o.created_ago
FROM (VALUES
    (1, 'DRAFT',     3, 'Reposição',             7,   'NONE',    'NONE',    NULL::interval,     interval '2 days'),
    (2, 'DONE',      1, 'Reposição',             -12, 'DONE',    'BILLED',  interval '18 days', interval '20 days'),
    (3, 'CONFIRMED', 2, 'Reposição periféricos', 5,   'PENDING', 'TO_BILL', interval '3 days',  interval '4 days')
) AS o(n, state, sup, origin, expected_days, receipt, bill, confirmed_ago, created_ago)
JOIN catalog.suppliers s ON s.public_id = pg_temp.sid('supplier', o.sup)
ORDER BY o.n;

INSERT INTO purchase.purchase_order_lines
    (purchase_order_id, position, product_id, description, product_type, quantity, unit_price, discount_percent, tax_percent, received_quantity)
SELECT pg_temp.purchase_order(l.po), l.pos, pg_temp.sid('prod', l.prod), p.description, p.type, l.qty, l.price, 0, 0, l.received
FROM (VALUES
    (1, 0, 7, 20, 210.00, 0),
    (2, 0, 1, 10, 3200.00, 10),
    (2, 1, 4, 15, 620.00, 15),
    (3, 0, 2, 30, 45.00, 0),
    (3, 1, 3, 30, 38.00, 0),
    (3, 2, 5, 10, 210.00, 0)
) AS l(po, pos, prod, qty, price, received)
JOIN catalog.products p ON p.public_id = pg_temp.sid('prod', l.prod);

-- =====================================================================================
-- Transferências (stock.pickings), na ordem em que seriam numeradas
--   1 IN  (P00002, validada)   2 OUT (S00004, validada)
--   3 OUT (S00003, a entregar) 4 IN  (P00003, a receber)   5 INT (Central -> Loja, rascunho)
-- =====================================================================================

INSERT INTO stock.pickings
    (public_id, number, type, state, source_stock_id, destination_stock_id, partner_id, partner_name,
     origin_type, origin_id, origin_number, scheduled_date, note, created_at, done_at)
SELECT pg_temp.sid('picking', k.n),
       CASE k.type
           WHEN 'RECEIPT'  THEN 'IN/'  || lpad(nextval('stock.picking_receipt_seq')::text, 5, '0')
           WHEN 'DELIVERY' THEN 'OUT/' || lpad(nextval('stock.picking_delivery_seq')::text, 5, '0')
           ELSE                 'INT/' || lpad(nextval('stock.picking_internal_seq')::text, 5, '0')
       END,
       k.type, k.state, pg_temp.stock(k.src), pg_temp.stock(k.dst), k.partner_id, k.partner_name,
       k.origin_type, k.origin_id, k.origin_number, current_date + k.scheduled_days, k.note,
       now() - k.created_ago, now() - k.done_ago
FROM (VALUES
    (1, 'RECEIPT',  'DONE',      NULL::int, 1,    pg_temp.sid('supplier', 1), 'Distribuidora Tech Brasil Ltda',
        'PURCHASE_ORDER', pg_temp.sid('purchase_order', 2), pg_temp.po_number(2), -12, NULL,
        interval '18 days', interval '12 days'),
    (2, 'DELIVERY', 'DONE',      1,         NULL, pg_temp.sid('customer', 2), 'Construtora Horizonte S.A.',
        'SALES_ORDER', pg_temp.sid('sales_order', 4), pg_temp.so_number(4), -8, NULL,
        interval '22 days', interval '8 days'),
    (3, 'DELIVERY', 'CONFIRMED', 1,         NULL, pg_temp.sid('customer', 1), 'Mercado Bom Preço Ltda',
        'SALES_ORDER', pg_temp.sid('sales_order', 3), pg_temp.so_number(3), 2, NULL,
        interval '5 days', NULL::interval),
    (4, 'RECEIPT',  'CONFIRMED', NULL,      1,    pg_temp.sid('supplier', 2), 'Alfa Componentes Eletrônicos Ltda',
        'PURCHASE_ORDER', pg_temp.sid('purchase_order', 3), pg_temp.po_number(3), 5, NULL,
        interval '3 days', NULL),
    (5, 'INTERNAL', 'DRAFT',     1,         2,    NULL, NULL,
        NULL, NULL, NULL, 1, 'Abastecer a loja',
        interval '1 day', NULL)
) AS k(n, type, state, src, dst, partner_id, partner_name, origin_type, origin_id, origin_number,
       scheduled_days, note, created_ago, done_ago)
ORDER BY k.n;

INSERT INTO stock.picking_lines (picking_id, position, catalog_product_public_id, description, demand, done)
SELECT pg_temp.picking(l.pick), l.pos, pg_temp.sid('prod', l.prod), p.description, l.demand, l.done
FROM (VALUES
    (1, 0, 1, 10, 10), (1, 1, 4, 15, 15),
    (2, 0, 1, 2, 2),   (2, 1, 8, 2, 2),
    (3, 0, 4, 5, 0),   (3, 1, 3, 5, 0),   (3, 2, 2, 5, 0),
    (4, 0, 2, 30, 0),  (4, 1, 3, 30, 0),  (4, 2, 5, 10, 0),
    (5, 0, 2, 10, 0),  (5, 1, 8, 5, 0)
) AS l(pick, pos, prod, demand, done)
JOIN catalog.products p ON p.public_id = pg_temp.sid('prod', l.prod);

-- movimentações das transferências validadas (referência = número da transferência, como o ValidatePickingUseCase)
INSERT INTO stock.movements
    (public_id, stock_id, catalog_product_public_id, type, quantity, resulting_balance, reason_id, reference, created_at)
SELECT pg_temp.sid('mov', m.n), pg_temp.stock(1), pg_temp.sid('prod', m.prod), m.type, m.qty, 0, NULL,
       pg_temp.picking_number(m.pick), now() - m.ago
FROM (VALUES
    (12, 1, 1, 'ENTRY', 10, interval '12 days'),
    (13, 1, 4, 'ENTRY', 15, interval '12 days'),
    (14, 2, 1, 'EXIT',  2,  interval '8 days'),
    (15, 2, 8, 'EXIT',  2,  interval '8 days')
) AS m(n, pick, prod, type, qty, ago);

-- saldo acumulado de cada movimentação e saldo atual de cada item
UPDATE stock.movements m
SET resulting_balance = r.balance
FROM (
    SELECT stock_movement_id,
           sum(CASE WHEN type IN ('EXIT', 'TRANSFER_OUT') THEN -quantity ELSE quantity END)
               OVER (PARTITION BY stock_id, catalog_product_public_id ORDER BY created_at, stock_movement_id) AS balance
    FROM stock.movements
    WHERE public_id LIKE 'mov\_00000000SEED%'
) r
WHERE m.stock_movement_id = r.stock_movement_id;

UPDATE stock.items i
SET current_value = last.resulting_balance
FROM (
    SELECT DISTINCT ON (stock_id, catalog_product_public_id) stock_id, catalog_product_public_id, resulting_balance
    FROM stock.movements
    WHERE public_id LIKE 'mov\_00000000SEED%'
    ORDER BY stock_id, catalog_product_public_id, created_at DESC, stock_movement_id DESC
) last
WHERE i.stock_id = last.stock_id AND i.catalog_product_public_id = last.catalog_product_public_id;

-- =====================================================================================
-- Financeiro (accounting)
--   1 INV  de S00004, lançada e paga          2 INV de S00003, rascunho automático
--   3 INV  avulsa para Carlos, lançada e vencida
--   4 BILL de P00002, lançada e paga parcialmente   5 BILL de P00003, rascunho automático
-- =====================================================================================

INSERT INTO accounting.invoices
    (public_id, kind, number, state, partner_id, partner_name, invoice_date, due_date, reference, note,
     origin_type, origin_id, origin_number, posted_at, created_at)
SELECT pg_temp.sid('invoice', f.n), f.kind,
       CASE WHEN f.state = 'POSTED' AND f.kind = 'CUSTOMER_INVOICE'
                THEN 'INV/'  || lpad(nextval('accounting.customer_invoice_number_seq')::text, 5, '0')
            WHEN f.state = 'POSTED'
                THEN 'BILL/' || lpad(nextval('accounting.vendor_bill_number_seq')::text, 5, '0')
       END,
       f.state, f.partner_id, f.partner_name, current_date + f.date_days, current_date + f.due_days, f.reference, NULL,
       f.origin_type, f.origin_id, f.origin_number, now() - f.posted_ago, now() - f.created_ago
FROM (VALUES
    (1, 'CUSTOMER_INVOICE', 'POSTED', pg_temp.sid('customer', 2), 'Construtora Horizonte S.A.', -20, 10, NULL,
        'SALES_ORDER', pg_temp.sid('sales_order', 4), pg_temp.so_number(4), interval '20 days', interval '22 days'),
    (2, 'CUSTOMER_INVOICE', 'DRAFT',  pg_temp.sid('customer', 1), 'Mercado Bom Preço Ltda', NULL, NULL, NULL,
        'SALES_ORDER', pg_temp.sid('sales_order', 3), pg_temp.so_number(3), NULL::interval, interval '5 days'),
    (3, 'CUSTOMER_INVOICE', 'POSTED', pg_temp.sid('customer', 5), 'Carlos Eduardo Lima', -45, -15, NULL,
        NULL, NULL, NULL, interval '45 days', interval '45 days'),
    (4, 'VENDOR_BILL',      'POSTED', pg_temp.sid('supplier', 1), 'Distribuidora Tech Brasil Ltda', -10, 20, 'NF-e 45872',
        'PURCHASE_ORDER', pg_temp.sid('purchase_order', 2), pg_temp.po_number(2), interval '10 days', interval '18 days'),
    (5, 'VENDOR_BILL',      'DRAFT',  pg_temp.sid('supplier', 2), 'Alfa Componentes Eletrônicos Ltda', NULL, NULL, NULL,
        'PURCHASE_ORDER', pg_temp.sid('purchase_order', 3), pg_temp.po_number(3), NULL, interval '3 days')
) AS f(n, kind, state, partner_id, partner_name, date_days, due_days, reference,
       origin_type, origin_id, origin_number, posted_ago, created_ago)
ORDER BY f.n;

-- as faturas de pedido copiam as linhas do pedido
INSERT INTO accounting.invoice_lines
    (invoice_id, position, product_id, description, quantity, unit_price, discount_percent, tax_percent)
SELECT pg_temp.invoice(x.inv), l.position, l.product_id, l.description, l.quantity, l.unit_price, l.discount_percent, l.tax_percent
FROM (VALUES (1, 4), (2, 3)) AS x(inv, so)
JOIN sales.sales_order_lines l ON l.sales_order_id = pg_temp.sales_order(x.so)
UNION ALL
SELECT pg_temp.invoice(x.inv), l.position, l.product_id, l.description, l.quantity, l.unit_price, l.discount_percent, l.tax_percent
FROM (VALUES (4, 2), (5, 3)) AS x(inv, po)
JOIN purchase.purchase_order_lines l ON l.purchase_order_id = pg_temp.purchase_order(x.po)
UNION ALL
SELECT pg_temp.invoice(3), 0, pg_temp.sid('prod', 9), 'Instalação e configuração (hora técnica)', 3, 150.00, 0, 5;

-- totais denormalizados: por linha, subtotal arredondado após o desconto e imposto arredondado em cima
UPDATE accounting.invoices i
SET untaxed_amount = t.untaxed, tax_amount = t.tax, total_amount = t.untaxed + t.tax
FROM (
    SELECT invoice_id, sum(sub) AS untaxed, sum(round(sub * tax_percent / 100, 2)) AS tax
    FROM (
        SELECT invoice_id, tax_percent, round(quantity * unit_price * (1 - discount_percent / 100), 2) AS sub
        FROM accounting.invoice_lines
    ) l
    GROUP BY invoice_id
) t
WHERE i.invoice_id = t.invoice_id AND i.public_id LIKE 'invoice\_00000000SEED%';

INSERT INTO accounting.payments (public_id, invoice_id, amount, paid_on, method, reference)
SELECT pg_temp.sid('payment', 1), invoice_id, total_amount, current_date - 5, 'PIX', 'PIX recebido'
FROM accounting.invoices WHERE invoice_id = pg_temp.invoice(1)
UNION ALL
SELECT pg_temp.sid('payment', 2), pg_temp.invoice(4), 20000.00, current_date - 2, 'BOLETO', 'Parcela 1/2';

UPDATE accounting.invoices i
SET paid_amount = p.paid,
    payment_state = CASE WHEN p.paid >= i.total_amount THEN 'PAID' WHEN p.paid > 0 THEN 'PARTIAL' ELSE 'NOT_PAID' END
FROM (
    SELECT inv.invoice_id, coalesce(sum(pay.amount), 0) AS paid
    FROM accounting.invoices inv
    LEFT JOIN accounting.payments pay ON pay.invoice_id = inv.invoice_id
    WHERE inv.public_id LIKE 'invoice\_00000000SEED%'
    GROUP BY inv.invoice_id
) p
WHERE i.invoice_id = p.invoice_id;

COMMIT;

\echo 'Seed aplicado:'
SELECT 'produtos' AS tabela, count(*) FROM catalog.products
UNION ALL SELECT 'clientes', count(*) FROM customer.customers
UNION ALL SELECT 'itens de estoque', count(*) FROM stock.items
UNION ALL SELECT 'transferências', count(*) FROM stock.pickings
UNION ALL SELECT 'leads/oportunidades', count(*) FROM crm.leads
UNION ALL SELECT 'pedidos de venda', count(*) FROM sales.sales_orders
UNION ALL SELECT 'pedidos de compra', count(*) FROM purchase.purchase_orders
UNION ALL SELECT 'faturas/contas', count(*) FROM accounting.invoices;
