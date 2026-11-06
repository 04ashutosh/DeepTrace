"""
Explicit Spark SQL Schema definition for HTTP observability telemetry.
Eliminates slow inferSchema runtime overhead.
"""
from pyspark.sql.types import (
    StructType, StructField, StringType, IntegerType, DoubleType, TimestampType
)

HTTP_EVENT_SPARK_SCHEMA = StructType([
    StructField("event_id", StringType(), False),
    StructField("trace_id", StringType(), False),
    StructField("span_id", StringType(), False),
    StructField("service_name", StringType(), False),
    StructField("http_method", StringType(), False),
    StructField("route", StringType(), False),
    StructField("status_code", IntegerType(), False),
    StructField("latency_ms", DoubleType(), False),
    StructField("client_ip", StringType(), True),
    StructField("user_agent", StringType(), True),
    StructField("timestamp", StringType(), False)
])