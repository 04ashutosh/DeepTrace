{{ config(materialized='table') }}

WITH time_buckets AS (
    SELECT
        service_name,
        date_bin('5 minutes', event_timestamp, TIMESTAMPTZ '2026-01-01 00:00:00Z') AS window_start,
        COUNT(*) AS total_requests,
        COUNT(*) FILTER (WHERE is_server_error = 1) AS errors_5xx,
        COUNT(*) FILTER (WHERE is_client_error = 1) AS errors_4xx,
        COUNT(*) FILTER (WHERE is_slow_request = 1) AS slow_requests,
        ROUND(AVG(latency_ms), 2) AS avg_latency_ms,
        ROUND(CAST(percentile_cont(0.50) WITHIN GROUP (ORDER BY latency_ms) AS numeric), 2) AS p50_latency_ms,
        ROUND(CAST(percentile_cont(0.95) WITHIN GROUP (ORDER BY latency_ms) AS numeric), 2) AS p95_latency_ms,
        ROUND(CAST(percentile_cont(0.99) WITHIN GROUP (ORDER BY latency_ms) AS numeric), 2) AS p99_latency_ms
    FROM {{ ref('stg_http_events') }}
    GROUP BY service_name, window_start
)

SELECT
    md5(service_name || '_' || window_start::text) AS window_id,
    service_name,
    window_start,
    window_start + INTERVAL '5 minutes' AS window_end,
    total_requests,
    errors_5xx,
    errors_4xx,
    slow_requests,
    ROUND((errors_5xx * 100.0) / NULLIF(total_requests, 0), 2) AS error_rate_5xx_pct,
    ROUND((errors_4xx * 100.0) / NULLIF(total_requests, 0), 2) AS error_rate_4xx_pct,
    avg_latency_ms,
    p50_latency_ms,
    p95_latency_ms,
    p99_latency_ms,
    CASE
    WHEN (errors_5xx * 100.0 / NULLIF(total_requests, 0)) > 5.0 OR p99_latency_ms > 2000.0 THEN 'CRITICAL'
    WHEN (errors_5xx * 100.0 / NULLIF(total_requests, 0)) > 1.0 OR p95_latency_ms > 500.0 THEN 'WARNING'
    ELSE 'HEALTHY'
END AS window_status
FROM time_buckets
ORDER BY window_start DESC, total_requests DESC