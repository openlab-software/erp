-- Pipeline stages (kanban columns). The default pipeline mirrors Odoo CRM: New, Qualified, Proposition, Won.
CREATE TABLE IF NOT EXISTS crm.stages (
    stage_id     BIGSERIAL    PRIMARY KEY,
    public_id    TEXT         NOT NULL UNIQUE,
    name         TEXT         NOT NULL,
    position     INT          NOT NULL DEFAULT 0,
    probability  INT          NOT NULL DEFAULT 0,
    is_won       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ,
    CONSTRAINT chk_stages_probability CHECK (probability BETWEEN 0 AND 100)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_stages_name_lower ON crm.stages (lower(name));
CREATE INDEX IF NOT EXISTS idx_stages_position ON crm.stages (position, stage_id);

INSERT INTO crm.stages (public_id, name, position, probability, is_won) VALUES
    ('stage_00000000000000000000000001', 'Novo',        10, 10,  FALSE),
    ('stage_00000000000000000000000002', 'Qualificado', 20, 30,  FALSE),
    ('stage_00000000000000000000000003', 'Proposta',    30, 60,  FALSE),
    ('stage_00000000000000000000000004', 'Ganho',       40, 100, TRUE)
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS crm.lost_reasons (
    lost_reason_id  BIGSERIAL    PRIMARY KEY,
    public_id       TEXT         NOT NULL UNIQUE,
    name            TEXT         NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_lost_reasons_name_lower ON crm.lost_reasons (lower(name));

INSERT INTO crm.lost_reasons (public_id, name) VALUES
    ('lost_reason_00000000000000000000000001', 'Preço muito alto'),
    ('lost_reason_00000000000000000000000002', 'Sem orçamento'),
    ('lost_reason_00000000000000000000000003', 'Escolheu o concorrente')
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS crm.leads (
    lead_id           BIGSERIAL      PRIMARY KEY,
    public_id         TEXT           NOT NULL UNIQUE,
    name              TEXT           NOT NULL,
    type              TEXT           NOT NULL,
    status            TEXT           NOT NULL,
    stage_id          BIGINT         NOT NULL REFERENCES crm.stages (stage_id),
    customer_id       TEXT,
    contact_name      TEXT,
    company_name      TEXT,
    email             TEXT,
    phone             TEXT,
    expected_revenue  NUMERIC(15, 2) NOT NULL DEFAULT 0,
    probability       INT            NOT NULL DEFAULT 0,
    priority          INT            NOT NULL DEFAULT 0,
    expected_closing  DATE,
    description       TEXT,
    assigned_to       TEXT,
    lost_reason_id    BIGINT         REFERENCES crm.lost_reasons (lost_reason_id),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ,
    closed_at         TIMESTAMPTZ,
    CONSTRAINT chk_leads_type        CHECK (type IN ('LEAD', 'OPPORTUNITY')),
    CONSTRAINT chk_leads_status      CHECK (status IN ('OPEN', 'WON', 'LOST')),
    CONSTRAINT chk_leads_probability CHECK (probability BETWEEN 0 AND 100),
    CONSTRAINT chk_leads_priority    CHECK (priority BETWEEN 0 AND 3)
);

CREATE INDEX IF NOT EXISTS idx_leads_stage ON crm.leads (stage_id);
CREATE INDEX IF NOT EXISTS idx_leads_status ON crm.leads (status);
CREATE INDEX IF NOT EXISTS idx_leads_customer ON crm.leads (customer_id);
CREATE INDEX IF NOT EXISTS idx_leads_created_at ON crm.leads (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_leads_name_lower ON crm.leads (lower(name));

CREATE TABLE IF NOT EXISTS crm.lead_tags (
    lead_id  BIGINT NOT NULL REFERENCES crm.leads (lead_id) ON DELETE CASCADE,
    tag      TEXT   NOT NULL,
    PRIMARY KEY (lead_id, tag)
);

CREATE TABLE IF NOT EXISTS crm.activities (
    activity_id  BIGSERIAL    PRIMARY KEY,
    public_id    TEXT         NOT NULL UNIQUE,
    lead_id      BIGINT       NOT NULL REFERENCES crm.leads (lead_id) ON DELETE CASCADE,
    type         TEXT         NOT NULL,
    summary      TEXT         NOT NULL,
    note         TEXT,
    due_date     DATE,
    assigned_to  TEXT,
    done         BOOLEAN      NOT NULL DEFAULT FALSE,
    done_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_activities_type CHECK (type IN ('TODO', 'CALL', 'MEETING', 'EMAIL'))
);

CREATE INDEX IF NOT EXISTS idx_activities_lead ON crm.activities (lead_id);
CREATE INDEX IF NOT EXISTS idx_activities_agenda ON crm.activities (done, due_date);
