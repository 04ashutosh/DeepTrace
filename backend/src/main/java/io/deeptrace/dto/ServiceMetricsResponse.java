package io.deeptrace.dto;

public record ServiceMetricsResponse(
        String serviceName,
        Long totalRequests,
        Double avgLatencyMs,
        Double p50LatencyMs,
        Double p95LatencyMs,
        Double p99LatencyMs
) {}