from datetime import datetime, timezone
import pytest
from pyspark.sql import SparkSession
from pyspark.sql.types import (
    DoubleType,
    IntegerType,
    StringType,
    StructField,
    StructType,
)

from spark.src.config import SparkJobConfig
from spark.src.jobs.incremental_aggregator import IncrementalAggregator
from spark.src.session import create_spark_session


@pytest.fixture(scope="session")
def spark() -> SparkSession:
    session = create_spark_session("DeepTrace-Incremental-Tests")
    yield session
    session.stop()


def test_incremental_filter_delta(spark: SparkSession):
    schema = StructType([
        StructField("event_id", StringType(), False),
        StructField("timestamp", StringType(), False),
        StructField("service_name", StringType(), True),
        StructField("status_code", IntegerType(), True),
        StructField("latency_ms", DoubleType(), True),
    ])

    data = [
        ("e1", "2026-10-31 10:00:00", "auth-service", 200, 45.0),
        ("e2", "2026-10-31 11:00:00", "order-service", 500, 120.0),
        ("e3", "2026-10-31 12:00:00", "payment-service", 200, 60.0),
    ]
    df = spark.createDataFrame(data, schema)
    config = SparkJobConfig()
    aggregator = IncrementalAggregator(spark, config)

    # When watermark is None: all records pass
    all_delta = aggregator.filter_delta(df, None)
    assert all_delta.count() == 3

    # When watermark is 10:30:00: only e2 and e3 pass
    watermark = datetime(2026, 10, 31, 10, 30, 0)
    filtered_delta = aggregator.filter_delta(df, watermark)
    assert filtered_delta.count() == 2
    event_ids = [row.event_id for row in filtered_delta.select("event_id").collect()]
    assert "e1" not in event_ids
    assert "e2" in event_ids
    assert "e3" in event_ids


def test_incremental_corrupt_records_dropped(spark: SparkSession):
    schema = StructType([
        StructField("event_id", StringType(), False),
        StructField("timestamp", StringType(), False),
        StructField("service_name", StringType(), True),
    ])

    data = [
        ("e1", "INVALID_DATE", "auth-service"),
        ("e2", "2026-10-31 11:00:00", ""),  # Blank service name
        ("e3", "2026-10-31 12:00:00", "order-service"),
    ]
    df = spark.createDataFrame(data, schema)
    config = SparkJobConfig()
    aggregator = IncrementalAggregator(spark, config)

    clean_delta = aggregator.filter_delta(df, None)
    assert clean_delta.count() == 1
    assert clean_delta.first().event_id == "e3"