{{ config(materialized='view') }}

WITH base AS (
    SELECT * FROM {{ ref('stg_http_events') }}
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
    http_status_class,
    is_server_error,
    is_client_error,
    is_slow_request,
    latency_ms,
    -- Window analytical function: latency of the preceding request on the same service
    LAG(latency_ms) OVER (
        PARTITION BY service_name
        ORDER BY event_timestamp
    ) AS prev_request_latency_ms,
    -- Time elapsed since preceding request on same service
    ROUND(
            CAST(
                    EXTRACT(EPOCH FROM (event_timestamp - LAG(event_timestamp) OVER (
                PARTITION BY service_name ORDER BY event_timestamp
            ))) AS numeric
            ), 3
    ) AS seconds_since_prev_request,
    client_ip,
    event_timestamp
FROM base