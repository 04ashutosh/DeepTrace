-- Bronze Raw Payload (Preserves original unparsed JSON for audit/reprocessing)
CREATE TABLE IF NOT EXISTS bronze_http_events (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  batch_id UUID NOT NULL,
                                                  file_source VARCHAR(256) NOT NULL,
    raw_payload JSONB NOT NULL,
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_bronze_http_events_batch_id ON bronze_http_events(batch_id);

-- Silver Validated Events (Normalized, typed, query-optimized)
CREATE TABLE IF NOT EXISTS silver_http_events (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  batch_id UUID NOT NULL,
                                                  event_id UUID NOT NULL UNIQUE,
                                                  trace_id VARCHAR(64) NOT NULL,
    span_id VARCHAR(32) NOT NULL,
    service_name VARCHAR(64) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    route VARCHAR(256) NOT NULL,
    status_code INTEGER NOT NULL CHECK (status_code >= 100 AND status_code <= 599),
    latency_ms NUMERIC(10, 2) NOT NULL CHECK (latency_ms >= 0.0),
    client_ip INET,
    user_agent VARCHAR(256),
    event_timestamp TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Compound indexes for analytical queries
CREATE INDEX IF NOT EXISTS idx_silver_http_events_service_time ON silver_http_events(service_name, event_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_silver_http_events_status_time ON silver_http_events(status_code, event_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_silver_http_events_trace_id ON silver_http_events(trace_id);
CREATE INDEX IF NOT EXISTS idx_silver_http_events_batch_id ON silver_http_events(batch_id);

-- Quarantine Table for Invalid/Corrupted Records
CREATE TABLE IF NOT EXISTS rejected_records (
                                                id BIGSERIAL PRIMARY KEY,
                                                batch_id UUID NOT NULL,
                                                source_file VARCHAR(256) NOT NULL,
    line_number INTEGER NOT NULL,
    raw_content TEXT NOT NULL,
    rejection_reason TEXT NOT NULL,
    failed_rule VARCHAR(128) NOT NULL,
    rejected_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_rejected_records_batch_id ON rejected_records(batch_id);