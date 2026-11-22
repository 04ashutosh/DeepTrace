package io.deeptrace.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "service_dependencies", schema = "observability")
@IdClass(ServiceDependencyEntity.ServiceDependencyId.class)
public class ServiceDependencyEntity {

    @Id
    @Column(name = "caller_service", length = 100)
    private String callerService;

    @Id
    @Column(name = "callee_service", length = 100)
    private String calleeService;

    @Column(name = "call_count")
    private Long callCount = 0L;

    @Column(name = "error_count")
    private Long errorCount = 0L;

    @Column(name = "avg_latency_ms")
    private Double avgLatencyMs = 0.0;

    @Column(name = "p95_latency_ms")
    private Double p95LatencyMs = 0.0;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ServiceDependencyEntity() {}

    public ServiceDependencyEntity(String callerService, String calleeService, Long callCount,
                                   Long errorCount, Double avgLatencyMs, Double p95LatencyMs) {
        this.callerService = callerService;
        this.calleeService = calleeService;
        this.callCount = callCount;
        this.errorCount = errorCount;
        this.avgLatencyMs = avgLatencyMs;
        this.p95LatencyMs = p95LatencyMs;
        this.updatedAt = Instant.now();
    }

    public String getCallerService() { return callerService; }
    public void setCallerService(String callerService) { this.callerService = callerService; }

    public String getCalleeService() { return calleeService; }
    public void setCalleeService(String calleeService) { this.calleeService = calleeService; }

    public Long getCallCount() { return callCount; }
    public void setCallCount(Long callCount) { this.callCount = callCount; }

    public Long getErrorCount() { return errorCount; }
    public void setErrorCount(Long errorCount) { this.errorCount = errorCount; }

    public Double getAvgLatencyMs() { return avgLatencyMs; }
    public void setAvgLatencyMs(Double avgLatencyMs) { this.avgLatencyMs = avgLatencyMs; }

    public Double getP95LatencyMs() { return p95LatencyMs; }
    public void setP95LatencyMs(Double p95LatencyMs) { this.p95LatencyMs = p95LatencyMs; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static class ServiceDependencyId implements Serializable {
        private String callerService;
        private String calleeService;

        public ServiceDependencyId() {}

        public ServiceDependencyId(String callerService, String calleeService) {
            this.callerService = callerService;
            this.calleeService = calleeService;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ServiceDependencyId that)) return false;
            return Objects.equals(callerService, that.callerService) &&
                    Objects.equals(calleeService, that.calleeService);
        }

        @Override
        public int hashCode() {
            return Objects.hash(callerService, calleeService);
        }
    }
}