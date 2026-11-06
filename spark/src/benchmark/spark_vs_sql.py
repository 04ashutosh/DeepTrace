"""
DeepTrace Performance Benchmark: PySpark vs PostgreSQL SQL
Measures execution duration, memory consumption, and disk storage efficiency.
"""
import os
import time
import psycopg
import psutil
from ..session import get_spark_session
from ..schema import HTTP_EVENT_SPARK_SCHEMA
from pyspark.sql import functions as F

def get_dir_size_bytes(path: str) -> int:
    total = 0
    if not os.path.exists(path):
        return 0
    for root, _, files in os.walk(path):
        for f in files:
            total += os.path.getsize(os.path.join(root, f))
    return total

def run_benchmark():
    process = psutil.Process()
    print("\n================ DEEPTRACE PERFORMANCE BENCHMARK ================")
    print("Workload: 50,000 Observability Records Analytics")
    print("-----------------------------------------------------------------")

    # --- 1. PostgreSQL Benchmark ---
    try:
        pg_start_time = time.perf_counter()
        pg_conn = psycopg.connect("host=localhost port=5434 dbname=deeptrace user=deeptrace_user password=deeptrace_password")
        with pg_conn.cursor() as cur:
            cur.execute("""
                SELECT service_name, COUNT(*),
                       ROUND(CAST(percentile_cont(0.50) WITHIN GROUP (ORDER BY latency_ms) AS numeric), 2) AS p50,
                       ROUND(CAST(percentile_cont(0.95) WITHIN GROUP (ORDER BY latency_ms) AS numeric), 2) AS p95,
                       ROUND(CAST(percentile_cont(0.99) WITHIN GROUP (ORDER BY latency_ms) AS numeric), 2) AS p99
                FROM silver_http_events
                GROUP BY service_name;
            """)
            pg_results = cur.fetchall()
            cur.execute("SELECT pg_total_relation_size('silver_http_events');")
            pg_table_size = cur.fetchone()[0]
        pg_duration_ms = (time.perf_counter() - pg_start_time) * 1000

        print(f"1. PostgreSQL SQL Execution:")
        print(f"   Query Time:   {pg_duration_ms:.2f} ms")
        print(f"   Table Size:   {pg_table_size / (1024 * 1024):.2f} MB")
    except Exception as e:
        print(f"1. PostgreSQL SQL Execution: Skipped (PostgreSQL offline or unreachable: {e})")

    # --- 2. PySpark Benchmark ---
    spark = get_spark_session()
    spark_start_time = time.perf_counter()
    
    df = (
        spark.read
        .option("header", "true")
        .schema(HTTP_EVENT_SPARK_SCHEMA)
        .csv("data/landing/http_events.csv")
    )
    
    spark_mart = (
        df.filter(F.col("service_name").isNotNull() & (F.col("service_name") != ""))
          .filter(F.col("status_code").between(100, 599))
          .filter(F.col("latency_ms") >= 0.0)
          .groupBy("service_name")
          .agg(
              F.count("*").alias("total_requests"),
              F.round(F.expr("percentile_approx(latency_ms, 0.50, 1000)"), 2).alias("p50"),
              F.round(F.expr("percentile_approx(latency_ms, 0.95, 1000)"), 2).alias("p95"),
              F.round(F.expr("percentile_approx(latency_ms, 0.99, 1000)"), 2).alias("p99")
          )
    )
    spark_mart.write.mode("overwrite").parquet("data/lakehouse/benchmark_marts")
    spark_duration_ms = (time.perf_counter() - spark_start_time) * 1000
    spark_parquet_size = get_dir_size_bytes("data/lakehouse/benchmark_marts")
    peak_rss_mb = process.memory_info().rss / (1024 * 1024)

    print(f"\n2. PySpark Lakehouse Execution:")
    print(f"   Job Time:     {spark_duration_ms:.2f} ms (Includes DAG optimization & Parquet write)")
    print(f"   Parquet Size: {spark_parquet_size / 1024:.2f} KB")
    print(f"   Peak RSS RAM: {peak_rss_mb:.2f} MB")
    print("=================================================================\n")

if __name__ == "__main__":
    run_benchmark()
