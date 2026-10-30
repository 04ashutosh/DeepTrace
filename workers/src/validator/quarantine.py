import json
import os
import uuid
from typing import List, Dict, Any
import psycopg

class QuarantineManager:
    def __init__(self, batch_id: uuid.UUID, source_file: str, quarantine_dir: str = "data/quarantine"):
        self.batch_id = batch_id
        self.source_file = source_file
        self.quarantine_dir = quarantine_dir
        os.makedirs(self.quarantine_dir, exist_ok=True)
        self.log_path = os.path.join(self.quarantine_dir, f"rejected_{self.batch_id}.jsonl")

    def flush_rejected(self, conn: psycopg.Connection, rejected_buffer: List[Dict[str, Any]]) -> None:
        if not rejected_buffer:
            return

        # 1. Write to local filesystem log for immediate offline review
        with open(self.log_path, "a", encoding="utf-8") as f:
            for item in rejected_buffer:
                f.write(json.dumps(item) + "\n")

        # 2. Persist to PostgreSQL rejected_records table for querying
        insert_query = """
            INSERT INTO rejected_records (batch_id, source_file, line_number, raw_content, rejection_reason, failed_rule)
            VALUES (%s, %s, %s, %s, %s, %s)
        """
        records = [
            (
                str(self.batch_id),
                self.source_file,
                item["line_number"],
                item["raw_content"],
                item["rejection_reason"],
                item["failed_rule"]
            )
            for item in rejected_buffer
        ]
        with conn.cursor() as cur:
            cur.executemany(insert_query, records)