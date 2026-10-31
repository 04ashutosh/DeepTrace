package io.deeptrace.repository;

import io.deeptrace.dto.ErrorRateWindowResponse;
import io.deeptrace.dto.ServiceMetricsResponse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Repository
public class AnalyticsRepository {
    private final JdbcClient jdbcClient;

    public AnalyticsRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ServiceMetricsResponse> getServiceLatencyMetrics() {
        String sql = """
            SELECT 
                service_name,
                COUNT(*) AS total_requests,
                ROUND(AVG(latency_ms), 2) AS avg_latency_ms,
                ROUND(CAST(percentile_cont(0.50) WITHIN GROUP (ORDER BY latency_ms) AS numeric), 2) AS p50_ms,
                ROUND(CAST(percentile_cont(0.95) WITHIN GROUP (ORDER BY latency_ms) AS numeric), 2) AS p95_ms,
                ROUND(CAST(percentile_cont(0.99) WITHIN GROUP (ORDER BY latency_ms) AS numeric), 2) AS p99_ms
            FROM silver_http_events
            GROUP BY service_name
            ORDER BY total_requests DESC
        """;

        return jdbcClient.sql(sql)
                .query((rs, rowNum) -> new ServiceMetricsResponse(
                        rs.getString("service_name"),
                        rs.getLong("total_requests"),
                        rs.getDouble("avg_latency_ms"),
                        rs.getDouble("p50_ms"),
                        rs.getDouble("p95_ms"),
                        rs.getDouble("p99_ms")
                ))
                .list();
    }

    public List<ErrorRateWindowResponse> getErrorRateWindows(String serviceName) {
        String sql = """
            WITH time_buckets AS (
                SELECT 
                    service_name,
                    date_bin('5 minutes', event_timestamp, TIMESTAMPTZ '2026-01-01 00:00:00Z') AS window_start,
                    COUNT(*) AS total_requests,
                    COUNT(*) FILTER (WHERE status_code >= 500) AS errors_5xx,
                    COUNT(*) FILTER (WHERE status_code >= 400 AND status_code < 500) AS errors_4xx
                FROM silver_http_events
                WHERE (:serviceName IS NULL OR service_name = :serviceName)
                GROUP BY service_name, window_start
            )
            SELECT 
                service_name,
                window_start,
                total_requests,
                errors_5xx,
                errors_4xx,
                ROUND((errors_5xx * 100.0) / NULLIF(total_requests, 0), 2) AS error_rate_5xx_pct
            FROM time_buckets
            ORDER BY window_start DESC
            LIMIT 50
        """;

        return jdbcClient.sql(sql)
                .param("serviceName", serviceName)
                .query((rs, rowNum) -> {
                    Timestamp ts = rs.getTimestamp("window_start");
                    return new ErrorRateWindowResponse(
                            rs.getString("service_name"),
                            ts != null ? ts.toInstant().atOffset(ZoneOffset.UTC) : null,
                            rs.getLong("total_requests"),
                            rs.getLong("errors_5xx"),
                            rs.getLong("errors_4xx"),
                            rs.getDouble("error_rate_5xx_pct")
                    );
                })
                .list();
    }

    public List<Map<String, Object>> getQuarantineBreakdown() {
        String sql = """
            SELECT failed_rule, COUNT(*) AS count
            FROM rejected_records
            GROUP BY failed_rule
            ORDER BY count DESC
        """;
        return jdbcClient.sql(sql).query().listOfRows();
    }
}