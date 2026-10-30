import csv
import os
import uuid
import pytest
from src.validator.schema import validate_raw_record

def test_csv_row_parsing(tmp_path):
    csv_file = tmp_path / "test.csv"
    with open(csv_file, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["event_id", "trace_id", "span_id", "service_name", "http_method", "route", "status_code", "latency_ms", "client_ip", "user_agent", "timestamp"])
        writer.writerow([str(uuid.uuid4()), "abcdef1234567890", "12345678", "auth-service", "POST", "/login", "200", "15.5", "10.0.0.1", "curl/8.0", "2026-10-25T12:00:00Z"])
        writer.writerow([str(uuid.uuid4()), "abcdef1234567890", "12345678", "", "POST", "/login", "200", "15.5", "10.0.0.1", "curl/8.0", "2026-10-25T12:00:00Z"]) # empty service

    with open(csv_file, "r", encoding="utf-8") as f:
        reader = list(csv.DictReader(f))
        assert len(reader) == 2
        ev1, rule1, _ = validate_raw_record(reader[0])
        ev2, rule2, _ = validate_raw_record(reader[1])

        assert ev1 is not None
        assert ev2 is None
        assert rule2 == "MISSING_MANDATORY_FIELD"