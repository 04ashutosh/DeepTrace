{{ config(materialized='table') }}

SELECT
    pipeline_id,
    COUNT(*) AS total_runs_count,
    COUNT(*) FILTER (WHERE status = 'SUCCEEDED') AS succeeded_runs_count,
    COUNT(*) FILTER (WHERE status = 'FAILED') AS failed_runs_count,
    SUM(records_read) AS total_records_read,
    SUM(records_accepted) AS total_records_accepted,
    SUM(records_rejected) AS total_records_rejected,
    ROUND(
            (SUM(records_rejected) * 100.0) / NULLIF(SUM(records_read), 0),
            2
    ) AS overall_rejection_rate_pct,
    ROUND(AVG(duration_ms), 0) AS avg_duration_ms,
    ROUND(AVG(throughput_records_per_sec), 2) AS avg_throughput_records_per_sec,
    MAX(completed_at) AS last_run_at
FROM {{ ref('stg_pipeline_runs') }}
GROUP BY pipeline_id