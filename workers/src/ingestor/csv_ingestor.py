import csv
import json
import time
import uuid
from typing import Dict, Any
import psycopg
from ..validator.schema import validate_raw_record
from ..validator.quarantine import QuarantineManager

class CsvStreamIngestor:
    def __init__(self, chunk_size: int = 5000):
        self.chunk_size = chunk_size

    def ingest(self, file_path: str, batch_id: uuid.UUID, conn: psycopg.Connection) -> Dict[str, Any]:
        quarantine = QuarantineManager(batch_id=batch_id, source_file=file_path)
        
        records_read = 0
        records_accepted = 0
        records_rejected = 0
        
        valid_buffer = []
        rejected_buffer = []
        
        start_time = time.perf_counter()

        with open(file_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            line_number = 1  # Header is line 1

            for row in reader:
                line_number += 1
                records_read += 1

                event, failed_rule, reason = validate_raw_record(row)
                if event:
                    valid_buffer.append(event)
                    records_accepted += 1
                else:
                    rejected_buffer.append({
                        "line_number": line_number,
                        "raw_content": json.dumps(row),
                        "rejection_reason": reason,
                        "failed_rule": failed_rule
                    })
                    records_rejected += 1

                # Flush chunks to keep memory bounded under 80 MB
                if len(valid_buffer) >= self.chunk_size:
                    self._flush_valid(conn, valid_buffer, batch_id)
                    valid_buffer.clear()

                if len(rejected_buffer) >= self.chunk_size:
                    quarantine.flush_rejected(conn, rejected_buffer)
                    rejected_buffer.clear()

        # Final flush
        if valid_buffer:
            self._flush_valid(conn, valid_buffer, batch_id)
            valid_buffer.clear()

        if rejected_buffer:
            quarantine.flush_rejected(conn, rejected_buffer)
            rejected_buffer.clear()

        duration_ms = int((time.perf_counter() - start_time) * 1000)

        return {
            "records_read": records_read,
            "records_accepted": records_accepted,
            "records_rejected": records_rejected,
            "duration_ms": duration_ms
        }

    def _flush_valid(self, conn: psycopg.Connection, valid_events, batch_id: uuid.UUID):
        # High-performance bulk insert using psycopg binary COPY
        with conn.cursor() as cur:
            with cur.copy(
                """
                COPY silver_http_events (
                    batch_id, event_id, trace_id, span_id, service_name,
                    http_method, route, status_code, latency_ms,
                    client_ip, user_agent, event_timestamp
                ) FROM STDIN
                """
            ) as copy:
                for ev in valid_events:
                    copy.write_row((
                        str(batch_id),
                        str(ev.event_id),
                        ev.trace_id,
                        ev.span_id,
                        ev.service_name,
                        ev.http_method,
                        ev.route,
                        ev.status_code,
                        ev.latency_ms,
                        ev.client_ip,
                        ev.user_agent,
                        ev.event_timestamp.isoformat()
                    ))