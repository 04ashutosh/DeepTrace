"""
PySpark Unit Test Suite
Asserts explicit schema parsing, filtering invariants, and Parquet round-trip serialization.
"""
import os
import pytest
from pyspark.sql import SparkSession
from spark.src.session import get_spark_session
from spark.src.schema import HTTP_EVENT_SPARK_SCHEMA

@pytest.fixture(scope="session")
def spark():
    session = get_spark_session()
    yield session
    session.stop()

def test_schema_and_filter_invariants(spark, tmp_path):
    data = [
        ("ev-1", "tr-1", "sp-1", "auth-service", "GET", "/login", 200, 25.5, "10.0.0.1", "curl", "2026-10-25T12:00:00Z"),
        ("ev-2", "tr-2", "sp-2", "auth-service", "GET", "/login", 999, 25.5, "10.0.0.1", "curl", "2026-10-25T12:00:00Z"), # Bad status
        ("ev-3", "tr-3", "sp-3", "order-service", "POST", "/checkout", 500, -10.0, "10.0.0.1", "curl", "2026-10-25T12:00:00Z"), # Negative latency
    ]
    df = spark.createDataFrame(data, schema=HTTP_EVENT_SPARK_SCHEMA)
    
    # Assert filter conditions drop invalid status codes and negative latencies
    valid_df = (
        df.filter(df.status_code.between(100, 599))
          .filter(df.latency_ms >= 0.0)
    )
    
    assert valid_df.count() == 1
    row = valid_df.first()
    assert row.event_id == "ev-1"
    assert row.service_name == "auth-service"

def test_parquet_round_trip(spark, tmp_path):
    out_dir = str(tmp_path / "parquet_test")
    data = [
        ("ev-10", "tr-10", "sp-10", "inventory-api", "GET", "/items", 200, 15.0, "127.0.0.1", "agent", "2026-10-25T12:00:00Z")
    ]
    df = spark.createDataFrame(data, schema=HTTP_EVENT_SPARK_SCHEMA)
    df.write.parquet(out_dir)
    
    read_back = spark.read.parquet(out_dir)
    assert read_back.count() == 1
    assert read_back.first().event_id == "ev-10"
