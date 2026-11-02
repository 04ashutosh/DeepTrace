-- Singular test: returns rows violating the rule (must return 0 rows to pass)
SELECT
    event_id,
    service_name,
    latency_ms
FROM {{ ref('stg_http_events') }}
WHERE latency_ms < 0.0