"""
DeepTrace Synthetic Observability Telemetry Generator
Generates realistic distributed HTTP logs with controlled data quality anomalies.
"""
import argparse
import csv
import json
import random
import uuid
from datetime import datetime, timedelta, timezone

SERVICES = [
    ("order-service", ["/api/v1/orders", "/api/v1/orders/checkout", "/api/v1/orders/history"]),
    ("auth-service", ["/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/verify"]),
    ("payment-gateway", ["/api/v1/payments/charge", "/api/v1/payments/refund"]),
    ("inventory-api", ["/api/v1/inventory/items", "/api/v1/inventory/reserve"]),
    ("notification-worker", ["/api/v1/notifications/email", "/api/v1/notifications/sms"])
]

HTTP_METHODS = ["GET", "POST", "PUT", "DELETE"]
STATUS_CODES = [200, 200, 200, 201, 204, 400, 401, 403, 404, 500, 502, 503]
STATUS_WEIGHTS = [60, 15, 10, 5, 2, 3, 1, 1, 1, 1, 0.5, 0.5]
USER_AGENTS = [
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
    "DeepTrace-Agent/1.0",
    "curl/8.4.0"
]

def generate_row(timestamp: datetime, corrupt: bool = False) -> dict:
    service, routes = random.choice(SERVICES)
    route = random.choice(routes)
    method = "GET" if "history" in route or "items" in route else random.choice(HTTP_METHODS)
    status = random.choices(STATUS_CODES, weights=STATUS_WEIGHTS)[0]
    
    # Latency: Log-normal distribution (median ~25ms, tail ~500ms+)
    latency = round(random.lognormvariate(3.2, 0.8), 2)
    if status >= 500:
        latency += round(random.uniform(500, 2500), 2)  # Errors correlate with high latency

    client_ip = f"192.168.{random.randint(1, 254)}.{random.randint(1, 254)}"
    
    row = {
        "event_id": str(uuid.uuid4()),
        "trace_id": uuid.uuid4().hex,
        "span_id": uuid.uuid4().hex[:16],
        "service_name": service,
        "http_method": method,
        "route": route,
        "status_code": status,
        "latency_ms": latency,
        "client_ip": client_ip,
        "user_agent": random.choice(USER_AGENTS),
        "timestamp": timestamp.isoformat()
    }

    if corrupt:
        # Deliberately inject one of five data quality anomalies
        anomaly_type = random.choice([1, 2, 3, 4, 5])
        if anomaly_type == 1:
            row["status_code"] = random.choice([999, -1, 99])  # Invalid status code
        elif anomaly_type == 2:
            row["latency_ms"] = -50.0  # Negative latency violation
        elif anomaly_type == 3:
            row["timestamp"] = "CORRUPT_TIMESTAMP_FORMAT"  # Invalid date
        elif anomaly_type == 4:
            row["service_name"] = ""  # Missing mandatory service name
        elif anomaly_type == 5:
            row["client_ip"] = "999.999.999.999"  # Invalid IP address

    return row

def main():
    parser = argparse.ArgumentParser(description="Generate synthetic HTTP observability logs.")
    parser.add_argument("--rows", type=int, default=50000, help="Number of rows to generate")
    parser.add_argument("--bad-rate", type=float, default=0.01, help="Proportion of corrupted rows (e.g. 0.01 = 1%%)")
    parser.add_argument("--format", choices=["csv", "json"], default="csv", help="Output format")
    parser.add_argument("--out", type=str, default="data/landing/http_events.csv", help="Output filepath")
    args = parser.parse_args()

    start_time = datetime.now(timezone.utc) - timedelta(hours=24)
    time_step = timedelta(seconds=(24 * 3600) / max(args.rows, 1))

    print(f"[DeepTrace Generator] Generating {args.rows} records ({args.bad_rate * 100:.1f}% bad data) -> {args.out}")

    with open(args.out, "w", newline="", encoding="utf-8") as f:
        if args.format == "csv":
            writer = None
            for i in range(args.rows):
                cur_time = start_time + (i * time_step)
                is_corrupt = random.random() < args.bad_rate
                row = generate_row(cur_time, corrupt=is_corrupt)
                if writer is None:
                    writer = csv.DictWriter(f, fieldnames=list(row.keys()))
                    writer.writeheader()
                writer.writerow(row)
        else:
            for i in range(args.rows):
                cur_time = start_time + (i * time_step)
                is_corrupt = random.random() < args.bad_rate
                row = generate_row(cur_time, corrupt=is_corrupt)
                f.write(json.dumps(row) + "\n")

    print(f"[DeepTrace Generator] Generation complete: {args.out}")

if __name__ == "__main__":
    main()