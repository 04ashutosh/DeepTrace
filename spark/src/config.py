"""
DeepTrace Spark Configuration: Low-Resource Profile
Configures driver memory and execution parallelism to fit safely on 8-16 GB RAM machines.
"""
import os
from dataclasses import dataclass

@dataclass(frozen=True)
class SparkConfig:
    app_name: str = "DeepTrace-Lakehouse-Engine"
    master: str = "local[2]"  # Bound to 2 cores locally
    driver_memory: str = "1536m"  # 1.5 GB memory budget
    shuffle_partitions: str = "4"  # Default 200 is excessive for local single-node
    warehouse_dir: str = "D:/deeptrace/data/lakehouse"