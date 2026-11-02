{{ config(materialized='table') }}

WITH service_summary AS (
    SELECT
        service_name,
        COUNT(*) AS total_lifetime_requests,
        COUNT(DISTINCT route) AS distinct_routes_count,
        MIN(event_timestamp) AS first_seen_at,
        MAX(event_timestamp) AS last_seen_at,
        ROUND(AVG(latency_ms), 2) AS baseline_avg_latency_ms,
        ROUND(CAST(percentile_cont(0.50) WITHIN GROUP (ORDER BY latency_ms) AS numeric), 2) AS baseline_p50_latency_ms,
        ROUND(CAST(percentile_cont(0.95) WITHIN GROUP (ORDER BY latency_ms) AS numeric), 2) AS baseline_p95_latency_ms,
        COUNT(*) FILTER (WHERE is_server_error = 1) AS total_server_errors,
        ROUND(
            (COUNT(*) FILTER (WHERE is_server_error = 1) * 100.0) / NULLIF(COUNT(*), 0),
            2
        ) AS lifetime_5xx_error_rate_pct
    FROM {{ ref('stg_http_events') }}
    GROUP BY service_name
)

SELECT
    service_name,
    total_lifetime_requests,
    distinct_routes_count,
    baseline_avg_latency_ms,
    baseline_p50_latency_ms,
    baseline_p95_latency_ms,
    total_server_errors,
    lifetime_5xx_error_rate_pct,
    CASE
        WHEN lifetime_5xx_error_rate_pct > 5.0 THEN 'CRITICAL'
        WHEN lifetime_5xx_error_rate_pct > 1.0 THEN 'DEGRADED'
        ELSE 'HEALTHY'
    END AS service_health_tier,
    first_seen_at,
    last_seen_at,
    CURRENT_TIMESTAMP AS dbt_updated_at
FROM service_summary
ORDER BY total_lifetime_requests DESC
