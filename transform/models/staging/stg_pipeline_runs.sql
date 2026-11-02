{{ config(materialized='view') }}

SELECT
    id AS run_id,
    pipeline_id,
    run_uuid,
    status,
    input_file,
    target_dataset_id,
    records_read,
    records_accepted,
    records_rejected,
    records_written,
    duration_ms,
    ROUND(
            (records_rejected * 100.0) / NULLIF(records_read, 0),
            2
    ) AS rejection_rate_pct,
    ROUND(
            (records_read * 1000.0) / NULLIF(duration_ms, 0),
            2
    ) AS throughput_records_per_sec,
    error_message,
    started_at,
    completed_at
FROM {{ source('deeptrace_lakehouse', 'pipeline_runs') }}