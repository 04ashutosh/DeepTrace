-- Phase 1 AI Engineering: Incidents and Agent Investigation Steps
CREATE TABLE IF NOT EXISTS observability.incidents (
                                                       incident_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    trace_id VARCHAR(64) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'HIGH', -- 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',   -- 'OPEN', 'INVESTIGATING', 'RESOLVED'
    root_cause TEXT NULL,
    mitigation_steps TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ NULL
    );

CREATE INDEX IF NOT EXISTS idx_incidents_status ON observability.incidents(status);
CREATE INDEX IF NOT EXISTS idx_incidents_trace ON observability.incidents(trace_id);

CREATE TABLE IF NOT EXISTS observability.investigation_steps (
                                                                 step_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_id UUID NOT NULL REFERENCES observability.incidents(incident_id) ON DELETE CASCADE,
    step_type VARCHAR(30) NOT NULL, -- 'THOUGHT', 'ACTION', 'OBSERVATION', 'CONCLUSION'
    content TEXT NOT NULL,
    tool_name VARCHAR(100) NULL,
    tool_input_json TEXT NULL,
    tool_output_json TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_steps_incident ON observability.investigation_steps(incident_id);

-- Seed a sample incident for testing
INSERT INTO observability.incidents (title, service_name, trace_id, severity, status)
VALUES
    ('Payment Gateway Upstream Timeout', 'payment-gateway', 'tr-checkout-err-9021', 'CRITICAL', 'OPEN')
    ON CONFLICT DO NOTHING;