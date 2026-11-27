# Runbook: Payment Gateway Timeout & Downstream 503

## Service: payment-gateway
## Severity: CRITICAL

### Symptoms
- Upstream HTTP 500 errors cascading into `order-service` and `api-gateway`.
- `payment-gateway` traces indicate spans taking > 400ms ending with HTTP 503 or socket read timeout.
- Third-party payment processor status page reports elevated latency.

### Immediate Mitigation Steps
1. **Enable Circuit Breaker:**
   - Execute command: `curl -X POST http://payment-gateway/internal/circuit-breaker/trip`
   - This immediately fails fast with a fallback response rather than exhausting thread pools in `order-service`.
2. **Switch to Secondary Processor:**
   - Change routing rule in configuration: `PAYMENT_ROUTING_PRIMARY=mock_backup_processor`.
3. **Notify On-Call:**
   - Post update in `#incident-war-room` stating upstream provider degraded.

### Verification
- Ensure `order-service` p99 latency returns below 100ms.
- Check that circuit breaker fallback orders are queued for retry.