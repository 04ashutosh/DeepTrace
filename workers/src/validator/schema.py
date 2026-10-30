import ipaddress
import uuid
from datetime import datetime
from typing import Optional, Tuple
from pydantic import BaseModel, Field, field_validator

class ValidatedHttpEvent(BaseModel):
    event_id: uuid.UUID
    trace_id: str = Field(min_length=8, max_length=64)
    span_id: str = Field(min_length=8, max_length=32)
    service_name: str = Field(min_length=1, max_length=64)
    http_method: str = Field(min_length=3, max_length=10)
    route: str = Field(min_length=1, max_length=256)
    status_code: int = Field(ge=100, le=599)
    latency_ms: float = Field(ge=0.0)
    client_ip: Optional[str] = None
    user_agent: Optional[str] = Field(default=None, max_length=256)
    event_timestamp: datetime

    @field_validator("client_ip")
    @classmethod
    def validate_ip(cls, v: Optional[str]) -> Optional[str]:
        if not v:
            return None
        try:
            ipaddress.ip_address(v)
            return v
        except ValueError:
            raise ValueError(f"Invalid IP address format: {v}")

    @field_validator("http_method")
    @classmethod
    def validate_method(cls, v: str) -> str:
        v_upper = v.upper()
        if v_upper not in {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"}:
            raise ValueError(f"Unsupported HTTP method: {v}")
        return v_upper

def validate_raw_record(record: dict) -> Tuple[Optional[ValidatedHttpEvent], Optional[str], Optional[str]]:
    """
    Validates a dictionary. Returns (event, None, None) on success,
    or (None, failed_rule, rejection_reason) on validation failure.
    """
    try:
        # Convert timestamp field name if necessary
        ts_val = record.get("timestamp") or record.get("event_timestamp")
        data = {**record, "event_timestamp": ts_val}
        validated = ValidatedHttpEvent(**data)
        return validated, None, None
    except Exception as e:
        failed_rule = "SCHEMA_VALIDATION_ERROR"
        reason = str(e).replace("\n", " ")
        if "status_code" in reason:
            failed_rule = "INVALID_STATUS_CODE"
        elif "latency_ms" in reason:
            failed_rule = "INVALID_LATENCY_RANGE"
        elif "client_ip" in reason:
            failed_rule = "INVALID_IP_FORMAT"
        elif "event_timestamp" in reason:
            failed_rule = "INVALID_TIMESTAMP_FORMAT"
        elif "service_name" in reason:
            failed_rule = "MISSING_MANDATORY_FIELD"
        return None, failed_rule, reason