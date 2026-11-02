{{ config(materialized='view') }}

WITH ranked_events AS (
    SELECT
        event_id,
        batch_id,
        trace_id,
        span_id,
        service_name,
        http_method,
        route,
        status_code,
        latency_ms,
        client_ip,
        user_agent,
        event_timestamp,
        created_at,
        ROW_NUMBER() OVER (
            PARTITION BY event_id
            ORDER BY event_timestamp DESC
        ) AS dedup_rank
    FROM {{ source('deeptrace_lakehouse', 'silver_http_events') }}
)

SELECT
    event_id,
    batch_id,
    trace_id,
    span_id,
    service_name,
    http_method,
    route,
    status_code,
    CASE
        WHEN status_code >= 200 AND status_code < 300 THEN '2xx_SUCCESS'
        WHEN status_code >= 300 AND status_code < 400 THEN '3xx_REDIRECTION'
        WHEN status_code >= 400 AND status_code < 500 THEN '4xx_CLIENT_ERROR'
        WHEN status_code >= 500 AND status_code < 600 THEN '5xx_SERVER_ERROR'
        ELSE 'UNKNOWN'
        END AS http_status_class,
    CASE WHEN status_code >= 500 THEN 1 ELSE 0 END AS is_server_error,
    CASE WHEN status_code >= 400 AND status_code < 500 THEN 1 ELSE 0 END AS is_client_error,
    CASE WHEN latency_ms > 1000.0 THEN 1 ELSE 0 END AS is_slow_request,
    latency_ms,
    client_ip,
    user_agent,
    event_timestamp,
    created_at
FROM ranked_events
WHERE dedup_rank = 1