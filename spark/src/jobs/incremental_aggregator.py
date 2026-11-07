"""
Incremental PySpark Aggregator with Watermark Checkpointing & Lineage.
Processes only records with timestamp > high_watermark.
"""
from datetime import datetime, timezone
from pathlib import Path
from typing import Optional, Tuple
import psycopg
from pyspark.sql import DataFrame, SparkSession
from pyspark.sql import functions as F

from spark.src.config import SparkJobConfig
from spark.src.schema import HTTP_EVENT_SCHEMA
from spark.src.session import create_spark_session


class IncrementalAggregator:
    def __init__(self, spark: SparkSession, config: SparkJobConfig, pipeline_name: str = "spark_http_events_incremental"):
        self.spark = spark
        self.config = config
        self.pipeline_name = pipeline_name

    def get_high_watermark(self) -> Optional[datetime]:
        """Fetch latest high watermark from PostgreSQL metadata table."""
        query = "SELECT high_watermark FROM metadata.pipeline_checkpoints WHERE pipeline_name = %s"
        try:
            with psycopg.connect(
                host=self.config.postgres_host,
                port=self.config.postgres_port,
                dbname=self.config.postgres_db,
                user=self.config.postgres_user,
                password=self.config.postgres_password,
            ) as conn:
                with conn.cursor() as cur:
                    cur.execute(query, (self.pipeline_name,))
                    row = cur.fetchone()
                    if row:
                        return row[0]
        except Exception:
            pass
        return None

    def update_high_watermark(self, new_watermark: datetime, records_count: int) -> None:
        """Upsert the new high watermark in PostgreSQL metadata table."""
        upsert_query = """
            INSERT INTO metadata.pipeline_checkpoints (pipeline_name, high_watermark, records_processed, updated_at)
            VALUES (%s, %s, %s, CURRENT_TIMESTAMP)
            ON CONFLICT (pipeline_name) DO UPDATE
            SET high_watermark = EXCLUDED.high_watermark,
                records_processed = metadata.pipeline_checkpoints.records_processed + EXCLUDED.records_processed,
                updated_at = CURRENT_TIMESTAMP;
        """
        lineage_query = """
            INSERT INTO metadata.lineage_edges (source_dataset, target_dataset, transformation_type, job_name)
            VALUES (%s, %s, %s, %s)
            ON CONFLICT (source_dataset, target_dataset, job_name) DO NOTHING;
        """
        with psycopg.connect(
            host=self.config.postgres_host,
            port=self.config.postgres_port,
            dbname=self.config.postgres_db,
            user=self.config.postgres_user,
            password=self.config.postgres_password,
        ) as conn:
            with conn.cursor() as cur:
                cur.execute(upsert_query, (self.pipeline_name, new_watermark, records_count))
                cur.execute(
                    lineage_query,
                    (
                        self.config.input_path,
                        self.config.output_path,
                        "SPARK_INCREMENTAL",
                        self.pipeline_name,
                    ),
                )
            conn.commit()

    def filter_delta(self, df: DataFrame, watermark: Optional[datetime]) -> DataFrame:
        """Filter incoming data against high watermark using ANSI try_to_timestamp."""
        parsed_df = (
            df.withColumn("parsed_timestamp", F.expr("try_to_timestamp(timestamp)"))
            .filter(F.col("parsed_timestamp").isNotNull())
            .filter(F.col("service_name").isNotNull() & (F.length(F.trim(F.col("service_name"))) > 0))
        )

        if watermark is not None:
            watermark_str = watermark.strftime("%Y-%m-%d %H:%M:%S")
            return parsed_df.filter(F.col("parsed_timestamp") > F.lit(watermark_str))

        return parsed_df

    def run(self) -> Tuple[int, Optional[datetime]]:
        """Executes the incremental processing cycle."""
        watermark = self.get_high_watermark()
        print(f"[*] Starting incremental run for '{self.pipeline_name}'. Current watermark: {watermark}")

        raw_df = (
            self.spark.read.format("csv")
            .option("header", "true")
            .schema(HTTP_EVENT_SCHEMA)
            .load(self.config.input_path)
        )

        delta_df = self.filter_delta(raw_df, watermark)
        delta_count = delta_df.count()

        if delta_count == 0:
            print("[*] No new records found past current watermark. Job finished.")
            return 0, watermark

        print(f"[*] Found {delta_count} delta records past watermark. Computing aggregates...")

        # Calculate max timestamp to advance watermark
        max_ts_row = delta_df.agg(F.max("parsed_timestamp")).collect()[0][0]
        new_watermark = max_ts_row if isinstance(max_ts_row, datetime) else datetime.now(timezone.utc)

        # Add partition and metadata columns
        processed_df = delta_df.withColumn(
            "event_date", F.to_date(F.col("parsed_timestamp"))
        ).withColumn(
            "processed_at", F.current_timestamp()
        )

        # Write partitioned Parquet in append mode
        (
            processed_df.write.mode("append")
            .partitionBy("service_name", "event_date")
            .parquet(self.config.output_path)
        )

        # Advance state checkpoint in PostgreSQL
        try:
            self.update_high_watermark(new_watermark, delta_count)
            print(f"[+] Advanced watermark to {new_watermark} ({delta_count} rows added).")
        except Exception as e:
            print(f"[-] Warning: Failed to persist watermark in PostgreSQL: {e}")

        return delta_count, new_watermark


if __name__ == "__main__":
    spark = create_spark_session("DeepTrace-Incremental-Aggregator")
    config = SparkJobConfig(
        input_path="data/landing/http_events.csv",
        output_path="data/lakehouse/incremental_events",
    )
    job = IncrementalAggregator(spark, config)
    count, wm = job.run()
    spark.stop()