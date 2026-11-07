"""
DeepTrace Spark Session Factory
Manages local SparkSession lifecycle with optimized warehouse, loopback networking,
Hadoop Windows compatibility, and parquet settings.
"""
import os
import sys
from pyspark.sql import SparkSession
from .config import SparkConfig

# Ensure PySpark executes with the virtual environment Python interpreter
os.environ["PYSPARK_PYTHON"] = sys.executable
os.environ["PYSPARK_DRIVER_PYTHON"] = sys.executable

# Configure local Hadoop winutils for Windows file operations
hadoop_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "hadoop"))
if os.path.exists(hadoop_dir):
    os.environ["HADOOP_HOME"] = hadoop_dir
    bin_dir = os.path.join(hadoop_dir, "bin")
    if bin_dir not in os.environ.get("PATH", ""):
        os.environ["PATH"] = bin_dir + os.pathsep + os.environ.get("PATH", "")

def get_spark_session(config: SparkConfig = None) -> SparkSession:
    cfg = config or SparkConfig()
    
    builder = (
        SparkSession.builder
        .appName(cfg.app_name)
        .master(cfg.master)
        .config("spark.driver.host", "127.0.0.1")
        .config("spark.driver.bindAddress", "127.0.0.1")
        .config("spark.driver.memory", cfg.driver_memory)
        .config("spark.sql.shuffle.partitions", cfg.shuffle_partitions)
        .config("spark.sql.warehouse.dir", cfg.warehouse_dir)
        .config("spark.sql.parquet.compression.codec", "snappy")
        .config("spark.sql.execution.arrow.pyspark.enabled", "true")
        .config("spark.hadoop.hadoop.home.dir", hadoop_dir)
        .config("spark.ui.enabled", "false")  # Save memory by disabling UI in batch mode
    )
    
    return builder.getOrCreate()

def create_spark_session(app_name: str = None) -> SparkSession:
    cfg = SparkConfig(app_name=app_name) if app_name else SparkConfig()
    return get_spark_session(cfg)
