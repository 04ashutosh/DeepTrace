-- Phase 5: Distributed Tracing, Spans & Service Dependency Graph
CREATE SCHEMA IF NOT EXISTS observability;

CREATE TABLE IF NOT EXISTS observability.spans (
                                                   span_id VARCHAR(64) PRIMARY KEY,
    trace_id VARCHAR(64) NOT NULL,
    parent_span_id VARCHAR(64) NULL,
    service_name VARCHAR(100) NOT NULL,
    operation_name VARCHAR(100) NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    duration_ms DOUBLE PRECISION NOT NULL,
    status_code INT NOT NULL,
    error_message TEXT NULL,
    attributes JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_spans_trace_id ON observability.spans(trace_id);
CREATE INDEX IF NOT EXISTS idx_spans_service_start ON observability.spans(service_name, start_time);
CREATE INDEX IF NOT EXISTS idx_spans_parent_span ON observability.spans(parent_span_id);

CREATE TABLE IF NOT EXISTS observability.service_dependencies (
                                                                  caller_service VARCHAR(100) NOT NULL,
    callee_service VARCHAR(100) NOT NULL,
    call_count BIGINT DEFAULT 0,
    error_count BIGINT DEFAULT 0,
    avg_latency_ms DOUBLE PRECISION DEFAULT 0.0,
    p95_latency_ms DOUBLE PRECISION DEFAULT 0.0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_service_dependencies PRIMARY KEY (caller_service, callee_service)
    );

CREATE INDEX IF NOT EXISTS idx_svc_dep_caller ON observability.service_dependencies(caller_service);
CREATE INDEX IF NOT EXISTS idx_svc_dep_callee ON observability.service_dependencies(callee_service);

-- Seed initial microservice topology edges
INSERT INTO observability.service_dependencies (caller_service, callee_service, call_count, error_count, avg_latency_ms, p95_latency_ms)
VALUES
    ('frontend-proxy', 'api-gateway', 12500, 15, 12.4, 25.0),
    ('api-gateway', 'auth-service', 11800, 4, 18.2, 35.1),
    ('api-gateway', 'order-service', 8400, 92, 45.6, 120.5),
    ('order-service', 'inventory-api', 8350, 10, 15.3, 28.0),
    ('order-service', 'payment-gateway', 8300, 82, 142.1, 480.0),
    ('order-service', 'notification-worker', 7900, 0, 8.5, 14.2)
    ON CONFLICT (caller_service, callee_service) DO NOTHING;