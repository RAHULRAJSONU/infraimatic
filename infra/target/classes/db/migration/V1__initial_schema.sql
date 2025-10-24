--
-- Initial database schema for Infraimatic
--

CREATE TABLE IF NOT EXISTS template_definitions (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    version INTEGER NOT NULL,
    name VARCHAR(255),
    json_schema JSONB,
    attribute_refs JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_template UNIQUE (tenant_id, type, version)
);

CREATE TABLE IF NOT EXISTS attribute_definitions (
    attribute_ref VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255),
    json_schema JSONB,
    ui_hint VARCHAR(255),
    nlp_aliases JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS records (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    client_submission_id VARCHAR(255),
    type VARCHAR(255) NOT NULL,
    template_id BIGINT,
    template_version INTEGER,
    status VARCHAR(32) NOT NULL,
    payload JSONB,
    normalized JSONB,
    processing_meta JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_template FOREIGN KEY (template_id) REFERENCES template_definitions(id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    tenant_id VARCHAR(255),
    entity_type VARCHAR(255) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(32) NOT NULL,
    actor VARCHAR(255),
    before JSONB,
    after JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);