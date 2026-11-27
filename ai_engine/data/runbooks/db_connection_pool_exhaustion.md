# Runbook: PostgreSQL Connection Pool Exhaustion

## Service: order-service
## Severity: HIGH

### Symptoms
- Logs indicate: `org.postgresql.util.PSQLException: HikariPool-1 - Connection is not available, request timed out after 30000ms`.
- High request latency across all endpoints interacting with relational storage.

### Immediate Mitigation Steps
1. **Kill Idle In Transaction Queries:**
   - Connect to DB admin console and run:
     `SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE state = 'idle in transaction' AND state_change < current_timestamp - INTERVAL '2 minutes';`
2. **Temporarily Scale HikariCP Maximum Pool Size:**
   - Scale environment variable: `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=30`.
3. **Restart Degraded Replica Pods:**
   - Trigger rolling restart to drop zombie client connections.