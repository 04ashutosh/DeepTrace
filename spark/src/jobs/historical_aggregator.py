"""
DeepTrace Historical Event Aggregation Job
Reads raw landing data, transforms timestamps, deduplicates, writes partitioned Parquet,
and computes rolling analytical percentiles.
"""
import sys
from pyspark.sql import functions as F
from pyspark.sql.window import Window
from ..session import get_spark_session
from ..schema import HTTP_EVENT_SPARK_SCHEMA

def process_historical_events(input_path: str, output_parquet_path: str, marts_path: str):
    spark = get_spark_session()
    print(f"\n[DeepTrace Spark] Starting job: Historical Event Processing")
    print(f"[DeepTrace Spark] Input: {input_path}")
    print(f"[DeepTrace Spark] Output: {output_parquet_path}")

    # 1. Read input CSV using explicit schema
    df = (
        spark.read
        .option("header", "true")
        .schema(HTTP_EVENT_SPARK_SCHEMA)
        .csv(input_path)
    )

    # 2. Clean, typecast and extract date partition column (resilient to corrupt data)
    cleaned_df = (
        df.filter(F.col("service_name").isNotNull() & (F.col("service_name") != ""))
          .filter(F.col("status_code").between(100, 599))
          .filter(F.col("latency_ms") >= 0.0)
          .withColumn("event_timestamp", F.expr("try_to_timestamp(timestamp)"))
          .filter(F.col("event_timestamp").isNotNull())
          .withColumn("event_date", F.to_date("event_timestamp"))
          .withColumn("is_server_error", F.when(F.col("status_code") >= 500, 1).otherwise(0))
          .withColumn("is_client_error", F.when((F.col("status_code") >= 400) & (F.col("status_code") < 500), 1).otherwise(0))
    )

    # 3. Deduplicate events by event_id using Window ranking
    dedup_window = Window.partitionBy("event_id").orderBy(F.col("event_timestamp").desc())
    deduped_df = (
        cleaned_df.withColumn("row_num", F.row_number().over(dedup_window))
                  .filter(F.col("row_num") == 1)
                  .drop("row_num")
    )

    # 4. Write partitioned Parquet (Lakehouse Silver Layer)
    (
        deduped_df.write
        .mode("overwrite")
        .partitionBy("service_name", "event_date")
        .parquet(output_parquet_path)
    )
    print(f"[DeepTrace Spark] Partitioned Lakehouse Parquet successfully written.")

    # 5. Compute Service Analytical Aggregation Mart (p50, p95, p99 percentiles)
    # Using approx_percentile with high accuracy (relative error = 0.01)
    service_mart = (
        deduped_df.groupBy("service_name")
        .agg(
            F.count("*").alias("total_requests"),
            F.round(F.avg("latency_ms"), 2).alias("avg_latency_ms"),
            F.round(F.expr("percentile_approx(latency_ms, 0.50, 1000)"), 2).alias("p50_latency_ms"),
            F.round(F.expr("percentile_approx(latency_ms, 0.95, 1000)"), 2).alias("p95_latency_ms"),
            F.round(F.expr("percentile_approx(latency_ms, 0.99, 1000)"), 2).alias("p99_latency_ms"),
            F.sum("is_server_error").alias("errors_5xx"),
            F.round((F.sum("is_server_error") * 100.0) / F.count("*"), 2).alias("error_rate_5xx_pct")
        )
        .orderBy(F.desc("total_requests"))
    )

    # 6. Write Aggregated Mart to Parquet
    service_mart.write.mode("overwrite").parquet(marts_path)
    print(f"[DeepTrace Spark] Aggregated Service Mart written to: {marts_path}")

    # Display summary to console
    print("\n============== SPARK AGGREGATED METRICS MART ==============")
    service_mart.show(truncate=False)

if __name__ == "__main__":
    in_file = sys.argv[1] if len(sys.argv) > 1 else "data/landing/http_events.csv"
    out_pq = sys.argv[2] if len(sys.argv) > 2 else "data/lakehouse/parquet_events"
    out_mart = sys.argv[3] if len(sys.argv) > 3 else "data/lakehouse/parquet_marts"
    process_historical_events(in_file, out_pq, out_mart)
