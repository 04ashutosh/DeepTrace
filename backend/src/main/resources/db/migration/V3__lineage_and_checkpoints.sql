-- Phase 4: State Checkpoints & Data Lineage Graph
CREATE TABLE IF NOT EXISTS metadata.pipeline_checkpoints (
                                                             checkpoint_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_name VARCHAR(100) NOT NULL UNIQUE,
    high_watermark TIMESTAMPTZ NOT NULL,
    records_processed BIGINT DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS metadata.lineage_edges (
                                                      edge_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_dataset VARCHAR(100) NOT NULL,
    target_dataset VARCHAR(100) NOT NULL,
    transformation_type VARCHAR(50) NOT NULL, -- e.g. 'SPARK_INCREMENTAL', 'DBT_MODEL', 'STREAMING'
    job_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_lineage_edge UNIQUE (source_dataset, target_dataset, job_name)
    );

CREATE INDEX IF NOT EXISTS idx_lineage_source ON metadata.lineage_edges(source_dataset);
CREATE INDEX IF NOT EXISTS idx_lineage_target ON metadata.lineage_edges(target_dataset);

-- Seed initial known lineage relationships from Phase 1, Phase 2, and Phase 3
INSERT INTO metadata.lineage_edges (source_dataset, target_dataset, transformation_type, job_name)
VALUES
    ('data/landing/http_events.csv', 'raw.raw_http_events', 'PYTHON_STREAMING', 'csv_ingestor'),
    ('raw.raw_http_events', 'analytics.stg_http_events', 'DBT_VIEW', 'stg_http_events'),
    ('analytics.stg_http_events', 'analytics.int_service_traffic_flow', 'DBT_VIEW', 'int_service_traffic_flow'),
    ('analytics.int_service_traffic_flow', 'analytics.mart_service_health', 'DBT_TABLE', 'mart_service_health'),
    ('raw.raw_http_events', 'data/lakehouse/parquet_events', 'SPARK_BATCH', 'historical_aggregator')
    ON CONFLICT (source_dataset, target_dataset, job_name) DO NOTHING;