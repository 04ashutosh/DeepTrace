import argparse
import sys
import uuid
from .db import DatabaseManager
from .ingestor.csv_ingestor import CsvStreamIngestor
from .ingestor.json_ingestor import JsonLinesStreamIngestor

def main():
    parser = argparse.ArgumentParser(description="DeepTrace Streaming Ingestion Worker")
    parser.add_argument("command", choices=["ingest"], help="Action to execute")
    parser.add_argument("--file", required=True, help="Path to input data file")
    parser.add_argument("--format", choices=["csv", "json"], default="csv", help="Input file format")
    parser.add_argument("--pipeline-name", default="http_log_pipeline", help="Registered pipeline name")
    args = parser.parse_args()

    run_uuid = uuid.uuid4()
    db = DatabaseManager()

    print(f"\n[DeepTrace Ingestion] Initializing run: {run_uuid} for pipeline: {args.pipeline_name}")
    print(f"[DeepTrace Ingestion] File: {args.file} | Format: {args.format}")

    with db.get_connection() as conn:
        with conn.cursor() as cur:
            # 1. Resolve pipeline ID
            cur.execute("SELECT id FROM pipelines WHERE name = %s", (args.pipeline_name,))
            pipe_row = cur.fetchone()
            if not pipe_row:
                print(f"[Error] Pipeline '{args.pipeline_name}' is not registered in PostgreSQL.")
                sys.exit(1)
            pipeline_id = pipe_row[0]

            # 2. Resolve dataset ID
            cur.execute("SELECT id FROM datasets WHERE name = 'silver_http_events'")
            ds_row = cur.fetchone()
            dataset_id = ds_row[0] if ds_row else None

            # 3. Create initial RUNNING state
            cur.execute(
                """
                INSERT INTO pipeline_runs (
                    pipeline_id, run_uuid, status, input_file, target_dataset_id, started_at
                ) VALUES (%s, %s, 'RUNNING', %s, %s, CURRENT_TIMESTAMP)
                """,
                (pipeline_id, str(run_uuid), args.file, dataset_id)
            )

    # 4. Execute streaming ingestion
    try:
        ingestor = CsvStreamIngestor() if args.format == "csv" else JsonLinesStreamIngestor()
        with db.get_connection() as conn:
            stats = ingestor.ingest(file_path=args.file, batch_id=run_uuid, conn=conn)

            # 5. Finalize SUCCEEDED status with exact counts
            with conn.cursor() as cur:
                cur.execute(
                    """
                    UPDATE pipeline_runs
                    SET status = 'SUCCEEDED',
                        records_read = %s,
                        records_accepted = %s,
                        records_rejected = %s,
                        records_written = %s,
                        duration_ms = %s,
                        completed_at = CURRENT_TIMESTAMP
                    WHERE run_uuid = %s
                    """,
                    (
                        stats["records_read"],
                        stats["records_accepted"],
                        stats["records_rejected"],
                        stats["records_accepted"],
                        stats["duration_ms"],
                        str(run_uuid)
                    )
                )

                # Update dataset record count & last run
                if dataset_id:
                    cur.execute(
                        """
                        UPDATE datasets
                        SET record_count = record_count + %s,
                            last_successful_run_at = CURRENT_TIMESTAMP,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = %s
                        """,
                        (stats["records_accepted"], dataset_id)
                    )

        print("\n================ DEEPTRACE INGESTION SUMMARY ================")
        print(f" Status:           SUCCEEDED")
        print(f" Run UUID:         {run_uuid}")
        print(f" Records Read:     {stats['records_read']:,}")
        print(f" Records Accepted: {stats['records_accepted']:,}")
        print(f" Records Rejected: {stats['records_rejected']:,}")
        print(f" Duration:         {stats['duration_ms']:,} ms")
        if stats["records_read"] > 0:
            rejection_pct = (stats["records_rejected"] / stats["records_read"]) * 100
            print(f" Rejection Rate:   {rejection_pct:.2f}%")
        print("============================================================\n")

    except Exception as e:
        print(f"\n[DeepTrace Ingestion Error] Fatal error during ingestion: {e}")
        with db.get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute(
                    """
                    UPDATE pipeline_runs
                    SET status = 'FAILED',
                        error_message = %s,
                        completed_at = CURRENT_TIMESTAMP
                    WHERE run_uuid = %s
                    """,
                    (str(e), str(run_uuid))
                )
        sys.exit(1)

if __name__ == "__main__":
    main()