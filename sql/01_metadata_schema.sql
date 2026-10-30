CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Dataset Catalog Registry
CREATE TABLE IF NOT EXISTS datasets (
                                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(128) NOT NULL UNIQUE,
    urn VARCHAR(256) NOT NULL UNIQUE,
    layer VARCHAR(32) NOT NULL CHECK (layer IN ('BRONZE', 'SILVER', 'GOLD')),
    format VARCHAR(32) NOT NULL,
    storage_location TEXT NOT NULL,
    schema_definition JSONB NOT NULL,
    record_count BIGINT DEFAULT 0,
    last_successful_run_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Pipeline Definitions
CREATE TABLE IF NOT EXISTS pipelines (
                                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(128) NOT NULL UNIQUE,
    description TEXT,
    schedule_type VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Pipeline Execution Run Tracking
CREATE TABLE IF NOT EXISTS pipeline_runs (
                                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pipeline_id UUID NOT NULL REFERENCES pipelines(id) ON DELETE CASCADE,
    run_uuid UUID NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'SUCCEEDED', 'FAILED', 'CANCELLED')),
    input_file VARCHAR(512),
    target_dataset_id UUID REFERENCES datasets(id),
    records_read BIGINT DEFAULT 0,
    records_accepted BIGINT DEFAULT 0,
    records_rejected BIGINT DEFAULT 0,
    records_written BIGINT DEFAULT 0,
    duration_ms BIGINT DEFAULT 0,
    error_message TEXT,
    execution_metadata JSONB DEFAULT '{}'::jsonb,
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ
    );

CREATE INDEX IF NOT EXISTS idx_pipeline_runs_pipeline_id ON pipeline_runs(pipeline_id);
CREATE INDEX IF NOT EXISTS idx_pipeline_runs_status ON pipeline_runs(status);
CREATE INDEX IF NOT EXISTS idx_pipeline_runs_started_at ON pipeline_runs(started_at DESC);

-- Seed Initial Default Pipeline & Dataset
INSERT INTO datasets (name, urn, layer, format, storage_location, schema_definition)
VALUES (
           'silver_http_events',
           'urn:deeptrace:dataset:silver:silver_http_events',
           'SILVER',
           'POSTGRES_TABLE',
           'public.silver_http_events',
           '{"fields": ["event_id", "trace_id", "span_id", "service_name", "http_method", "route", "status_code", "latency_ms", "client_ip", "user_agent", "event_timestamp"]}'::jsonb
       ) ON CONFLICT (name) DO NOTHING;

INSERT INTO pipelines (name, description, schedule_type)
VALUES (
           'http_log_pipeline',
           'Standard ingestion and validation pipeline for distributed HTTP access logs',
           'MANUAL'
       ) ON CONFLICT (name) DO NOTHING;