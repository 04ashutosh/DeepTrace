package io.deeptrace.controller;

import io.deeptrace.dto.ErrorRateWindowResponse;
import io.deeptrace.dto.ServiceMetricsResponse;
import io.deeptrace.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Real-time Telemetry & Quality Analytics API")
public class AnalyticsController {
    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/services")
    @Operation(summary = "Get request counts and latency percentiles (p50, p95, p99) by service")
    public ResponseEntity<List<ServiceMetricsResponse>> getServiceMetrics() {
        return ResponseEntity.ok(service.getServiceMetrics());
    }

    @GetMapping("/error-rates")
    @Operation(summary = "Get 5-minute tumbling window error rates")
    public ResponseEntity<List<ErrorRateWindowResponse>> getErrorRates(
            @RequestParam(required = false) String serviceName
    ) {
        return ResponseEntity.ok(service.getErrorRateWindows(serviceName));
    }

    @GetMapping("/quarantine")
    @Operation(summary = "Get quarantine rejection breakdown by rule violation")
    public ResponseEntity<List<Map<String, Object>>> getQuarantineBreakdown() {
        return ResponseEntity.ok(service.getQuarantineBreakdown());
    }
}