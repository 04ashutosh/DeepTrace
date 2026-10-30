import uuid
from src.validator.schema import validate_raw_record

def test_valid_record_passes():
    record = {
        "event_id": str(uuid.uuid4()),
        "trace_id": "abcdef1234567890abcdef1234567890",
        "span_id": "1234567890abcdef",
        "service_name": "order-service",
        "http_method": "GET",
        "route": "/api/v1/orders",
        "status_code": 200,
        "latency_ms": 45.2,
        "client_ip": "192.168.1.10",
        "user_agent": "Mozilla/5.0",
        "timestamp": "2026-10-25T12:00:00Z"
    }
    event, failed_rule, reason = validate_raw_record(record)
    assert event is not None
    assert failed_rule is None
    assert event.service_name == "order-service"
    assert event.status_code == 200

def test_invalid_status_code_is_rejected():
    record = {
        "event_id": str(uuid.uuid4()),
        "trace_id": "abcdef1234567890abcdef1234567890",
        "span_id": "1234567890abcdef",
        "service_name": "order-service",
        "http_method": "GET",
        "route": "/api/v1/orders",
        "status_code": 999,  # Invalid HTTP code
        "latency_ms": 45.2,
        "client_ip": "192.168.1.10",
        "user_agent": "Mozilla/5.0",
        "timestamp": "2026-10-25T12:00:00Z"
    }
    event, failed_rule, reason = validate_raw_record(record)
    assert event is None
    assert failed_rule == "INVALID_STATUS_CODE"

def test_negative_latency_is_rejected():
    record = {
        "event_id": str(uuid.uuid4()),
        "trace_id": "abcdef1234567890abcdef1234567890",
        "span_id": "1234567890abcdef",
        "service_name": "order-service",
        "http_method": "GET",
        "route": "/api/v1/orders",
        "status_code": 200,
        "latency_ms": -10.0,  # Negative latency
        "client_ip": "192.168.1.10",
        "user_agent": "Mozilla/5.0",
        "timestamp": "2026-10-25T12:00:00Z"
    }
    event, failed_rule, reason = validate_raw_record(record)
    assert event is None
    assert failed_rule == "INVALID_LATENCY_RANGE"