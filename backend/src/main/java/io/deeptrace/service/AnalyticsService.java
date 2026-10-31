package io.deeptrace.service;

import io.deeptrace.dto.ErrorRateWindowResponse;
import io.deeptrace.dto.ServiceMetricsResponse;
import io.deeptrace.repository.AnalyticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {
    private final AnalyticsRepository repository;

    public AnalyticsService(AnalyticsRepository repository) {
        this.repository = repository;
    }

    public List<ServiceMetricsResponse> getServiceMetrics() {
        return repository.getServiceLatencyMetrics();
    }

    public List<ErrorRateWindowResponse> getErrorRateWindows(String serviceName) {
        return repository.getErrorRateWindows(serviceName);
    }

    public List<Map<String, Object>> getQuarantineBreakdown() {
        return repository.getQuarantineBreakdown();
    }
}